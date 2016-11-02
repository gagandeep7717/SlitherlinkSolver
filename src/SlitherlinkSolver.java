import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Authors: Gagandeep Randhawa.
 *          Hemant Nimje.
 * File Created on on 10/22/2016
 */
public class SlitherlinkSolver {

    //row and col count static to allow dynamic size of puzzle
    static int rowCount = 0;
    static int colCount = 0;

    static ArrayList<edge> nonEssEdges = new ArrayList<edge>();    //Arraylist for NonEssential Edges wit reduced domain to be used for reduction

    static HashMap<edge, ArrayList<String>> edgeHM = new HashMap<edge, ArrayList<String>>();    //Hashmap storing Key: Edges and Value: Domain
    static HashMap<node, ArrayList<String>> nodeHM = new HashMap<node, ArrayList<String>>();    //Hashmap for Key: Nodes and Value: Satisfying Assignments (Node Degree either 0 or 2)


    public static void main(String args[]) throws IOException {

        Path path = Paths.get(System.getProperty("user.dir") + "\\src\\output.txt");
        PrintStream out = new PrintStream(new FileOutputStream(path.toFile()));
        System.setOut(out);

        int[][] readMatrix = readPuzzle();  //readPuzzle - reads puzzle from text file and prints to console


        //Generating Initial Edge Domain Hashmap
        generateInitialEdgeHashMap(readMatrix);
        System.out.println("\nInitial Edge Domain:\n");
        printEdgeHM(edgeHM);

        //Generating Initial Node Assignments Hashmap
        System.out.println("\nInitial Node Assignments:\n");
        generateInitialNodeHashMap(readMatrix);
        findNodeonWallsandCorners(readMatrix);
        printNodeHM(nodeHM);


        //Applying Domain Reduction on where cell value is zero.
        applyZeroAC(readMatrix);
        printEdgeHM(edgeHM);
        printEdgeArraylist(nonEssEdges);

    }

    public static int[][] readPuzzle() throws IOException {
        //Read File and Add to String Buffer
        StringBuilder sb = null;
        Path path = Paths.get(System.getProperty("user.dir") + "\\src\\puzzle.txt");
        FileInputStream in = new FileInputStream(path.toFile());
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try {
            sb = new StringBuilder();
            String line = br.readLine();
            colCount = (line.length() / 2) + 1;
            while (line != null) {
                rowCount++;
                sb.append(line);
                line = br.readLine();
            }
        }
        finally {
            br.close();
        }

        //Remove "," and replace "?" with 7 to store to integer matrix
        //Reason to replace ? with 7, is to make sure we are using integer matrix
        //No additional step of converting each character to integer everytime we check for constraints
        String noCommas = sb.toString().replaceAll(",", "");
        String cellValues = noCommas.replaceAll("\\?", "7");
        char[] everything = cellValues.toCharArray();

        //Create matrix based on row and col count of the file.
        int[][] matrix = new int[rowCount][colCount];
        int c = 0;
        for(int i=0; i<rowCount; i++){
            for(int j=0; j<colCount; j++){
                String s = ""+everything[c];
                matrix[i][j] = Integer.parseInt(s);
                c++;
            }
        }

        //Final Row and Column count to be provided to the solver
        System.out.println("Rows = " + rowCount );
        System.out.println("Columns = " + colCount);

        //Print puzzle matrix
        System.out.println("*********Puzzle Matrix**********");
        printMatrix(matrix);
        System.out.println("");
        return matrix;
    }

    public static void printMatrix(int[][] matrix){
        System.out.print("\t" + "");
        for(int i=0; i<colCount; i++){
            System.out.print("\t" + i);
        }
        System.out.println("");
        for(int i=0; i<rowCount; i++){
            System.out.printf("\t" + i);
            for(int j=0; j<colCount; j++){
                if(matrix[i][j] != 7)
                    System.out.print("\t" + matrix[i][j]);
                else
                    System.out.print("\t" + '\u221e');

            }
            System.out.println();
        }
    }



    public static void generateInitialEdgeHashMap(int [][] matrix){
        LinkedHashMap<edge, ArrayList<String>> orderedEdgeHM = new LinkedHashMap<edge, ArrayList<String>>();
        for(int i=0; i<rowCount; i++){
            for(int j=0; j<colCount; j++){
                orderedEdgeHM.put(new edge("H", i, j), new ArrayList<String>(Arrays.asList("0","1")));
                orderedEdgeHM.put(new edge("V", i, j), new ArrayList<String>(Arrays.asList("0","1")));
                orderedEdgeHM.put(new edge("H", i+1, j), new ArrayList<String>(Arrays.asList("0","1")));
                orderedEdgeHM.put(new edge("V", i, j+1), new ArrayList<String>(Arrays.asList("0","1")));

                edgeHM = orderedEdgeHM;
            }
        }
    }

    public static void generateInitialNodeHashMap(int [][] matrix){
        LinkedHashMap<node, ArrayList<String>> orderedNodeHM = new LinkedHashMap<node, ArrayList<String>>();
        for(int i=0; i<rowCount+1; i++){
            for(int j=0; j<colCount+1; j++){
                String[] assignments = new String[] {"0000", "0011", "0110", "1100", "0101", "1010", "1001"};
                orderedNodeHM.put(new node("N", i, j), new ArrayList<String>(Arrays.asList(assignments)));

                nodeHM = orderedNodeHM;
            }
        }
    }

    public static void removeDuplicates(ArrayList<edge> nonEssEdges){
        Set<edge> hs = new HashSet<>();
        hs.addAll(nonEssEdges);
        nonEssEdges.clear();
        nonEssEdges.addAll(hs);
    }

    public static void printStringArraylist(ArrayList<String> al){
        for(String str:al){
            System.out.print("\t" + str);
        }
    }

    public static void printEdgeArraylist(ArrayList<edge> al){
        if(al.isEmpty()){
            System.out.println("No - Non Essential Edge Present!");
        }
        else {
            for (edge str : al) {
                str.printEdge();
            }
        }
    }

    public static void printNodeArraylist(ArrayList<node> al){
        for(node str:al){
            str.printNode();
        }
    }

    public static void printSortedHM(HashMap<edge, ArrayList<String>> edgeHM){
        Set set = edgeHM.entrySet();
        int edgeCount=0;
        Iterator i = set.iterator();
        while(i.hasNext()){
            edgeCount++;
            Map.Entry me = (Map.Entry) i.next();
            System.out.print(edgeCount + "\t" + ":");
            edge edge = (edge) me.getKey();
            edge.printEdge();
            ArrayList<String> alDomain= (ArrayList<String>) me.getValue();
            printStringArraylist(alDomain);
            System.out.println("");
        }
    }

    public static void printEdgeHM(HashMap<edge,ArrayList<String>> unsortedHashMap){
        List<Map.Entry<edge, ArrayList<String>>> entries =
                new ArrayList<Map.Entry<edge, ArrayList<String>>>(unsortedHashMap.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<edge, ArrayList<String>>>() {
            public int compare(Map.Entry<edge, ArrayList<String>> a, Map.Entry<edge, ArrayList<String>> b){
                edge edgeA = a.getKey();
                edge edgeB = b.getKey();
                int asciiA = (int) edgeA.edgeType.charAt(0);
                int asciiB = (int) edgeB.edgeType.charAt(0);
                int sortbyI = (asciiA - asciiB) + edgeA.i - edgeB.i;
                return sortbyI;
            }
        });

        HashMap<edge, ArrayList<String>> sortedMap = new LinkedHashMap<edge, ArrayList<String>>();

        for (Map.Entry<edge, ArrayList<String>> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        Map map = Collections.synchronizedMap(sortedMap);
        Set set = map.entrySet();
        int edgeCount=0;
        synchronized (map){
            Iterator i = set.iterator();
            while(i.hasNext()){
                edgeCount++;
                Map.Entry me = (Map.Entry) i.next();
                edge printEdge = (edge) me.getKey();
                System.out.print(edgeCount + ":" + "\t");
                printEdge.printEdge();
                ArrayList<String> alDomain= (ArrayList<String>) me.getValue();
                printStringArraylist(alDomain);
                System.out.println("");
            }
        }

    }

    public static void printNodeHM(HashMap<node, ArrayList<String>> hashMap){

        List<Map.Entry<node, ArrayList<String>>> entries =
                new ArrayList<Map.Entry<node, ArrayList<String>>>(hashMap.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<node, ArrayList<String>>>() {
            public int compare(Map.Entry<node, ArrayList<String>> a, Map.Entry<node, ArrayList<String>> b){
                node nodeA = a.getKey();
                node nodeB = b.getKey();
                int asciiA = (int) nodeA.nodeType.charAt(0);
                int asciiB = (int) nodeB.nodeType.charAt(0);
                int sortbyI = (asciiA - asciiB) + nodeA.i - nodeB.i;
                return sortbyI;
            }
        });

        HashMap<node, ArrayList<String>> sortedMap = new LinkedHashMap<node, ArrayList<String>>();

        for (Map.Entry<node, ArrayList<String>> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        Set set = sortedMap.entrySet();
        int nodeCount=0;
        Iterator i = set.iterator();
        while(i.hasNext()){
            nodeCount++;
            Map.Entry me = (Map.Entry) i.next();
            node printNode = (node) me.getKey();
            System.out.print(nodeCount + ":" + "\t" );
            printNode.printNode();
            ArrayList<String> alDomain= (ArrayList<String>) me.getValue();
            printStringArraylist(alDomain);
            System.out.println("");
        }
    }

    public static void findNodeonWallsandCorners(int[][] matrix){
        System.out.println("Reducing satisfying assignments for Nodes on corners and walls");
        ArrayList<node> TopWall = new ArrayList<node>();
        ArrayList<node> RightWall = new ArrayList<node>();
        ArrayList<node> BottomWall = new ArrayList<node>();
        ArrayList<node> LeftWall = new ArrayList<node>();
        ArrayList<node> TopLeftCorner = new ArrayList<node>();
        ArrayList<node> TopRightCorner = new ArrayList<node>();
        ArrayList<node> BottomRightCorner = new ArrayList<node>();
        ArrayList<node> BottomLeftCorner = new ArrayList<node>();

        for(int i=0; i<=rowCount; i++){
            for(int j=0; j<=colCount; j++){
                if(i == 0 && j == 0){
                    TopLeftCorner.add(new node("N", i, j));
                }
                else if(i == 0 && j == colCount){
                    TopRightCorner.add(new node("N", i, j));
                }
                else if(i == rowCount && j == colCount){
                    BottomRightCorner.add(new node("N", i, j));
                }
                else if(i == rowCount && j == 0){
                    BottomLeftCorner.add(new node("N", i, j));
                }
                else if(i == 0 && (j != 0 || j == colCount)){
                    TopWall.add(new node("N", i, j));
                }
                else if(j == colCount && (i!=0 || i != rowCount)){
                    RightWall.add(new node("N", i, j));
                }
                else if(i == rowCount && (j != 0 || j != colCount)) {
                    BottomWall.add(new node("N", i, j));
                }
                else if(j == 0 && (i != 0 || i == rowCount)) {
                    LeftWall.add(new node("N", i, j));
                }
            }
        }

        System.out.print("\nLeftTopCorner node: "); printNodeArraylist(TopLeftCorner);
        System.out.print("\nRightTopCorner node: "); printNodeArraylist(TopRightCorner);
        System.out.print("\nRightBottomCorner node: "); printNodeArraylist(BottomRightCorner);
        System.out.print("\nLeftBottomCorner node: "); printNodeArraylist(BottomLeftCorner);
        System.out.print("\nTop Wall: "); printNodeArraylist(TopWall);
        System.out.print("\nRight Wall: "); printNodeArraylist(RightWall);
        System.out.print("\nBottom Wall: "); printNodeArraylist(BottomWall);
        System.out.print("\nLeft Wall: "); printNodeArraylist(LeftWall);
        System.out.println("\n");

        String[] assignmentsTopLeftCorner = new String[]{"0000","0011"};

        String[] assignmentsTopRightCorner = new String[]{"0000","1001"};

        String[] assignmentsBottomRightCorner = new String[]{"0000","1100"};

        String[] assignmentsBottomLeftCorner = new String[]{"0000","0110"};

        String[] assignmentsTopWall = new String[]{"0000","1010","1001","0011"};

        String[] assignmentsRightWall = new String[]{"0000","0110","0101","0011"};

        String[] assignmentsBottomWall = new String[]{"0000","1100","0110","1010"};

        String[] assignmentsLeftWall = new String[]{"0000","0110","0101","0011"};

        for(node str: TopLeftCorner){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList(assignmentsTopLeftCorner)));
        }

        for(node str: TopRightCorner){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList(assignmentsTopRightCorner)));
        }

        for(node str: BottomRightCorner){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList(assignmentsBottomRightCorner)));
        }

        for(node str: BottomLeftCorner){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList(assignmentsBottomLeftCorner)));
        }

        for(node str: TopWall){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList(assignmentsTopWall)));
        }

        for(node str: RightWall){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList(assignmentsRightWall)));
        }

        for(node str: BottomWall){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList(assignmentsBottomWall)));
        }

        for(node str: LeftWall){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList(assignmentsLeftWall)));
        }
    }

    //Apply arc consistency on Cell with Values equal to zero (0)
    public static void applyZeroAC(int [][] matrix){
        for(int i=0; i<rowCount; i++){
            for(int j=0; j<colCount; j++){
                if(matrix[i][j] == 0){
                    nonEssEdges.add(new edge("H", i, j));   // H[i][j]
                    nonEssEdges.add(new edge("V", i, j));   // V[i][j]
                    nonEssEdges.add(new edge("H", i+1, j)); // H[i+1][j]
                    nonEssEdges.add(new edge("V", i, j+1)); // H[i][j+1]
                }
            }
        }
        System.out.println("");
        System.out.println("Printing Non Essential Edges - removal of Cell Value CV=0");
        removeDuplicates(nonEssEdges);
        printEdgeArraylist(nonEssEdges);
        reduceEdgeDomain(edgeHM, nonEssEdges);
        System.out.println("");
        System.out.println("");

    }

    public static void reduceEdgeDomain(HashMap<edge, ArrayList<String>> edgeHM, ArrayList<edge> nonEssEdges){
        for(edge edge : nonEssEdges)
        {
            if(edgeHM.containsKey(edge)){
                edgeHM.remove(edge);
                edgeHM.put(edge, new ArrayList<String>(Arrays.asList("0")));
            }
        }

    }
}
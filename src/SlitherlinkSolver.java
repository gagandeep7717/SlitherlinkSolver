import com.sun.javafx.geom.Edge;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern;
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
        SlitherlinkSolver solver = new SlitherlinkSolver();

        Path path = Paths.get(System.getProperty("user.dir") + "\\src\\output.txt");
        PrintStream out = new PrintStream(new FileOutputStream(path.toFile()));
        System.setOut(out);

        int[][] readMatrix = solver.readPuzzle();  //readPuzzle - reads puzzle from text file and prints to console


        //Generating Initial Edge Domain Hashmap
        solver.generateInitialEdgeHashMap(readMatrix);
        System.out.println("\nInitial Edge Domain:\n");
        solver.printEdgeHM(edgeHM);

        //Generating Initial Node Assignments Hashmap
        System.out.println("\nInitial Node Assignments:\n");
        solver.generateInitialNodeHashMap(readMatrix);
        solver.findNodeonWallsandCorners(readMatrix);
        solver.printNodeHM(nodeHM);


        //Applying Domain Reduction on where cell value is zero.
        solver.applyZeroAC(readMatrix);
        solver.printEdgeHM(edgeHM);
        solver.printEdgeArraylist(nonEssEdges);


        System.out.println();
        solver.reduceNodeAssignments(nodeHM, nonEssEdges);
        solver.printNodeHM(nodeHM);

    }

    public int[][] readPuzzle() throws IOException {
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

    public void printMatrix(int[][] matrix){
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



    public void generateInitialEdgeHashMap(int [][] matrix){
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

    public void generateInitialNodeHashMap(int [][] matrix){
        LinkedHashMap<node, ArrayList<String>> orderedNodeHM = new LinkedHashMap<node, ArrayList<String>>();
        for(int i=0; i<rowCount+1; i++){
            for(int j=0; j<colCount+1; j++){
                String[] assignments = new String[] {"0000", "0011", "0110", "1100", "0101", "1010", "1001"};
                orderedNodeHM.put(new node("N", i, j), new ArrayList<String>(Arrays.asList("0000", "0011", "0110", "1100", "0101", "1010", "1001")));

                nodeHM = orderedNodeHM;
            }
        }
    }

    public void removeDuplicates(ArrayList<edge> nonEssEdges){
        Set<edge> hs = new HashSet<>();
        hs.addAll(nonEssEdges);
        nonEssEdges.clear();
        nonEssEdges.addAll(hs);
    }

    public void printStringArraylist(ArrayList<String> al){
        for(String str:al){
            System.out.print("\t" + str);
        }
    }

    public void printEdgeArraylist(ArrayList<edge> al){
        if(al.isEmpty()){
            System.out.println("No - Non Essential Edge Present!");
        }
        else {
            for (edge str : al) {
                str.printEdge();
            }
        }
    }

    public void printNodeArraylist(ArrayList<node> al){
        for(node str:al){
            str.printNode();
        }
    }

    public void printSortedHM(HashMap<edge, ArrayList<String>> edgeHM){
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

    public void printEdgeHM(HashMap<edge,ArrayList<String>> unsortedHashMap){
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

    public void printNodeHM(HashMap<node, ArrayList<String>> hashMap){

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

    public void findNodeonWallsandCorners(int[][] matrix){
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
        for(node str: TopLeftCorner){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","0011")));
        }

        String[] assignmentsTopRightCorner = new String[]{"0000","1001"};
        for(node str: TopRightCorner){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","1001")));
        }

        String[] assignmentsBottomRightCorner = new String[]{"0000","1100"};
        for(node str: BottomRightCorner){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","1100")));
        }

        String[] assignmentsBottomLeftCorner = new String[]{"0000","0110"};
        for(node str: BottomLeftCorner){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","0110")));
        }


        String[] assignmentsTopWall = new String[]{"0000","1010","1001","0011"};
        for(node str: TopWall){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","1010","1001","0011")));
        }

        String[] assignmentsRightWall = new String[]{"0000","0110","0101","0011"};
        for(node str: RightWall){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","0110","0101","0011")));
        }

        String[] assignmentsBottomWall = new String[]{"0000","1100","0110","1010"};
        for(node str: BottomWall){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","1100","0110","1010")));
        }

        String[] assignmentsLeftWall = new String[]{"0000","0110","0101","0011"};
        for(node str: LeftWall){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","0110","0101","0011")));
        }
    }

    //Apply arc consistency on Cell with Values equal to zero (0)
    public void applyZeroAC(int [][] matrix){
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
        nonEssEdges = reduceNonEssEdgeDomain(edgeHM, nonEssEdges);
        System.out.println("");
        System.out.println("");

    }

    public ArrayList<edge> reduceNonEssEdgeDomain(HashMap<edge, ArrayList<String>> edgeHM, ArrayList<edge> nonEssEdges1){
        for(edge edge : nonEssEdges1)
        {
            if(edgeHM.containsKey(edge)){
                edgeHM.remove(edge);
                edgeHM.put(edge, new ArrayList<String>(Arrays.asList("0")));
            }
        }



        /*int initSizeofAL = nonEssEdges1.size();
        for(int i=0; i<initSizeofAL; i++){
            nonEssEdges1.remove(0);
        }*/

        return nonEssEdges1;
    }

    public void reduceNodeAssignments(HashMap<node, ArrayList<String>> nodeHashMap, ArrayList<edge>nonEssEdges1){
        int initSizeofAL = nonEssEdges1.size();
        String nodeLeftNonEss = "..1.";
        String nodeRightNonEss = "1...";
        try{
            for(int i=0; i<initSizeofAL; i++){
                edge edge = nonEssEdges1.get(i);
                //if Horizontal edge - reduce satisfying assignment for nodes on left and right of the edge
                if(edge.edgeType.equals("H")){
                    node nodeLeft = new node("N", edge.i, edge.j);
                    node nodeRight = new node("N", edge.i, edge.j+1);

                    if(nodeHashMap.containsKey(nodeLeft)){
                        System.out.print("\nReducing Node on Left: ");
                        nodeLeft.printNode();
                        ArrayList<String> al = nodeHashMap.get(nodeLeft);
                        ArrayList<String> alTemp = nodeHashMap.get(nodeLeft);
                        for(int j=0; j<al.size(); j++){
                            String str = al.get(j);
                            System.out.print("\nChecking for: " + str + "\t");
                            if(str.matches("..1.")){
                                System.out.print("Removed: " + str + "\n");
                                nodeHashMap.get(nodeLeft).remove(str);
                            }
                        }
                        nodeHM.put(nodeLeft, alTemp);
                    }

                    if(nodeHashMap.containsKey(nodeRight)){
                        System.out.print("\nReducing Node on Right: ");
                        nodeRight.printNode();
                        ArrayList<String> al = nodeHashMap.get(nodeRight);
                        ArrayList<String> alTemp = nodeHashMap.get(nodeRight);
                        for(int j=0; j<al.size(); j++){
                            String str = al.get(j);
                            System.out.print("\nChecking for: " + str + "\t");
                            if(str.matches("1...")){
                                System.out.print("Removed: " + str + "\n");
                                nodeHashMap.get(nodeRight).remove(str);
                            }
                        }
                        nodeHM.put(nodeRight, alTemp);
                    }

                    /*//Reduce Left Node Assignments
                    ArrayList<String> al = nodeHashMap.get(nodeLeft);
                    for(int j=0; j<al.size(); j++){
                        if(al.get(j).matches("..1")){
                            nodeHashMap.get(nodeLeft).remove(al.get(j));
                        }
                    }*/

                    /*al.clear();
                    //Reduce Right Node Assignments
                    al = nodeHashMap.get(nodeRight);
                    for(int j=0; j<al.size(); j++){
                        if(al.get(j).matches("1*")){
                            nodeHashMap.get(nodeRight).remove(al.get(j));
                        }
                    }*/

                }

                //if Vertical edge - reduce satisfying assignment for nodes on top and bottom of the edge
                if(edge.edgeType.equals("V")){
                    node nodeTop = new node("N", edge.i, edge.j);
                    node nodeBottom = new node("N", edge.i, edge.j+1);

                    if(nodeHashMap.containsKey(nodeTop)){
                        System.out.print("\nReducing Node on Left: ");
                        nodeTop.printNode();
                        ArrayList<String> al = nodeHashMap.get(nodeTop);
                        ArrayList<String> alTemp = nodeHashMap.get(nodeTop);
                        for(int j=0; j<al.size(); j++){
                            String str = al.get(j);
                            System.out.print("\nChecking for: " + str + "\t");
                            if(str.matches("...1")){
                                System.out.print("Removed: " + str + "\n");
                                nodeHashMap.get(nodeTop).remove(str);
                            }
                        }
                        nodeHM.put(nodeTop, alTemp);
                    }

                    if(nodeHashMap.containsKey(nodeBottom)){
                        System.out.print("\nReducing Node on Right: ");
                        nodeBottom.printNode();
                        ArrayList<String> al = nodeHashMap.get(nodeBottom);
                        ArrayList<String> alTemp = nodeHashMap.get(nodeBottom);
                        for(int j=0; j<al.size(); j++){
                            String str = al.get(j);
                            System.out.print("\nChecking for: " + str + "\t");
                            if(str.matches(".1..")){
                                System.out.print("Removed: " + str + "\n");
                                nodeHashMap.get(nodeBottom).remove(str);
                            }
                        }
                        nodeHM.put(nodeBottom, alTemp);
                    }

                }


            }

        }
        catch (ConcurrentModificationException e){
            System.err.println("Caught Concurrent Modifications: " + e.getMessage());
        }

    }




}
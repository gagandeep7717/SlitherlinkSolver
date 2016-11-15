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
    public static int rowCount = 0;
    public static int colCount = 0;

    public static ArrayList<edge> nonEssEdges = new ArrayList<edge>();    //Arraylist for NonEssential Edges wit reduced domain to be used for reduction
    public static Queue<edge> nonEssEdgeQueue = new PriorityQueue<edge>();

    public static HashMap<edge, ArrayList<String>> edgeHM = new HashMap<edge, ArrayList<String>>();    //Hashmap storing Key: Edges and Value: Domain
    public static HashMap<node, ArrayList<String>> nodeHM = new HashMap<node, ArrayList<String>>();    //Hashmap for Key: Nodes and Value: Satisfying Assignments (Node Degree either 0 or 2)

    public static ArrayList<cell> allCells = new ArrayList<cell>();
    public static ArrayList<cell> zeroCells = new ArrayList<cell>();
    public static ArrayList<cell> oneCells = new ArrayList<cell>();
    public static ArrayList<cell> twoCells = new ArrayList<cell>();
    public static ArrayList<cell> threeCells = new ArrayList<cell>();
    public static ArrayList<cell> emptyCells = new ArrayList<cell>();

    Queue<cell> cellQueue = new PriorityQueue<>();



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


        solver.applyArcConsistency(zeroCells);


        solver.printEdgeHM(edgeHM);
        System.out.println();
        solver.printNodeHMexclude0(nodeHM);

        Backtracking backtracking = new Backtracking(oneCells, twoCells, threeCells, emptyCells, nonEssEdges, edgeHM, nodeHM);
        backtracking.printData();


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
                allCells.add(new cell(i,j,matrix[i][j]));
                switch (matrix[i][j]){
                    case 0:
                        zeroCells.add(new cell(i,j,matrix[i][j]));
                        break;
                    case 1:
                        oneCells.add(new cell(i,j,matrix[i][j]));
                        break;
                    case 2:
                        twoCells.add(new cell(i,j,matrix[i][j]));
                        break;
                    case 3:
                        threeCells.add(new cell(i,j,matrix[i][j]));
                        break;
                    case 7:
                        emptyCells.add(new cell(i,j,matrix[i][j]));
                        break;
                    default:
                        break;

                }
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
        //Linked HashMap to store edges in order of cells
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
                String[] assignments = new String[] { "0000","0011", "0110", "1100", "0101", "1010", "1001"};
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

    public void removeStringDuplicates(ArrayList<String> al){
        Set<String> hs = new HashSet<>();
        hs.addAll(al);
        al.clear();
        al.addAll(hs);
    }

    public void printStringArraylist(ArrayList<String> al){
        for(String str:al){
            System.out.print("\t" + str);
        }
    }

    public void printStringArraylistexclude0(ArrayList<String> al){
        for(String str:al){
            if(str.equals("0000"))
                System.out.print("");
            else
                System.out.print("\t" + str);
        }
    }

    public void printEdgeArraylist(ArrayList<edge> al){
        if(al.isEmpty()){
            System.out.println("No - Non Essential Edge Present!");
        }
        else {
            Collections.sort(al, new Comparator<edge>() {
                @Override
                public int compare(edge edgeA, edge edgeB){
                    int asciiA = (int) edgeA.edgeType.charAt(0);
                    int asciiB = (int) edgeB.edgeType.charAt(0);
                    int sortbyI = (asciiA - asciiB) + edgeA.i - edgeB.i;
                    return sortbyI;
                }
            });
            System.out.println("\nNon Essential Edges (Size: " + al.size() + ")");
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
        System.out.println("");
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
        System.out.println("");
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

    public void printNodeHMexclude0(HashMap<node, ArrayList<String>> hashMap){
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
        System.out.println("");
        while(i.hasNext()){
            nodeCount++;
            Map.Entry me = (Map.Entry) i.next();
            node printNode = (node) me.getKey();
            System.out.print(nodeCount + ":" + "\t" );
            printNode.printNode();
            ArrayList<String> alDomain= (ArrayList<String>) me.getValue();
            printStringArraylistexclude0(alDomain);
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

        for(node str: TopLeftCorner){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","0011")));
        }

        for(node str: TopRightCorner){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","1001")));
        }

        for(node str: BottomRightCorner){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","1100")));
        }

        for(node str: BottomLeftCorner){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","0110")));
        }


        for(node str: TopWall){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","1010","1001","0011")));
        }

        for(node str: RightWall){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","1100","0101","1001")));
        }

        for(node str: BottomWall){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","1100","0110","1010")));
        }

        for(node str: LeftWall){
            nodeHM.put(str, new ArrayList<String>(Arrays.asList("0000","0110","0101","0011")));
        }
    }

    public boolean isOutofBound(edge edge ){
        if(edge.i < 0 || edge.j < 0)
            return true;

        return false;
    }

    public void removeOutofBoundEdges(ArrayList<edge> edgeList) {
        for (Iterator<edge> it = edgeList.iterator(); it.hasNext(); ) {
            edge edgeA = it.next();
            if(isOutofBound(edgeA)){
                it.remove();
            }
        }
    }

    //Apply arc consistency on Cell with Values equal to zero (0)
    public void applyArcConsistency(ArrayList<cell> zeroCells){
        ArrayList<cell> local = new ArrayList<>();
        local.addAll(zeroCells);

        for (Iterator<cell> it = local.iterator(); it.hasNext();) {
            cell cell = it.next();
            if(cell.value == 0){
                edge Hij = new edge("H", cell.i, cell.j);
                edge Vij = new edge("V", cell.i, cell.j);
                edge Hi1j = new edge("H", cell.i+1, cell.j);
                edge Vij1 = new edge("V", cell.i, cell.j+1);

                if(!nonEssEdgeQueue.contains(Hij) && !isOutofBound(Hij))
                    nonEssEdgeQueue.add(Hij);   // H[i][j]
                if(!nonEssEdgeQueue.contains(Vij) && !isOutofBound(Vij))
                    nonEssEdgeQueue.add(Vij);   // V[i][j]
                if(!nonEssEdgeQueue.contains(Hi1j)  && !isOutofBound(Hi1j))
                    nonEssEdgeQueue.add(Hi1j); // H[i+1][j]
                if(!nonEssEdgeQueue.contains(Vij1)  && !isOutofBound(Vij1))
                    nonEssEdgeQueue.add(Vij1); // V[i][j+1]
            }

        }

        reduceNonEssEdgeDomain(edgeHM);

        int acExhaustCount = 0;
        while(acExhaustCount<5) {
            System.out.print("\n************Exhaust Loop: " + acExhaustCount);
            acExhaustCount++;
            if (!oneCells.isEmpty())
                checkCellValueConstraintAndReduce(oneCells, 1);
            if (!twoCells.isEmpty())
                checkCellValueConstraintAndReduce(twoCells, 2);
            if (!threeCells.isEmpty())
                checkCellValueConstraintAndReduce(threeCells, 3);
        }

    }

    public boolean addEssEdgetoQueue(edge edge){
        if(edgeHM.get(edge).size()==1){
            if(edgeHM.get(edge).get(0).equals("1")){
                return true;
            }

            if(edgeHM.get(edge).get(0).equals("0")){
                return false;
            }
        }

        if(edgeHM.get(edge).size()==2){
            return true;
        }
        return false;
    }

    public void printCells(ArrayList<cell> cellList, int value){
        if(!cellList.isEmpty()){
            System.out.println("\n Printing all remaining cells for CV " + value);
            for(cell remainingCell: cellList){
                System.out.print("\t" + "C" + remainingCell.i + remainingCell.j + "-" + remainingCell.value);
            }
        }
        else {
            System.out.println("\n No remaining cells for CV " + value);
        }
    }

    public void checkCellValueConstraintAndReduce(ArrayList<cell> cellArrayList, int value){
        ArrayList<cell> local = new ArrayList<>();
        local.addAll(cellArrayList);

        for (Iterator<cell> it = local.iterator(); it.hasNext(); ) {
            Queue<edge> essEdgeQueue = new PriorityQueue<edge>();
            cell cell = it.next();

            edge Hij = new edge("H", cell.i, cell.j);
            edge Vij = new edge("V", cell.i, cell.j);
            edge Hi1j = new edge("H", cell.i+1, cell.j);
            edge Vij1 = new edge("V", cell.i, cell.j+1);

            int count = 0;

            if (addEssEdgetoQueue(Hij)){
                count++;
                essEdgeQueue.add(Hij);
            }
            if (addEssEdgetoQueue(Vij)){
                count++;
                essEdgeQueue.add(Vij);
            }
            if (addEssEdgetoQueue(Hi1j)){
                count++;
                essEdgeQueue.add(Hi1j);
            }
            if (addEssEdgetoQueue(Vij1)){
                count++;
                essEdgeQueue.add(Vij1);
            }

            if(count == value){
                cellArrayList.remove(cell);
                while (!essEdgeQueue.isEmpty()){
                    edge edge = essEdgeQueue.poll();
                    edgeHM.get(edge).remove("0");
                    reduceNodeAssignments(nodeHM, edge, 1);
                }
            }
        }

        printCells(cellArrayList, value);

        reduceNonEssEdgeDomain(edgeHM);
        System.out.println("");
    }

    public void printEdgeQueue(Queue<edge> nonEssQueue){
        int queueCount = 0;
        ArrayList<edge> newAL = new ArrayList<>();
        newAL.addAll(nonEssQueue);

        Collections.sort(newAL, new Comparator<edge>() {
            @Override
            public int compare(edge edgeA, edge edgeB){
                int asciiA = (int) edgeA.edgeType.charAt(0);
                int asciiB = (int) edgeB.edgeType.charAt(0);
                int sortbyI = (asciiA - asciiB) + edgeA.i - edgeB.i;
                return sortbyI;
            }
        });

        System.out.print("\nCurrent Non Essential Edge Queue : ");
        for (Iterator<edge> it = newAL.iterator(); it.hasNext(); ) {
            edge edge = it.next();
            edge.printEdge();
            queueCount++;
        }
        System.out.print("\nTotal Non Ess Edge Queue Size: " + queueCount);
    }

    public void reduceNonEssEdgeDomain(HashMap<edge, ArrayList<String>> edgeHM){
        //System.out.println("Printing Non Essential Edges - removal of Cell Value CV=0 Size: " + nonEssEdges.size());
        //printEdgeArraylist(nonEssEdges);

        printEdgeQueue(nonEssEdgeQueue);

        while(!nonEssEdgeQueue.isEmpty()){
            edge edge = nonEssEdgeQueue.poll();
            nonEssEdges.add(edge);
            if(edge != null && !isOutofBound(edge)){
                if(edgeHM.containsKey(edge) ){
                    //edgeHM.put(edge, new ArrayList<String>(Arrays.asList("0")));
                    edgeHM.get(edge).remove("1");
                    reduceNodeAssignments(nodeHM, edge, 0);
                }
            }
        }

        removeDuplicates(nonEssEdges);
        System.out.print("\nTotal Non Essential Edges removed : (Size: " + nonEssEdges.size() + ") : ");
        printEdgeArraylist(nonEssEdges);


    }

    public void reduceNodeAssignments(HashMap<node, ArrayList<String>> nodeHashMap, edge nonEss, int keepDV){
        try{
                //use varable x=0,1 to be replaced in following algos (x = (passed value) ? 0: 1)
                //use str1.concat(str2) for regex
                String x = (keepDV == 0)? String.valueOf(1):String.valueOf(0);

                edge edge = nonEss;
                //if Horizontal edge - reduce satisfying assignment for nodes on left and right of the edge
                if(edge.edgeType.equals("H")){
                    node nodeLeft = new node("N", edge.i, edge.j);
                    node nodeRight = new node("N", edge.i, edge.j+1);

                    if(nodeHashMap.containsKey(nodeLeft)){
                        for (Iterator<String> it = nodeHashMap.get(nodeLeft).iterator(); it.hasNext(); ) {
                            String str = it.next();
                            if(str.matches(("..").concat(x).concat("."))){
                                it.remove();
                            }
                        }
                        forReducedAssignmentsReduceEdgeDomains(nodeLeft, nodeHashMap.get(nodeLeft));
                    }

                    if(nodeHashMap.containsKey(nodeRight)){
                        for (Iterator<String> it = nodeHashMap.get(nodeRight).iterator(); it.hasNext(); ) {
                            String str = it.next();
                            if(str.matches(x.concat("..."))){
                                it.remove();
                            }
                        }
                        forReducedAssignmentsReduceEdgeDomains(nodeRight, nodeHashMap.get(nodeRight));
                    }
                }

                //if Vertical edge - reduce satisfying assignment for nodes on top and bottom of the edge
                if(edge.edgeType.equals("V")){
                    node nodeTop = new node("N", edge.i, edge.j);
                    node nodeBottom = new node("N", edge.i+1, edge.j);

                    if(nodeHashMap.containsKey(nodeTop)){
                        for (Iterator<String> it = nodeHashMap.get(nodeTop).iterator(); it.hasNext(); ) {
                            String str = it.next();
                            if(str.matches(("...").concat(x))){
                                it.remove();
                            }
                        }
                        forReducedAssignmentsReduceEdgeDomains(nodeTop, nodeHashMap.get(nodeTop));
                    }

                    if(nodeHashMap.containsKey(nodeBottom)){
                        for (Iterator<String> it = nodeHashMap.get(nodeBottom).iterator(); it.hasNext(); ) {
                            String str = it.next();
                            if(str.matches((".").concat(x).concat(".."))){
                                it.remove();
                            }
                        }
                        forReducedAssignmentsReduceEdgeDomains(nodeBottom, nodeHashMap.get(nodeBottom));
                    }
                }
        }
        catch (ConcurrentModificationException e){
            System.err.println("Caught Concurrent Modifications: " + e.getMessage());
        }
    }

    public void forReducedAssignmentsReduceEdgeDomains(node node, ArrayList<String> al){
        ArrayList<String> Hij_1 = new ArrayList<>();
        ArrayList<String> Vi_1j = new ArrayList<>();
        ArrayList<String> Hij = new ArrayList<>();
        ArrayList<String> Vij = new ArrayList<>();

        for (Iterator<String> it = al.iterator(); it.hasNext(); ) {
            String str = it.next();
            Hij_1.add(String.valueOf(str.charAt(0)));
            Vi_1j.add(String.valueOf(str.charAt(1)));
            Hij.add(String.valueOf(str.charAt(2)));
            Vij.add(String.valueOf(str.charAt(3)));
        }

        removeStringDuplicates(Hij_1);
        //System.out.print("\n Hij-1: "); printStringArraylist(Hij_1);
        if (Hij_1.size()==1 && Hij_1.get(0).equals("0")){
            edge edge = new edge("H", node.i, node.j-1);
            if(!nonEssEdges.contains(edge) && !isOutofBound(edge))
                nonEssEdgeQueue.add(edge);
        }

        removeStringDuplicates(Vi_1j);
        //System.out.print("\n Vi-1j: "); printStringArraylist(Vi_1j);
        if (Vi_1j.size()==1 && Vi_1j.get(0).equals("0")){
            edge edge = new edge("V", node.i-1, node.j);
            if(!nonEssEdges.contains(edge) && !isOutofBound(edge))
                nonEssEdgeQueue.add(edge);
        }
        removeStringDuplicates(Hij);
        //System.out.print("\n Hij: "); printStringArraylist(Hij);
        if (Hij.size()==1 && Hij.get(0).equals("0")){
            edge edge = new edge("H", node.i, node.j);
            if(!nonEssEdges.contains(edge) && !isOutofBound(edge))
                nonEssEdgeQueue.add(edge);
        }
        removeStringDuplicates(Vij);
        //System.out.print("\n Vij: "); printStringArraylist(Vij);
        if (Vij.size()==1 && Vij.get(0).equals("0")){
            edge edge = new edge("V", node.i, node.j);
            if(!nonEssEdges.contains(edge) && !isOutofBound(edge))
                nonEssEdgeQueue.add(edge);
        }
    }
}
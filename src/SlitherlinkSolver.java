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

    static ArrayList<String> nonEssEdges = null;    //Arraylist for NonEssential Edges wit reduced domain to be used for reduction

    static HashMap<String, ArrayList<String>> edgeHM = new HashMap<String, ArrayList<String>>();    //Hashmap storing Key: Edges and Value: Domain
    static HashMap<String, ArrayList<String>> nodeHM = new HashMap<String, ArrayList<String>>();    //Hashmap for Key: Nodes and Value: Satisfying Assignments (Node Degree either 0 or 2)


    public static void main(String args[]) throws IOException {

        Path path = Paths.get(System.getProperty("user.dir") + "\\src\\output.txt");
        PrintStream out = new PrintStream(new FileOutputStream(path.toFile()));
        System.setOut(out);

        int[][] readMatrix = readPuzzle();  //readPuzzle - reads puzzle from text file


        //Generating Initial Edge Domain Hashmap
        generateInitialEdgeHashMap(readMatrix);
        System.out.println("Initial Edge Domain:\n");
        Map<String, ArrayList<String>> treeMapEdge = new TreeMap<String, ArrayList<String>>(edgeHM); //to print sorted Edge HashMap
        printHM(treeMapEdge);

        //Generating Initial Node Assignments Hashmap
        System.out.println("Initial Node Assignments:\n");
        generateInitialNodeHashMap(readMatrix);
        findNodeonWallsandCorners(readMatrix);
        Map<String, ArrayList<String>> treeMapNode = new TreeMap<String, ArrayList<String>>(nodeHM); //to print sorted Node hashmap
        printHM(treeMapNode);


        //applyZeroAC(readMatrix);


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
        for(int i=0; i<rowCount; i++){
            for(int j=0; j<colCount; j++){
                String Hij = "H" + i + j;
                String Vij = "V" + i + j;
                String Hi1j = "H" + (i+1) + j;
                String Vij1 = "V" + i + (j+1);

                ArrayList<String>  initDomain = new ArrayList<String>();
                initDomain.add("0");
                initDomain.add("1");

                edgeHM.put(Hij, initDomain);
                edgeHM.put(Vij, initDomain);
                edgeHM.put(Hi1j, initDomain);
                edgeHM.put(Vij1, initDomain);
                //System.out.println("Cell: " + matrix[i][j] + "\t Edges: " + Hij + "=" + printArraylist(initDomain) + "\t" + Vij + "=" + printArraylist(initDomain) + "\t" + Hi1j + "=" + printArraylist(initDomain) + "\t" + Vij1 + "=" + printArraylist(initDomain) );
            }
        }
    }

    public static void generateInitialNodeHashMap(int [][] matrix){
        for(int i=0; i<rowCount+1; i++){
            for(int j=0; j<colCount+1; j++){
                String Nij = "N" + i + j;


                ArrayList<String>  initAssignment = new ArrayList<String>();
                String[] assignments = new String[] {"0000", "0011", "0110", "1100", "0101", "1010", "1001"};
                initAssignment.addAll(Arrays.asList(assignments));
                nodeHM.put(Nij, initAssignment);
            }
        }
    }

    public static void removeDuplicates(ArrayList<String> nonEssEdges){
        Set<String> hs = new HashSet<>();
        hs.addAll(nonEssEdges);
        nonEssEdges.clear();
        nonEssEdges.addAll(hs);
    }

    public static void printArraylist(ArrayList<String> al){
        for(String str:al){
            System.out.print("\t" + str);
        }
    }

    public static <K, V> void printHM(Map<K, V> edgeHM){
        Set set = edgeHM.entrySet();
        int edgeCount=0;
        Iterator i = set.iterator();
        while(i.hasNext()){
            edgeCount++;
            Map.Entry me = (Map.Entry) i.next();
            System.out.print(edgeCount + "\t" + me.getKey() + ":");
            ArrayList<String> alDomain= (ArrayList<String>) me.getValue();
            printArraylist(alDomain);
            System.out.println("");
        }
    }

    public static void findNodeonWallsandCorners(int[][] matrix){
        ArrayList<String> TopWall = new ArrayList<String>();
        ArrayList<String> RightWall = new ArrayList<String>();
        ArrayList<String> BottomWall = new ArrayList<String>();
        ArrayList<String> LeftWall = new ArrayList<String>();
        ArrayList<String> TopLeftCorner = new ArrayList<String>();
        ArrayList<String> TopRightCorner = new ArrayList<String>();
        ArrayList<String> BottomRightCorner = new ArrayList<String>();
        ArrayList<String> BottomLeftCorner = new ArrayList<String>();

        for(int i=0; i<=rowCount; i++){
            for(int j=0; j<=colCount; j++){
                if(i == 0 && j == 0){
                    TopLeftCorner.add("N" + i + j);
                }
                else if(i == 0 && j == colCount){
                    TopRightCorner.add("N" + i + j);
                }
                else if(i == rowCount && j == colCount){
                    BottomRightCorner.add("N" + i + j);
                }
                else if(i == rowCount && j == 0){
                    BottomLeftCorner.add("N" + i + j);
                }
                else if(i == 0 && (j != 0 || j == colCount)){
                    TopWall.add("N" + i + j);
                }
                else if(j == colCount && (i!=0 || i != rowCount)){
                    RightWall.add("N" + i + j);
                }
                else if(i == rowCount && (j != 0 || j != colCount)) {
                    BottomWall.add("N" + i + j);
                }
                else if(j == 0 && (i != 0 || i == rowCount)) {
                    LeftWall.add("N" + i + j);
                }
            }
        }
        System.out.println("LeftTop node: " + TopLeftCorner);
        System.out.println("RightTop node: " + TopRightCorner);
        System.out.println("RightBottom node: " + BottomRightCorner);
        System.out.println("LeftBottom node: " + BottomLeftCorner);
        System.out.println("Top Wall: " + TopWall);
        System.out.println("Right Wall: " + RightWall);
        System.out.println("Bottom Wall: " + BottomWall);
        System.out.println("Left Wall: " + LeftWall);


        ArrayList<String> alTopLeftCorner = new ArrayList<String>();
        String[] assignmentsTopLeftCorner = new String[]{"0000","0011"};
        alTopLeftCorner.addAll(Arrays.asList(assignmentsTopLeftCorner));

        ArrayList<String> alTopRightCorner = new ArrayList<String>();
        String[] assignmentsTopRightCorner = new String[]{"0000","1001"};
        alTopRightCorner.addAll(Arrays.asList(assignmentsTopRightCorner));

        ArrayList<String> alBottomRightCorner = new ArrayList<String>();
        String[] assignmentsBottomRightCorner = new String[]{"0000","1100"};
        alBottomRightCorner.addAll(Arrays.asList(assignmentsBottomRightCorner));

        ArrayList<String> alBottomLeftCorner = new ArrayList<String>();
        String[] assignmentsBottomLeftCorner = new String[]{"0000","0110"};
        alBottomLeftCorner.addAll(Arrays.asList(assignmentsBottomLeftCorner));

        ArrayList<String> alTopWall = new ArrayList<String>();
        String[] assignmentsTopWall = new String[]{"0000","1010","1001","0011"};
        alTopWall.addAll(Arrays.asList(assignmentsTopWall));

        ArrayList<String> alRightWall = new ArrayList<String>();
        String[] assignmentsRightWall = new String[]{"0000","0110","0101","0011"};
        alRightWall.addAll(Arrays.asList(assignmentsRightWall));

        ArrayList<String> alBottomWall = new ArrayList<String>();
        String[] assignmentsBottomWall = new String[]{"0000","1100","0110","1010"};
        alBottomWall.addAll(Arrays.asList(assignmentsBottomWall));

        ArrayList<String> alLeftWall = new ArrayList<String>();
        String[] assignmentsLeftWall = new String[]{"0000","0110","0101","0011"};
        alLeftWall.addAll(Arrays.asList(assignmentsLeftWall));

        for(String str: TopLeftCorner){
            nodeHM.put(str, alTopLeftCorner);
        }

        for(String str: TopRightCorner){
            nodeHM.put(str, alTopRightCorner);
        }

        for(String str: BottomRightCorner){
            nodeHM.put(str, alBottomRightCorner);
        }

        for(String str: BottomLeftCorner){
            nodeHM.put(str, alBottomLeftCorner);
        }

        for(String str: TopWall){
            nodeHM.put(str, alTopWall);
        }

        for(String str: RightWall){
            nodeHM.put(str, alRightWall);
        }

        for(String str: BottomWall){
            nodeHM.put(str, alBottomWall);
        }

        for(String str: LeftWall){
            nodeHM.put(str, alLeftWall);
        }
    }

    //Apply arc consistency on Cell with Values equal to zero (0)
    public static void applyZeroAC(int [][] matrix){
        for(int i=0; i<rowCount; i++){
            for(int j=0; j<colCount; j++){
                if(matrix[i][j] == 0){


                }
            }
        }
    }

}
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by Gagandeep.Randhawa on 11/14/2016.
 */
public class Backtracking extends SlitherlinkSolver {
    static ArrayList<cell> allCells = new ArrayList<cell>();
    static ArrayList<cell> zeroCells = new ArrayList<cell>();
    static ArrayList<cell> oneCells = new ArrayList<cell>();
    static ArrayList<cell> twoCells = new ArrayList<cell>();
    static ArrayList<cell> threeCells = new ArrayList<cell>();
    static ArrayList<cell> emptyCells = new ArrayList<cell>();

    static HashMap<edge, ArrayList<String>> edgeHM = new HashMap<edge, ArrayList<String>>();    //Hashmap storing Key: Edges and Value: Domain
    static HashMap<node, ArrayList<String>> nodeHM = new HashMap<node, ArrayList<String>>();

    static ArrayList<edge> nonEssEdges = new ArrayList<edge>();    //Arraylist for NonEssential Edges wit reduced domain to be used for reduction

    public Backtracking(ArrayList<cell> oneCells, ArrayList<cell> twoCells, ArrayList<cell> threeCells, ArrayList<cell> emptyCells, ArrayList<edge> nonEssEdges, HashMap<edge, ArrayList<String>> edgeHM, HashMap<node, ArrayList<String>> nodeHM){
        this.oneCells = oneCells;
        this.twoCells = twoCells;
        this.threeCells = threeCells;
        this.emptyCells = emptyCells;

        this.edgeHM = edgeHM;
        this.nodeHM = nodeHM;

        this.nonEssEdges = nonEssEdges;
    }

    public void printData(){
        System.out.println("\n*********EDGE HM in Backtracking*************");
        printEdgeHM(edgeHM);
        System.out.println("\n*********NODE HM in Backtracking*************");
        printNodeHM(nodeHM);

        printCells(oneCells, 1);
        printCells(twoCells, 2);
        printCells(threeCells, 3);
        printEdgeArraylist(nonEssEdges);
    }
}

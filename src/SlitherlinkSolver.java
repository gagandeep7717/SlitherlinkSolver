import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Authors: Gagandeep Randhawa.
 *          Hemant Nimje.
 * File Created on on 10/22/2016
 */
public class SlitherlinkSolver {
    //row and col count static to allow dynamic size of puzzle
    static int rowCount = 0;
    static int colCount = 0;

    // Static Horizontal and Vertical Edge Matrix to apply Arc Consistencies
    static int[][] horEdgeMatrix;
    static int[][] verEdgeMatrix;

    public static void main(String args[]) throws IOException {
        int[][] readMatrix = readPuzzle();
        horEdgeMatrix = new int[rowCount+1][colCount];
        verEdgeMatrix = new int[rowCount][colCount+1];
        System.out.println("Printing Read Matrix");
        printMatrix(readMatrix);
        System.out.println("Initial Cell Values and Corresponding H and V Edges");
        //printCellEdgesValue(readMatrix);
        System.out.println("Initial Edge Matrix without Arc Consistency");
        generateInitialEdgeMatrix(readMatrix);
        printHorEdgeMatrix(horEdgeMatrix);
        printVerEdgeMatrix(verEdgeMatrix);

        applyZeroAC(readMatrix);
        printHorEdgeMatrix(horEdgeMatrix);
        printVerEdgeMatrix(verEdgeMatrix);

        AConLeftTop(readMatrix);
        printHorEdgeMatrix(horEdgeMatrix);
        printVerEdgeMatrix(verEdgeMatrix);

        AConLeftBottom(readMatrix);
        printHorEdgeMatrix(horEdgeMatrix);
        printVerEdgeMatrix(verEdgeMatrix);

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

        //printMatrix(matrix);

        //Final Row and Column count to be provided to the solver
        System.out.println("Rows = " + rowCount );
        System.out.println("Columns = " + colCount);
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
                System.out.print("\t" + matrix[i][j]);
            }
            System.out.println();
        }
    }
    public static void printHorEdgeMatrix(int[][] matrix){
        System.out.println("Horizontal Edge Matrix:");
        System.out.print("\t" + "H");
        for(int i=0; i<colCount; i++){
            System.out.print("\t" + i);
        }
        System.out.println("");
        for(int i=0; i<rowCount+1; i++){
            System.out.printf("\t" + i);
            for(int j=0; j<colCount; j++){
                System.out.print("\t" + matrix[i][j]);
            }
            System.out.println();
        }
    }
    public static void printVerEdgeMatrix(int[][] matrix){
        System.out.println("Vertical Edge Matrix:");
        System.out.print("\t" + "V");
        for(int i=0; i<colCount+1; i++){
            System.out.print("\t" + i);
        }
        System.out.println("");
        for(int i=0; i<rowCount; i++){
            System.out.printf("\t" + i);
            for(int j=0; j<colCount+1; j++){
                System.out.print("\t" + matrix[i][j]);
            }
            System.out.println();
        }
    }

    public static void printCellEdgesValue(int [][] matrix){
        for(int i=0; i<rowCount; i++){
            for(int j=0; j<colCount; j++){
                if(matrix[i][j] != 7){
                    System.out.print("C" + i + j + "=" + matrix[i][j]);
                    System.out.print("\t Edges = H" + i + j + "=" + 0);
                    System.out.print(", V" + i + j + "=" + 0);
                    System.out.print(", H" + (i+1) + j + "=" + 0);
                    System.out.print(", V" + i + (j+1) + "=" + 0);
                    System.out.println("");

                }
                else {
                    System.out.print("C" + i + j + "=" + '\u221e');
                    System.out.print("\t Edges = H" + i + j + "=" + 0);
                    System.out.print(", V" + i + j + "=" + 0);
                    System.out.print(", H" + (i+1) + j + "=" + 0);
                    System.out.print(", V" + i + (j+1) + "=" + 0);
                    System.out.println("");
                }
            }
            System.out.println();
        }

    }
    //Function to Generate Horizontal and Vertical edge values before applying Arc Consistency
    public static void generateInitialEdgeMatrix(int [][] matrix){
        for(int i=0; i<rowCount; i++){
            for(int j=0; j<colCount; j++){
                if(matrix[i][j] != 7){
                    System.out.print("C" + i + j + "=" + matrix[i][j]);
                    System.out.print("\t Edges = H" + i + j + "=" + 0);
                    horEdgeMatrix[i][j] = 0;
                    System.out.print(", V" + i + j + "=" + 0);
                    verEdgeMatrix[i][j] = 0;
                    System.out.print(", H" + (i+1) + j + "=" + 0);
                    horEdgeMatrix[i+1][j] = 0;
                    System.out.print(", V" + i + (j+1) + "=" + 0);
                    verEdgeMatrix[i][j+1] = 0;
                    System.out.println("");

                }
                else {
                    System.out.print("C" + i + j + "=" + '\u221e');
                    System.out.print("\t Edges = H" + i + j + "=" + 0);
                    horEdgeMatrix[i][j] = 0;
                    System.out.print(", V" + i + j + "=" + 0);
                    verEdgeMatrix[i][j] = 0;
                    System.out.print(", H" + (i+1) + j + "=" + 0);
                    horEdgeMatrix[i+1][j] = 0;
                    System.out.print(", V" + i + (j+1) + "=" + 0);
                    verEdgeMatrix[i][j+1] = 0;
                    System.out.println("");
                }
            }
            System.out.println();
        }

    }

    //Apply arc consistency on Cell with Values equal to zero (0)
    public static void applyZeroAC(int [][] matrix){
        for(int i=0; i<rowCount; i++){
            for(int j=0; j<colCount; j++){
                if(matrix[i][j] == 0){
                    //System.out.print("C" + i + j + "=" + matrix[i][j]);
                    //System.out.print("\t Edges = H" + i + j + "=" + 0);
                    horEdgeMatrix[i][j] = -1;
                    //System.out.print(", V" + i + j + "=" + 0);
                    verEdgeMatrix[i][j] = -1;
                    //System.out.print(", H" + i + (j+1) + "=" + 0);
                    horEdgeMatrix[i+1][j] = -1;
                    //System.out.print(", V" + (i+1) + j + "=" + 0);
                    verEdgeMatrix[i][j+1] = -1;
                    //System.out.println("");

                }
            }
        }
    }

    //Apply Arc Consistency on corners
    public static void applyAConCorners(int [][] matrix){
        int leftTop = matrix[0][0];
        int leftBottom = matrix[rowCount-1][0];
        int rightTop = matrix[0][colCount-1];
        int rightBottom = matrix[rowCount-1][colCount-1];
        System.out.println("LT=" + leftTop + "LB=" + leftBottom + "RT=" + rightTop + "RB=" + rightBottom );


    }

    public static void AConLeftTop(int [][] matrix){
        int leftTop = matrix[0][0];
        switch(leftTop){
            case 0:
                //If left top is 0, all four edges of cell are already -1 by zeroAC
                //set H01 of C01 and V10 of C10 to -1
                horEdgeMatrix[0][1] = -1;
                verEdgeMatrix[1][0] = -1;
                break;

            case 1:
                //if Left top is 1, set H00 and V00 to -1
                horEdgeMatrix[0][0] = -1;
                verEdgeMatrix[0][0] = -1;
                break;

            case 2:
                //if left top is 2, set H01 of C01 and V10 of C10 to 1
                horEdgeMatrix[0][1] = 1;
                verEdgeMatrix[1][0] = 1;
                break;

            case 3:
                //if Left top is 3, set H00 and V00 to 1
                horEdgeMatrix[0][0] = 1;
                verEdgeMatrix[0][0] = 1;
                break;

            default:
                break;
        }
    }

    public static void AConLeftBottom(int [][] matrix){
        int leftBottom = matrix[rowCount-1][0];
        switch(leftBottom){
            case 0:
                //If left Bottom is 0, all four edges of cell are already -1 by zeroAC
                horEdgeMatrix[rowCount][1] = -1;
                verEdgeMatrix[rowCount-2][0] = -1;
                break;

            case 1:
                //if left bottom is 1
                horEdgeMatrix[rowCount][0] = -1;
                verEdgeMatrix[rowCount-1][0] = -1;
                break;

            case 2:
                //If left Bottom is 2
                horEdgeMatrix[rowCount][1] = 1;
                verEdgeMatrix[rowCount-2][0] = 1;
                break;

            case 3:
                //if left bottom is 3
                horEdgeMatrix[rowCount][0] = 1;
                verEdgeMatrix[rowCount-1][0] = 1;
                break;

            default:
                break;
        }
    }

    public static void AConRightTop(int [][] matrix){
        int leftTop = matrix[0][0];
        switch(leftTop){
            case 0:
                break;

            case 1:
                break;

            case 2:
                break;

            case 3:
                break;

            default:
                break;
        }
    }

    public static void AConRightBottom(int [][] matrix){
        int leftTop = matrix[0][0];
        switch(leftTop){
            case 0:
                break;

            case 1:
                break;

            case 2:
                break;

            case 3:
                break;

            default:
                break;
        }
    }
}
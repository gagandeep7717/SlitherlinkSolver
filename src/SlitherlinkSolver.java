import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Gagandeep.Randhawa on 10/22/2016.
 */
public class SlitherlinkSolver {
    public static void main(String args[]) throws IOException {
        int rowCount = 0;
        int colCount = 0;

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

        for(int i=0; i<rowCount; i++){
            for(int j=0; j<colCount; j++){
                System.out.print(matrix[i][j]);
            }
            System.out.println();
        }
        //Final Row and Column count to be provided to the solver
        System.out.println("Rows = " + rowCount );
        System.out.println("Columns = " + colCount);

        //Created Branch Gagandeep-Staging
    }

}




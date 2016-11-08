/**
 * Created by Gagandeep.Randhawa on 11/8/2016.
 */
public class cell implements Comparable{
    int i;
    int j;
    int value;

    public cell(int i, int j, int value){
        this.i = i;
        this.j = j;
        this.value = value;
    }

    public void printCell(){
        System.out.print("Cell C" + i + j + "\tVal: " + value);
        System.out.print("\t");
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) {
            return 1;
        }

        if (!(o instanceof cell)) {
            return 0;
        }

        cell cell = (cell) o;
        return (i == cell.i && j == cell.j && value == cell.value) ? 1 : 0;
    }
}

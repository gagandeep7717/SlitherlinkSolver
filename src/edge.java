import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.security.*;

/**
 * Created by Gagandeep.Randhawa on 10/31/2016.
 */
public class edge {
    String edgeType;
    int i;
    int j;

    public edge(String edgeType, int i, int j){
        this.edgeType = edgeType;
        this.i = i;
        this.j = j;
    }

    public void printEdge(){
        System.out.print(edgeType + i + j);
        System.out.print("\t");
    }

    @Override
    public boolean equals(Object o){
        if (this == o){
            return true;
        }

        if(!(o instanceof edge)){
            return false;
        }

        edge edge = (edge) o;
        return i==edge.i && j==edge.j && Objects.equals(edgeType, edge.edgeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edgeType, i, j);
    }


}


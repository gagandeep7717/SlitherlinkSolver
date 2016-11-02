import java.util.Objects;

public class node {
    String nodeType;
    int i;
    int j;

    public node(String nodeType, int i, int j){
        this.nodeType = nodeType;
        this.i = i;
        this.j = j;
    }

    public void printNode(){
        System.out.print(nodeType + i + j);
        System.out.print("\t");
    }

    @Override
    public boolean equals(Object o){
        if (o == this) return true;

        if(!(o instanceof node)){
            return false;
        }

        node node = (node) o;
        return i==node.i && j==node.j && Objects.equals(nodeType, node.nodeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeType, i, j);
    }


}

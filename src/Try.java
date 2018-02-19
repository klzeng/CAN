
import java.util.LinkedList;

public class Try {

    public static void main(String args[]){
        LinkedList<Integer> test = new LinkedList<Integer>();
        test.add(1);
        test.add(2);
        test.add(3);
        test.add(4);
        test.add(5);
        LinkedList<Integer> toRemove = new LinkedList<Integer>();
        for(Integer x : test){
            if(x == 3 || x==4 ) toRemove.add((new Integer(x)));
        }
        for(Integer each : toRemove) test.remove(each);

        for(int each: test) System.out.println(each);
    }
}

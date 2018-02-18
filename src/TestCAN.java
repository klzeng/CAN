
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.assertj.core.api.Assertions.*;

/**
 * Created by soukonling on 2018/2/17.
 */
public class TestCAN {


    @org.junit.Test
    public void testCase(){
        System.out.println("testing");
    }

    @org.junit.Test
    public void testComputeHash(){
        Peer node = new Peer();
        System.out.print(node.computeHash("hello Phoenix"));
    }

    @org.junit.Test
    public void testSplitZone(){
        Peer node = new Peer();
        Point start = new Point(0,0);
        Point end = new Point(10,10);
        Point test_point = new Point(1,3);

        Zone oriZone = new Zone(start, end);
        node.set_zone(oriZone);
        Zone toReturn = node.splitZone(test_point);
        Assertions.assertThat(node.node.zone.start_point).isEqualToComparingFieldByField(new Point(5,0));
        Assertions.assertThat(node.node.zone.end_point).isEqualToComparingFieldByField(new Point(10,10));
        Assertions.assertThat(toReturn.start_point).isEqualToComparingFieldByField(new Point(0,0));
        Assertions.assertThat(toReturn.end_point).isEqualToComparingFieldByField(new Point(5,10));
    }

}
import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;

/**
 * Created by soukonling on 2018/2/16.
 */
public interface Node extends Remote{

    // 5 commands

    public String insert(String keyword, LinkedList<String> path) throws RemoteException;

    public String search(String keyword, LinkedList<String> path) throws RemoteException;

    public LinkedList<String> join(String peer, float[] destPointArray, LinkedList<String> path) throws RemoteException;

    public float[] join() throws RemoteException;

    public String view(String peer) throws RemoteException;

    public String view(LinkedList<String> viewed) throws RemoteException;

    // helper commands

    public void leave() throws RemoteException;

    public String testInvoke(String cmd) throws RemoteException;

    public String cmdDispatch(String input) throws RemoteException;

    public boolean updateNeighbors(String newNeighbor, LinkedList<float[]> coordtns, boolean leaving, boolean updateCoord) throws RemoteException;

    public LinkedList<float[]> getCoordnts() throws RemoteException;

    public LinkedList<float[]> takeOver(LinkedList<float[]> coordnts, LinkedList<String> contents) throws RemoteException;

}

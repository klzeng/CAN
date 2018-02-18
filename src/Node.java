import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;

/**
 * Created by soukonling on 2018/2/16.
 */
public interface Node extends Remote{

    public String insert(String keyword) throws RemoteException;

    public String search(String keyword) throws RemoteException;

    public float[] join(String peer) throws RemoteException;

    public float[] join() throws RemoteException;

    public String view(String peer) throws RemoteException;

    public String view(LinkedList<String> viewed) throws RemoteException;

    public String leave() throws RemoteException;

    public String testInvoke(String cmd) throws RemoteException;

    public String cmdDispatch(String input) throws RemoteException;

}

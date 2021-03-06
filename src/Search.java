import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;

public class Search {

    public static void main(String[] args){

        if(System.getSecurityManager() == null){
            System.setSecurityManager(new SecurityManager());
        }

        if(args.length < 2){
            System.out.println("usage: Search 'keyword' 'peer'");
            return;
        }

        try{
            String nodeName = args[1];
            Registry registry = LocateRegistry.getRegistry(nodeName, Peer.RMI_PORT);
            Node node = (Node) registry.lookup(nodeName);
            LinkedList<String> path =  new LinkedList<String>();
            String reply = node.search(args[0], path);
            String response = "-----------------------------------------\nSearching ...\nIP Route:\n";
            response += reply;
            System.out.println(response);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

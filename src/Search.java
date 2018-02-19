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
            String name = "Node";
            Registry registry = LocateRegistry.getRegistry(args[1]);
            Node node = (Node) registry.lookup(name);
            LinkedList<String> path =  new LinkedList<String>();
            String reply = node.search(args[0], path);
            String response = "-----------------------------------------\nSearching ...\nIP Route:\n";
            response += InetAddress.getByName(args[1]).getHostAddress() + "->\n" + reply;
            System.out.println(response);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;

public class View {

    public static void main(String[] args){

        if(System.getSecurityManager() == null){
            System.setSecurityManager(new SecurityManager());
        }

        if(args.length <=1){
            if(args.length==0){
                // call the bootnode to view all nodes
                try{
                    Registry registry = LocateRegistry.getRegistry(Peer.BOOTSTRAP_HOSTNAME, Peer.RMI_PORT);
                    Node node = (Node) registry.lookup(Peer.BOOTSTRAP_HOSTNAME);
                    LinkedList<String> viwed = new LinkedList<String>();
                    String reply = node.view(viwed);;
                    System.out.println(reply);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else {
                try{
                    String nodeName = args[0];
                    Registry registry = LocateRegistry.getRegistry(nodeName, Peer.RMI_PORT);
                    Node node = (Node) registry.lookup(nodeName);
                    String reply = node.view(nodeName);;
                    System.out.println(reply);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }else {
            System.out.println("usage: View <peer>");
        }


    }
}

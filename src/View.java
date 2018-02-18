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
                    String name = "Node";
                    Registry registry = LocateRegistry.getRegistry("Zengs-Macbook.fios-router.home");
                    Node node = (Node) registry.lookup(name);
                    LinkedList<String> viwed = new LinkedList<String>();
                    String reply = node.view(viwed);;
                    System.out.println(reply);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else {
                try{
                    String name = "Node";
                    Registry registry = LocateRegistry.getRegistry(args[0]);
                    Node node = (Node) registry.lookup(name);
                    String reply = node.view(args[0]);;
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

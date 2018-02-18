import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Join {
    public static void main(String args[]){
        if(System.getSecurityManager() == null){
            System.setSecurityManager(new SecurityManager());
        }

        if(args.length != 0){
            String hostName = args[0];
            try{
                String name = "Node";
                Registry registry = LocateRegistry.getRegistry("Zengs-Macbook.fios-router.home");
                Node node = (Node) registry.lookup(name);
                InetAddress hostIP = InetAddress.getByName(hostName);
                String IP = hostIP.getHostAddress();

                float[] coords = node.join(hostName);
                Point start =  new Point(coords[0], coords[1]);
                Point end = new Point(coords[2], coords[3]);
                Zone zone = new Zone(start, end);
                Peer peer = new Peer();
                peer.set_zone(zone);
                peer.node.setIP(IP);
                peer.node.setName(hostName);

                Node stub = (Node) UnicastRemoteObject.exportObject(peer, 0);
//                LocateRegistry.createRegistry(0);
                Registry registry1 = LocateRegistry.getRegistry();
                registry1.rebind(name,stub);
                System.out.println("Joined!");
                System.out.println(peer.node.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            try{
                String name = "Node";
                Registry registry = LocateRegistry.getRegistry(args[0]);
                Node bootNode = (Node) registry.lookup(name);
                Scanner scan = new Scanner(System.in);

            }catch (Exception e){
                e.printStackTrace();
            }
        }


    }
}

//while(true){
//        System.out.println("------------------------");
//        System.out.println("Please enter your command:");
//        System.out.println("------------------------");
//        String input = scan.nextLine();
//        String inputs[] = input.split(" ");
//        String cmd = inputs[0];
//        String arg = "";
//        if(inputs.length > 1){
//        arg = inputs[1];
//        }
//
//        String reply = "";
//        if(cmd.contentEquals("join")){
//        if(!arg.contentEquals("")){
//        float[] myZone = bootNode.join(arg);
////                        Runtime r = Runtime.getRuntime();
////                        r.exec("rmiregistry &");
////                        String nodeName = "Node";
////                        Peer node = new Peer();
////                        node.set_zone(myZone);
////                        Node stub = (Node) UnicastRemoteObject.exportObject(node, 0);
////                        Registry myregistry = LocateRegistry.getRegistry();
////                        myregistry.rebind(name, stub);
////                        System.out.println("Node Bound.");
//        for(int x=0; x<myZone.length; x++){
//        System.out.println(myZone[x]);
//        }
//        }else {
//        float[] myZone = bootNode.join();
//        }
//        }else if(cmd.contentEquals("insert")){
//        if(inputs.length != 3){
//        System.out.println("usage: insert 'keyword' 'peer'");
//        continue;
//        }
//        String peer = inputs[2];
//        bootNode.insert(arg);
//        }else if(cmd.contentEquals("view")){
//        if(!arg.contentEquals("")){
//        reply = bootNode.view();
//        }else {
//        reply = bootNode.view(arg);
//        }
//        }else if(cmd.contentEquals("leave")){
//        // need to check if joined
//        reply = bootNode.leave();
//        }
//
////                reply = bootNode.testInvoke(cmd);
//        System.out.println(reply);
//        }
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Scanner;

public class Join {
    public static void main(String args[]){
        if(System.getSecurityManager() == null){
            System.setSecurityManager(new SecurityManager());
        }

        if(args.length != 0){
            String newNodeName = args[0];
            try{
                Registry registry = LocateRegistry.getRegistry(Peer.BOOTSTRAP_HOSTNAME, Peer.RMI_PORT);
                Node bootNode = (Node) registry.lookup(Peer.BOOTSTRAP_HOSTNAME);

                float x = (float) (Math.random()*10);
                float y = (float) (Math.random()*10);

                LinkedList<String> path2join = new LinkedList<String>();
                LinkedList<String> joinInfo = bootNode.join(newNodeName, new float[]{x,y}, path2join);

                if(joinInfo == null) {
                    System.out.println("Failure!");
                    System.exit(0);
                }


                // set up the zone of the new node
                float xStart = Float.parseFloat(joinInfo.poll());
                float yStart = Float.parseFloat(joinInfo.poll());
                float xEnd = Float.parseFloat(joinInfo.poll());
                float yEnd = Float.parseFloat(joinInfo.poll());
                Point start =  new Point(xStart, yStart);
                Point end = new Point(xEnd, yEnd);
                Zone zone = new Zone(start, end);
                Peer peer = new Peer();
                peer.addZone(zone);

                InetAddress hostIP = InetAddress.getByName(newNodeName);
                String IP = hostIP.getHostAddress();
                peer.node.setIP(IP);
                peer.node.setName(newNodeName);

                // set up the neighbors of the new node
                while(!joinInfo.isEmpty()){
                    String neighbor = joinInfo.poll();
                    if(neighbor.contentEquals("KEYWORDS")) break;

//                    System.out.println("\nadding neighbor: " + neighbor + "\n");

                    Registry registry1 = LocateRegistry.getRegistry(neighbor, Peer.RMI_PORT);
                    Node toAdd = (Node) registry1.lookup(neighbor);
                    LinkedList<float[]> zoneCoordnts = toAdd.getCoordnts();

                    String neighborIP = InetAddress.getByName(neighbor).getHostAddress();

                    Node_Base neighbor2Add = new Node_Base();
                    neighbor2Add.name = neighbor;
                    neighbor2Add.IP = neighborIP;
                    neighbor2Add.addZones(zoneCoordnts);
                    peer.neighbors.add(neighbor2Add);
                }

                // store the keywords belong to the new node
                while (!joinInfo.isEmpty()){
                    String keyword = joinInfo.poll();
                    peer.contents.add(keyword);
                }

                // set up done, now register it
                Node stub = (Node) UnicastRemoteObject.exportObject(peer, 0);
                Registry registry1 = LocateRegistry.createRegistry(Peer.RMI_PORT);
                registry1.rebind(newNodeName,stub);
                System.out.println("\nJoined!\n\n");
                System.out.println(peer.node.toString());

                peer.spin();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            try{
                String name = "Node";
                Registry registry = LocateRegistry.getRegistry(Peer.BOOTSTRAP_HOSTNAME, Peer.RMI_PORT);
                Node bootNode = (Node) registry.lookup(name);
                Scanner scan = new Scanner(System.in);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}

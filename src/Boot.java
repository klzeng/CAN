/**
 * Created by soukonling on 2018/2/16.
 */
import javax.xml.ws.soap.Addressing;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Boot {
    public static void main(String args[]){

        if(System.getSecurityManager() == null){
            System.setSecurityManager(new SecurityManager());
        }

        // initialize the boostrap node
        Point start_point = new Point(0,0);
        Point end_point = new Point(10,10);
        String name = "Node";

        Peer bootNode = new Peer();
        if (args.length > 0){
            try{
                InetAddress IP = InetAddress.getByName(args[0]);
                bootNode.node.setName(IP.getHostName());
                bootNode.node.setIP(new String(IP.getHostAddress()));

            }catch (UnknownHostException e) {
                System.err.println("Unknow Host");
                e.printStackTrace();
            }
        }else {
            try{
                InetAddress IP = InetAddress.getLocalHost();
                bootNode.node.setName(IP.getHostName());
                bootNode.node.setIP(new String(IP.getHostAddress()));
            }catch (UnknownHostException e){
                e.printStackTrace();
            }
        }

        bootNode.set_zone(new Zone(start_point, end_point));

        try{
            // register it
            Node stub = (Node) UnicastRemoteObject.exportObject(bootNode, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("------------------------");
            System.out.println("Bootstrap node bound");
            System.out.println("HOST_NAME: " + bootNode.node.name);
            System.out.println("HOST_IP: " + bootNode.node.IP);
            System.out.println("------------------------");
        }catch (Exception e){
            System.err.println("BootStrap expection:");
            e.printStackTrace();
        }
    }
}

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
import java.util.LinkedList;
import java.util.Scanner;


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

        bootNode.addZone(new Zone(start_point, end_point));

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
            System.out.println("command you could issue:\n");
            System.out.println("insert, search, view, leave\n");
            System.out.println("------------------------");

            Scanner scan = new Scanner(System.in);
            while (true){
                System.out.print("> ");
                String input = scan.nextLine();
                String inputs[] = input.split(" ");
                String cmd = inputs[0];
                String arg = "";
                if(inputs.length > 1){
                    arg = inputs[1];
                }

                String reply = "";
                LinkedList<String> path = new LinkedList<String>();
                if(cmd.contentEquals("insert")){
                    reply += "\nRoute to insert node:\n";
                    if(inputs.length ==2){
                        arg = arg.split("\"")[1];
                        reply += bootNode.insert(arg, path);
                    }else {
                        inputs = input.split("\"");
                        if(inputs.length != 2){
                            System.out.println("usage: insert \"keyword\"");
                            continue;
                        }
                        reply += bootNode.insert(inputs[1], path);
                    }
                }else if(cmd.contentEquals("view")){
                    if(arg.contentEquals("")){
                        LinkedList<String> viewed = new LinkedList<String>();
                        reply = bootNode.view(viewed);
                    }else {
                        reply = bootNode.view(arg);
                    }
                }else if(cmd.contentEquals("leave")){
                    System.out.println("Issue leave on Bootstrap node make it unscalable(in my implementation).\n");
                    bootNode.leave();
                }else if(cmd.contentEquals("search")){
                    reply += "\nSearching Route:";
                    if(inputs.length ==2){
                        reply = bootNode.search(arg, path);
                    }else {
                        inputs = input.split("\"");
                        if(inputs.length != 2){
                            System.out.println("usage: search \"keyword\"");
                            continue;
                        }
                        reply = bootNode.search(inputs[1], path);
                    }
                }

                System.out.println(reply);
            }
        }catch (Exception e){
            System.err.println("BootStrap expection:");
            e.printStackTrace();
        }
    }
}

/**
 * Created by soukonling on 2018/2/16.
 */
import java.io.File;
import java.lang.System;
import java.lang.Error;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;


public class Peer implements Node{
    Node_Base node;
    LinkedList<Node_Base> neighbors;
    LinkedList<String> contents;

    //---------------------------------------------------------
    // constructors
    public Peer(){
        this.node = new Node_Base();
        this.neighbors =  new LinkedList<Node_Base>();
        this.contents = new LinkedList<String>();
    }

    //---------------------------------------------------------
    // remote methods


    @Override
    public String cmdDispatch(String input) {
        String inputs[] = input.split(" ");
        String cmd = inputs[0];
        if(input.length() == 2){
            String arg = inputs[1];
        }
        return null;
    }

    @Override
    public float[] getCoordnts() {
        return this.node.zone.getCoordinateArray();
    }

    @Override
    public LinkedList<String> join(String peer, float[] destPointArray) {
//        float x = (float) (Math.random()*10);
//        float y = (float) (Math.random()*10);
        Point destPoint = new Point(destPointArray[0], destPointArray[1]);
        System.out.println("--------------------------------\n");
        System.out.println("Notification - New Node Joining:\n\n");
        System.out.println("\nthe point chosed to join: " + destPoint.toString());
        if(this.node.zone.inZone(destPoint)){

            Zone splitOut = this.splitZone(destPoint);
            System.out.println("\nthe zone to split out: " + splitOut.toString());
            System.out.println("\nmy zone is: " + this.node.zone.toString());

            // add new neighbor
            Node_Base newNeighbor = this.addnewNeighbor(peer, splitOut.getCoordinateArray());

            LinkedList<String> ret = new LinkedList<String>();
            ret.add(new String(Float.toString(splitOut.start_point.x)));
            ret.add(new String(Float.toString(splitOut.start_point.y)));
            ret.add(new String(Float.toString(splitOut.end_point.x)));
            ret.add(new String(Float.toString(splitOut.end_point.y)));
            ret.add(this.node.name);

            for(Node_Base each : this.neighbors){
                // notify neighbors new node joined
                if(each.name == newNeighbor.name) continue;

                try {
                    Registry registry = LocateRegistry.getRegistry(each.name);
                    Node neighbor = (Node) registry.lookup("Node");
                    // if need to add the new node as neighbor
                    boolean neighborOfNew = neighbor.updateNeighbors(newNeighbor.name, newNeighbor.zone.getCoordinateArray());
                    if(neighborOfNew){
                        ret.add(each.name);
                    }
                    // if need to remove the old node from neighbors
                    neighbor.updateNeighbors(this.node.name, this.node.zone.getCoordinateArray());
                }catch (Exception e){
                    System.out.println("notify neighbor failed!");
                    e.printStackTrace();
                }

                if(!this.isNeighbor(each.zone.getCoordinateArray())) this.neighbors.remove(each);
            }

            this.neighbors.add(newNeighbor);
            System.out.println("\n\nSuccees!\n--------------------------------\n");
            return ret;
        }else {
            Node_Base closerNode = this.route2next(destPoint);
            if( closerNode != null){
                try{
                    Registry registry = LocateRegistry.getRegistry(closerNode.name);
                    Node node  = (Node) registry.lookup("Node");
                    return node.join(peer, destPointArray);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    @Override
    public boolean updateNeighbors(String newNeighbor, float[] coordnts) {
        Point start = new Point(coordnts[0], coordnts[1]);
        Point end = new Point(coordnts[2], coordnts[3]);

        if(this.isNeighbor(coordnts)) {
            this.addnewNeighbor(newNeighbor, coordnts);
            return true;
        }
        else {
            for(Node_Base each : this.neighbors){
                if( each.name == newNeighbor){
                    this.neighbors.remove(each);
                }
            }
        }

        return false;
    }

    public Node_Base addnewNeighbor(String neighborName, float[] coordnts){

        Node_Base newNeighbor = new Node_Base();
        newNeighbor.setName(neighborName);

        try {
            newNeighbor.setIP(InetAddress.getByName(neighborName).getHostAddress());
        } catch (Exception e){
            e.printStackTrace();
        }
        Point start = new Point(coordnts[0], coordnts[1]);
        Point end =  new Point(coordnts[2], coordnts[3]);
        newNeighbor.set_zone(start, end);
        this.neighbors.add(newNeighbor);

        return newNeighbor;
    }

    public boolean isNeighbor(float[] coordnts){

        Point start = new Point(coordnts[0], coordnts[1]);
        Point end = new Point(coordnts[2], coordnts[3]);

        if( start.y <= this.node.zone.start_point.y && end.y >= this.node.zone.end_point.y ||
                start.y >= this.node.zone.start_point.y && end.y <= this.node.zone.end_point.y){
            // y axes collapse
            if( start.x == this.node.zone.end_point.x || end.x == this.node.zone.start_point.x){
                // contiguous
                return true;
            }
        }

        if( start.x <= this.node.zone.start_point.x && end.x >= this.node.zone.end_point.x ||
                start.x >= this.node.zone.start_point.x && end.x <= this.node.zone.end_point.x){
            // x axes collapse
            if( start.y == this.node.zone.end_point.y || end.y == this.node.zone.start_point.y){
                // contiguous
                return true;
            }
        }

        return false;
    }

    @Override
    public float[] join() {
        return null;
    }

    @Override
    public String insert(String keyword) {

        Point destPoint = this.computeHash(keyword);
        if(this.node.zone.inZone(destPoint)){
            this.contents.add(keyword);
            String reply = "\nStored in node:\n";
            reply += this.node.toString();
            return this.node.IP + "\n--->" + reply;
        }else {
            Node_Base nextNeighbor = this.route2next(destPoint);

            if(nextNeighbor != null){
                try {
                    String name = "Node";
                    Registry registry = LocateRegistry.getRegistry(nextNeighbor.name);
                    Node node = (Node) registry.lookup(name);
                    return node.insert(keyword);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        return "Failure";
    }

    @Override
    public String search(String keyword) {
        Point destPoint = this.computeHash(keyword);
        if(this.node.zone.inZone(destPoint)){
            if(this.contents.contains(keyword)){
                String reply = "Found in node:\n";
                reply += this.node.toString();
                return this.node.IP + "\n" + reply;
            }
            return "Failure";
        }

        Node_Base nextNeighbor = this.route2next(destPoint);

        if(nextNeighbor != null){
            try {
                String name = "Node";
                Registry registry = LocateRegistry.getRegistry(nextNeighbor.name);
                Node node = (Node) registry.lookup(name);
                return node.search(keyword);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return "Failure";

    }

    @Override
    public String view(String peer) {
        if(this.node.name.contentEquals(peer)){

            String info = "\n\nInfo of " + peer + "\n\n";
            info += this.node.toString();
            info += "\nneighbors:\n";
            for(Node_Base each : this.neighbors){
                info += each.name + "\n";
            }
            info += "---------------------------\n";
            info += "keywords in this node:\n";
            for(String each: this.contents){
                info += each + "\n";
            }
            info += "---------------------------";
            return info;
        }else {
            String info = "something is wrong.\n";
            info += "passed in: " + peer + "\n";
            info += "this node: " + this.node.name + "\n\n";
            return info;
        }
    }

    @Override
    public String view(LinkedList<String> viewed) {
        if(viewed.contains(this.node.name)){
            return null;
        }else {
            String info = this.view(this.node.name);
            viewed.add(this.node.name);
            for(Node_Base each: this.neighbors){
                try{
                    Registry registry = LocateRegistry.getRegistry(each.name);
                    Node next = (Node) registry.lookup("Node");
                    info += next.view(each.name);
                }catch (Exception e){
                    System.out.println("forwarding view request failed:\n");
                    System.out.println("tried to forward to:" + each.name);
                    e.printStackTrace();
                }
            }
            return info;
        }
    }

    @Override
    public String leave() {
        return "";
    }

    @Override
    public String testInvoke(String cmd){
        return "Your command is: " + cmd;
    }


    //------------------------------------------------------------------------------------------------------------------
    /*
     * util functions, to facilitate the remote methods
     * - computerHash: compute the point given a keyword
     * - splitZone: given a point, split the zone to two halves
     * - route2next: find the next neighbor closer to destination
     */
    public void set_zone(Zone zone){
        this.node.zone = zone ;
    }

    // computeHash: compute the point given a keyword
    public Point computeHash(String keyword){
        int x, y;

        // compute x: CharAtOdd mod 10
        int charAtOdd =0;
        for(int i=0; i< keyword.length(); i+=2){
            charAtOdd += keyword.charAt(i);
        }
        x = charAtOdd%10;

        // compute y: CharAtEven mode 10
        int charAtEven =0;
        for(int i=1; i<keyword.length();i+=2){
            charAtEven += keyword.charAt(i);
        }
        y = charAtEven%10;

        return new Point(x, y);
    }

    /*
     * splitZone: split the zone to two halves
     * - argarment: Point
     * - return: the halve zone contains the input Point
     */
    public Zone splitZone(Point point){

        // first split to two halves
        // if square zone, split vertically, otherwise split horizontally
        float width = this.node.zone.getWidth();
        float height = this.node.zone.getHeight();
        Zone zone1, zone2;
        if(width == height){
            float midX = this.node.zone.start_point.x + width/2;
            Point midPointTop = new Point(midX, this.node.zone.end_point.y);
            Point midPointBottom = new Point(midX, this.node.zone.start_point.y);
            zone1 = new Zone(this.node.zone.start_point, midPointTop); // left halve
            zone2 = new Zone(midPointBottom, this.node.zone.end_point);// right halve
        }else {
            float midY = this.node.zone.start_point.y + height/2;
            Point midPointLeft = new Point(this.node.zone.start_point.x, midY);
            Point midPointRight = new Point(this.node.zone.end_point.x, midY);
            zone1 = new Zone(this.node.zone.start_point, midPointRight); // bottom halve
            zone2 = new Zone(midPointLeft, this.node.zone.end_point);
        }

        if(zone1.inZone(point)){
            this.set_zone(zone2);
            return zone1;
        }else {
            this.set_zone(zone1);
            return zone2;
        }
    }

    // route2next: find the next neighbor closer to destination
    public Node_Base route2next(Point destPoint){
        // we go vertically then horizontally
        float midX = this.node.zone.getMidX();
        float midY = this.node.zone.getMidY();
        Node_Base nextNeighbor = null;
        for(Node_Base neighbor : this.neighbors){
            if(destPoint.y > this.node.zone.end_point.y){
                // higher than current zone
                if(neighbor.zone.start_point.y > this.node.zone.end_point.y){
                    if(destPoint.x > this.node.zone.end_point.x && neighbor.zone.start_point.x >= this.node.zone.start_point.x){
                        nextNeighbor = neighbor;
                        break;
                    }
                    if(destPoint.x < this.node.zone.start_point.x && neighbor.zone.end_point.x <= this.node.zone.end_point.x){
                        nextNeighbor = neighbor;
                        break;
                    }
                }
            }else if(destPoint.y < this.node.zone.start_point.y){
                // lower than current zone
                if(neighbor.zone.start_point.y < this.node.zone.end_point.y){
                    if(destPoint.x > this.node.zone.end_point.x && neighbor.zone.start_point.x >= this.node.zone.start_point.x){
                        nextNeighbor = neighbor;
                        break;
                    }
                    if(destPoint.x < this.node.zone.start_point.x && neighbor.zone.end_point.x <= this.node.zone.end_point.x){
                        nextNeighbor = neighbor;
                        break;
                    }
                }
            }else {
                // move horizontally
                if(destPoint.x > this.node.zone.end_point.x && neighbor.zone.start_point.x >= this.node.zone.end_point.x){
                    nextNeighbor = neighbor;
                    break;
                }
                if(destPoint.x < this.node.zone.start_point.x && neighbor.zone.end_point.x <= this.node.zone.start_point.x){
                    nextNeighbor = neighbor;
                    break;
                }
            }
        }
        return nextNeighbor;
    }

}

/*
 * Node_Base to provide the very basic info of a node:
 * - name, IP, zone
 */
class Node_Base{
    protected String name;
    protected String IP;
    protected Zone zone;

    public Node_Base(){
        this.zone = new Zone();
    }

    public void set_zone(Point start, Point end){
        this.zone.start_point = start;
        this.zone.end_point = end;
    }

    public void setIP(String IP){
        this.IP = IP;
    }

    public void setName(String name){
        this.name = name;
    }

    public String toString(){
        String ret = "\n------------------\n";
        ret += "name: " + this.name + "\n";
        ret += "IP  :" + this.IP + '\n';
        ret += "zone:" + this.zone.toString();
        ret += "\n------------------\n";
        return ret;
    }

}



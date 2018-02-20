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
    final static String BOOTSTRAP_HOSTNAME = "node31";
    final static int RMI_PORT = 1100;
    final static boolean DUBUG = true;

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

    // Main Commands: insert, search, view, join, leave
    @Override
    public String insert(String keyword, LinkedList<String> path) {

        Point destPoint = this.computeHash(keyword);

        if(Peer.DUBUG){
            System.out.println("\ninserting: " + keyword);
            System.out.println("hash point: " + destPoint.toString());
        }


        if(this.node.inZone(destPoint)){
            this.contents.add(keyword);
            String reply = "\nStored in node:\n";
            reply += this.node.toString();
            return this.node.IP + "\n--->" + reply;
        }else {
            if(Peer.DUBUG){ System.out.println("not under my watch.");}

            path.add(this.node.name);
            Node_Base nextNeighbor = this.route2next(destPoint, path);

            if(nextNeighbor != null){
                try {
                    Registry registry = LocateRegistry.getRegistry(nextNeighbor.name, Peer.RMI_PORT);
                    Node node = (Node) registry.lookup(nextNeighbor.name);
                    return node.insert(keyword, path);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        return "Failure";
    }

    @Override
    public String search(String keyword, LinkedList<String> path) {
        Point destPoint = this.computeHash(keyword);
        if(this.node.inZone(destPoint)){
//            System.out.println("\ncontents in this node:");
//            for(String each: this.contents) System.out.println(each);
//            System.out.println("keyword is: " + keyword);
            if(this.contents.contains(keyword)){
                String reply = "Found in node:\n";
                reply += this.node.toString();
                return this.node.IP + "\n" + reply;
            }else {
                return "Failure in destination";
            }

        }

        path.add(this.node.name);
        Node_Base nextNeighbor = this.route2next(destPoint, path);

        if(nextNeighbor != null){
            try {
                Registry registry = LocateRegistry.getRegistry(nextNeighbor.name, Peer.RMI_PORT);
                Node node = (Node) registry.lookup(nextNeighbor.name);
                return node.search(keyword, path);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return "Failure, could not find keyword in the CAN.";

    }

    @Override
    public String view(String peer) {
        if(this.node.name.contentEquals(peer)){

            String info = "---------------------------\n";
            info += "\nInfo of " + peer + "\n\n";
            info += this.node.toString();
            info += "\nneighbors:\n";
            for(Node_Base each : this.neighbors){
                info += each.name + "\n";
            }
            info += "\n";
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
                    Registry registry = LocateRegistry.getRegistry(each.name, Peer.RMI_PORT);
                    Node next = (Node) registry.lookup(each.name);
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
    public LinkedList<String> join(String peer, float[] destPointArray, LinkedList<String> path) {
        Point destPoint = new Point(destPointArray[0], destPointArray[1]);

        if(this.node.inZone(destPoint)){
            System.out.println("--------------------------------\n");
            System.out.println("Notification - New Node Joining:\n\n");
            System.out.println("\nthe point chosed to join: " + destPoint.toString());

            for(Zone zone : this.node.zones){
                if(zone.inZone(destPoint)){
                    Zone splitOut = this.splitZone(zone, destPoint);
                    System.out.println("\nbefore split: " + this.node.toString());
                    System.out.println("\nthe zone to split out: " + splitOut.toString());

                    // add new neighbor
                    LinkedList<float[]> newNeighborCoord = new LinkedList<float[]>();
                    newNeighborCoord.add(splitOut.getCoordinateArray());
                    Node_Base newNeighbor = this.addnewNeighbor(peer, newNeighborCoord);

                    LinkedList<String> ret = new LinkedList<String>();
                    ret.add(new String(Float.toString(splitOut.start_point.x)));
                    ret.add(new String(Float.toString(splitOut.start_point.y)));
                    ret.add(new String(Float.toString(splitOut.end_point.x)));
                    ret.add(new String(Float.toString(splitOut.end_point.y)));
                    ret.add(this.node.name);

                    LinkedList<Node_Base> toRemove = new LinkedList<Node_Base>();
                    for(Node_Base each : this.neighbors){
                        // notify neighbors new node joined
                        if(each.name.contentEquals(newNeighbor.name)) continue;

                        try {
                            Registry registry = LocateRegistry.getRegistry(each.name, Peer.RMI_PORT);
                            Node neighbor = (Node) registry.lookup(each.name);
                            // if need to add the new node as neighbor
                            boolean neighborOfNew = neighbor.updateNeighbors(newNeighbor.name, newNeighbor.getCoordinateArray(), false,false);
                            if(neighborOfNew){
                                ret.add(each.name);
                            }
                            // if need to remove the old node from neighbors
                            neighbor.updateNeighbors(this.node.name, this.node.getCoordinateArray(), false, false);
                        }catch (Exception e){
                            System.out.println("notify neighbor failed!");
                            e.printStackTrace();
                        }

                        if(!this.isNeighbor(each.getCoordinateArray()))toRemove.add(each);
                    }

                    for(Node_Base each: toRemove) this.neighbors.remove(each);

                    ret.add("KEYWORDS");
                    LinkedList<String> toRemoveKW = new LinkedList<String>();
                    for(String keyword : this.contents){
                        if(! this.node.inZone(this.computeHash(keyword))){
                            ret.add(keyword);
                            toRemoveKW.add(keyword);
                        }
                    }
                    for(String each: toRemoveKW) this.contents.remove(each);

                    System.out.println("\n\nSuccees!\n--------------------------------\n");
                    return ret;
                }
            }
        }else {
            path.add(this.node.name);
            Node_Base closerNode = this.route2next(destPoint, path);
            if( closerNode != null){
                try{
                    Registry registry = LocateRegistry.getRegistry(closerNode.name, Peer.RMI_PORT);
                    Node node  = (Node) registry.lookup(closerNode.name);
                    return node.join(peer, destPointArray, path);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /*
     * when a node leave:
     * - choose the neighbor to handover
     * -- if could merge, choose to merge
     * -- otherwise, give it to the smallest neighbor
     * - call the chosen neighbor to takeover: zone and keywords
     * - notify the neighbors its going to leave
     * - leave
     */
    @Override
    public void leave() {
        // choose the neighbor to handover
        Node_Base handOverNode = null;

        // try to find the neighbor could merge -> same area
        for(Node_Base neighbor: this.neighbors){
            if(neighbor.area == this.node.area){
                handOverNode = neighbor;
                break;
            }
        }

        // not mergeable neighbor, find the smallest neighbor
        if(handOverNode == null){
            handOverNode = this.neighbors.peek();
            for(Node_Base neighbor :  this.neighbors){
                if(neighbor.area < handOverNode.area){
                    handOverNode = neighbor;
                }
            }
        }

        // call target node to take over
        try{
            Registry registry = LocateRegistry.getRegistry(handOverNode.name, Peer.RMI_PORT);
            Node handOverHost = (Node) registry.lookup(handOverNode.name);
            handOverHost.takeOver(this.node.getCoordinateArray(), this.contents);
        }catch (Exception e){
            e.printStackTrace();
        }

        // notify the neighbors its going to leave
        for(Node_Base neighbor: this.neighbors){
            try{
                Registry registry = LocateRegistry.getRegistry(neighbor.name, Peer.RMI_PORT);
                Node neighborHost = (Node) registry.lookup(neighbor.name);
                neighborHost.updateNeighbors(this.node.name, new LinkedList<>(),true,false);
                neighborHost.updateNeighbors(handOverNode.name, handOverNode.getCoordinateArray(), false,false);
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("Failure!");
                System.exit(0);
            }
        }

        System.out.println("   \nBYE!\n");
        System.exit(0);
    }


    // ------------------------------------------------
    // helper remote methods

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
    public LinkedList<float[]> getCoordnts() {
        return this.node.getCoordinateArray();
    }

    @Override
    public boolean updateNeighbors(String newNeighbor, LinkedList<float[]> coordnts, boolean leaving, boolean updateCoord) {

        if(leaving){
            Node_Base toRemove = null;
            for(Node_Base each : this.neighbors){
                if( each.name.contentEquals(newNeighbor)) toRemove = each;
            }
            this.neighbors.remove(toRemove);
            return false;
        }else {
            // already a neighbor, comes from a node taking over zone of another
            if(updateCoord){
                for(Node_Base each: this.neighbors){
                    if(each.name.contentEquals(newNeighbor)){
                        each.zones.clear();
                        each.addZones(coordnts);
                        return true;
                    }
                }
            }else {
                // dont know if neighbor, comes from a node join
                if(this.isNeighbor(coordnts)) {
                    for(Node_Base each: this.neighbors){
                        if(each.name.contentEquals(newNeighbor)) return true;
                    }

                    this.addnewNeighbor(newNeighbor, coordnts);
                    return true;
                }
                else {
                    Node_Base toRemove = null;
                    for(Node_Base each : this.neighbors){
                        if( each.name.contentEquals(newNeighbor)) toRemove = each;
                    }
                    this.neighbors.remove(toRemove);
                }
            }
        }
        return false;
    }

    public boolean isNeighbor(LinkedList<float[]> coordntArrays){

        for(float[] coordnts : coordntArrays){
            Point start = new Point(coordnts[0], coordnts[1]);
            Point end = new Point(coordnts[2], coordnts[3]);

            for(Zone zone: this.node.zones){
                // y axes collapse
                if( start.y <= zone.start_point.y && end.y >= zone.end_point.y ||
                        start.y >= zone.start_point.y && end.y <= zone.end_point.y){
                    if( start.x == zone.end_point.x || end.x == zone.start_point.x){
                        // contiguous
                        return true;
                    }
                }

                // x axes collapse
                if( start.x <= zone.start_point.x && end.x >= zone.end_point.x ||
                        start.x >= zone.start_point.x && end.x <= zone.end_point.x){
                    if( start.y == zone.end_point.y || end.y == zone.start_point.y){
                        // contiguous
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public Node_Base addnewNeighbor(String neighborName, LinkedList<float[]> coordnts){

        Node_Base newNeighbor = new Node_Base();
        newNeighbor.setName(neighborName);

        try {
            newNeighbor.setIP(InetAddress.getByName(neighborName).getHostAddress());
        } catch (Exception e){
            e.printStackTrace();
        }

        newNeighbor.addZones(coordnts);
        this.neighbors.add(newNeighbor);

        return newNeighbor;
    }

    @Override
    public LinkedList<float[]> takeOver(LinkedList<float[]> coordnts, LinkedList<String> contents) {

        LinkedList<float[]> toRemove = new LinkedList<float[]>();

        for(float[] coordnt : coordnts){
            Point start = new Point(coordnt[0], coordnt[1]);
            Point end = new Point(coordnt[2], coordnt[3]);
            Zone zone = new Zone(start,end);
            for(Zone myzone : this.node.zones){
                if(Peer.DUBUG){
                    System.out.println("zone: height " + zone.getHeight() + "width " + zone.getWidth());
                    System.out.println("myzone: height " + myzone.getHeight() + "width " + myzone.getWidth());
                }

                if(zone.getHeight() == myzone.getHeight() && zone.getWidth() == myzone.getWidth()){
                    if(Peer.DUBUG){
                        System.out.println("same area.");
                    }

                    Zone newZone = null;
                    if(zone.end_point.x == myzone.start_point.x){
                        if(Peer.DUBUG){
                            System.out.println("two rectangles, zone to the left");
                        }
                        // two rectangles, zone to the left
                        newZone = new Zone(zone.start_point, myzone.end_point);
                    }
                    if( myzone.end_point.x == zone.start_point.x){
                        if(Peer.DUBUG){
                            System.out.println("two rectangles, zone to the left");
                        }
                        // two rectangles, myzone to the left
                        newZone = new Zone(myzone.start_point, zone.end_point);
                    }
                    if(zone.end_point.y == myzone.start_point.y ){
                        if(Peer.DUBUG){
                            System.out.println("two squares, zone under myzone");
                        }
                        // two squares, zone under myzone
                        newZone = new Zone(zone.start_point, myzone.end_point);

                    }
                    if(myzone.end_point.y == zone.start_point.y){
                        if(Peer.DUBUG){
                            System.out.println("two squares, myzone under zone");
                        }
                        // two squares, myzone under zone
                        newZone = new Zone(myzone.start_point, zone.end_point);
                    }
                    this.node.setZone(myzone, newZone);
                    toRemove.add(coordnt);
                    break;
                }
            }
        }

        for(float[] each: toRemove) coordnts.remove(each);

        this.node.addZones(coordnts);
        for(String content: contents) this.contents.add(content);

        // notify its neighbors to update its coordinates
        for(Node_Base neighbor : this.neighbors){
            try{
                Registry registry =  LocateRegistry.getRegistry(neighbor.name, Peer.RMI_PORT);
                Node neighborHost = (Node)registry.lookup(neighbor.name);
                neighborHost.updateNeighbors(this.node.name, this.node.getCoordinateArray(), false, true);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return this.node.getCoordinateArray();
    }

    @Override
    public float[] join() {
        return null;
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
    public void addZone(Zone zone){
        this.node.addZone(zone.start_point, zone.end_point);
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
    public Zone splitZone(Zone zone, Point point){

        // first split to two halves
        // if square zone, split vertically, otherwise split horizontally
        float width = zone.getWidth();
        float height = zone.getHeight();
        Zone zone1, zone2;
        if(width == height){
            float midX = zone.start_point.x + width/2;
            Point midPointTop = new Point(midX, zone.end_point.y);
            Point midPointBottom = new Point(midX, zone.start_point.y);
            zone1 = new Zone(zone.start_point, midPointTop); // left halve
            zone2 = new Zone(midPointBottom, zone.end_point);// right halve
        }else {
            float midY = zone.start_point.y + height/2;
            Point midPointLeft = new Point(zone.start_point.x, midY);
            Point midPointRight = new Point(zone.end_point.x, midY);
            zone1 = new Zone(zone.start_point, midPointRight); // bottom halve
            zone2 = new Zone(midPointLeft, zone.end_point);
        }

        if(zone1.inZone(point)){
            this.node.setZone(zone, zone2);
            return zone1;
        }else {
            this.node.setZone(zone, zone1);
            return zone2;
        }
    }

    /*
     * route2next: find the next neighbor closer to destination
     * could be 3 situation:
     * - point looking for is above then the zone of current node
     * - point looking for is blow then the zone of current node
     * - point looking for sit horizontally,
     */
    public Node_Base route2next(Point destPoint, LinkedList<String> path){
        // we go vertically then horizontally

        if (Peer.DUBUG){
            System.out.println("looking for closer neighbor.");
        }
        // above the zone
        if(destPoint.y > this.node.yTop){
            if(Peer.DUBUG){System.out.println("above my zone");}
            for(Node_Base neighbor : this.neighbors){
                if(path.contains(neighbor.name)) continue;

                if(Peer.DUBUG){System.out.println("neighbor: " + neighbor.name);}
                if(Peer.DUBUG){System.out.println(neighbor.xLeft + " " + neighbor.yBottom + " " + neighbor.xRight + " " + neighbor.yTop);}

                if(neighbor.yTop > this.node.yTop){
                    // to the right
                    if(destPoint.x > this.node.xLeft && neighbor.xLeft >= this.node.xLeft){
                        return neighbor;
                    }
                    // to the left
                    if(destPoint.x < this.node.xRight && neighbor.xRight <= this.node.xRight){
                        return neighbor;
                    }
                }
            }
        }

        // below the zone
        else  if(destPoint.y < this.node.yBottom){
            if(Peer.DUBUG){System.out.println("below my zone");}
            for(Node_Base neighbor : this.neighbors){
                if(path.contains(neighbor.name)) continue;

                if(neighbor.yBottom < this.node.yBottom){
                    // to the right
                    if(destPoint.x > this.node.xLeft && neighbor.xLeft >= this.node.xLeft){
                        return neighbor;
                    }
                    // to the left
                    if(destPoint.x < this.node.xRight && neighbor.xRight <= this.node.xRight){
                        return neighbor;
                    }
                }
            }
        }

        // horizontally
        else{
            if(Peer.DUBUG){System.out.println("horizontally");}

            for(Node_Base neighbor : this.neighbors){
                if(Peer.DUBUG){System.out.println("candidate " + neighbor.name);}
                if(path.contains(neighbor.name)) continue;

                if(neighbor.yBottom <= destPoint.y && neighbor.yTop >= destPoint.y){
                    // to the right
                    if(destPoint.x > this.node.xLeft && neighbor.xLeft > this.node.xLeft){
                        if(Peer.DUBUG){System.out.println("to the right");}
                        return neighbor;
                    }
                    // to the left
                    if(destPoint.x < this.node.xRight && neighbor.xRight < this.node.xRight){
                        if(Peer.DUBUG){System.out.println("to the left");}
                        return neighbor;
                    }
                }
            }

        }

        return null;
    }

}

/*
 * Node_Base to provide the very basic info of a node:
 * - name, IP, zone
 */
class Node_Base{
    protected String name;
    protected String IP;
    protected LinkedList<Zone> zones;
    protected float area;
    protected float xLeft;
    protected float xRight;
    protected float yBottom;
    protected float yTop;

    public Node_Base(){
        this.zones = new LinkedList<Zone>();
        this.area = 0;
        this.xLeft = this.xRight = this.yBottom = this.yTop = 0;
    }

    public void setZone(Zone oldZone, Zone newZone){
        this.zones.remove(oldZone);
        this.update();
        this.addZone(newZone.start_point, newZone.end_point);
    }

    public void addZone(Point start, Point end){
        Zone newZone = new Zone();
        newZone.start_point = start;
        newZone.end_point = end;
        if(this.zones.isEmpty()){
            this.xLeft = start.x;
            this.xRight = end.x;
            this.yBottom = start.y;
            this.yTop = end.y;
        }
        this.zones.add(newZone);
        this.area += newZone.getHeight()*newZone.getWidth();
        if(start.x < this.xLeft) this.xLeft = start.x;
        if(end.x > this.xRight) this.xRight = end.x;
        if(start.y < this.yBottom)  this.yBottom = start.y;
        if(end.y > this.yTop) this.yTop = end.y;
    }

    public void addZones(LinkedList<float[]> coordntsArray){
        for(float[] each: coordntsArray){
            Point start  = new Point(each[0], each[1]);
            Point end = new Point(each[2], each[3]);
            this.addZone(start, end);
        }
    }

    public void setIP(String IP){
        this.IP = IP;
    }

    public void setName(String name){
        this.name = name;
    }

    public boolean inZone(Point point){
        // iterate through all zones, call inZone on them.
        for(Zone zone: zones){
            if(zone.inZone(point)) return true;
        }
        return false;
    }

    public void update(){
        this.area = 0;
        this.xLeft = this.xRight = this.yBottom = this.yTop = 0;
        for(Zone zone : this.zones){
            this.area += zone.getHeight()*zone.getWidth();
            if(zone.start_point.x < this.xLeft) this.xLeft = zone.start_point.x;
            if(zone.end_point.x > this.xRight) this.xRight = zone.end_point.x;
            if(zone.start_point.y < this.yBottom)  this.yBottom = zone.start_point.y;
            if(zone.end_point.y > this.yTop) this.yTop = zone.end_point.y;
        }
    }

    public LinkedList<float[]>  getCoordinateArray(){

        LinkedList<float[]> ret= new LinkedList<float[]>();

        for(Zone zone: this.zones){
            ret.add(zone.getCoordinateArray());
        }

        return ret;
    }

    public String toString(){
        String ret = "";
        ret += "name: " + this.name + "\n";
        ret += "IP  :" + this.IP + '\n';
        for(Zone zone : zones){
            ret += "zone:" + zone.toString() + "\n";
        }
        return ret;
    }

}

import java.net.InetAddress;

public class Try {

    public static void main(String args[]){
        try{
            InetAddress IP = InetAddress.getByName(args[0]);
            System.out.println(IP.getHostAddress());
            System.out.println(IP.getHostName());
            System.out.println("--------\nLocal:");
            IP = InetAddress.getLocalHost();
            System.out.println(IP.getHostAddress());
            System.out.println(IP.getHostName());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

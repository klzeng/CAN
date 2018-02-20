
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class Try {

    public static void main(String args[]){
       try {
           InetAddress host = InetAddress.getByName("192.168.1.251");
            System.out.println(host.getHostName());
            System.out.println(host.getHostAddress());
       }catch (UnknownHostException e){
           e.printStackTrace();
       }
    }
}

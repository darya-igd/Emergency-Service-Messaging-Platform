package bgu.spl.net.impl.stomp;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {

        if(args.length!=2){
            System.out.println("you must supply two arguments: <port>, <type_of_server> - tpc / reactor");
            System.exit(1);
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Error: Port must be an integer.");
            return;
        }

         String serverType = args[1];

        if (serverType.equals("tpc")){
            Server.threadPerClient(
               port,
               ()-> new StompProtocol(),
               StompEncoderDecoder::new
            ).serve();
        }

        else if(serverType.equals("reactor")){
            Server.reactor(
                Runtime.getRuntime().availableProcessors(), 
                port, 
                ()-> new StompProtocol(), 
                StompEncoderDecoder::new
            ).serve();    
        }

        else {
            System.out.println("Error: Unknown server type. Use 'tpc' or 'reactor'.");
            System.exit(1);
        }  
    } 
}

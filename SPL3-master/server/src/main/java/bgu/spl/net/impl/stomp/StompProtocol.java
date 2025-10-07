package bgu.spl.net.impl.stomp;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.impl.stomp.frame.ConnectFrame;
import bgu.spl.net.impl.stomp.frame.DisconnectFrame;
import bgu.spl.net.impl.stomp.frame.Frame;
import bgu.spl.net.impl.stomp.frame.SendFrame;
import bgu.spl.net.impl.stomp.frame.SubscribeFrame;
import bgu.spl.net.impl.stomp.frame.UnsubscribeFrame;
import bgu.spl.net.srv.Connections;
//import bgu.spl.net.srv.ConnectionsImpl;

//import java.time.LocalDateTime;
//import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StompProtocol implements StompMessagingProtocol<String> {

    private int connectionId;
    private Connections<String> connections;
    private boolean shouldTerminate= false; 


    @Override
    public void start (int connectionId, Connections<String> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
    }

    private Frame parseFrame(String msg) {
        //from msg to Frame object
        String[] lines = msg.split("\n");
        String command = lines[0].trim();
        Map<String, String> headers = new ConcurrentHashMap<>();
        String body = "";
        boolean isBody = false;

        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                isBody = true;
                continue;
            }
            if (isBody && lines[i] != "\u0000") {
                body += lines[i] + "\n";
            } else {
                String[] headerParts = lines[i].split(":");
                if (headerParts.length == 2) {
                    headers.put(headerParts[0].trim(), headerParts[1].trim());
                }
            }
        }
        
        switch (command) {   
            case "CONNECT":
                return new ConnectFrame(headers, body);
            case "SEND":
                return new SendFrame(headers, body);
            case "SUBSCRIBE":
                return new SubscribeFrame(headers, body);
            case "UNSUBSCRIBE": 
                return new UnsubscribeFrame(headers, body);
            case "DISCONNECT": 
                return new DisconnectFrame(headers, body);  
            default:
                return null;              
        }
    }

    @Override
    public void process(String msg) {
        Frame frame = parseFrame(msg);
        if (frame != null) {
            frame.process(connectionId, connections);
        }    
    }  


    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

}

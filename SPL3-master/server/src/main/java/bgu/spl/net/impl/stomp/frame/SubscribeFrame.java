package bgu.spl.net.impl.stomp.frame;

import java.io.IOException;
import java.util.Map;

//import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;
//import bgu.spl.net.srv.ConnectionsImpl;

public class SubscribeFrame extends Frame{
    
    public SubscribeFrame(Map<String, String> headers, String body) {
        super("SUBSCRIBE", headers, body);
    }

    @Override
    public void process(int connectionId, Connections<String> connections) {
        boolean legalFrame= true;

        try{
            if(!headers.containsKey("destination")){
                throw new IOException(" missing destination header");
            }
            if(!headers.containsKey("id")){
                throw new IOException(" missing id header");
            } else if( connections.getHandler(connectionId).getUser().getTopics().containsKey(Integer.parseInt(headers.get("id")))){
                throw new IOException(" id already subscribed to this channel");
            }
        } catch (IOException e){
            legalFrame= false;
            //error frame
            Error errorFrame= new Error(this, e.getMessage(), "", headers.get("receipt"));
            errorFrame.process(connectionId, connections);
        }

        if(legalFrame){
            connections.subscribe(headers.get("destination"), Integer.parseInt(headers.get("id")), connectionId);
            //receipt frame
            if(headers.containsKey("receipt")){
                Receipt receiptFrame= new Receipt(headers.get("receipt"));
                receiptFrame.process(connectionId, connections);
            }
        }   
    }
    
}

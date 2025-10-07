package bgu.spl.net.impl.stomp.frame;

import java.io.IOException;
import java.util.Map;

//import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;
//import bgu.spl.net.srv.ConnectionsImpl;

public class DisconnectFrame extends Frame {
    public DisconnectFrame(Map<String, String> headers, String body) {
        super("DISCONNECT", headers, body);
    }

    @Override
    public void process(int connectionId, Connections<String> connections) {
        boolean legalFrame= true;

        try{
            if(!headers.containsKey("receipt")){
                throw new IOException(" missing receipt header");
            }
        }
        catch (IOException e){
            legalFrame= false;
            //error frame
            Error errorFrame= new Error(this, e.getMessage(), "", headers.get("receipt"));
            errorFrame.process(connectionId, connections);
        }

        if (legalFrame){
            //receipt frame
            Receipt receiptFrame= new Receipt(headers.get("receipt"));
            receiptFrame.process(connectionId, connections);
            connections.disconnect(connectionId);
        }
    }
    
}

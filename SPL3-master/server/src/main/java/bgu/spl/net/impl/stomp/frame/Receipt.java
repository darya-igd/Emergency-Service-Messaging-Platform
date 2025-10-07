package bgu.spl.net.impl.stomp.frame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class Receipt{
    private String receiptID;


    public Receipt(String receiptID) {
        this.receiptID=receiptID;
    }

    
    public void process(int connectionId, Connections<String> connections) {
        //headers
        Map<String, String> receiptHeaders = new ConcurrentHashMap<>();
        receiptHeaders.put("receipt-id", receiptID);

        connections.send(connectionId, new ReceiptFrame(receiptHeaders, "").toString());
 
    }


    private class ReceiptFrame extends Frame{

        public ReceiptFrame (Map<String, String> headers, String body ){
            super("RECEIPT", headers, body);
        }

        @Override
        public void process(int connectionId, Connections<String> connections) {}
        
    } 
    
    
}

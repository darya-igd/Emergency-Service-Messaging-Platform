package bgu.spl.net.impl.stomp.frame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class Error {

    private Frame causedError; //must
    private String shortDescription; //must
    private String detailedInfo;      //optional
    private String receiptID;      //if exicting-must

           


    public Error(Frame causedError, String shortDescription, String detailedInfo , String receiptID) {
        this.causedError = causedError;
        this.shortDescription = shortDescription;
        this.detailedInfo = detailedInfo;
        this.receiptID = receiptID;
        
    }

    
    public void process(int connectionId, Connections<String> connections) {
        // build error body

        String errorBody = "The message: \\n";
        errorBody+= "--------- \\n";
        errorBody+= causedError.toString();
        errorBody+= "--------- \\n";
        errorBody += detailedInfo;
        
        
        // build error headers

        Map<String, String> errorHeaders = new ConcurrentHashMap<>();
        if (receiptID!=null){
            errorHeaders.put("receipt-id", receiptID);
        }
        errorHeaders.put("messege", shortDescription);

        connections.send(connectionId, new ErrorFrame(errorHeaders, errorBody).toString());

    }


    private class ErrorFrame extends Frame {
        public ErrorFrame(Map<String, String> headers, String body) {
            super("ERROR", headers, body);
        }
    
        @Override
        public void process( int connectionId, Connections<String> connections) {}
    }
  
}

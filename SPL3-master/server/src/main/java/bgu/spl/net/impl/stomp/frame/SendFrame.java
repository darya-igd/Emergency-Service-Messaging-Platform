package bgu.spl.net.impl.stomp.frame;

import java.io.IOException;
//import java.security.KeyStore.Entry;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;
//import bgu.spl.net.srv.ConnectionsImpl;

public class SendFrame extends Frame {

    public SendFrame(Map<String, String> headers, String body) {
        super("Send", headers, body);
    }    

    @Override
    public void process(int connectionId, Connections<String> connections){
        boolean legalFrame= true;

        try{
            if (!headers.containsKey("destination")) {
                throw new IOException("missing destination header");
            } else if (!connections.isTopicExcits(headers.get("destination"))){
                throw new IOException("Topic is not exisitng");
            } else if(!connections.getHandler(connectionId).getUser().getTopics().contains(headers.get("destination"))){
                    throw new IOException("user is not subscribed to this channel");
            }    
        } catch (IOException e){
            legalFrame= false;
            //error frame
            Error errorFrame= new Error(this, e.getMessage(), "", "");
            errorFrame.process(connectionId, connections);

        }

        if (legalFrame){
            //message frame
            String destination = headers.get("destination");
            //headers

            Map<String, String> messageHeaders = new ConcurrentHashMap<>();
            LinkedList<Integer> subscribers = connections.getTopics().get(destination);
            //sending to everybody subscribed to the chanell
            for(int subscriber: subscribers){
                int connectionID=subscriber;
                Map<Integer, String> userTopics = connections.getHandler(connectionID).getUser().getTopics();
                //search for the subscriptionID if each one
                for(Map.Entry<Integer, String> entry : userTopics.entrySet()){
                    if (entry.getValue().equals(destination)){
                        int subscriptionId = entry.getKey();
                        messageHeaders.put("subscription", String.valueOf(subscriptionId));
                        break;
                    }
                }
                messageHeaders.put("message-id", String.valueOf(connections.generateId()));
                messageHeaders.put("destination", destination);

                MessageFrame msgFrame= new MessageFrame(messageHeaders, body);

                connections.send(connectionID, msgFrame.toString());

            }


        
            if(headers.containsKey("receipt")){
                //receipt frame
                Receipt receiptFrame= new Receipt(headers.get("receipt"));
                receiptFrame.process(connectionId, connections);
            }
        }
    }
    
    private class MessageFrame extends Frame{
        public MessageFrame(Map<String, String> headers, String body) {
            super("MESSAGE", headers, body);
        }
    
        @Override
        public void process(int connectionId, Connections<String> connections) {}
    }
    
}

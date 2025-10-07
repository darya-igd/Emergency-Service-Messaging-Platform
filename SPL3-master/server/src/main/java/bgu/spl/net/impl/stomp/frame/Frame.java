package bgu.spl.net.impl.stomp.frame;

//import java.security.KeyStore.Entry;
import java.util.Map;

//import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;
//import bgu.spl.net.srv.ConnectionsImpl;

public abstract class Frame {
    protected String command;
    protected Map<String, String> headers;
    protected String body;

    public Frame(String command, Map<String, String> headers, String body) {
        this.command = command;
        this.headers = headers;
        this.body = body;
    }

    public String toString() {
        StringBuilder msg = new StringBuilder();
        msg.append(command).append("\n");
        
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            msg.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        
        msg.append("\n");
        
        if (this.body != null && !this.body.isEmpty()) {
            msg.append(this.body);
        }
        
        return msg.toString();
    }

    public abstract void process(int connectionId, Connections<String> connections);
}


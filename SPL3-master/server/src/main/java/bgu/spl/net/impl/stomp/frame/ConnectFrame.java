package bgu.spl.net.impl.stomp.frame;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;
//import bgu.spl.net.srv.ConnectionsImpl;

public class ConnectFrame extends Frame {

    public ConnectFrame(Map<String, String> headers, String body) {
        super("CONNECT", headers, body);
    }

    @Override
    public void process(int connectionId, Connections<String> connections) {
        boolean legalFrame = true;

        try {
            // legality of accept version
            if (!headers.containsKey("accept-version")) {
                throw new IOException(" missing accept-version 1.2 header");
            } else if (!headers.get("accept-version").equals("1.2")) {
                throw new IOException(" wrong accept-version header. must be 1.2");
            }
            // legality of host
            else if (!headers.containsKey("host")) {
                throw new IOException(" missing host header");
            } else if (!headers.get("host").equals("stomp.cs.bgu.ac.il")) {
                throw new IOException(" wrong host header. must be stomp.cs.bgu.ac.il");
            }
            // legality of username+password
            else if (headers.containsKey("login") && headers.containsKey("passcode")) {
                if (!connections.checkUser(headers.get("login"), headers.get("passcode"))) {
                    System.out.println("wrong login details received");
                    throw new IOException(" wrong login details received");
                } else if (connections.isUserLogedIn(headers.get("login"), headers.get("passcode"))) {
                    throw new IOException("User already logged in");
                }
            } else {
                throw new IOException(" missing login or passcode header");
            }
        } catch (IOException e) {
            System.out.println("error in connect frame");
            legalFrame = false;
            // error frame
            Error errorFrame = new Error(this, e.getMessage(), "", headers.get("receipt"));
            errorFrame.process(connectionId, connections);
        }

        if (legalFrame) {
            connections.LoginUser(connectionId, headers.get("login"), headers.get("passcode"));
            Map<String, String> ConnectedHeaders = new ConcurrentHashMap<>();
            ConnectedHeaders.put("version", "1.2");
            connectedFrame connectedFrame = new connectedFrame(ConnectedHeaders, "");
            connectedFrame.process(connectionId, connections);

            if (headers.containsKey("receipt")) {
                // reciept frame
                Receipt receiptFrame = new Receipt(headers.get("receipt"));
                receiptFrame.process(connectionId, connections);

            }

        }

    }

    private class connectedFrame extends Frame {
        public connectedFrame(Map<String, String> headers, String body) {
            super("CONNECTED", headers, body);
        }

        @Override
        public void process(int connectionId, Connections<String> connections) {
            connections.send(connectionId, this.toString());
        }
    }
}

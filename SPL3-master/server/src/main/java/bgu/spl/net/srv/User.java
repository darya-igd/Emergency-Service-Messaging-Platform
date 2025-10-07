package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class User<T> {
   private int connectionId;
   private final String username;
   private final String password;
   private boolean isConnected;
   private ConnectionHandler<T> connectionHandler;
   private final ConcurrentHashMap<Integer, String> topics;

   public User(int connectionId, String username, String password, ConnectionHandler<T> connectionHandler) {
      this.connectionId = connectionId;
      this.username = username;
      this.password = password;
      this.connectionHandler = connectionHandler;
      this.isConnected = true;
      this.topics = new ConcurrentHashMap<>();
   }

   public int getConnectionId() {
      return connectionId;
   }

   public String getUsername() {
      return username;
   }

   public String getPassword() {
      return password;
   }

   public boolean getIsConnected() {
      return isConnected;
   }

   public ConnectionHandler<T> getConnectionHandler() {
      return connectionHandler;
   }

   public ConcurrentHashMap<Integer, String> getTopics() {
      return topics;
   }

   public void setIsConnected(boolean isConnected) {
      this.isConnected = isConnected;
   }

   public void setConnectionId(int connectionId) {
      this.connectionId = connectionId;
   }

   public void setConnectionHandler(ConnectionHandler<T> handler) {
      connectionHandler = handler;
   }
}


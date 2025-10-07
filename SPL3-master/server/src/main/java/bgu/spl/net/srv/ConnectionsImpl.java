package bgu.spl.net.srv;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.LinkedList;
import java.util.Map;


//import bgu.spl.net.srv.Connections;

public class ConnectionsImpl<T> implements Connections<T> {

     private final ConcurrentHashMap<Integer, ConnectionHandler<T>> ActiveClients= new ConcurrentHashMap<>(); //ID->ConnectionHandler
     private final ConcurrentHashMap<String, LinkedList<Integer>> topics= new ConcurrentHashMap<>(); //topic->ID's
     private final AtomicInteger messageID= new AtomicInteger(0);  //unique messegeID generator
     private final ConcurrentHashMap<String, User<T>> users= new ConcurrentHashMap<>(); //username->User


    @Override
    public boolean send(int connectionId, T msg){
        ConnectionHandler<T> CH = ActiveClients.get(connectionId);
        if( CH != null){
            CH.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void send(String channel, T msg){}

    
    @Override
    public ConcurrentHashMap<String, LinkedList<Integer>> getTopics(){
        return topics;
    }


    @Override
    public void disconnect(int connectionId){
        User<T> user = (ActiveClients.get(connectionId)).getUser();
        if (user != null) {
         //remove from all topics
         for (Map.Entry<Integer, String> entry : user.getTopics().entrySet()) {
            (topics.get(entry.getValue())).remove(connectionId);
         }
        user.getTopics().clear();
        user.setIsConnected(false);
        user.setConnectionHandler(null);
        user.setConnectionId(-1);
        }
        ActiveClients.remove(connectionId);
    }

    public void subscribe(String topic, int subscriptionId, int connectionId) {
      Map<Integer, String> userTopics = (ActiveClients.get(connectionId)).getUser().getTopics();
      userTopics.put(subscriptionId, topic);
      if (!topics.containsKey(topic)) {
        topics.put(topic, new LinkedList<Integer>());
      }

      (topics.get(topic)).add(connectionId);
   }


   public void unsubscribe(int subscriptionId, int connectionId) {
    Map<Integer, String> userTopics = (ActiveClients.get(connectionId)).getUser().getTopics();
    String topic = userTopics.get(subscriptionId);
    (topics.get(topic)).remove((Integer)connectionId);
    if(topics.get(topic).isEmpty()){
        topics.remove(topic);
    }
    userTopics.remove(subscriptionId);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void LoginUser(int connectionId, String username, String password) {
        ConnectionHandler<T> newHandler = (ConnectionHandler)ActiveClients.get(connectionId);
        User user;
        if(!users.containsKey(username) ){ //new user
            user = new User(connectionId, username, password, newHandler);
            users.put(username, user);
        } 
        else{ //existing user
            user= users.get(username);
            user.setIsConnected(true);
            user.setConnectionId(connectionId);
            user.setConnectionHandler(newHandler);
        } 
        newHandler.setUser(user);

    }

    public int generateId(){
        return messageID.getAndIncrement();
    }


    public boolean checkUser(String username, String password){
        return (!this.users.containsKey(username) || 
        (users.get(username)).getPassword().equals(password));
    }

    public boolean isUserLogedIn(String username, String password) {
        return users.containsKey(username) && (users.get(username)).getIsConnected();
    }

    public void addClient(ConnectionHandler<T> handler, int connectionId) {
        this.ActiveClients.put(connectionId, handler);
     }

     public ConnectionHandler<T> getHandler(int connectionId) {
        return ActiveClients.get(connectionId);
     }

     public boolean isTopicExcits(String topic){
        return topics.containsKey(topic);
     }



    

}
package bgu.spl.net.srv;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

//import java.io.IOException;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void send(String channel, T msg);

    void disconnect(int connectionId);

    void subscribe(String topic, int subscriptionId, int connectionId);

    void unsubscribe(int subscriptionId, int connectionId);

    void LoginUser(int connectionId, String username, String password);

    int generateId();

    boolean checkUser(String username, String password);

    boolean isUserLogedIn(String username, String password);

    void addClient(ConnectionHandler<T> handler, int connectionId);

    ConnectionHandler<T> getHandler(int connectionId);

    boolean isTopicExcits(String topic);

    ConcurrentHashMap<String, LinkedList<Integer>> getTopics();
}

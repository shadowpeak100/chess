package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.serverMessages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    //public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> connections = new ConcurrentHashMap<>();

    public void add(String playerName, int gameID, Session session) {

        //this should be GameID -> set of connections :)
        if(playerName == null){
            playerName = "null";
        }
        var connection = new Connection(playerName, session);
        if (!connections.containsKey(gameID)) {
            connections.put(gameID, new ConcurrentHashMap<>());
        }
        connections.get(gameID).put(playerName, connection);
    }

    public void remove(String playerName, Integer gameID) {
        if (connections.containsKey(gameID)) {
            ConcurrentHashMap<String, Connection> gameConnections = connections.get(gameID);
            gameConnections.remove(playerName);
        }
    }

    //sends to everyone except the excludeVisitorName
    public void broadcast(String excludePlayerName, String onlySendTo, Integer gameID, ServerMessage notification) throws IOException {
        if(onlySendTo != null && !onlySendTo.isEmpty()){
            if (connections.containsKey(gameID)) {
                ConcurrentHashMap<String, Connection> gameConnections = connections.get(gameID);

                for (Map.Entry<String, Connection> entry : gameConnections.entrySet()) {
                    String playerName = entry.getKey();
                    Connection connection = entry.getValue();

                    if (playerName.equals(onlySendTo)) {
                        try {
                            connection.send(new Gson().toJson(notification));
                        } catch (IOException e) {
                            System.err.println("Failed to send message to player " + playerName + ": " + e.getMessage());
                        }
                    }
                }
            } else{
                throw new IOException("Could not find gameID");
            }
        }else{
            if (connections.containsKey(gameID)) {

                ConcurrentHashMap<String, Connection> gameConnections = connections.get(gameID);

                for (Map.Entry<String, Connection> entry : gameConnections.entrySet()) {
                    String playerName = entry.getKey();
                    Connection connection = entry.getValue();

                    if (!playerName.equals(excludePlayerName)) {
                        try {
                            connection.send(new Gson().toJson(notification));
                        } catch (IOException e) {
                            System.err.println("Failed to send message to player " + playerName + ": " + e.getMessage());
                        }
                    }
                }

            }else{
                throw new IOException("Could not find gameID");
            }
        }
    }
}

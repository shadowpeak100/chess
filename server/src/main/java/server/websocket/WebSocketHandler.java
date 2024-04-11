package server.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;
import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.commandType) {
            case UserGameCommand.CommandType.JOIN_PLAYER -> {
                JoinPlayer jp = new Gson().fromJson(message, JoinPlayer.class);
                joinPlayer(jp.getPlayerName(), jp.getGameID(), session);
            }
            case UserGameCommand.CommandType.JOIN_OBSERVER -> {
                JoinObserver jo = new Gson().fromJson(message, JoinObserver.class);
                joinObserver(jo.getPlayerName(), jo.getGameID(), session);
            }
            case UserGameCommand.CommandType.MAKE_MOVE -> {
                MakeMove mm = new Gson().fromJson(message, MakeMove.class);
                makeMove(mm.getPlayerName(), mm.getGameID(), mm.getMove());
            }
            case UserGameCommand.CommandType.LEAVE -> {
                Leave l = new Gson().fromJson(message, Leave.class);
                leave(l.getPlayerName(), l.getGameID());
            }
            case UserGameCommand.CommandType.RESIGN -> {
                Resign r = new Gson().fromJson(message, Resign.class);
                resign(r.getPlayerName(), r.getGameID());
            }
        }
    }

    private void joinPlayer(String playerName, Integer gameID, Session session) throws IOException {
        connections.add(playerName, gameID, session);
        var message = String.format("%s has joined the game", playerName);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(playerName, gameID, notification);
    }

    private void joinObserver(String visitorName, Integer gameId, Session session) throws IOException {
        connections.add(visitorName, gameId, session);
        var message = String.format("%s has joined as an observer", visitorName);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(visitorName, gameId, notification);
    }

    private void makeMove(String visitorName, Integer gameId, ChessMove move) throws IOException {
        var message = String.format("%s has made a move", visitorName);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(visitorName, gameId, notification);
    }

    private void leave(String visitorName, Integer gameId) throws IOException {
        connections.remove(visitorName, gameId);
        var message = String.format("%s has left the game", visitorName);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(visitorName, gameId, notification);
    }

    private void resign(String visitorName, Integer gameId) throws IOException {
        connections.remove(visitorName, gameId);
        var message = String.format("%s has resigned", visitorName);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(visitorName, gameId, notification);
    }
}

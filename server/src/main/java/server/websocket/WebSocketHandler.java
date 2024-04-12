package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataAccess.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.Server;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;
import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final Server server;

    public WebSocketHandler(Server server){
        this.server = server;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        UserGameCommand action = new Gson().fromJson(message, UserGameCommand.class);
        switch (action.commandType) {
            case UserGameCommand.CommandType.JOIN_PLAYER -> {
                JoinPlayer jp = new Gson().fromJson(message, JoinPlayer.class);
                jp.loadData();
                joinPlayer(jp.getPlayerName(), jp.getAuthString(), jp.getGameID(), jp.getPlayerColor(), server.gd.getGame(jp.getGameID()).getGame(), session);
            }
            case UserGameCommand.CommandType.JOIN_OBSERVER -> {
                JoinObserver jo = new Gson().fromJson(message, JoinObserver.class);
                joinObserver(jo.getPlayerName(), jo.getGameID(), session);
            }
            case UserGameCommand.CommandType.MAKE_MOVE -> {
                MakeMove mm = new Gson().fromJson(message, MakeMove.class);
                makeMove(mm.getPlayerName(), mm.getGameID(), mm.getMove(), mm.getOpposingBoard(), mm.isInCheck(), mm.isInCheckmate());
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

    private void joinPlayer(String playerName, String auth, Integer gameID, ChessGame.TeamColor color, ChessGame game, Session session) throws IOException, DataAccessException {
        if(playerName == "" || playerName == null){
            playerName = server.ad.getUsernameWithAuth(auth);
        }

        //is this person already in the game?
        if(server.gd.getGame(gameID).getWhiteUsername() == playerName && color == ChessGame.TeamColor.BLACK){
            var message = String.format("Error: %s has tried to join twice", playerName);
            var notification = new Error(ServerMessage.ServerMessageType.ERROR, message);
            connections.broadcast("", playerName, gameID, notification);
        }
        if(server.gd.getGame(gameID).getBlackUsername() == playerName && color == ChessGame.TeamColor.WHITE){
            var message = String.format("Error: %s has tried to join twice", playerName);
            var notification = new Error(ServerMessage.ServerMessageType.ERROR, message);
            connections.broadcast("", playerName, gameID, notification);
        }

        connections.add(playerName, gameID, session);

        var message = String.format("%s has joined the game as team %s", playerName, color);
        var notification = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, message, game);
        connections.broadcast("", playerName, gameID, notification);

        var notificationJoin = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(playerName, "", gameID, notificationJoin);
    }

    private void joinObserver(String visitorName, Integer gameId, Session session) throws IOException {
        connections.add(visitorName, gameId, session);
        var message = String.format("%s has joined as an observer", visitorName);
        var notification = new Notification(ServerMessage.ServerMessageType.LOAD_GAME, message);
        connections.broadcast("", "", gameId, notification);
    }

    private void makeMove(String visitorName, Integer gameId, ChessMove move, String opposingBoard, Boolean inCheck, Boolean inCheckmate) throws IOException {

        String advisory = "";
        if(inCheck){
            advisory = "\nYou are in check!";
        }
        if(inCheckmate){
            advisory = advisory + "\nYou are in checkmate!";
        }

        var message = String.format("%s has made a move from row:%d col:%d to row:%d col:%d %s \nHere is the new board: \n%s", visitorName, move.getStartPosition().getRow(), move.getStartPosition().getColumn(), move.getEndPosition().getRow(), move.getEndPosition().getColumn(), advisory, opposingBoard);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(visitorName, "", gameId, notification);
    }

    private void leave(String visitorName, Integer gameId) throws IOException {
        var message = String.format("%s has left the game", visitorName);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(visitorName, "", gameId, notification);
        connections.remove("", gameId);
    }

    private void resign(String visitorName, Integer gameId) throws IOException {
        var message = String.format("%s has resigned", visitorName);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(visitorName, "", gameId, notification);
        connections.remove("", gameId);
    }
}

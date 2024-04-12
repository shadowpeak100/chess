package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.springframework.security.core.parameters.P;
import server.Server;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.Notification;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

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
                joinPlayer(jp.getPlayerName(), jp.getAuthString(), jp.getGameID(), jp.getPlayerColor(), server.gd.getGame(jp.getGameID()), session);
            }
            case UserGameCommand.CommandType.JOIN_OBSERVER -> {
                JoinObserver jo = new Gson().fromJson(message, JoinObserver.class);
                joinObserver(jo.getPlayerName(), jo.getGameID(), jo.getAuthString(), server.gd.getGame(jo.getGameID()), session);
            }
            case UserGameCommand.CommandType.MAKE_MOVE -> {
                MakeMove mm = new Gson().fromJson(message, MakeMove.class);

                Collection<ChessMove> validMoves = server.gd.getGame(mm.getGameID()).getGame().possibleMoves(mm.getMove().getStartPosition());
                boolean validMove = validMoves.contains(mm.getMove());
                ChessGame.TeamColor currentTurn = server.gd.getGame(mm.getGameID()).getGame().getTeamTurn();
                makeMove(mm.getPlayerName(), mm.getGameID(), mm.getMove(), server.gd.getGame(mm.getGameID()), mm.isInCheck(), mm.isInCheckmate(), mm.getAuthString(), validMove, currentTurn);
            }
            case UserGameCommand.CommandType.LEAVE -> {
                Leave l = new Gson().fromJson(message, Leave.class);
                leave(l.getPlayerName(), l.getGameID());
            }
            case UserGameCommand.CommandType.RESIGN -> {
                Resign r = new Gson().fromJson(message, Resign.class);
                resign(r.getPlayerName(), r.getGameID(), r.getAuthString());
            }
        }
    }

    private void joinPlayer(String playerName, String auth, Integer gameID, ChessGame.TeamColor color, GameData game, Session session) throws IOException, DataAccessException {
        if(playerName == "" || playerName == null){
            playerName = server.ad.getUsernameWithAuth(auth);
        }

        connections.add(playerName, gameID, session);

        boolean error = false;
        if(playerName == null){
            var message = String.format("Error: %s player does not exist", playerName);
            var notification = new Error(ServerMessage.ServerMessageType.ERROR, message);
            connections.broadcast("", "null", gameID, notification);
            error = true;
        }

        if(game == null || Objects.equals(game.getGameName(), "testGameEmpty")){
            var message = String.format("Error: %s has tried to join twice", playerName);
            var notification = new Error(ServerMessage.ServerMessageType.ERROR, message);
            connections.broadcast("", playerName, gameID, notification);
            error = true;
        }

        //is this person already in the game
        if(Objects.equals(game.getWhiteUsername(), playerName) && color == ChessGame.TeamColor.BLACK){
            var message = String.format("Error: %s has tried to join twice", playerName);
            var notification = new Error(ServerMessage.ServerMessageType.ERROR, message);
            connections.broadcast("", playerName, gameID, notification);
            error = true;
        }
        if(Objects.equals(game.getBlackUsername(), playerName) && color == ChessGame.TeamColor.WHITE){
            var message = String.format("Error: %s has tried to join twice", playerName);
            var notification = new Error(ServerMessage.ServerMessageType.ERROR, message);
            connections.broadcast("", playerName, gameID, notification);
            error = true;
        }

        if(!error){
            var message = String.format("%s has joined the game as team %s", playerName, color);
            var notification = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, message, game.getGame());
            connections.broadcast("", playerName, gameID, notification);

            var notificationJoin = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(playerName, "", gameID, notificationJoin);
        }
    }

    private void joinObserver(String visitorName, Integer gameId, String auth, GameData game, Session session) throws IOException, DataAccessException {
        if(visitorName == null || visitorName == ""){
            visitorName = server.ad.getUsernameWithAuth(auth);
        }

        connections.add(visitorName, gameId, session);

        boolean error = false;
        if(game == null || Objects.equals(game.getGameName(), "testGameEmpty") || visitorName == null){
            var message = "Error: game cannot be found";
            var notification = new Error(ServerMessage.ServerMessageType.ERROR, message);
            connections.broadcast("", visitorName, gameId, notification);
            error = true;
        }

        if(!error){
            var message = String.format("%s has joined as an observer", visitorName);

            var notification = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, message, game.getGame());
            connections.broadcast("", visitorName, gameId, notification);

            if(game.getBlackUsername() != visitorName && game.getWhiteUsername() != visitorName){
                var notificationJoin = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
                connections.broadcast(visitorName, "", gameId, notificationJoin);
            }
        }
    }

    private void makeMove(String visitorName, Integer gameId, ChessMove move, GameData game, Boolean inCheck, Boolean inCheckmate, String auth, boolean valid, ChessGame.TeamColor currentTurn) throws IOException, DataAccessException {
        if(visitorName == null || visitorName == ""){
            visitorName = server.ad.getUsernameWithAuth(auth);
        }

        game = server.gd.getGame(gameId);

        ChessGame.TeamColor ourTeam = null;
        ChessGame.TeamColor otherTeam = null;
        if(Objects.equals(game.getBlackUsername(), visitorName)){
            ourTeam = ChessGame.TeamColor.BLACK;
            otherTeam = ChessGame.TeamColor.WHITE;
        }
        if(Objects.equals(game.getWhiteUsername(), visitorName)){
            ourTeam = ChessGame.TeamColor.WHITE;
            otherTeam = ChessGame.TeamColor.BLACK;

        }

        if(!valid || currentTurn != ourTeam || game.getGame().getTeamTurn() == ChessGame.TeamColor.DONE){
            var message = "Error: not a valid move";
            var notification = new Error(ServerMessage.ServerMessageType.ERROR, message);
            connections.broadcast("", visitorName, gameId, notification);
            return;
        }

        String advisory = "";
        if(inCheck != null && inCheck){
            advisory = "\nYou are in check!";
        }
        if(inCheckmate != null && inCheckmate){
            advisory = advisory + "\nYou are in checkmate!";
        }

        var message = String.format("%s has made a move from row:%d col:%d to row:%d col:%d", visitorName, move.getStartPosition().getRow(), move.getStartPosition().getColumn(), move.getEndPosition().getRow(), move.getEndPosition().getColumn());
        var notificationLoad = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, message, game.getGame());
        connections.broadcast("", "", gameId, notificationLoad);
        GameData gd = server.gd.getGame(gameId);
        gd.getGame().setTeamTurn(otherTeam);
        server.gd.updateGame(gameId, gd);

        gd = server.gd.getGame(gameId);

        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, advisory);
        connections.broadcast(visitorName, "", gameId, notification);
    }

    private void leave(String visitorName, Integer gameId) throws IOException {
        var message = String.format("%s has left the game", visitorName);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(visitorName, "", gameId, notification);
        connections.remove("", gameId);
    }

    private void resign(String visitorName, Integer gameId, String auth) throws IOException, DataAccessException {
        if(visitorName == null || visitorName == ""){
            visitorName = server.ad.getUsernameWithAuth(auth);
        }

        var message = String.format("%s has resigned", visitorName);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);

        GameData gd = server.gd.getGame(gameId);
        gd.getGame().setTeamTurn(ChessGame.TeamColor.DONE);
        server.gd.updateGame(gameId, gd);

        connections.broadcast(visitorName, "", gameId, notification);
        connections.remove("", gameId);
    }
}

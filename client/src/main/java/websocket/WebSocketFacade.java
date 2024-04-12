package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import server.ResponseException;
import webSocketMessages.userCommands.*;
import webSocketMessages.serverMessages.*;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.websocket.*;


public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/connect");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    System.out.println("\n\n\n" + message);
                    Notification notification = new Gson().fromJson(message, Notification.class);
                    notificationHandler.notify(notification);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void joinGame(String playerName, String authToken, int gameID, ChessGame.TeamColor color, ChessGame game) throws ResponseException {
        try {
            var action = new JoinPlayer(playerName, authToken, gameID, color, game);
            action.loadData();
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void joinObserver(String playerName, String authToken, int gameID) throws ResponseException {
        try {
            var action = new JoinObserver(playerName, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void leave(String playerName, String authToken, int gameID) throws ResponseException {
        try {
            var action = new Leave(playerName, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
            this.session.close();
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void makeMove(String playerName, String authToken, int gameID, ChessMove chessMove, String opposingBoard, boolean inCheck, boolean inCheckmate) throws ResponseException {
        try {
            var action = new MakeMove(playerName, authToken, gameID, chessMove, opposingBoard, inCheck, inCheckmate);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void resign(String playerName, String authToken, int gameID) throws ResponseException {
        try {
            var action = new Resign(playerName, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    @Override
    public void onOpen(javax.websocket.Session session, EndpointConfig endpointConfig) {

    }
}

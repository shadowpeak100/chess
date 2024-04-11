package webSocketMessages.userCommands;

import chess.ChessGame;

public class JoinObserver extends UserGameCommand{
    public JoinObserver(String playerName, String authToken, int gameID) {
        super(authToken);
        this.commandType = CommandType.JOIN_OBSERVER;
        this.gameID = gameID;
        this.playerName = playerName;
    }

    public int getGameID(){
        return this.gameID;
    }

    public String getPlayerName(){
        return this.playerName;
    }

    private int gameID;
    private String playerName;
}

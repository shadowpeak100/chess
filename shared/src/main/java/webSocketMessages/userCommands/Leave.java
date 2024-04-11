package webSocketMessages.userCommands;

import chess.ChessMove;

public class Leave extends UserGameCommand{
    public Leave(String playerName, String authToken, int gameID) {
        super(authToken);
        this.commandType = CommandType.LEAVE;
        this.gameID = gameID;
        this.playerName = playerName;
    }

    public int getGameID(){
        return this.gameID;
    }

    public String getPlayerName(){
        return this.playerName;
    }

    private String playerName;
    private int gameID;
}

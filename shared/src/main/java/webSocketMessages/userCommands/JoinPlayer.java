package webSocketMessages.userCommands;
import chess.*;

public class JoinPlayer extends UserGameCommand{
    public JoinPlayer(String playerName, String authToken, int gameID, ChessGame.TeamColor color) {
        super(authToken);
        this.playerName = playerName;
        this.commandType = CommandType.JOIN_PLAYER;
        this.gameID = gameID;
        this.playerColor = color;
    }

    public int getGameID(){
        return this.gameID;
    }

    public String getPlayerName(){
        return this.playerName;
    }

    public ChessGame.TeamColor getPlayerColor(){
        return this.playerColor;
    }

    private int gameID;
    private String playerName;
    private ChessGame.TeamColor playerColor;
}

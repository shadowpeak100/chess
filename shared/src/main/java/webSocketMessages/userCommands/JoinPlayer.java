package webSocketMessages.userCommands;
import chess.*;

public class JoinPlayer extends UserGameCommand{
    public JoinPlayer(String authToken, int gameID) {
        super(authToken);
        this.commandType = CommandType.JOIN_PLAYER;
        this.gameID = gameID;
    }

    private int gameID;
    private ChessGame.TeamColor playerColor;
}

package webSocketMessages.userCommands;

import chess.ChessMove;

public class Resign extends UserGameCommand{
    public Resign(String authToken, int gameID) {
        super(authToken);
        this.commandType = CommandType.RESIGN;
        this.gameID = gameID;
    }

    private int gameID;
}

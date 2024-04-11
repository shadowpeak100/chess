package webSocketMessages.userCommands;

import chess.ChessMove;

public class MakeMove extends UserGameCommand{
    public MakeMove(String playerName, String authToken, int gameID, ChessMove move) {
        super(authToken);
        this.commandType = CommandType.MAKE_MOVE;
        this.gameID = gameID;
        this.move = move;
        this.playerName = playerName;
    }

    public int getGameID(){
        return this.gameID;
    }

    public String getPlayerName(){
        return this.playerName;
    }

    public ChessMove getMove(){
        return this.move;
    }

    private int gameID;
    private ChessMove move;
    private String playerName;
}

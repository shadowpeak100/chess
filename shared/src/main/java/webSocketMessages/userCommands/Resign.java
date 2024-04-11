package webSocketMessages.userCommands;

import chess.ChessMove;

public class Resign extends UserGameCommand{
    public Resign(String Playername, String authToken, int gameID) {
        super(authToken);
        this.commandType = CommandType.RESIGN;
        this.gameID = gameID;
        this.playerName = Playername;
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

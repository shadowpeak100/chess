package webSocketMessages.userCommands;

import chess.ChessGame;
import chess.ChessMove;

public class MakeMove extends UserGameCommand{
    public MakeMove(String playerName, String authToken, int gameID, ChessMove move, String boardPrint, Boolean inCheck, Boolean inCheckmate, ChessGame.TeamColor currentTurn) {
        super(authToken);
        this.commandType = CommandType.MAKE_MOVE;
        this.gameID = gameID;
        this.move = move;
        this.playerName = playerName;
        this.opposingBoard = boardPrint;
        this.inCheck = inCheck;
        this.inCheckmate = inCheckmate;
        this.currentTurn = currentTurn;
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

    public String getOpposingBoard(){
        return opposingBoard;
    }

    public Boolean isInCheck(){
        return inCheck;
    }

    public Boolean isInCheckmate(){
        return inCheckmate;
    }

    public Boolean isValid(){
        return isValid;
    }

    public ChessGame.TeamColor currentTurn(){
        return currentTurn;
    }

    private int gameID;
    private Boolean isValid;
    private ChessMove move;
    private String playerName;
    private String opposingBoard;
    private Boolean inCheck;
    private Boolean inCheckmate;
    private ChessGame.TeamColor currentTurn;
}

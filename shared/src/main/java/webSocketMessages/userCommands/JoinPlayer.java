package webSocketMessages.userCommands;
import chess.*;

public class JoinPlayer extends UserGameCommand{
    public JoinPlayer(String playerName, String authToken, int gameID, ChessGame.TeamColor color, ChessGame game) {
        super(authToken);
        this.playerName = playerName;
        this.commandType = CommandType.JOIN_PLAYER;
        this.gameID = gameID;
        this.playerColorStruct = color;
        this.game = game;

        if(color == null){
            if(playerColor.equalsIgnoreCase("white")){
                playerColorStruct = ChessGame.TeamColor.WHITE;
            }
            if(playerColor.equalsIgnoreCase("black")){
                playerColorStruct = ChessGame.TeamColor.BLACK;
            }
        }
    }

    public void loadData(){
        if(playerColorStruct == null){
            if(playerColor.equalsIgnoreCase("white")){
                playerColorStruct = ChessGame.TeamColor.WHITE;
            }
            if(playerColor.equalsIgnoreCase("black")){
                playerColorStruct = ChessGame.TeamColor.BLACK;
            }
        }
    }

    public int getGameID(){
        return this.gameID;
    }

    public String getPlayerName(){
        return this.playerName;
    }

    public ChessGame.TeamColor getPlayerColor(){
        return this.playerColorStruct;
    }

    public ChessGame getGame(){ return game; }

    private int gameID;
    private String playerName;
    private ChessGame.TeamColor playerColorStruct;
    private String playerColor;
    private ChessGame game;
}

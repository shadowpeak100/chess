package model;

import chess.ChessGame;

public class GameData {
    private int gameID;
    private String whiteUsername;
    private String blackUsername;
    private String gameName;
    private ChessGame game;

    //c'tors
    public GameData(int gameId, String whiteUsername, String blackUsername, String gamename, ChessGame game) {
        this.gameID = gameId;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gamename;
        this.game = game;
    }

    public GameData(){
        this.gameID = 0;
        this.whiteUsername = null;
        this.blackUsername = null;
        this.gameName = null;
        this.game = new ChessGame();
    }

    //getters and setters
    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public void setWhiteUsername(String whiteUsername) {
        this.whiteUsername = whiteUsername;
    }

    public String getBlackUsername() {
        return blackUsername;
    }

    public void setBlackUsername(String blackUsername) {
        this.blackUsername = blackUsername;
    }

    public String getGameName() {
        return gameName;
    }

    public ChessGame getGame() { return game; }

    public void setGame(ChessGame game) {this.game = game; }

}


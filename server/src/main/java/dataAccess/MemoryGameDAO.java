package dataAccess;

import chess.ChessGame;
import model.GameData;
import service.GamesWrapper;

import javax.xml.crypto.Data;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class MemoryGameDAO implements GameDAO{

    //maybe use an array list - must implement list interface
    private final ArrayList<GameData> games = new ArrayList<GameData>();
    int count;

    @Override
    public void clearAll() throws DataAccessException {
        games.clear();
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException{
        GameData returnVal = games.get(gameID);
        if(returnVal == null){
            throw new DataAccessException("");
        }else{
            return returnVal;
        }
    }

    @Override
    public GamesWrapper listGames() {
        return new GamesWrapper(games);
    }

    @Override
    public void updateGame() {

    }

    @Override
    public int newGame(String GameName) throws DataAccessException {
        GameData game = new GameData(count, null, null , GameName, new ChessGame());
        games.add(game);
        this.count ++;
        return game.getGameID();
    }
}

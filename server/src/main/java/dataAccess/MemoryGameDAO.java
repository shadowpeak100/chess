package dataAccess;

import chess.ChessGame;
import model.GameData;
import service.GamesWrapper;

import java.util.*;

public class MemoryGameDAO implements GameDAO{

    //maybe use an array list - must implement list interface
    private final ArrayList<GameData> games;
    int count;
    private final Map<Integer, Integer> gameIDtoIndexMap = new HashMap<>();


    public MemoryGameDAO(){
        count = 0;
        games = new ArrayList<GameData>();
    }

    @Override
    public void clearAll() throws DataAccessException {
        games.clear();
        count = 0;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException{
        if(!gameIDtoIndexMap.containsKey(gameID)){
            throw new DataAccessException("");
        }else{
            int gameIndex = gameIDtoIndexMap.get(gameID);
            GameData returnVal = games.get(gameIndex);
            return returnVal;
        }
    }

    @Override
    public GamesWrapper listGames() {
        return new GamesWrapper(games);
    }

    @Override
    public int newGame(String gameName) throws DataAccessException{
        int gameID = Math.abs(UUID.randomUUID().hashCode());
        GameData game = new GameData(gameID, null, null , gameName, new ChessGame());
        games.add(game);
        gameIDtoIndexMap.put(gameID, count);
        this.count ++;
        return game.getGameID();
    }

    @Override
    public void updateGame(int Gameid, GameData game) throws DataAccessException {}
}

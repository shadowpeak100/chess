package dataAccess;

import model.GameData;
import service.GamesWrapper;

public class SQLGameDAO implements GameDAO{
    public DatabaseManager manager;

    public SQLGameDAO(DatabaseManager manager){
        this.manager = manager;
    }

    @Override
    public void clearAll() throws DataAccessException {

    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public GamesWrapper listGames() {
        return null;
    }

    @Override
    public int newGame(String gameName) throws DataAccessException {
        return 0;
    }
}

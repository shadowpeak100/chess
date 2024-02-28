package dataAccess;

import model.GameData;
import server.BadRequestException;
import service.GamesWrapper;

import javax.xml.crypto.Data;

public interface GameDAO{
    void clearAll() throws DataAccessException;
    GameData getGame(int GameID) throws DataAccessException;
    GamesWrapper listGames();
    void updateGame();
    int newGame(String GameName) throws DataAccessException;
}

package dataAccess;

import model.GameData;
import server.BadRequestException;
import service.GamesWrapper;

import javax.xml.crypto.Data;

public interface GameDAO{
    void clearAll() throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    GamesWrapper listGames();
    int newGame(String gameName) throws DataAccessException;
    void updateGame(int Gameid, GameData game) throws DataAccessException;
}

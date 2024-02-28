package dataAccess;

public interface GameDAO{
    void clearAll() throws DataAccessException;
    void getGame();
    void listGames();
    void updateGame();
}

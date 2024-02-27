package dataAccess;

public interface GameDAO{
    void clear() throws DataAccessException;
    void getGame();
    void listGames();
    void updateGame();
}

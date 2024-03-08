package dataAccess;

public interface AuthDAO{
    String createAuth(String username) throws DataAccessException;
    String getUsernameWithAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void clearAll() throws DataAccessException;
}

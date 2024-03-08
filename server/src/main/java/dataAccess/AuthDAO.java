package dataAccess;

public interface AuthDAO{
    String createAuth(String username);
    String getUsernameWithAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken);
    void clearAll();
}

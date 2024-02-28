package dataAccess;

public interface AuthDAO{
    String createAuth(String username);
    String getUsernameWithAuth(String authToken);
    void deleteAuth(String authToken);
    void clearAll();
}

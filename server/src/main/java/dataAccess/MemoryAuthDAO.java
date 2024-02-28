package dataAccess;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO{

    //auth tokens to usernames
    private final HashMap<String, String> authUserMap = new HashMap<>();

    @Override
    public String createAuth(String username) {
        var authToken = UUID.randomUUID().toString();
        authUserMap.put(authToken, username);
        return authToken;
    }

    @Override
    public String getUsernameWithAuth(String authToken) {
        return authUserMap.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        authUserMap.remove(authToken);
    }

    public void clearAll(){
        authUserMap.clear();
    }
}
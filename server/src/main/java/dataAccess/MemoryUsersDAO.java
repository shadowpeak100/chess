package dataAccess;

import model.UserData;
import org.eclipse.jetty.server.Authentication;

import java.util.HashMap;

public class MemoryUsersDAO implements UserDAO{

    //usernames to userData
    final private HashMap<String, UserData> users = new HashMap<>();

    @Override
    public UserData getUser(String username){
        return users.get(username);
    }

    @Override
    public void createUser(String username, String password, String email) {
        UserData usr = new UserData(username, password, email);
        users.put(username, usr);
    }

    public void clearAll() {
        users.clear();
    }
}

package dataAccess;

import model.UserData;
import org.eclipse.jetty.server.Authentication;

import java.util.HashMap;

public class MemoryUsersDAO implements UserDAO{

    //usernames to userData
    final private HashMap<String, UserData> users = new HashMap<>();

    @Override
    public UserData getUser(String Username){
        return users.get(Username);
    }

    @Override
    public void createUser(String Username, String Password, String Email) {
        UserData usr = new UserData(Username, Password, Email);
        users.put(Username, usr);
    }

    public void clearAll() {
        users.clear();
    }
}

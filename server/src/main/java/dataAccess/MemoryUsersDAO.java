package dataAccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUsersDAO implements UserDAO{

    //Auth tokens to userData
    final private HashMap<String, UserData> games = new HashMap<>();


    @Override
    public UserData getUser(String Username){
        return games.get(Username);
    }

    @Override
    public void createUser() {

    }
}

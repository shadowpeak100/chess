package dataAccess;

import model.UserData;

public interface UserDAO{
    UserData getUser(String username);
    void createUser(String username, String password, String email);
    void clearAll();
}


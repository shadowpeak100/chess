package dataAccess;

import model.UserData;

public interface UserDAO{
    UserData getUser(String Username);
    void createUser(String Username, String Password, String Email);
    void clearAll();
}


package dataAccess;

import model.UserData;

public interface UserDAO{
    UserData getUser(String Username);

    void createUser();
}


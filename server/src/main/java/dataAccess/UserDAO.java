package dataAccess;

import model.UserData;

public interface UserDAO{
    UserData getUser(String username) throws DataAccessException;
    void createUser(String username, String password, String email) throws DataAccessException;
    void clearAll() throws DataAccessException;
}


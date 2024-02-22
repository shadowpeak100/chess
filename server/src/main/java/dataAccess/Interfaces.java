package dataAccess;

interface UserDAO{
    void getUser();
    void createUser();
}

interface GameDAO{
    void clear();
    void getGame();
    void listGames();
    void updateGame();
}

interface AuthDAO{
    void createAuth();
    void getAuth();
    void deleteAuth();
}

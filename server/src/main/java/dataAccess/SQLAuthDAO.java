package dataAccess;

public class SQLAuthDAO implements AuthDAO{
    public DatabaseManager manager;

    public SQLAuthDAO(DatabaseManager manager){
        this.manager = manager;
    }

    @Override
    public String createAuth(String username) {
        return null;
    }

    @Override
    public String getUsernameWithAuth(String authToken) {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) {

    }

    @Override
    public void clearAll() {

    }
}

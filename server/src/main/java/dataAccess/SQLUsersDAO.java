package dataAccess;

import model.UserData;

import java.sql.SQLException;

public class SQLUsersDAO implements UserDAO {
    public DatabaseManager manager;

    public SQLUsersDAO(DatabaseManager manager){
        this.manager = manager;
    }

    @Override
    public UserData getUser(String username) {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT 1+1")) {
                var rs = preparedStatement.executeQuery();
                rs.next();
                System.out.println(rs.getInt(1));
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void createUser(String username, String password, String email) {

    }

    @Override
    public void clearAll() {

    }
}

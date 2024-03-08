package dataAccess;

import com.google.gson.Gson;
import model.GameData;
import model.UserData;

import java.sql.SQLException;

public class SQLUsersDAO implements UserDAO {

    @Override
    public UserData getUser(String username) throws DataAccessException{
        UserData userData = null;

        String selectQuery = "SELECT * FROM users WHERE username = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, username);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String json = resultSet.getString("json");
                    Gson gson = new Gson();
                    userData = gson.fromJson(json, UserData.class);
                }
            }
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to fetch user by username: %s", ex.getMessage()));
        }

        return userData;
    }

    @Override
    public void createUser(String username, String password, String email) throws DataAccessException{
        String insertQuery = "INSERT INTO users (username, json) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(insertQuery)) {
            UserData userData = new UserData(username, password, email);

            Gson gson = new Gson();
            String userDataJsonString = gson.toJson(userData);

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, userDataJsonString);

            preparedStatement.executeUpdate();

        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to create user: %s", ex.getMessage()));
        }
    }

    @Override
    public void clearAll() throws DataAccessException{
        String deleteQuery = "DELETE FROM users";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(deleteQuery)) {
            preparedStatement.executeUpdate();
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to clear users table: %s", ex.getMessage()));
        }
    }
}

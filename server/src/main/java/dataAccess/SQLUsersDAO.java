package dataAccess;

import com.google.gson.Gson;
import model.GameData;
import model.UserData;

import java.sql.SQLException;

public class SQLUsersDAO implements UserDAO {

    @Override
    public UserData getUser(String username) {
        UserData userData = null;

        // SQL query to select user data based on username
        String selectQuery = "SELECT * FROM users WHERE username = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, username); // Set the username parameter
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String json = resultSet.getString("json");
                    Gson gson = new Gson();
                    userData = gson.fromJson(json, UserData.class);
                }
            }
        } catch (SQLException | DataAccessException ex) {
            //throw new DataAccessException(String.format("Unable to fetch user by username: %s", ex.getMessage()));
        }

        return userData;
    }

    @Override
    public void createUser(String username, String password, String email) {
        String insertQuery = "INSERT INTO users (username, json) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(insertQuery)) {
            UserData userData = new UserData(username, password, email);

            Gson gson = new Gson();
            String userDataJsonString = gson.toJson(userData);

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, userDataJsonString);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " rows inserted."); // Optionally print the number of rows inserted

            try (var generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    System.out.println("Generated user ID: " + userId);
                }
            }
        } catch (SQLException | DataAccessException ex) {
            //throw new DataAccessException(String.format("Unable to create user: %s", ex.getMessage()));
        }
    }

    @Override
    public void clearAll() {
        String deleteQuery = "DELETE FROM users";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(deleteQuery)) {
            int rowsAffected = preparedStatement.executeUpdate(); // Execute the delete statement
            System.out.println(rowsAffected + " rows deleted."); // Optionally print the number of rows deleted
        } catch (SQLException | DataAccessException ex) {
            //throw new DataAccessException(String.format("Unable to clear users table: %s", ex.getMessage()));
        }
    }
}

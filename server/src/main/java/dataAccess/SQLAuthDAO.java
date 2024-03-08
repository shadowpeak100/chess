package dataAccess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLAuthDAO implements AuthDAO{

    @Override
    public String createAuth(String username){
        var authToken = UUID.randomUUID().toString();
        var statement = "INSERT INTO authTokenToUsername (authtoken, username) VALUES (?, ?)";
        int linesAffected;

        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                preparedStatement.setString(2, username);
                linesAffected = preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException ex) {
            //throw new DataAccessException(String.format("Unable to insert auth token to database: %s", ex.getMessage()));
        }

        //System.out.println(linesAffected);

        return authToken;
    }

    @Override
    public String getUsernameWithAuth(String authToken) throws DataAccessException {
        String username = null;
        var statement = "SELECT username FROM authTokenToUsername WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        username = resultSet.getString("username"); // Retrieve the username from the result set
                    }
                }
            }
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to get username from auth token: %s", ex.getMessage()));
        }

        System.out.println(username);
        return username;
    }

    @Override
    public void deleteAuth(String authToken) {
        String deleteStatement = "DELETE FROM authTokenToUsername WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(deleteStatement)) {
            preparedStatement.setString(1, authToken);
            int rowsAffected = preparedStatement.executeUpdate(); // Execute the delete statement
            System.out.println(rowsAffected + " rows deleted."); // Optionally print the number of rows deleted
        } catch (SQLException | DataAccessException ex) {
            System.out.println("failed deleting auth");
            //throw new DataAccessException(String.format("Unable to delete row by auth token: %s", ex.getMessage()));
        }
    }

    @Override
    public void clearAll() {
        String deleteStatement = "DELETE FROM authTokenToUsername";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(deleteStatement)) {
            int rowsAffected = preparedStatement.executeUpdate(); // Execute the delete statement
            System.out.println(rowsAffected + " rows deleted."); // Optionally print the number of rows deleted
        } catch (SQLException | DataAccessException ex) {
            System.out.println("failed deleting all");
            //throw new DataAccessException(String.format("Unable to delete all rows from the table: %s", ex.getMessage()));
        }
    }
}

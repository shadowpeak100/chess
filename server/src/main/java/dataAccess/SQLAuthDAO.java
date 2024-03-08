package dataAccess;

import javax.xml.crypto.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLAuthDAO implements AuthDAO{

    @Override
    public String createAuth(String username) throws DataAccessException {
        if(username == null){
            throw new DataAccessException("error: username cannot be null for auth token generation");
        }
        var authToken = UUID.randomUUID().toString();
        var statement = "INSERT INTO authTokenToUsername (authtoken, username) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                preparedStatement.setString(2, username);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to insert auth token to database: %s", ex.getMessage()));
        }

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
                        username = resultSet.getString("username");
                    }
                }
            }
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to get username from auth token: %s", ex.getMessage()));
        }

        return username;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException{
        if (authToken==null){
            throw new DataAccessException("error: auth token must not be null for deletion");
        }
        String deleteStatement = "DELETE FROM authTokenToUsername WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(deleteStatement)) {
            preparedStatement.setString(1, authToken);
            preparedStatement.executeUpdate();
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to delete row by auth token: %s", ex.getMessage()));
        }
    }

    @Override
    public void clearAll() throws DataAccessException{
        String deleteStatement = "DELETE FROM authTokenToUsername";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(deleteStatement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to delete all rows from the table: %s", ex.getMessage()));
        }
    }
}

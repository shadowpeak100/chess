package dataAccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import service.GamesWrapper;

import java.sql.SQLException;
import java.util.*;

public class SQLGameDAO implements GameDAO{


    @Override
    public void clearAll() throws DataAccessException {
        String deleteStatement = "DELETE FROM gameIdToGame";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(deleteStatement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to delete all rows from the gameIdToGame table: %s", ex.getMessage()));
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData game = null;

        String selectQuery = "SELECT gamedata FROM gameIdToGame WHERE gameid = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(selectQuery)) {
            preparedStatement.setInt(1, gameID);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String json = resultSet.getString("gamedata");
                    Gson gson = new Gson();
                    game = gson.fromJson(json, GameData.class);
                }
            }
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to retrieve game data: %s", ex.getMessage()));
        }

        return game;
    }

    @Override
    public GamesWrapper listGames() throws DataAccessException{
        ArrayList<GameData> games = new ArrayList<>();

        String selectQuery = "SELECT gamedata FROM gameIdToGame";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(selectQuery);
             var resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String json = resultSet.getString("gamedata");
                Gson gson = new Gson();
                GameData game = gson.fromJson(json, GameData.class);
                games.add(game);
            }
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to retrieve games: %s", ex.getMessage()));
        }

         return new GamesWrapper(games);
    }

    @Override
    public int newGame(String gameName) throws DataAccessException {
        if (gameName==null){
            throw new DataAccessException("error: must have game name specified");
        }

        int gameID = Math.abs(UUID.randomUUID().hashCode());
        GameData game = new GameData(gameID, null, null , gameName, new ChessGame());
        game.getGame().defaultSetup();

        Gson gson = new Gson();
        String json = gson.toJson(game);

        String insertStatement = "INSERT INTO gameIdToGame (gameid, gamedata) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(insertStatement)) {
            preparedStatement.setInt(1, gameID);
            preparedStatement.setString(2, json);
            preparedStatement.executeUpdate();
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to insert game data into the gameIdToGame table: %s", ex.getMessage()));
        }

        return gameID;
    }

    public void updateGame(int Gameid, GameData game) throws DataAccessException{
        String updateQuery = "UPDATE gameIdToGame SET gamedata = ? WHERE gameid = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(updateQuery)) {

            Gson gson = new Gson();
            String json = gson.toJson(game);

            preparedStatement.setString(1, json);
            preparedStatement.setInt(2, Gameid);
            preparedStatement.executeUpdate();
        } catch (SQLException | DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to update game data: %s", ex.getMessage()));
        }
    }
}

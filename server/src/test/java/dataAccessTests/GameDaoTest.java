package dataAccessTests;

import chess.ChessGame;
import dataAccess.*;
import org.junit.jupiter.api.*;
import service.GamesWrapper;
import model.GameData;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameDaoTest {
    private Object connection;
    private SQLGameDAO gameDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        DatabaseManager.configureDatabase();
        connection = DatabaseManager.getConnection();
        gameDAO = new SQLGameDAO();
    }

    @Test
    public void testNewGame() {
        String gameName1 = "Game 1";
        insertTestData(gameName1);

        GamesWrapper gamesWrapper = null;
        try {
            gamesWrapper = gameDAO.listGames();
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        List<GameData> games = gamesWrapper.getGames();

        assertTrue(games.stream().anyMatch(game -> game.getGameName().equals(gameName1)));
    }

    @Test
    public void testGetGame() {
        String gameName = "Test Game";
        int gameID = insertTestData(gameName);

        GameData retrievedGame = null;
        try {
            retrievedGame = gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        assertNotNull(retrievedGame);
        assertEquals(gameID, retrievedGame.getGameID());
        assertEquals(gameName, retrievedGame.getGameName());
    }

    @Test
    public void testListGames() {
        String gameName1 = "Game 1";
        String gameName2 = "Game 2";
        insertTestData(gameName1);
        insertTestData(gameName2);

        GamesWrapper gamesWrapper = null;
        try {
            gamesWrapper = gameDAO.listGames();
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        List<GameData> games = gamesWrapper.getGames();

        assertTrue(games.stream().anyMatch(game -> game.getGameName().equals(gameName1)));
        assertTrue(games.stream().anyMatch(game -> game.getGameName().equals(gameName2)));
    }

    @Test
    public void testClearGames() {
        String gameName1 = "Game 1";
        String gameName2 = "Game 2";
        insertTestData(gameName1);
        insertTestData(gameName2);

        GamesWrapper gamesWrapper = null;

        try {
            gameDAO.clearAll();
            gamesWrapper = gameDAO.listGames();
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        List<GameData> games = gamesWrapper.getGames();
        assertEquals(0, games.size());
    }

    // Helper method to insert test game data into the database
    private int insertTestData(String gameName) {
        try {
            return gameDAO.newGame(gameName);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }



}

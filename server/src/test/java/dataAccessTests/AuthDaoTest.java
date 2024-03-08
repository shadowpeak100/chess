package dataAccessTests;

import dataAccess.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthDaoTest {
    private Object connection;
    private SQLAuthDAO authDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        DatabaseManager.configureDatabase();
        connection = DatabaseManager.getConnection();
        authDAO = new SQLAuthDAO();
    }

    @Test
    public void testCreateAuth() {
        String username = "test_user";
        String authToken = null;
        try {
            authToken = authDAO.createAuth(username);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        assertNotNull(authToken);
    }

    @Test
    public void testGetUsernameWithAuth() {
        String username = "test_user";
        String authToken = null;
        try {
            authToken = authDAO.createAuth(username);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }


        assertNotNull(authToken);
        String retrievedUsername = null;
        try {
            retrievedUsername = authDAO.getUsernameWithAuth(authToken);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        assertEquals(username, retrievedUsername);
    }

    @Test
    public void testGetUsernameWithNonexistentAuth() {
        String username = "test_user";
        String authToken = null;
        try {
            authToken = authDAO.createAuth(username);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }


        assertNotNull(authToken);
        String retrievedUsername = null;
        try {
            retrievedUsername = authDAO.getUsernameWithAuth("It's just like magic!");
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        assertNull(retrievedUsername);
    }

    @Test
    public void testDeleteAuth() {
        String username = "test_user";
        String authToken = null;
        try {
            authToken = authDAO.createAuth(username);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        assertNotNull(authToken);
        String retrievedUsername = null;
        try {
            authDAO.deleteAuth(authToken);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        assertNull(retrievedUsername);
    }

    @Test
    public void testClearAll() {
        String username = "test_user";
        String authToken = null;
        try {
            authToken = authDAO.createAuth(username);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        assertNotNull(authToken);

        String retrievedUsername = null;
        try {
            authDAO.clearAll();
            retrievedUsername = authDAO.getUsernameWithAuth(authToken);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        assertNull(retrievedUsername);
    }
}

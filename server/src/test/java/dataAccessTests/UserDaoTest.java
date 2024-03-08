package dataAccessTests;

import dataAccess.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import model.UserData;

public class UserDaoTest {

    private Object connection;
    private SQLUsersDAO usersDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        DatabaseManager.configureDatabase();
        connection = DatabaseManager.getConnection();
        usersDAO = new SQLUsersDAO();
    }

    @Test
    public void testCreateUser() throws DataAccessException {
        String username = "test_user";
        String password = "test_password";
        String email = "test@example.com";

        assertDoesNotThrow(() -> {
            usersDAO.createUser(username, password, email);
        });
    }

    @Test
    public void testCreateDepulicateUser() throws DataAccessException {
        String username = "";
        String password = null;
        String email = "";

        assertThrows(DataAccessException.class, () -> {
            usersDAO.createUser(username, password, email);
        });
    }

    @Test
    public void testGetUser() throws DataAccessException {
        String username = "test_user";
        String password = "test_password";
        String email = "test@example.com";

        usersDAO.createUser(username, password, email);

        UserData userData = usersDAO.getUser(username);
        assertNotNull(userData);
        assertEquals(username, userData.getUsername());
        assertEquals(password, userData.getPassword());
        assertEquals(email, userData.getEmail());
    }

    @Test
    public void testGetNonexistentUser() throws DataAccessException {
        UserData userData = usersDAO.getUser("");
        assertEquals(userData.getEmail(), "");
    }


    @Test
    public void testClear() throws DataAccessException {
        String username = "test_user";
        String password = "test_password";
        String email = "test@example.com";

        usersDAO.createUser(username, password, email);

        UserData userData = usersDAO.getUser(username);
        assertNotNull(userData);
        assertEquals(username, userData.getUsername());
        assertEquals(password, userData.getPassword());
        assertEquals(email, userData.getEmail());

        usersDAO.clearAll();
        userData = usersDAO.getUser(username);
        assertNull(userData);
    }
}

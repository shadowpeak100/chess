package serviceTests;

import dataAccess.*;
import org.junit.jupiter.api.*;
import server.ResponseException;
import service.ClearService;
import static org.junit.jupiter.api.Assertions.*;


public class ClearServiceTest {

    @Test
    public void testClearAll() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();
        int id;
        try{
            id = gd.newGame("testGame");
        }catch (DataAccessException ignored){

        }
        ad.createAuth("jimmy");
        ud.createUser("testPerson", "password", "b@g.c");

        ClearService clearService = new ClearService(gd, ad, ud);

        try{
            clearService.clearAll();
        }catch (ResponseException ignored){

        }
        assertNull(ud.getUser("testPerson"));
    }
}

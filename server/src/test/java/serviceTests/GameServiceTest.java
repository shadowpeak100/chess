package serviceTests;

import com.google.gson.Gson;
import dataAccess.*;
import org.junit.jupiter.api.Test;
import server.BadRequestException;
import server.ResponseException;
import server.UnauthorizedException;
import service.ClearService;
import service.GameService;
import service.GamesWrapper;

import javax.xml.crypto.Data;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    @Test
    public void testListGamesPositive() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();
        int id = 0;
        try{
            id = gd.newGame("testGame");
        }catch (DataAccessException ignored){

        }

        String token = ad.createAuth("Hammy");
        ud.createUser("Hammy", "Jackson", "das@");

        GameService gameService = new GameService(gd, ad, ud);


        try{
            GamesWrapper gamesObject = gameService.listGames(token);
            var serializer = new Gson();
            var json = serializer.toJson(gamesObject);
            String x = json;
            assertEquals("{\"games\":[{\"gameID\":" + id + ",\"gameName\":\"testGame\",\"game\":{\"board\":{\"board\":[[null,null,null,null,null,null,null,null],[null,null,null,null,null,null,null,null],[null,null,null,null,null,null,null,null],[null,null,null,null,null,null,null,null],[null,null,null,null,null,null,null,null],[null,null,null,null,null,null,null,null],[null,null,null,null,null,null,null,null],[null,null,null,null,null,null,null,null]]},\"currentTurn\":\"WHITE\"}}]}",json);
        }catch (UnauthorizedException ignored){

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testListGamesNoUser() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();

        String token = ad.createAuth("Hammy");

        GameService gameService = new GameService(gd, ad, ud);

        assertThrows(UnauthorizedException.class, () -> {
            gameService.listGames(token);
        });
    }

    @Test
    public void testCreateGameSuccessful() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();
        int id = 0;
        try{
            id = gd.newGame("testGame");
        }catch (DataAccessException ignored){

        }

        ud.createUser("Tommy", "", "");
        String token = ad.createAuth("Tommy");
        GameService gameService = new GameService(gd, ad, ud);
        try{
            id = gameService.createGame(token, "sam's game");
        }catch (BadRequestException | UnauthorizedException ignored){
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        assertNotEquals(0, id);
    }

    @Test
    public void testCreateGameUnsuccessful() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();

        GameService gameService = new GameService(gd, ad, ud);

        assertThrows(UnauthorizedException.class, () -> {
            gameService.createGame("ASDLJ", "sam");
        });
    }

    @Test
    public void testJoinGameSuccessful() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();
        int id = 0;
        try{
            id = gd.newGame("testGame");
        }catch (DataAccessException ignored){

        }

        String tok = ad.createAuth("gzs");
        GameService gameService = new GameService(gd, ad, ud);

        int finalId = id;
        assertDoesNotThrow(() -> {
            gameService.joinGame(tok, finalId, "BLACK");
        });
    }

    @Test
    public void testJoinGameUnsuccessful() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();

        String tok = ad.createAuth("gzs");
        //no game ID
        GameService gameService = new GameService(gd, ad, ud);

        assertThrows(BadRequestException.class, () -> {
            gameService.joinGame(tok, 16, "BLACK");
        });
    }
}

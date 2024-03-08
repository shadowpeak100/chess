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

        String token = null;
        try{
           token = ad.createAuth("Hammy");
        }catch (DataAccessException e){
            assertEquals(1, 0);
        }
        assertDoesNotThrow(() -> {
            ud.createUser("Hammy", "Jackson", "das@");
        });

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

        String token = null;
        try{
            token = ad.createAuth("Hammy");
        }catch (DataAccessException e){
            assertEquals(1, 0);
        }


        GameService gameService = new GameService(gd, ad, ud);

        String finalToken = token;
        assertThrows(UnauthorizedException.class, () -> {
            gameService.listGames(finalToken);
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

        assertDoesNotThrow(() -> {
            ud.createUser("Tommy", "", "");
        });

        String token = null;
        try{
            token = ad.createAuth("Tommy");
        }catch (DataAccessException e){
            assertEquals(1, 0);
        }
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


        String tok = null;
        try{
            tok = ad.createAuth("gzs");
        }catch (DataAccessException e){
            assertEquals(1, 0);
        }

        GameService gameService = new GameService(gd, ad, ud);

        int finalId = id;
        String finalTok = tok;
        assertDoesNotThrow(() -> {
            gameService.joinGame(finalTok, finalId, "BLACK");
        });
    }

    @Test
    public void testJoinGameUnsuccessful() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();

        String tok = null;
        try{
            tok = ad.createAuth("gzs");
        }catch (DataAccessException e){
            assertEquals(1, 0);
        }
        //no game ID
        GameService gameService = new GameService(gd, ad, ud);

        String finalTok = tok;
        assertThrows(BadRequestException.class, () -> {
            gameService.joinGame(finalTok, 16, "BLACK");
        });
    }
}

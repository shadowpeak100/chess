package serviceTests;

import dataAccess.*;
import model.UserData;
import org.junit.jupiter.api.Test;
import server.BadRequestException;
import server.TakenException;
import server.UnauthorizedException;
import service.GameService;
import service.UserService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceTest {

    @Test
    public void testRegister() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();

        UserService userService = new UserService(gd, ad, ud);

        assertDoesNotThrow(() -> {
            userService.register(new UserData("James", "GoodPassword!", "NoEmailBro"));
        });
    }

    @Test
    public void testRegisterTakenName() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();

        UserService userService = new UserService(gd, ad, ud);
        try{
            userService.register(new UserData("James", "GoodPassword!", "NoEmailBro"));
        }catch (TakenException | BadRequestException ignored){
        }

        assertThrows(TakenException.class, () -> {
            userService.register(new UserData("James", "GoodPassword!", "NoEmailBro"));
        });
    }

    @Test
    public void testSuccessfulLogout() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();

        String tok = null;
        tok = ad.createAuth("James");

        UserService userService = new UserService(gd, ad, ud);
        try{
            userService.register(new UserData("James", "GoodPassword!", "NoEmailBro"));
        }catch (TakenException | BadRequestException ignored){
        }

        String finalTok = tok;
        assertDoesNotThrow(() -> {
            userService.logout(finalTok);
        });
    }

    @Test
    public void testLogoutTwice() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();

        String tok = null;
        tok = ad.createAuth("James");

        UserService userService = new UserService(gd, ad, ud);
        try{
            userService.register(new UserData("James", "GoodPassword!", "NoEmailBro"));
        }catch (TakenException | BadRequestException ignored){
        }

        String finalTok = tok;
        assertDoesNotThrow(() -> {
            userService.logout(finalTok);
        });

        assertThrows(UnauthorizedException.class, () -> {
            userService.logout(finalTok);
        });
    }

    @Test
    public void testLoginSuccessful() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();

        String tok = null;
        tok = ad.createAuth("James");


        UserData userJames = new UserData("James", "GoodPassword!", "NoEmailBro");

        UserService userService = new UserService(gd, ad, ud);
        try{
            userService.register(new UserData("James", "GoodPassword!", "NoEmailBro"));
            userService.logout(tok);
        }catch (TakenException | BadRequestException | UnauthorizedException ignored){
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        assertDoesNotThrow(() -> {
            userService.login(userJames);
        });
    }

    @Test
    public void testLoginUnsuccessful() {
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();

        UserData userJames = new UserData("James", "GoodPassword!", "NoEmailBro");

        UserService userService = new UserService(gd, ad, ud);

        assertThrows(UnauthorizedException.class, () -> {
            userService.login(userJames);
        });
    }
}

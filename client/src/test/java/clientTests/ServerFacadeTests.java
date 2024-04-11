package clientTests;

import dataAccess.DataAccessException;
import org.junit.jupiter.api.*;
import server.ResponseException;
import server.Server;
import server.UnauthorizedException;
import ui.ChessClientFacade;
import ui.Repl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ServerFacadeTests {

    private static Server server;
    private static ChessClientFacade clientFacade;
    private static Repl repl;
    private static String serverUrl;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        serverUrl = "http://localhost:8080";
        server.run(8123);
        repl = new Repl(serverUrl);
        repl.run();

        //clientFacade = new ChessClientFacade(serverUrl, );

        //register a user
        String[] params = new String[]{"Willie", "Wonka", "itsASecret@wizbang.com"};
        try{
            clientFacade.register(params);
        }catch(Exception e){
            System.out.println("advise: " + e);
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void startFacade(){
        repl = new Repl(serverUrl);
        repl.run();
        //clientFacade = new ChessClientFacade();
    }

    @Test
    public void loginTest() {
        String[] params = new String[]{"Willie", "Wonka", "itsASecret@wizbang.com"};

        assertDoesNotThrow(() -> {
            clientFacade.login(params);
        });
    }

    @Test
    public void loginNonExistentUserTest() {
        String[] params = new String[]{"James", "The Spy", "shhh@shush.com"};

        assertThrows(UnauthorizedException.class, () -> {
            clientFacade.login(params);
        });
    }

    @Test
    public void registerTest(){
        int suffix = (int)(Math.random() * 10000);
        String[] params = new String[]{"John" + suffix, "The Spy", "shhh@shush.com"};

        assertDoesNotThrow(() -> {
            clientFacade.register(params);
        });
    }

    @Test
    public void registerMalformedUserTest(){
        String[] params = new String[]{"John", "The Spy"};

        assertThrows(DataAccessException.class, () -> {
            clientFacade.register(params);
        });
    }

    @Test
    public void logoutTest(){
        String[] params = new String[]{"Willie", "Wonka", "itsASecret@wizbang.com"};

        assertDoesNotThrow(() -> {
            clientFacade.login(params);
        });

        assertDoesNotThrow(() -> {
            clientFacade.logout();
        });
    }

    @Test
    public void logoutNotSignedInTest(){
        assertThrows(ResponseException.class, () -> {
            clientFacade.logout();
        });
    }

    @Test
    public void createGameTest(){
        String[] params = new String[]{"Willie", "Wonka", "itsASecret@wizbang.com"};

        assertDoesNotThrow(() -> {
            clientFacade.login(params);
        });

        int suffix = (int)(Math.random() * 10000);

        String[] params2 = new String[]{"game" + suffix};

        assertDoesNotThrow(() -> {
            clientFacade.createGame(params2);
        });
    }

    @Test
    public void createGameBadInputTest(){
        assertThrows(DataAccessException.class, () -> {
            clientFacade.createGame();
        });
    }

    @Test
    public void listGamesTest(){
        String[] params = new String[]{"Willie", "Wonka", "itsASecret@wizbang.com"};

        assertDoesNotThrow(() -> {
            clientFacade.login(params);
        });

        assertDoesNotThrow(() -> {
            clientFacade.listGames();
        });
    }

    @Test
    public void listGamesNotSignedInTest(){
        assertThrows(DataAccessException.class, () -> {
            clientFacade.listGames();
        });
    }

    public static String extractID(String str) {
        // Find the index of the last space character
        int lastSpaceIndex = str.lastIndexOf(" ");

        // Extract the substring following the last space
        if (lastSpaceIndex != -1 && lastSpaceIndex + 1 < str.length()) {
            return str.substring(lastSpaceIndex + 1);
        }
        return "";
    }

    @Test
    public void joinGameTest(){
        String[] params = new String[]{"Willie", "Wonka", "itsASecret@wizbang.com"};

        assertDoesNotThrow(() -> {
            clientFacade.login(params);
        });

        String[] params2 = new String[]{"Willie's game"};
        String id = "";

        try{
            String joinString = clientFacade.createGame(params2);
            id = extractID(joinString);
        }catch(Exception e){
            System.out.println("advise: " + e);
        }

        String[] params3 = new String[]{id, "black"};
        assertDoesNotThrow(() -> {
            clientFacade.joinGame(params3);
        });
    }

    @Test
    public void joinGameBadIdTest(){
        String[] params = new String[]{"Willie", "Wonka", "itsASecret@wizbang.com"};

        assertDoesNotThrow(() -> {
            clientFacade.login(params);
        });

        assertThrows(DataAccessException.class, () -> {
            clientFacade.joinGame();
        });
    }

    @Test
    public void joinObserverTest(){
        String[] params = new String[]{"Willie", "Wonka", "itsASecret@wizbang.com"};

        assertDoesNotThrow(() -> {
            clientFacade.login(params);
        });

        String[] params2 = new String[]{"Willie's game"};
        String id = "";

        try{
            String joinString = clientFacade.createGame(params2);
            id = extractID(joinString);
        }catch(Exception e){
            System.out.println("advise: " + e);
        }

        String[] params3 = new String[]{id};
        assertDoesNotThrow(() -> {
            clientFacade.joinObserver(params3);
        });
    }

    @Test
    public void joinObserverNoIdTest(){
        assertThrows(DataAccessException.class, () -> {
            clientFacade.joinObserver();
        });
    }



}

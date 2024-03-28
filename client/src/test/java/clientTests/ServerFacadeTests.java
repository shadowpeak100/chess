package clientTests;

import org.junit.jupiter.api.*;
import server.Server;
import server.UnauthorizedException;
import ui.ChessClientFacade;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ServerFacadeTests {

    private static Server server;
    private static ChessClientFacade clientFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        clientFacade = new ChessClientFacade();

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
        clientFacade = new ChessClientFacade();
    }

    @Test
    public void login() {
        String[] params = new String[]{"Willie", "Wonka", "itsASecret@wizbang.com"};

        assertDoesNotThrow(() -> {
            clientFacade.login(params);
        });
    }

    @Test
    public void loginNonExistentUser() {
        String[] params = new String[]{"John", "The Spy", "shhh@shush.com"};

        assertThrows(UnauthorizedException.class, () -> {
            clientFacade.login(finalToken);
        });
    }

}

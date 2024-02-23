package server;

import dataAccess.*;
import org.eclipse.jetty.server.Authentication;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {

    private final UserService userService;
    private final ClearService clearService;
    private final GameService gameService;

    public Server(){
        GameDAO gd = new MemoryGameDAO();
        AuthDAO ad = new MemoryAuthDAO();
        UserDAO ud = new MemoryUsersDAO();
        this.clearService = new ClearService(gd, ad, ud);
        this.userService = new UserService(gd, ad, ud);
        this.gameService = new GameService(gd, ad, ud);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", clearService::clear);
        Spark.post("/user", userService::register);
        Spark.post("/session", userService::login);
        Spark.delete("/session", userService::logout);
        Spark.get("/game", gameService::listGames);
        Spark.post("/game", gameService::createGame);
        Spark.put("/game", gameService::joinGame);
        Spark.exception(DataAccessException.class, this::exceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(DataAccessException ex, Request req, Response res) {
        res.status(400);
        res.body("Invalid request");
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}

package server;

import com.google.gson.Gson;
import dataAccess.*;
import model.LoginDenial;
import model.LoginSuccess;
import model.UserData;
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
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", userService::logout);
        Spark.get("/game", gameService::listGames);
        Spark.post("/game", gameService::createGame);
        Spark.put("/game", gameService::joinGame);
        Spark.exception(DataAccessException.class, this::exceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object register(Request request, Response response){
        var usrData = new Gson().fromJson(request.body(), UserData.class);
        try{
            Object loginSuccess = userService.register(usrData);
            response.status(200);
            var serializer = new Gson();
            var json = serializer.toJson(loginSuccess);
            response.body(json);
            return json;
        }catch (TakenException e){
            response.status(403);
            return e.getMessage();
        }
    }

    private Object login(Request request, Response response){
        var usrData = new Gson().fromJson(request.body(), UserData.class);
        try{
            Object loginSuccess = userService.login(usrData);
            var serializer = new Gson();
            var json = serializer.toJson(loginSuccess);
            response.status(200);
            response.body(json);
            return json;
        }catch (UnauthorizedException e){
            response.status(401);
            return e.getMessage();
        }
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

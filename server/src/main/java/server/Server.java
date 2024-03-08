package server;

import com.google.gson.Gson;
import dataAccess.*;
import model.*;
import service.*;
import spark.*;

import javax.xml.crypto.Data;

public class Server {

    private final UserService userService;
    private final ClearService clearService;
    private final GameService gameService;

    public Server(){
        try{
            DatabaseManager.configureDatabase();
        }catch (DataAccessException e){
            System.out.println("Exception on creating database manager: " + e);
        }

        GameDAO gd = new SQLGameDAO();
        AuthDAO ad = new SQLAuthDAO();
        UserDAO ud = new SQLUsersDAO();
        this.clearService = new ClearService(gd, ad, ud);
        this.userService = new UserService(gd, ad, ud);
        this.gameService = new GameService(gd, ad, ud);
    }

    public int run(int desiredPort) {

        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", this::clear);
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.exception(DataAccessException.class, this::exceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object clear(Request request, Response response){
        try{
            clearService.clearAll();
            response.status(200);
            return "";
        }catch (ResponseException e){
            response.status(e.statusCode);
            return e.getMessage();
        }

    }

    private Object register(Request request, Response response){
        var usrData = new Gson().fromJson(request.body(), UserData.class);
        try{
            Object loginSuccess = userService.register(usrData);
            response.status(200);
            var serializer = new Gson();
            var json = serializer.toJson(loginSuccess);
            return json;
        }catch (TakenException | BadRequestException e){
            response.status(e.statusCode);
            return e.getMessage();
        }catch (DataAccessException e){
            response.status(500);
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
            return json;
        }catch (UnauthorizedException e){
            response.status(e.statusCode);
            return e.getMessage();
        } catch (DataAccessException e) {
            response.status(500);
            return e.toString();
        }
    }

    private Object logout(Request request, Response response){
        String authToken = request.headers("authorization");
        try{
            userService.logout(authToken);
            response.status(200);
            return "";
        }catch (UnauthorizedException e){
            response.status(e.statusCode);
            return e.getMessage();
        } catch (DataAccessException e) {
            response.status(500);
            return e.toString();
        }
    }

    private Object listGames(Request request, Response response){
        String authToken = request.headers("authorization");
        try{
            GamesWrapper gamesObject = gameService.listGames(authToken);
            var serializer = new Gson();
            var json = serializer.toJson(gamesObject);
            response.status(200);
            return json;
        }catch (UnauthorizedException e){
            response.status(e.statusCode);
            return e.getMessage();
        } catch (DataAccessException e) {
            response.status(500);
            return e.toString();
        }
    }

    private Object createGame(Request request, Response response){
        String authToken = request.headers("authorization");
        var gameData = new Gson().fromJson(request.body(), GameData.class);
        try{
            int id = gameService.createGame(authToken, gameData.getGameName());

            GameData game = new GameData();
            game.setGameID(id);
            var serializer = new Gson();
            var json = serializer.toJson(game);
            response.status(200);
            return json;
        }catch (UnauthorizedException | BadRequestException e){
            response.status(e.statusCode);
            return e.getMessage();
        } catch (DataAccessException e) {
            response.status(500);
            return e.toString();
        }
    }

    private Object joinGame(Request request, Response response){
        String authToken = request.headers("authorization");
        var gameData = new Gson().fromJson(request.body(), GameJoinRequest.class);
        try{
            gameService.joinGame(authToken, gameData.gameID, gameData.playerColor);
            response.status(200);
            return "";
        }catch (UnauthorizedException | BadRequestException | TakenException e){
            response.status(e.statusCode);
            return e.getMessage();
        } catch (DataAccessException e) {
            response.status(500);
            return e.toString();
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

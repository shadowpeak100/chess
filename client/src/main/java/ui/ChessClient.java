package ui;

import dataAccess.DataAccessException;
import model.LoginSuccess;
import model.UserData;
import server.BadRequestException;
import server.ResponseException;
import server.Server;
import server.UnauthorizedException;

import javax.xml.crypto.Data;
import java.util.Arrays;

public class ChessClient {
    private Server chessServer;
    public State state;

    public ChessClient(){
        chessServer = new Server();
        state = State.SIGNEDOUT;
    }

    public String eval(String input) throws ResponseException{
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "quit" -> quit();
                case "login" -> login(params);
                case "register" -> register(params);
                case "logout" -> logout();
                case "createGame" -> createGame(params);
                case "listGames" -> listGames();
                case "joinGame" -> joinGame(params);
                case "joinObserver" -> joinObserver(params);
                default -> help();
            };
        } catch (ResponseException | DataAccessException ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        if (state == State.SIGNEDOUT){
            return """
                    - help
                    - login <your username>
                    - quit
                    - register <username> <password> <email>
                    """;
        }else{
            return """
                    - help
                    - logout
                    - createGame <game name>
                    - listGames
                    - joinGame <game id> <team color>
                    - joinObserver <game id>
                    """;
        }
    }

    public String quit() throws ResponseException{
        return "";
    }

    public String login(String... params) throws ResponseException, DataAccessException {
        if (params.length >= 3){
            UserData usr = new UserData(params[0], params[1], params[2]);
            LoginSuccess user = chessServer.userService.login(usr);
            return "user: " + user.username + " was signed in successfully with auth token: " + user.authToken;
        }
        throw new DataAccessException("Data could not be accessed, incorrect parameter length");
    }

    public String register() throws ResponseException{
        return "";
    }

    public String logout() throws ResponseException{
        return "";
    }

    public String createGame() throws ResponseException{
        return "";
    }

    public String listGames() throws ResponseException{
        return "";
    }

    public String joinGame() throws ResponseException{
        return "";
    }

    public String joinObserver() throws ResponseException{
        return "";
    }


}

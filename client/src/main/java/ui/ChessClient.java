package ui;

import server.BadRequestException;
import server.ResponseException;
import server.Server;

import java.util.Arrays;

public class ChessClient {
    private Server chessServer;
    private State state;

    public ChessClient(){
        chessServer = new Server();
        state = State.SIGNEDOUT;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "help" -> help();
                case "quit" -> quit();
                case "login" -> login();
                case "register" -> register();
                case "logout" -> logout();
                case "createGame" -> createGame();
                case "listGames" -> listGames();
                case "joinGame" -> joinGame();
                case "joinObserver" -> joinObserver();
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String help() throws ResponseException {
        return "";
    }

    public String quit() throws ResponseException{
        return "";
    }

    public String login() throws ResponseException{
        return "";
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

package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import dataAccess.DataAccessException;
import model.GameData;
import model.LoginSuccess;
import model.UserData;
import server.ResponseException;
import server.Server;
import service.GamesWrapper;

import java.util.Arrays;

public class ChessClientFacade {
    private final Server chessServer;
    public State state;
    public String username;
    public String authToken;

    public ChessClientFacade(){
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
                case "create_game" -> createGame(params);
                case "list_games" -> listGames();
                case "join_game" -> joinGame(params);
                case "join_observer" -> joinObserver(params);
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
                    - login <your_username> <your_password>
                    - quit
                    - register <username> <password> <email>
                    """;
        }else if (state == State.SIGNEDIN){
            return """
                    - quit
                    - help
                    - logout
                    - create_game <game_name>
                    - list_games
                    - join_game <game_id> <team_color>
                    - join_observer <game_id>
                    """;
        }else{
            return """
                    - quit
                    - help
                    """;
        }
    }

    public String quit() throws ResponseException{
        return "quit";
    }

    public String login(String... params) throws ResponseException, DataAccessException {
        if (params.length >= 2){
            UserData usr = new UserData(params[0], params[1], "");
            LoginSuccess user = chessServer.userService.login(usr);
            username = user.username;
            authToken = user.authToken;
            state = State.SIGNEDIN;
            return "user: " + user.username + " was signed in successfully with auth token: " + user.authToken;
        }
        throw new DataAccessException("Data could not be accessed, incorrect parameter length");
    }

    public String register(String... params) throws ResponseException, DataAccessException {
        if (params.length >= 3){
            UserData usr = new UserData(params[0], params[1], params[2]);
            LoginSuccess user = chessServer.userService.register(usr);
            username = user.username;
            authToken = user.authToken;
            return "user: " + user.username + " has been registered";
        }
        throw new DataAccessException("Data could not be accessed, incorrect parameter length");
    }

    public String logout() throws ResponseException{
        if(state==State.INGAME || state==State.SIGNEDIN){
            state = State.SIGNEDOUT;
            username = null;
            authToken = null;
            return "user was logged out successfully";
        }
        throw new ResponseException(400, "user must be signed in to log out");
    }

    public String createGame(String... params) throws ResponseException, DataAccessException {
        if (params.length >= 1){
            int id = chessServer.gameService.createGame(authToken, params[0]);
            return "new chess game was created with id: " + id;
        }
        throw new DataAccessException("Data could not be accessed, incorrect parameter length");
    }

    public String listGames() throws ResponseException, DataAccessException {
        if (this.authToken != null && !this.authToken.isEmpty()){
            GamesWrapper gameList = chessServer.gameService.listGames(this.authToken);
            StringBuilder output = new StringBuilder();

            for (int i = 0; i < gameList.games.size(); i++) {
                GameData game = gameList.games.get(i);
                output.append("game name: " + game.getGameName() + " with game ID " + game.getGameID() + " white player " + game.getWhiteUsername() + " black player " + game.getBlackUsername() + "\n");
            }
            return output.toString();
        }
        throw new DataAccessException("Games could not be listed, user's auth token could not be found");
    }

    public String joinGame(String... params) throws ResponseException, DataAccessException {
        if (params.length >= 2) {
            int gameId;
            try {
                gameId = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                throw new DataAccessException("the game ID must be an int, string input could not be parsed to int");
            }
            chessServer.gameService.joinGame(this.authToken, gameId, params[1]);
            state = State.INGAME;

            String gamePrint = printGame(gameId, params[1]);
            return "game " + params[0] + " was joined successfully, here is the layout:\n" + gamePrint;
        }
        throw new DataAccessException("Data could not be accessed, incorrect parameter length");
    }

    public String joinObserver(String... params) throws ResponseException, DataAccessException {
        if (params.length >= 1) {
            int gameId;
            try {
                gameId = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                throw new DataAccessException("the game ID must be an int, string input could not be parsed to int");
            }
            chessServer.gameService.joinGame(this.authToken, gameId, "");
            state = State.INGAME;

            String gamePrint = printGame(gameId, "");
            return "game " + params[0] + " is now being observed, here is the game:\n" + gamePrint;
        }
        throw new DataAccessException("Data could not be accessed, incorrect parameter length");
    }

    public String printGame(int gameId, String teamColor) throws DataAccessException {
        ChessGame game = chessServer.gd.getGame(gameId).getGame();
        String spacer = "\u2001\u2005\u200A";
        String output = "";

        //todo: add something that checkers the board, if col & row are both even/odd, should be black, else white

        String sample = "a | R | K |   |   |   |   |   |   | a";

        //build in perspective of white at bottom
        if(teamColor.equalsIgnoreCase("white")) {
            String header = "     a     b     c     d     e     f     g     h\n";
            output = header;
            for (int i = 0; i < 8; i++) {
                output = output + (i+1) + " |";
                for (int j = 0; j < 8; j++) {
                    ChessPosition pos = new ChessPosition(i+1, j+1);
                    ChessPiece piece = game.getBoard().getPiece(pos);
                    String shape = getAppropriateCharacter(piece);
                    output = output + " " + shape + " |";
                }
                output = output + " " + (8-i) + "\n";
            }
            output = output + header;
        }else{
            //print black
            String header = "     h     g     f     e     d     c     b     a\n";
            output = header;

            for(int i = 0; i < 8; i++){
                output = output + (8-i) + " |";
                for(int j = 0; j < 8; j++){
                    ChessPosition pos = new ChessPosition(8-i, 8-j);
                    ChessPiece piece = game.getBoard().getPiece(pos);
                    String shape = getAppropriateCharacter(piece);
                    output = output + " " + shape + " |";
                }
                output = output + " " + (i+1) + "\n";
            }
            output = output + header;
        }
        return output;
    }

    private String getAppropriateCharacter(ChessPiece piece){
        if (piece == null){
            return "   ";
        }
        switch (piece.getPieceType()){
            case KING -> {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    return EscapeSequences.WHITE_KING;
                }else {
                    return EscapeSequences.BLACK_KING;
                }
            }
            case QUEEN -> {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    return EscapeSequences.WHITE_QUEEN;
                }else {
                    return EscapeSequences.BLACK_QUEEN;
                }
            }
            case BISHOP -> {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    return EscapeSequences.WHITE_BISHOP;
                }else {
                    return EscapeSequences.BLACK_BISHOP;
                }
            }
            case KNIGHT -> {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    return EscapeSequences.WHITE_KNIGHT;
                }else {
                    return EscapeSequences.BLACK_KNIGHT;
                }
            }
            case ROOK -> {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    return EscapeSequences.WHITE_ROOK;
                }else {
                    return EscapeSequences.BLACK_ROOK;
                }
            }
            case PAWN -> {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    return EscapeSequences.WHITE_PAWN;
                }else {
                    return EscapeSequences.BLACK_PAWN;
                }
            }
            default -> {
                return " ";
            }
        }
    }
}

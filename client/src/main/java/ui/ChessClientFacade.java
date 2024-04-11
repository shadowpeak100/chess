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
import websocket.NotificationHandler;
import websocket.WebSocketFacade;

import java.util.Arrays;

public class ChessClientFacade {
    private final Server chessServer;
    public State state;
    public String username;
    public String authToken;
    public ChessGame.TeamColor teamColor;
    public int GameID;
    private final String serverUrl;
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;


    public ChessClientFacade(String serverUrl, NotificationHandler notificationHandler){
        chessServer = new Server();
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
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
                case "redraw_board" -> redrawBoard();
                case "leave" -> leave();
                case "make_move" -> makeMove(params);
                case "resign" -> resign();
                case "highlight_legal_moves" -> highlightLegalMoves(params);
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
                    - redraw_board
                    - leave
                    - make_move <start_position (ex: b7)> <end_position (ex: d6)>
                    - resign
                    - highlight_legal_moves <piece_position (ex: a1)>
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
        //we want to open Web socket connection with server using /connect, to send/recieve messages
        //send a JOIN_PLAYER websocket message to server

        if (params.length >= 2) {
            try {
                GameID = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                throw new DataAccessException("the game ID must be an int, string input could not be parsed to int");
            }
            if (params[1].toUpperCase() == "WHITE"){
                teamColor = ChessGame.TeamColor.WHITE;
            }
            if (params[1].toUpperCase() == "BLACK"){
                teamColor = ChessGame.TeamColor.BLACK;
            }
            chessServer.gameService.joinGame(this.authToken, GameID, params[1]);
            state = State.INGAME;

            ws = new WebSocketFacade(serverUrl, notificationHandler);
            ws.joinGame(username, authToken, GameID, teamColor);

            String gamePrint = printGame(GameID, params[1]);
            return "game " + params[0] + " was joined successfully, here is the layout:\n" + gamePrint;
        }
        throw new DataAccessException("Data could not be accessed, incorrect parameter length");
    }

    public String joinObserver(String... params) throws ResponseException, DataAccessException {
        //we want to open Web socket connection with server using /connect, to send/recieve messages
        //send a JOIN_OBSERVER websocket message to server

        if (params.length >= 1) {
            try {
                GameID = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                throw new DataAccessException("the game ID must be an int, string input could not be parsed to int");
            }
            chessServer.gameService.joinGame(this.authToken, GameID, "");
            state = State.INGAME;
            ws = new WebSocketFacade(serverUrl, notificationHandler);
            ws.joinObserver(username, authToken, GameID);
            String gamePrint = printGame(GameID, "");
            return "game " + params[0] + " is now being observed, here is the game:\n" + gamePrint;
        }
        throw new DataAccessException("Data could not be accessed, incorrect parameter length");
    }

    public String redrawBoard()throws DataAccessException {
        String color;
        if(teamColor == ChessGame.TeamColor.WHITE){
            color = "white";
        }else if(teamColor == ChessGame.TeamColor.BLACK){
            color = "black";
        }else{
            color = "";
        }

        if (state == State.INGAME) {
            return printGame(GameID, color);
        }

        throw new DataAccessException("could not access game to print");
    }

    public String leave(){
        return "";
    }

    public String makeMove(String ... params){
        return "";
    }

    public String resign(){
        return "";
    }

    public String highlightLegalMoves(String ... params){
        return "";
    }

    public String printGame(int gameId, String teamColor) throws DataAccessException {
        ChessGame game = chessServer.gd.getGame(gameId).getGame();
        String spacer = "\u2001\u2005\u200A";
        String output;

        //build in perspective of white at bottom
        if(teamColor.equalsIgnoreCase("black")) {
            //print black
            String header = EscapeSequences.RESET_BG_COLOR + "     h     g     f     e     d     c     b     a\n";
            output = header;

            for(int i = 0; i < 8; i++){
                output = output + (8-i) + " |";
                for(int j = 0; j < 8; j++){
                    ChessPosition pos = new ChessPosition(8-i, 8-j);
                    ChessPiece piece = game.getBoard().getPiece(pos);
                    String shape = getAppropriateCharacter(piece);
                    if(i % 2 == 0 && j % 2 == 0){
                        output = output + EscapeSequences.SET_BG_COLOR_DARK_GREY +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                    }else if(i % 2 == 1 && j % 2 == 1){
                        output = output + EscapeSequences.SET_BG_COLOR_DARK_GREY +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                    }else{
                        output = output + EscapeSequences.SET_BG_COLOR_LIGHT_GREY +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                    }
                    //output = output + " " + shape + " |";
                }
                output = output + " " + (i+1) + "\n";
            }
            output = output + header;
        }else{
            //print white
            String header = EscapeSequences.RESET_BG_COLOR + "     a     b     c     d     e     f     g     h\n";
            output = header;
            for (int i = 0; i < 8; i++) {
                output = output + (i+1) + " |";
                for (int j = 0; j < 8; j++) {
                    ChessPosition pos = new ChessPosition(i+1, j+1);
                    ChessPiece piece = game.getBoard().getPiece(pos);
                    String shape = getAppropriateCharacter(piece);
                    if(i % 2 == 0 && j % 2 == 0){
                        output = output + EscapeSequences.SET_BG_COLOR_DARK_GREY +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                    }else if(i % 2 == 1 && j % 2 == 1){
                        output = output + EscapeSequences.SET_BG_COLOR_DARK_GREY +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                    }else{
                        output = output + EscapeSequences.SET_BG_COLOR_LIGHT_GREY +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                    }
                    //output = output + " " + shape + " |";
                }
                output = output + " " + (8-i) + "\n";
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

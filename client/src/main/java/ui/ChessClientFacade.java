package ui;

import chess.*;
import dataAccess.DataAccessException;
import model.GameData;
import model.LoginSuccess;
import model.UserData;
import server.ResponseException;
import server.Server;
import service.GamesWrapper;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;

import javax.swing.text.Position;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Scanner;

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
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    public String joinGame(String... params) throws Exception {
        //we want to open Web socket connection with server using /connect, to send/recieve messages
        //send a JOIN_PLAYER websocket message to server

        if (params.length >= 2) {
            try {
                GameID = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                throw new DataAccessException("the game ID must be an int, string input could not be parsed to int");
            }
            if (params[1].equalsIgnoreCase("white")){
                teamColor = ChessGame.TeamColor.WHITE;
            }
            if (params[1].equalsIgnoreCase("black")){
                teamColor = ChessGame.TeamColor.BLACK;
            }
            chessServer.gameService.joinGame(this.authToken, GameID, params[1]);
            state = State.INGAME;

            ws = new WebSocketFacade(serverUrl, notificationHandler);
            ws.joinGame(username, authToken, GameID, teamColor, chessServer.gd.getGame(GameID).getGame());

            String gamePrint = printGame(GameID, params[1], null);
            return "game " + params[0] + " was joined successfully, here is the layout:\n" + gamePrint;
        }
        throw new DataAccessException("Data could not be accessed, incorrect parameter length");
    }

    public String joinObserver(String... params) throws Exception {
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
            String gamePrint = printGame(GameID, "", null);
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
            return printGame(GameID, color, null);
        }

        throw new DataAccessException("could not access game to print");
    }

    public String leave() throws Exception {
        if(state == State.INGAME){
            state = State.SIGNEDIN;
            ws.leave(username, authToken, GameID);
            return username + " has left the game";
        }
        throw new DataAccessException("expected user to be in game, but found another state");
    }

    public String makeMove(String ... params) throws Exception {
        if(state == State.INGAME && params.length >= 2){
            ChessMove chessMove = chessMoveFromParams(params[0], params[1]);
            GameData game = chessServer.gd.getGame(GameID);

            if(game.getGame().getTeamTurn() != teamColor){
                throw new DataAccessException("making a move was not successful, it is not your turn or the game may be over");
            }

            game.getGame().makeMove(chessMove);
            chessServer.gd.updateGame(GameID, game);

            boolean check;
            boolean checkmate;
            boolean stalemate;
            if(teamColor == ChessGame.TeamColor.WHITE){
                stalemate = game.getGame().isInStalemate(ChessGame.TeamColor.BLACK);
                checkmate = game.getGame().isInCheckmate(ChessGame.TeamColor.BLACK);
                check = game.getGame().isInCheck(ChessGame.TeamColor.BLACK);
            }else{
                stalemate = game.getGame().isInStalemate(ChessGame.TeamColor.WHITE);
                checkmate = game.getGame().isInCheckmate(ChessGame.TeamColor.WHITE);
                check = game.getGame().isInCheck(ChessGame.TeamColor.WHITE);
            }
//            System.out.println("check: " + check + " checkmate: " + checkmate + " checkmatePre: " + checkmatePre + " stalemate: " + stalemate);

            if(!stalemate){
                checkmate = false;
            }

            //if checkmate/stalemate, no more moves!
            if(stalemate || checkmate){
                game.getGame().setTeamTurn(null);
                chessServer.gd.updateGame(GameID, game);
                check = false;
            }

            String opposingBoard;
            String ourBoard;
            if(teamColor == ChessGame.TeamColor.WHITE){
                opposingBoard = printGame(game.getGameID(), "black", null);
                ourBoard = printGame(game.getGameID(), "white", null);
            }else{
                opposingBoard = printGame(game.getGameID(), "white", null);
                ourBoard = printGame(game.getGameID(), "black", null);
            }

            ChessGame.TeamColor currentTurn = teamColor;
            ws.makeMove(username, authToken, GameID, chessMove, opposingBoard, check, checkmate, currentTurn);

            String advise = "";
            if(check){
                advise = "opponent is in check\n";
            }
            if(stalemate){
                advise = "stalemate reached, game over\n";
            }
            if(checkmate){
                advise = "opponent is in checkmate, you win!\n";
            }

            return username + " has moved from " + params[0] + " to " + params[1] + "\n" + advise + ourBoard;
        }
        throw new DataAccessException("making a move was not successful, parameter length provided was " + params.length);
    }

    public ChessMove chessMoveFromParams(String startString, String endString){
        int startRow; //should be a number
        int startCol; //alpha character
        int endRow;
        int endCol;
        if(startString.length() != 2 || endString.length() != 2){
            throw new RuntimeException("positions are not formatted correct, unable to make move.");
        }

        startCol = mapCharToNumber(startString.charAt(0));
        endCol = mapCharToNumber(endString.charAt(0));
        startRow = startString.charAt(1) - '0';
        endRow = endString.charAt(1) - '0';

        if(isInChessBounds(startCol) && isInChessBounds(startRow) && isInChessBounds(endCol) && isInChessBounds(endRow)){
            return new ChessMove(new ChessPosition(startRow, startCol), new ChessPosition(endRow, endCol), null);
        }
        throw new RuntimeException("positions must be 1-8 and a-h");
    }

    public boolean isInChessBounds(int i){
        if (i >= 1 && i <= 8){
            return true;
        }
        return false;
    }

    public int mapCharToNumber(char c) {
        c = Character.toLowerCase(c);
        return c - 'a' + 1;
    }

    public String resign() throws Exception {
        if(state == State.INGAME){
            GameData game = chessServer.gd.getGame(GameID);
            game.getGame().setTeamTurn(ChessGame.TeamColor.DONE);
            chessServer.gd.updateGame(GameID, game);
            GameData x = chessServer.gd.getGame(GameID);
            ws.resign(username, authToken, GameID);
            return "You have resigned, the game is over";
        }
        return "";
    }

    public String highlightLegalMoves(String ... params) throws DataAccessException {
        String startString = params[0];

        if(startString.length() != 2){
            throw new RuntimeException("positions are not formatted correct, unable to make move.");
        }

        int startCol = mapCharToNumber(startString.charAt(0));
        int startRow = startString.charAt(1) - '0';

        ChessPosition position = new ChessPosition(startRow, startCol);
        ChessGame game = chessServer.gd.getGame(GameID).getGame();
        Collection<ChessMove> validMoves = game.validMoves(position);

        if(teamColor == ChessGame.TeamColor.WHITE){
            return printGame(GameID, "white", validMoves);
        }else{
            return printGame(GameID, "black", validMoves);
        }
    }

    public boolean checkIfPositionIsValidMove(Collection<ChessMove> validMoves, ChessPosition pos){
        for (ChessMove move : validMoves) {
            if (move.getEndPosition().getRow() == pos.getRow() && move.getEndPosition().getColumn() == pos.getColumn()) {
                return true;
            }
        }
        return false;
    }

    public String printGame(int gameId, String teamColor, Collection<ChessMove> validMoves) throws DataAccessException {
        ChessGame game = chessServer.gd.getGame(gameId).getGame();
        String spacer = "\u2001\u2005\u200A";
        String output;

        //build in perspective of white at bottom
        if(teamColor.equalsIgnoreCase("black")) {
            //print black
            String header = EscapeSequences.RESET_BG_COLOR + "     h     g     f     e     d     c     b     a\n";
            output = header;

            for(int i = 0; i < 8; i++){
                output = output + (i+1) + " |";
                for(int j = 7; j >= 0; j--){
                    ChessPosition pos = new ChessPosition(i+1, j+1);

                    boolean validMove = false;
                    if(validMoves != null){
                        validMove = checkIfPositionIsValidMove(validMoves, pos);
                    }

                    ChessPiece piece = game.getBoard().getPiece(pos);
                    String shape = getAppropriateCharacter(piece);
                    if(i % 2 == 0 && j % 2 == 0){
                        if(validMove){
                            output = output + EscapeSequences.SET_BG_COLOR_DARK_GREEN +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                        }else{
                            output = output + EscapeSequences.SET_BG_COLOR_DARK_GREY +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                        }
                    }else if(i % 2 == 1 && j % 2 == 1){
                        if(validMove){
                            output = output + EscapeSequences.SET_BG_COLOR_DARK_GREEN +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                        }else{
                            output = output + EscapeSequences.SET_BG_COLOR_DARK_GREY +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                        }
                    }else{
                        if(validMove){
                            output = output + EscapeSequences.SET_BG_COLOR_GREEN +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                        }else{
                            output = output + EscapeSequences.SET_BG_COLOR_LIGHT_GREY +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                        }
                    }
                }
                output = output + " " + (i+1) + "\n";
            }
            output = output + header;
        }else{
            //print white
            String header = EscapeSequences.RESET_BG_COLOR + "     a     b     c     d     e     f     g     h\n";
            output = header;
            for (int i = 7; i >= 0; i--) {
                output = output + (i+1) + " |";
                for (int j = 0; j < 8; j++) {
                    ChessPosition pos = new ChessPosition(i+1, j+1);

                    boolean validMove = false;
                    if(validMoves != null){
                        validMove = checkIfPositionIsValidMove(validMoves, pos);
                    }

                    ChessPiece piece = game.getBoard().getPiece(pos);
                    String shape = getAppropriateCharacter(piece);
                    if(i % 2 == 0 && j % 2 == 0){
                        if(validMove){
                            output = output + EscapeSequences.SET_BG_COLOR_DARK_GREEN +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                        }else{
                            output = output + EscapeSequences.SET_BG_COLOR_DARK_GREY +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                        }
                    }else if(i % 2 == 1 && j % 2 == 1){
                        if(validMove){
                            output = output + EscapeSequences.SET_BG_COLOR_DARK_GREEN +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                        }else{
                            output = output + EscapeSequences.SET_BG_COLOR_DARK_GREY +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                        }
                    }else{
                        if(validMove){
                            output = output + EscapeSequences.SET_BG_COLOR_GREEN +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                        }else{
                            output = output + EscapeSequences.SET_BG_COLOR_LIGHT_GREY +  " " + shape + " " + EscapeSequences.RESET_BG_COLOR + "|";
                        }
                    }
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
                    return EscapeSequences.SET_TEXT_COLOR_GREEN + EscapeSequences.WHITE_KING + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }else {
                    return EscapeSequences.SET_TEXT_COLOR_BLUE +EscapeSequences.BLACK_KING + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }
            }
            case QUEEN -> {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    return EscapeSequences.SET_TEXT_COLOR_GREEN + EscapeSequences.WHITE_QUEEN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }else {
                    return EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.BLACK_QUEEN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }
            }
            case BISHOP -> {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    return EscapeSequences.SET_TEXT_COLOR_GREEN + EscapeSequences.WHITE_BISHOP + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }else {
                    return EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.BLACK_BISHOP + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }
            }
            case KNIGHT -> {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    return EscapeSequences.SET_TEXT_COLOR_GREEN + EscapeSequences.WHITE_KNIGHT + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }else {
                    return EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.BLACK_KNIGHT + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }
            }
            case ROOK -> {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    return EscapeSequences.SET_TEXT_COLOR_GREEN + EscapeSequences.WHITE_ROOK + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }else {
                    return EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.BLACK_ROOK + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }
            }
            case PAWN -> {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    return EscapeSequences.SET_TEXT_COLOR_GREEN + EscapeSequences.WHITE_PAWN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }else {
                    return EscapeSequences.SET_TEXT_COLOR_BLUE + EscapeSequences.BLACK_PAWN + EscapeSequences.SET_TEXT_COLOR_WHITE;
                }
            }
            default -> {
                return " ";
            }
        }
    }
}

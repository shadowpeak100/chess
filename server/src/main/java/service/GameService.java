package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import dataAccess.UserDAO;
import model.GameData;
import model.UserData;
import server.BadRequestException;
import server.TakenException;
import server.UnauthorizedException;

import java.util.Objects;

public class GameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    public GameService(GameDAO gd, AuthDAO ad, UserDAO ud) {
        this.gameDAO = gd;
        this.authDAO = ad;
        this.userDAO = ud;
    }

    public GamesWrapper listGames(String AuthToken) throws UnauthorizedException {
        String username = authDAO.getUsernameWithAuth(AuthToken);
        UserData user = userDAO.getUser(username);
        if(user == null){
            throw new UnauthorizedException();
        }else{
            //wrap this in a games object, with a variable that maps to the games list, called games
            return gameDAO.listGames();
        }
    }

    public int createGame(String AuthToken, String gameName) throws BadRequestException, UnauthorizedException {
        String username = authDAO.getUsernameWithAuth(AuthToken);
        if(username == null){
            throw new UnauthorizedException();
        }else{
            try{
                return gameDAO.newGame(gameName);
            }catch (DataAccessException e){
                throw new BadRequestException();
            }
        }
    }

    public void joinGame(String AuthToken, int gameID, String playerColor) throws UnauthorizedException, BadRequestException, TakenException {
        String username = authDAO.getUsernameWithAuth(AuthToken);
        if(username == null){
            throw new UnauthorizedException();
        }else{
            try{
                GameData game = gameDAO.getGame(gameID);
                if(Objects.equals(playerColor, "WHITE") || Objects.equals(playerColor, "BLACK") || Objects.equals(playerColor, "")){
                    if(playerColor.equals("WHITE")){
                        if(game.getWhiteUsername() != null){
                            throw new TakenException();
                        }
                        game.setWhiteUsername(username);
                    }else if (playerColor.equals("BLACK")){
                        if(game.getBlackUsername() != null){
                            throw new TakenException();
                        }
                        game.setBlackUsername(username);
                    }
                }else{
                    throw new BadRequestException();
                }
            }catch (DataAccessException e){
                throw new BadRequestException();
            }
        }

    }
}

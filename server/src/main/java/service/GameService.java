package service;

import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.UserDAO;
import spark.Request;
import spark.Response;

public class GameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    public GameService(GameDAO gd, AuthDAO ad, UserDAO ud) {
        this.gameDAO = gd;
        this.authDAO = ad;
        this.userDAO = ud;
    }

    public Object listGames(Request request, Response response) {
        return "";
    }

    public Object createGame(Request request, Response response) {
        return "";
    }

    public Object joinGame(Request request, Response response) {
        return "";
    }
}

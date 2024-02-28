package service;

import com.google.gson.Gson;
import dataAccess.*;
import model.LoginDenial;
import server.ResponseException;

public class ClearService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    public ClearService(GameDAO gd, AuthDAO ad, UserDAO ud) {
        this.gameDAO = gd;
        this.authDAO = ad;
        this.userDAO = ud;
    }

    public void clearAll() throws ResponseException {
        try{
            this.gameDAO.clearAll();
            this.authDAO.clearAll();
            this.userDAO.clearAll();
        }catch (DataAccessException e){
            throw new ResponseException(500, new Gson().toJson(new LoginDenial("Error: " + e.toString())));
        }
    }
}

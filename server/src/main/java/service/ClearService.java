package service;

import dataAccess.*;
import org.eclipse.jetty.server.Authentication;
import spark.Request;
import spark.Response;

public class ClearService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    public ClearService(GameDAO gd, AuthDAO ad, UserDAO ud) {
        this.gameDAO = gd;
        this.authDAO = ad;
        this.userDAO = ud;
    }

    public Object clear(Request request, Response response) {
        clearAll(response);
        response.status(200);
        return "";
    }

    public void clearAll(Response res){
        try{
            this.gameDAO.clear();
        }catch (DataAccessException e){
            //return if bad
            res.status(500);
            res.body(e.toString());
        }
    }
}

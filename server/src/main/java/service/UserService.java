package service;

import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.UserDAO;
import model.AuthData;
import model.LoginDenial;
import model.LoginSuccess;
import model.UserData;
import server.ResponseException;
import server.TakenException;
import server.UnauthorizedException;
import spark.Request;
import spark.Response;

import java.util.Objects;

public class UserService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    public UserService(GameDAO gd, AuthDAO ad, UserDAO ud) {
        this.gameDAO = gd;
        this.authDAO = ad;
        this.userDAO = ud;
    }

//    public record LoginDen(String message){
//
//    }

    public Object register(UserData usrData) throws TakenException {

        //see if user exists first
        UserData user = userDAO.getUser(usrData.getUsername());
        if(user == null){
            userDAO.createUser(usrData.getUsername(), usrData.getPassword(), usrData.getEmail());
            String auth = authDAO.createAuth(usrData.getUsername());

            return new LoginSuccess(usrData.getUsername(), auth);
        }else{
            throw new TakenException();
        }
    }

    public Object logout(Request request, Response response) {
        var usrData = new Gson().fromJson(request.body(), AuthData.class);

        String username = authDAO.getUsernameWithAuth(usrData.getAuthToken());
        if (username != null){
            //remove the auth token, the user is not deleted
            authDAO.deleteAuth(usrData.getAuthToken());
            response.status(200);
            return "";
        }else{
            response.status(401);
            var serializer = new Gson();
            LoginDenial l = new LoginDenial("Error: unauthorized");
            var json = serializer.toJson(l);
            response.body(json);
            return json;
        }
    }

    public Object login(UserData usrData) throws UnauthorizedException {
        UserData user = userDAO.getUser(usrData.getUsername());

        if(user == null || !Objects.equals(user.getPassword(), usrData.getPassword())){
            throw new UnauthorizedException();
        }else{
            String auth = authDAO.createAuth(user.getUsername());
            return new LoginSuccess(user.getUsername(), auth);
        }
    }
}

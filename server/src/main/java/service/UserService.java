package service;

import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.UserDAO;
import model.AuthData;
import model.LoginDenial;
import model.LoginSuccess;
import model.UserData;
import server.BadRequestException;
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

    public Object register(UserData usrData) throws TakenException, BadRequestException {
        if (usrData.getPassword() == ""){
            throw new BadRequestException();
        }
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

    public void logout(String authToken) throws UnauthorizedException{
        String username = authDAO.getUsernameWithAuth(authToken);
        if (username != null){
            //remove the auth token, the user is not deleted
            authDAO.deleteAuth(authToken);
        }else{
            throw new UnauthorizedException();
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

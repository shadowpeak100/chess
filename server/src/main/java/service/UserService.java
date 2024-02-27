package service;

import com.google.gson.Gson;
import dataAccess.AuthDAO;
import dataAccess.GameDAO;
import dataAccess.UserDAO;
import model.UserData;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class UserService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    public UserService(GameDAO gd, AuthDAO ad, UserDAO ud) {
        this.gameDAO = gd;
        this.authDAO = ad;
        this.userDAO = ud;
    }

    public Object register(Request request, Response response) {
        var usrData = new Gson().fromJson(request.body(), UserData.class);
        return "";
    }

    public Object logout(Request request, Response response) {
        var usrData = new Gson().fromJson(request.body(), UserData.class);
        return "";
    }

    public class loginSuccess{
        public String username;
        public String authToken;

        public loginSuccess(String Username, String AuthToken){
            this.username = Username;
            this.authToken = AuthToken;
        }
    }

    public Object login(Request request, Response response) {
        var usrData = new Gson().fromJson(request.body(), UserData.class);

        UserData user = userDAO.getUser(usrData.getUsername());

        if(!Objects.equals(user.getPassword(), usrData.getPassword())){
            //Throw exception?
            response.status(401);
            System.out.println("invalid password");
        }else{
            String auth = authDAO.createAuth(user.getUsername());
            loginSuccess l = new loginSuccess();
            l.setAuthToken(auth);
            l.setUsername(user.getUsername());

            var serializer = new Gson();
            var json = serializer.toJson(l);

            response.body(json);
            return json;
        }


        return "";
    }
}

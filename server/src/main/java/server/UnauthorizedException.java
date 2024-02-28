package server;

import com.google.gson.Gson;
import service.UserService;
import model.LoginDenial;

public class UnauthorizedException extends ResponseException {
    public UnauthorizedException() {
        super(401, new Gson().toJson(new LoginDenial("Error: unauthorized")));
    }
}

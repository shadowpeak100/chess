package server;

import com.google.gson.Gson;
import model.LoginDenial;
import service.UserService;

public class BadRequestException extends ResponseException{
    public BadRequestException() {
        super(400, new Gson().toJson(new LoginDenial("Error: bad request")));
    }
}



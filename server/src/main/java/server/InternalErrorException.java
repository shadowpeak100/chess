package server;

import com.google.gson.Gson;
import model.LoginDenial;

public class InternalErrorException extends ResponseException{
    public InternalErrorException() {
        super(500, new Gson().toJson(new LoginDenial("Internal Server Error")));
    }
}

package server;

import com.google.gson.Gson;
import service.UserService;
import model.LoginDenial;

public class TakenException extends ResponseException {
    public TakenException() {
        super(403, new Gson().toJson(new LoginDenial("Error: already taken")));
    }
}

package model;

public class LoginSuccess{
    public String username;
    public String authToken;

    public LoginSuccess(String username, String authToken){
        this.username = username;
        this.authToken = authToken;
    }
}

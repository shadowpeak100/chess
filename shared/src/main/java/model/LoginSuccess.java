package model;

public class LoginSuccess{
    public String username;
    public String authToken;

    public LoginSuccess(String Username, String AuthToken){
        this.username = Username;
        this.authToken = AuthToken;
    }
}

package model;

public class AuthData {
    private String username;
    private String authToken;

    //c'tors
    public AuthData(String username, String authToken) {
        this.authToken = authToken;
        this.username = username;
    }

    public AuthData(){
        this.authToken = "";
        this.username = "";
    }

    //getters

    public String getUsername(){
        return username;
    }
}

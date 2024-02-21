package model;

public class UserData {
    private String username;
    private String password;
    private String email;

    //c'tors
    UserData(String username, String password, String email){
        this.username = username;
        this.password = password;
        this.email = email;
    }

    UserData(){
        this.username = "";
        this.password = "";
        this.email = "";
    }

    //getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}

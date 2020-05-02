package sample;

public class User {
    private String fname;
    private String lname;
    private String login;
    private String password;
    private String token;

    public User(String fname, String lname, String login, String password) {
        this.fname = fname;
        this.lname = lname;
        this.login = login;
        this.password = password;
        this.token=null;
    }

    public String getFname() {
        return fname;
    }

    public String getToken() {
        return token;
    }

    public String getLname() {
        return lname;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void tokenResetter(){
        this.token=null;
    }

    public void generateToken() {
        this.token="token";
    }
}

package sample;

import java.util.Random;

public class User {
    private String fname;
    private String lname;
    private String login;
    private String password;
    private String token;

    public void setPassword(String password) {
        this.password = password;
    }

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
        int size=25;
        Random rnd = new Random();
        String generatedString="";
        for(int i = 0;i<size;i++) {
            int type=rnd.nextInt(4);
            switch (type) {
                case 0:
                    generatedString += (char) ((rnd.nextInt(26)) + 65);
                    break;
                case 1:
                    generatedString += (char) ((rnd.nextInt(10)) + 48);
                    break;
                default:
                    generatedString += (char) ((rnd.nextInt(26)) + 97);
            }
        }
        this.token=generatedString;
    }
}

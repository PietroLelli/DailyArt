package com.example.dailyart;

public class User {

    public String username;
    public String password;
    public String email;
    public String birthday;

    public User(){

    }

    public User(String username, String password, String email, String birthday) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.birthday = birthday;
    }
}

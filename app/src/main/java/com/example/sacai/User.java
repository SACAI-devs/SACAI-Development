package com.example.sacai;

public class User {

    public String email;
    public String password;
    public String userType;

    public User(){
    }

    public User(String email, String password, String userType) {
        this.email = email;
        this.password = password;
        this.userType = userType;
    }
}

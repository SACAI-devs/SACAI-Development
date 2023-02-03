package com.example.sacai.dataclasses;

public class User {

    public String email;

    public String userType;

    public User(){
    }

    public User(String email, String userType) {
        this.email = email;
        this.userType = userType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}

package com.example.sacai.dataclasses;

public class Commuter {

    private String firstname;
    private String lastname;
    private String email;
    private boolean mobility;
    private boolean auditory;
    private boolean wheelchair;
    private String username;

    public Commuter() {
    }

    public Commuter(String firstname, String lastname, String email, boolean mobility, boolean auditory, boolean wheelchair) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.mobility = mobility;
        this.auditory = auditory;
        this.wheelchair = wheelchair;
        this.username = firstname + lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isMobility() {
        return mobility;
    }

    public void setMobility(boolean mobility) {
        this.mobility = mobility;
    }

    public boolean isAuditory() {
        return auditory;
    }

    public void setAuditory(boolean auditory) {
        this.auditory = auditory;
    }

    public boolean isWheelchair() {
        return wheelchair;
    }

    public void setWheelchair(boolean wheelchair) {
        this.wheelchair = wheelchair;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

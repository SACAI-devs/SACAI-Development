package com.example.sacai.dataclasses;

public class Commuter {

    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private boolean mobility;
    private boolean auditory;
    private boolean wheelchair;
    private String homeAddress;
    private String workAddress;
    public Commuter() {
    }

    public Commuter(String firstname, String lastname, String email, String username, boolean mobility, boolean auditory, boolean wheelchair, String homeAddress, String workAddress) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.username = username;
        this.mobility = mobility;
        this.auditory = auditory;
        this.wheelchair = wheelchair;
        this.homeAddress = homeAddress;
        this.workAddress = workAddress;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getWorkAddress() {
        return workAddress;
    }

    public void setWorkAddress(String workAddress) {
        this.workAddress = workAddress;
    }
}

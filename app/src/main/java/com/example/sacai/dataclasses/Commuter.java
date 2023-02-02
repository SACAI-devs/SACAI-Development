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
    private String uid;
    private Trip current_ride;
    private Trip ride_history;

    public Commuter() {
    }

    public Commuter(String firstname, String lastname, String email, String username, boolean mobility, boolean auditory, boolean wheelchair, String homeAddress, String workAddress, String uid) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.username = username;
        this.mobility = mobility;
        this.auditory = auditory;
        this.wheelchair = wheelchair;
        this.homeAddress = homeAddress;
        this.workAddress = workAddress;
        this.uid = uid;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Trip getCurrent_ride() {
        return current_ride;
    }

    public void setCurrent_ride(Trip current_ride) {
        this.current_ride = current_ride;
    }

    public Trip getRide_history() {
        return ride_history;
    }

    public void setRide_history(Trip ride_history) {
        this.ride_history = ride_history;
    }
}

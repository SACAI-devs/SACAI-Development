package com.example.sacai.dataclasses;

public class Commuter_Shared {
    private String uid;
    private String username;
    private String mobile_impairment;
    private String auditory_impairment;
    private String origin;
    private String destination;

    public Commuter_Shared() {
    }

    public Commuter_Shared(String username, String mobile_impairment, String auditory_impairment, String origin, String destination) {

        this.username = username;
        this.mobile_impairment = mobile_impairment;
        this.auditory_impairment = auditory_impairment;
        this.origin = origin;
        this.destination = destination;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobile_impairment() {
        return mobile_impairment;
    }

    public void setMobile_impairment(String mobile_impairment) {
        this.mobile_impairment = mobile_impairment;
    }

    public String getAuditory_impairment() {
        return auditory_impairment;
    }

    public void setAuditory_impairment(String auditory_impairment) {
        this.auditory_impairment = auditory_impairment;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}

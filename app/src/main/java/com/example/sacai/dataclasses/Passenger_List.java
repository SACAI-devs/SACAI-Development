package com.example.sacai.dataclasses;

public class Passenger_List {
    private String id;          // ID of the commuter in the passengerlist
    private String username;    // Commuter username
    private String mobility;    // Commuter mobility needs
    private String auditory;    // Commuter auditory needs
    private String wheelchair;  // Commuter wheelchair needs
    private String origin;      // Commuter origin bus stop
    private String destination; // Commuter destination bus stop
    private String para_status; // Commuter if sasakay or bababa

    private Passenger_List () {
    }

    public Passenger_List(String id, String username, String mobility, String auditory, String wheelchair, String origin, String destination, String para_status) {
        this.id = id;
        this.username = username;
        this.mobility = mobility;
        this.auditory = auditory;
        this.wheelchair = wheelchair;
        this.origin = origin;
        this.destination = destination;
        this.para_status = para_status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobility() {
        return mobility;
    }

    public void setMobility(String mobility) {
        this.mobility = mobility;
    }

    public String getAuditory() {
        return auditory;
    }

    public void setAuditory(String auditory) {
        this.auditory = auditory;
    }

    public String getWheelchair() {
        return wheelchair;
    }

    public void setWheelchair(String wheelchair) {
        this.wheelchair = wheelchair;
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

    public String getPara_status() {
        return para_status;
    }

    public void setPara_status(String para_status) {
        this.para_status = para_status;
    }
}

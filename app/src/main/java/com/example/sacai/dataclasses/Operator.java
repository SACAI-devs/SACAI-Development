package com.example.sacai.dataclasses;

public class Operator {
    private String driver;
    private String conductor;
    private String franchise;
    private String plate;
    private boolean wheelchairCapacity;
    private String email;
    private String username;
    private String uid;
    public Operator() {
    }

    public Operator(String driver, String conductor, String franchise, String plate, boolean wheelchairCapacity, String email, String username, String uid) {
        this.driver = driver;
        this.conductor = conductor;
        this.franchise = franchise;
        this.plate = plate;
        this.wheelchairCapacity = wheelchairCapacity;
        this.email = email;
        this.username = username;
        this.uid = uid;
    }

    //====== SETTER AND GETTER FUNCTIONS ======//
    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getConductor() {
        return conductor;
    }

    public void setConductor(String conductor) {
        this.conductor = conductor;
    }

    public String getFranchise() {
        return franchise;
    }

    public void setFranchise(String franchise) {
        this.franchise = franchise;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public boolean isWheelchairCapacity() {
        return wheelchairCapacity;
    }

    public void setWheelchairCapacity(boolean wheelchairCapacity) {
        this.wheelchairCapacity = wheelchairCapacity;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

package com.example.sacai.dataclasses;

public class Operator {
    private String drivername;
    private String conductorname;
    private String franchise;
    private String plate;
    private boolean wheelchairCapacity;
    private String email;
    private String username;
    private String uid;
    public Operator() {
    }

    public Operator(String drivername, String conductorname, String franchise, String plate, boolean wheelchairCapacity, String email, String username, String uid) {
        this.drivername = drivername;
        this.conductorname = conductorname;
        this.franchise = franchise;
        this.plate = plate;
        this.wheelchairCapacity = wheelchairCapacity;
        this.email = email;
        this.username = username;
        this.uid = uid;
    }

    public String getDrivername() {
        return drivername;
    }

    public void setDrivername(String drivername) {
        this.drivername = drivername;
    }

    public String getConductorname() {
        return conductorname;
    }

    public void setConductorname(String conductorname) {
        this.conductorname = conductorname;
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

    public boolean getWheelchairCapacity() {
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

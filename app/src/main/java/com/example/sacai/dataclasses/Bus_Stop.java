package com.example.sacai.dataclasses;

public class Bus_Stop {
    private String busStopName;
    private String center_lat;
    private String center_long;
    private int radius;

    public Bus_Stop() {
    }

    public Bus_Stop(String busStopName, String center_lat, String center_long, int radius) {
        this.busStopName = busStopName;
        this.center_lat = center_lat;
        this.center_long = center_long;
        this.radius = radius;
    }

    public String getBusStopName() {
        return busStopName;
    }

    public void setBusStopName(String busStopName) {
        this.busStopName = busStopName;
    }

    public String getCenter_lat() {
        return center_lat;
    }

    public void setCenter_lat(String center_lat) {
        this.center_lat = center_lat;
    }

    public String getCenter_long() {
        return center_long;
    }

    public void setCenter_long(String center_long) {
        this.center_long = center_long;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}

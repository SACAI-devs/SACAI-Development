package com.example.sacai.dataclasses;

import java.util.ArrayList;

public class Operator_Trip {
    private String id;
    private String route_name;
    private String origin_stop;
    private String destination_stop;
    private String current_stop;
    private String current_lat;
    private String current_long;
    private ArrayList <String> passenger_list;
    private boolean seating_availability;

    public Operator_Trip() {
    }

    public Operator_Trip(String route_name) {
        this.route_name = route_name;
    }

    public Operator_Trip(String id, String route_name, String origin_stop, String destination_stop, String current_stop, String current_lat, String current_long, ArrayList<String> passenger_list, boolean seating_availability) {
        this.id = id;
        this.route_name = route_name;
        this.origin_stop = origin_stop;
        this.destination_stop = destination_stop;
        this.current_stop = current_stop;
        this.current_lat = current_lat;
        this.current_long = current_long;
        this.passenger_list = passenger_list;
        this.seating_availability = seating_availability;
    }

    public Operator_Trip(String route_name, boolean seating_availability) {
        this.route_name = route_name;
        this.seating_availability = seating_availability;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoute_name() {
        return route_name;
    }

    public void setRoute_name(String route_name) {
        this.route_name = route_name;
    }

    public String getOrigin_stop() {
        return origin_stop;
    }

    public void setOrigin_stop(String origin_stop) {
        this.origin_stop = origin_stop;
    }

    public String getDestination_stop() {
        return destination_stop;
    }

    public void setDestination_stop(String destination_stop) {
        this.destination_stop = destination_stop;
    }

    public String getCurrent_stop() {
        return current_stop;
    }

    public void setCurrent_stop(String current_stop) {
        this.current_stop = current_stop;
    }

    public ArrayList<String> getPassenger_list() {
        return passenger_list;
    }

    public void setPassenger_list(ArrayList<String> passenger_list) {
        this.passenger_list = passenger_list;
    }

    public String getCurrent_lat() {
        return current_lat;
    }

    public void setCurrent_lat(String current_lat) {
        this.current_lat = current_lat;
    }

    public String getCurrent_long() {
        return current_long;
    }

    public void setCurrent_long(String current_long) {
        this.current_long = current_long;
    }

    public boolean isSeating_availability() {
        return seating_availability;
    }

    public void setSeating_availability(boolean seating_availability) {
        this.seating_availability = seating_availability;
    }
}

package com.example.sacai.dataclasses;

public class Operator_Trip {
    private String id;
    private String route_name;
    private String origin_stop;
    private String destination_stop;
    private String current_stop;
    private String current_lat;
    private String current_long;
    private String passenger_list_id;

    public Operator_Trip() {
    }

    public Operator_Trip(String id, String route_name, String origin_stop, String destination_stop, String current_stop, String current_lat, String current_long, String passenger_list_id) {
        this.id = id;
        this.route_name = route_name;
        this.origin_stop = origin_stop;
        this.destination_stop = destination_stop;
        this.current_stop = current_stop;
        this.current_lat = current_lat;
        this.current_long = current_long;
        this.passenger_list_id = passenger_list_id;
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

    public String getPassenger_list_id() {
        return passenger_list_id;
    }

    public void setPassenger_list_id(String passenger_list_id) {
        this.passenger_list_id = passenger_list_id;
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
}

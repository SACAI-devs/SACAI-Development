package com.example.sacai.dataclasses;

public class Trip {
    // Important trip information
    private String id;              // uid of the trip
    private String date;            // date of the trip
    private String time_started;    // what time did the user embark a bus
    private String time_ended;      // what time did the user disembark the bus
    private String pickup_station;  // where did they embark
    private String dropoff_station; // where did they disembark
    private String operator_id;     // track the operator of the bus


    public Trip() {
    }

    public Trip(String date, String time_started, String time_ended, String pickup_station, String dropoff_station) {
        this.date = date;
        this.time_started = time_started;
        this.time_ended = time_ended;
        this.pickup_station = pickup_station;
        this.dropoff_station = dropoff_station;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime_started() {
        return time_started;
    }

    public void setTime_started(String time_started) {
        this.time_started = time_started;
    }

    public String getTime_ended() {
        return time_ended;
    }

    public void setTime_ended(String time_ended) {
        this.time_ended = time_ended;
    }

    public String getPickup_station() {
        return pickup_station;
    }

    public void setPickup_station(String pickup_station) {
        this.pickup_station = pickup_station;
    }

    public String getDropoff_station() {
        return dropoff_station;
    }

    public void setDropoff_station(String dropoff_station) {
        this.dropoff_station = dropoff_station;
    }

    public String getOperator_id() {
        return operator_id;
    }

    public void setOperator_id(String operator_id) {
        this.operator_id = operator_id;
    }
}

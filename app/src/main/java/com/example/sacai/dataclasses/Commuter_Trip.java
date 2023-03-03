package com.example.sacai.dataclasses;

public class Commuter_Trip {
    // Important trip information
    private String date;            // date of the trip
    private String time_started;    // what time did the user embark a bus
    private String time_ended;      // what time did the user disembark the bus
    private String origin_stop;  // where did they embark
    private String destination_stop; // where did they disembark
    private String operator_id;     // track the operator of the bus

    public Commuter_Trip() {
    }

    public Commuter_Trip(String date, String time_started, String origin_stop, String destination_stop) {
        this.date = date;
        this.time_started = time_started;
        this.origin_stop = origin_stop;
        this.destination_stop = destination_stop;
    }

    public Commuter_Trip(String date, String time_started, String time_ended, String origin_stop, String destination_stop, String operator_id) {
        this.date = date;
        this.time_started = time_started;
        this.time_ended = time_ended;
        this.origin_stop = origin_stop;
        this.destination_stop = destination_stop;
        this.operator_id = operator_id;
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
    public String getOperator_id() {
        return operator_id;
    }

    public void setOperator_id(String operator_id) {
        this.operator_id = operator_id;
    }
}

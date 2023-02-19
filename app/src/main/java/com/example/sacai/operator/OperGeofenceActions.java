package com.example.sacai.operator;

import android.util.Log;

public class OperGeofenceActions {

    public OperGeofenceActions() {
    }

    public void setOperatorVisibility() {
        String TAG = "setOperatorVisibility";
        Log.i("ClassCalled", "setOperatorVisibility: is running");
        // Get the operator's route information
        // Get the current geofence that it is in
        // Get operator geoposition

    }

    public void addOperatorInGeofence() {
        String TAG = "addOperatorInGeofence";
        Log.i("ClassCalled", "addOperatorInGeofence: is running");
        // Adds the operator into the current geofence it is in
    }

    public void removeOperatorVisibility() {
        String TAG = "removeOperatorVisibility";
        Log.i("ClassCalled", "removeOperatorVisibility: is running");
        // Removes operator visibility in specific geofence
    }
}

package com.example.sacai.operator;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.Log;

import com.example.sacai.commuter.CommGeofenceBroadcastReceiver;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

public class OperGeofenceHelper extends ContextWrapper {

    public OperGeofenceHelper(Context base) {
        super(base);
    }

    PendingIntent pendingIntent;

    // Function to request for a geofence
    public GeofencingRequest getGeofencingRequest(Geofence geofence) {
        Log.i("ClassCalled", "getGeofencingRequest: is running");
        // Returns the geofencing request

        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();

    }

    // Function to return the geofence
    public Geofence getGeofence(String ID, LatLng latLng, float radius, int transitionTypes) {
        Log.i("ClassCalled", "getGeofence: is running");

        // Return the built geofence
        return new Geofence.Builder()
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setRequestId(ID)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(5000)    // Time delay between determining whether you've entered a geofence to be considered DWELLING in the geofence
                .setExpirationDuration(60 * 10)
                .build();

    }

    // Function to return a pending intent
    @SuppressLint("UnspecifiedImmutableFlag")
    public PendingIntent getPendingIntent() {
        Log.i("ClassCalled", "getPendingIntent: is running");
        if (pendingIntent != null) {
            Log.i("getPendingIntent", "getPendingIntent: null");
            return pendingIntent;
        }

        // We can get the same pending intent for adding and removing geofences
        Intent intent = new Intent(this, OperatorBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 2607, intent, PendingIntent.FLAG_MUTABLE);
        return pendingIntent;
    }

    // Function to get the error string for when a geofence fails
    public String getErrorString(Exception e) {
        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            switch (apiException.getStatusCode()) {
                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    return "GEOFENCE NOT AVAILABLE";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    return  "TOO MANY GEOFENCES";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "TOO MANY PENDING INTENTS";
                case GeofenceStatusCodes.GEOFENCE_INSUFFICIENT_LOCATION_PERMISSION:
                    return "INSUFICIENT LOCATION PERMISSIONS";
                case GeofenceStatusCodes.GEOFENCE_REQUEST_TOO_FREQUENT:
                    return "GEOFENCE REQUEST TOO FREQUENT";
            }
        }
        return e.getLocalizedMessage();
    }
}

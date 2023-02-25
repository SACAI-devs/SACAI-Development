package com.example.sacai.commuter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.Commuter_in_Geofence;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CommGeofenceBroadcastReceiver extends BroadcastReceiver {
    CommGeofenceActions action = new CommGeofenceActions();
    String triggered_geofence;


    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String TAG = "onReceive";
        Log.i("ClassCall", "onReceive: is running");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        try {
            if (geofencingEvent.hasError()) {   // should not proceed if the geofencing event has an error
                Log.i(TAG, "onReceive: error receiving geofence event");
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "onReceive: ", e);
        }
        // List that holds triggered geofences
        List<Geofence> geofenceList =  geofencingEvent.getTriggeringGeofences();    // get which geofences where triggered

        for (Geofence geofence: geofenceList) {
            triggered_geofence = geofence.getRequestId();
            Log.i(TAG, "onReceive: geofence request id " + geofence.getRequestId());    // Request ID will be the name of the Bus Stop
         }

        int transitionType = geofencingEvent.getGeofenceTransition();
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.i(TAG, "onReceive: user entered the geofence");
                Toast.makeText(context, "You are now approaching bus stop .", Toast.LENGTH_SHORT).show();
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.i(TAG, "onReceive: user is in geofence");
                action.setCommuterVisibility();
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.i(TAG, "onReceive: user exited geofence");
                action.removeCommuterVisibility();
                Toast.makeText(context, "You are not within the range of a bus stop. Go to the nearest bus stop so operators in the area can be notified.", Toast.LENGTH_SHORT).show();
                break;
        }
    }


}
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
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();
    String triggered_geofence;
    boolean wheelchair_user;

    Commuter_in_Geofence commuter = new Commuter_in_Geofence();
    CommGeofenceActions action = new CommGeofenceActions();


    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String TAG = "onReceive";
        Log.i("ClassCall", "onReceive: is running");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        try {
            if (geofencingEvent.hasError()) {   // should not proceed if the geofencing event has an error
                Log.i(TAG, "onReceive: errror receiving geofence event");
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "onReceive: ", e);
        }
        List<Geofence> geofenceList =  geofencingEvent.getTriggeringGeofences();    // get which geofences where triggered

        for (Geofence geofence: geofenceList) {
            triggered_geofence = geofence.getRequestId();
            Log.i(TAG, "onReceive: " + geofence.getRequestId());
         }
//        Location location = geofencingEvent.getTriggeringLocation();    // get the location of a triggered geofence

        int transitionType = geofencingEvent.getGeofenceTransition();
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.i(TAG, "onReceive: user entered the geofence");
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.i(TAG, "onReceive: user is in geofence");

                Toast.makeText(context, "You are now approaching bus stop .", Toast.LENGTH_SHORT).show();

                action.setCommuterVisibility();
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.i(TAG, "onReceive: user exited geofence");

                action.removeCommuterVisibility();
                Toast.makeText(context, "You are not within the range of a bus stop. Go to the nearest bus stop so operators in the area can be notified.", Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.i(TAG, "onReceive: user is not in a geofence");
                Toast.makeText(context, "Please approach the bus station so we can alert operators in the area.", Toast.LENGTH_SHORT).show();
                break;
        }
    }


}
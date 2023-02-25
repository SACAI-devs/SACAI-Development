package com.example.sacai.operator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class OperatorBroadcastReceiver extends BroadcastReceiver {

    OperGeofenceActions action = new OperGeofenceActions();
    ArrayList<String> triggered_geofences = new ArrayList<>();


    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String TAG = "onReceive";
        Log.i("ClassCalled", "onReceive: is running");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        try {
            if (geofencingEvent.hasError()) {
                Log.i(TAG, "onReceive: error receiving geofence event");
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "onReceive: exception ", e);
        }


        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();

        for (Geofence geofence : geofenceList) {
            triggered_geofences.add(geofence.getRequestId());   // Request ID will be the name of the Bus Stop
            Log.i(TAG, "onReceive: geofence request id " + triggered_geofences);
        }

        int transitionType = geofencingEvent.getGeofenceTransition();
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.i(TAG, "onReceive: operator entered the geofence");
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.i(TAG, "onReceive: operator is in geofence");

                Toast.makeText(context, "You are now visible from bus stop", Toast.LENGTH_SHORT).show();
                action.setOperatorVisibility(triggered_geofences);
                Log.i(TAG, "onReceive: action passed.");
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.i(TAG, "onReceive: operator exited geofence");

                Toast.makeText(context, "You are not within the range of a bus stop.", Toast.LENGTH_SHORT).show();
                action.removeOperatorVisibility();
                break;
            default:
                Log.i(TAG, "onReceive: user is not in a geofence of a bus stop");
                break;
        }
    }
}
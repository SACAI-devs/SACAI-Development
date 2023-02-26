package com.example.sacai.operator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class OperatorBroadcastReceiver extends BroadcastReceiver {

    OperGeofenceActions action = new OperGeofenceActions();
    String triggered_geofences;


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
            triggered_geofences = geofence.getRequestId();   // Request ID will be the name of the Bus Stop
            Log.i(TAG, "onReceive: geofence request id " + triggered_geofences);
        }

        int transitionType = geofencingEvent.getGeofenceTransition();
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.i(TAG, "onReceive: user entered the geofence");
                Toast.makeText(context, "You are now approaching the following station/s" , Toast.LENGTH_SHORT).show();
                String gf;
                for (Geofence geofence : geofenceList) {
                    gf = geofence.getRequestId();
                    Toast.makeText(context, gf + " station", Toast.LENGTH_SHORT).show();
                }
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.i(TAG, "onReceive: user is in geofence");
                Log.i(TAG, "onReceive: triggered geofences " + triggered_geofences);

                action.updateCurrentStop(geofenceList, triggered_geofences);
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.i(TAG, "onReceive: user exited geofence");
                Toast.makeText(context, "You are not within the range of a bus stop.", Toast.LENGTH_SHORT).show();
                action.updateCurrentStop(geofenceList, "");
                break;
        }
    }
}
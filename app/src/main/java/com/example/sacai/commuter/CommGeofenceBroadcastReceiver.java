package com.example.sacai.commuter;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.sacai.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class CommGeofenceBroadcastReceiver extends BroadcastReceiver {
    CommGeofenceActions action = new CommGeofenceActions();
    String triggered_geofence;
    AlertDialog.Builder builder;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String TAG = "onReceive";
        Log.i("ClassCall", "onReceive: is running");

        builder = new AlertDialog.Builder(context);

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
                Toast.makeText(context, R.string.msg_you_are_now_within_range_of_a_bus_stop, Toast.LENGTH_SHORT).show();
                action.commuterEntersGeofence(triggered_geofence);
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.i(TAG, "onReceive: user is in geofence");
                action.commuterEntersGeofence(triggered_geofence);
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.i(TAG, "onReceive: user exited geofence");
                Toast.makeText(context, R.string.msg_you_are_not_within_range_of_a_bus_stop, Toast.LENGTH_SHORT).show();
                action.commuterLeavesGeofence(triggered_geofence);
                break;
        }
    }

    public void alertLeaveGeofence() {
        String TAG = "alertLeaveGeofence";
        Log.i("ClassCalled", "alertLeaveGeofence: is running");
        builder.setTitle("You are leaving a bus stop!")
                .setMessage("We can't inform Operators if you're too far from your origin bus stop.")
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(TAG, "onClick: user clicked okay");
                    }
                })
                .show();
    }


}
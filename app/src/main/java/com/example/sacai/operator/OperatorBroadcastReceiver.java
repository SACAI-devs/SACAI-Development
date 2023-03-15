package com.example.sacai.operator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.metrics.LogSessionId;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.sacai.R;
import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.Commuter_in_Geofence;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
                Toast.makeText(context, R.string.msg_you_are_now_within_range_of_a_bus_stop, Toast.LENGTH_SHORT).show();
                action.updateCurrentStop(geofenceList, triggered_geofences);
//                action.addOperatorInGeofence(triggered_geofences);
                showCommutersInGeofence(context);
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.i(TAG, "onReceive: user is in geofence");
                action.updateCurrentStop(geofenceList, triggered_geofences);
//                action.addOperatorInGeofence(triggered_geofences);
//                showCommutersInGeofence(context);
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.i(TAG, "onReceive: user exited geofence");
                Toast.makeText(context, R.string.msg_you_are_not_within_range_of_a_bus_stop, Toast.LENGTH_SHORT).show();
//                action.updateCurrentStop(triggered_geofences);
                action.removeOperatorStop(triggered_geofences);
                break;
        }

    }

    private void showCommutersInGeofence(Context context) {
        String TAG = "showCommutersInGeofence";
        Log.i(TAG, "showCommutersInGeofence: is running");
        DatabaseReference dbGeofence = FirebaseDatabase.getInstance().getReference(Commuter_in_Geofence.class.getSimpleName());
        dbGeofence.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Log.i(TAG, "onComplete: CHECKING FOR COMMUTERS IN THE GEOFENCE....");
                for (DataSnapshot dspCommuters : task.getResult().getChildren()) {
                    Log.i(TAG, "onDataChange: GETTING KEY...");
                    Log.i(TAG, "onDataChange: key " + dspCommuters.getKey());
                }
//                Toast.makeText(context, " " + dspCommuters Toast.LENGTH_SHORT).show();
            }
        });



    }
}
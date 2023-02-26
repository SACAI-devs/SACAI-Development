package com.example.sacai.operator;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.sacai.dataclasses.Operator;
import com.example.sacai.dataclasses.Operator_Trip;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class OperGeofenceActions {

    String current_trip_id;
    Handler handler = new Handler();
    Timer timer = new Timer();
    public OperGeofenceActions() {
    }

    public void updateCurrentStop(List<Geofence> geofenceList, String current_stop) {

        Log.i("Number of triggered geofences ", "updateCurrentStop: " +geofenceList.size());
        if (geofenceList.size() > 1) {
          updateStop(geofenceList.get(0).getRequestId());
          waitTime(current_stop);
        } else {
          updateStop(current_stop);
        }

    }

    //To start timer
    private void waitTime(String current_stop){
        String TAG = "waitTime";
        Log.i("ClassCalled", "waitTime: is running");
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run(){
                        Log.i(TAG, "run: waiting...");
                    }
                });
            }
        };
        timer.schedule(timerTask, 10000);
    }

    private void updateStop (String current_stop) {
        String TAG = "updateStop";
        Log.i("ClassCalled", "updateStop: is running");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get the operator's route information
        // Get the current geofence that it is in
        // Update current trip
        // Add current geofence to current_stop
        DatabaseReference db = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName()).child(user.getUid()).child("current_trip");
        Log.i(TAG, "setOperatorVisibility: db reference " + db);
        Log.i(TAG, "updateCurrentStop: geofence list overlap check");

        Operator_Trip trip = new Operator_Trip();
        db.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isComplete()) {
                    if (task.isSuccessful()) {
                        try {
                            for (DataSnapshot dsp : task.getResult().getChildren()) {
                                current_trip_id = dsp.getKey();
                                if (dsp.child("current_stop").getValue().toString().isEmpty()) {
                                    db.child(dsp.getKey()).child("current_stop").setValue(current_stop);
                                    Log.i(TAG, "onComplete: dsp.current_stop " + dsp.child("current_stop"));
                                } else {
                                    timer = new Timer();
                                    TimerTask timerTask = new TimerTask() {
                                        public void run() {
                                            handler.post(new Runnable() {
                                                public void run(){
                                                    Log.i(TAG, "run: waiting...");
                                                    db.child(dsp.getKey()).child("current_stop").setValue(current_stop);
                                                    Log.i(TAG, "onComplete: dsp.current_stop " + dsp.child("current_stop"));
                                                }
                                            });
                                        }
                                    };
                                    timer.schedule(timerTask, 300000);
                                }

                            }
                        } catch (Exception e) {
                            Log.e(TAG, "onComplete: exception ", e);
                        }

                    } else {
                        Log.i(TAG, "onComplete: not successful");
                    }
                } else {
                    Log.i(TAG, "onComplete: cannot be completed");
                }
            }
        });
    }
}

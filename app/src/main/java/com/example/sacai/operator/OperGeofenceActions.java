package com.example.sacai.operator;

import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.Operator;
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
            whenStopsOverlap(geofenceList.get(0).getRequestId());
            waitTime(current_stop);
        } else if (geofenceList.size() == 1){
            Log.i("Hello", "updateCurrentStop: we're not delaying this");
            updateStop(current_stop);
//            whenStopsOverlap(current_stop);
        }

    }

    private void updateStop(String current_stop) {
        String TAG = "updateStop";
        Log.i(TAG, "updateStop: ============================================");
        Log.i(TAG, "updateStop: is running...");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        DatabaseReference dbOperator = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName());
        dbOperator.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for (DataSnapshot dspOperator : task.getResult().getChildren()) {
                    Log.i(TAG, "onComplete: CHECKING CURRENT TRIP...");
                    Log.i(TAG, "onComplete: getKey " + dspOperator.getKey());
                    if (dspOperator.getKey().equals(uid)) {
                        Log.i(TAG, "onComplete: dspOperator.getCurrentrip " + dspOperator.child("current_trip"));
                        for (DataSnapshot dspCurrentTrip : dspOperator.child("current_trip").getChildren()) {
                            dbOperator.child(uid).child("current_trip").child(dspCurrentTrip.getKey()).child("current_stop").setValue(current_stop);
                        }
                    }

                }
            }
        });
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
        timer.schedule(timerTask, 20000);
    }

    private void whenStopsOverlap(String current_stop) {
        String TAG = "whenStopsOverlap";
        Log.i("ClassCalled", "whenStopsOverlap: is running");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName()).child(uid).child("current_trip");
        db.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                try {
                    for (DataSnapshot dspOperator : task.getResult().getChildren()) {
                        current_trip_id = dspOperator.getKey();
                        Log.i(TAG, "onComplete: CHECKING TRIP ID...");
                        Log.i(TAG, "onComplete: current_trip_id " + dspOperator.getKey());
                        try {
                            if (dspOperator.child("current_stop").exists()) {
                                Log.i(TAG, "run: WAITING...");
                                db.child(dspOperator.getKey()).child("current_stop").setValue(current_stop);
                                Log.i(TAG, "run: CHECKING VALUES...");
                                Log.i(TAG, "onComplete: dsp.current_stop " + dspOperator.child("current_stop"));
                                timer = new Timer();
                                TimerTask timerTask = new TimerTask() {
                                    public void run() {
                                        handler.post(new Runnable() {
                                            public void run(){

                                            }
                                        });
                                    }
                                };
                                timer.schedule(timerTask, 5000);

                            } else {
                                Log.i(TAG, "onComplete: CHECKING VALUES...");
                                Log.i(TAG, "onComplete: dsp.current_stop " + dspOperator.child("current_stop").getValue());
                                try {
                                    db.child(dspOperator.getKey()).child("current_stop").setValue(current_stop);
                                } catch (Exception e) {
                                    Log.e(TAG, "onComplete: exception ", e);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "onComplete: exception ", e);
                        }

                    }
                } catch (Exception e) {
                    Log.e(TAG, "onComplete: exception ", e);
                }
            }
        });

        DatabaseReference dbPassengers = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        dbPassengers.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for (DataSnapshot dspCommuters : task.getResult().getChildren()) {
                    Log.i("CHECK PASSENGERS", "getCurrentStop: dbPassengers.getKey(); " + dspCommuters.getKey());
                }
            }
        });
    }
}

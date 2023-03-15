package com.example.sacai.operator;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.Commuter_in_Geofence;
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

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();

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

    public void removeOperatorStop (String current_stop) {
        Log.i("ClassCalled", "removeCommuterVisibility: is running...");
        String TAG = "removeOperatorStop";

        DatabaseReference dbCurrentTrip = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName()).child(uid).child("current_trip");
        dbCurrentTrip.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                try {
                    for (DataSnapshot dspOperator : task.getResult().getChildren()) {
                        dbCurrentTrip.child(dspOperator.getKey()).child("current_stop").removeValue();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onComplete: exception ", e);
                }
            }
        });

    }

    boolean wheelchair;
    String operator_id;
    String route_name;
    String plate;
    public void addOperatorInGeofence(String current_stop) {
        String TAG = "addOperatorInGeofence";
        Log.i("ClassCalled", "addOperatorInGeofence: is running");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dbOperator;

        dbOperator = FirebaseDatabase.getInstance().getReference();
        Log.i(TAG, "addOperatorInGeofence: dbOPerator " + dbOperator);
        dbOperator.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for (DataSnapshot dsp : task.getResult().getChildren()) {
                    if (dsp.getKey().equals(user.getUid())) {
                        wheelchair = (boolean) dsp.child("wheelchairCapacity").getValue();
                        operator_id = user.getUid();
                        plate = dsp.child("plate").getValue().toString();
                        for (DataSnapshot dspCurrentTrip : dsp.child("current_trip").getChildren()) {
                            route_name = dspCurrentTrip.child("route_name").getValue().toString();
                        }
                    }
                }
                if (wheelchair) {
                    Log.i(TAG, "addCommuterData: COMMUTER HAS A WHEELCHAIR, ADDING TO GEOFENCE...");
                    dbOperator.child("Operator_in_Geofence").child(current_stop).child("has_wheelchair").child(user.getUid()).child("plate").setValue(plate);
                    dbOperator.child("Operator_in_Geofence").child(current_stop).child("has_wheelchair").child(user.getUid()).child("route").setValue(route_name);


                } else {
                    Log.i(TAG, "addCommuterData: Commuter HAS NO WHEELCHAIR, ADDING TO GEOFENCE...");
                    dbOperator.child("Operator_in_Geofence").child(current_stop).child("no_wheelchair").child(user.getUid()).child("plate").setValue(plate);
                    dbOperator.child("Operator_in_Geofence").child(current_stop).child("no_wheelchair").child(user.getUid()).child("route").setValue(route_name);
                }
                Log.i(TAG, "onComplete: plate " + plate);
                Log.i(TAG, "onComplete: plate " + route_name);
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

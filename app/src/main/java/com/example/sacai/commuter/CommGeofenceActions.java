package com.example.sacai.commuter;

import android.media.metrics.LogSessionId;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.Commuter_in_Geofence;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CommGeofenceActions {

    Commuter_in_Geofence commuter = new Commuter_in_Geofence();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();
    public CommGeofenceActions() {
    }

    // Function to set commuter visibility from the map
    public void commuterEntersGeofence(String current_stop) {
        Log.i("ClassCalled", "commuterEntersGeofence: is running");
        String TAG = "commuterEntersGeofence";

        // Get the reference for the current_trip of a user

        Log.i(TAG, "commuterEntersGeofence: ==========================================================");
        DatabaseReference dbCommuter = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        dbCommuter.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for (DataSnapshot dspDetails : task.getResult().getChildren()) {
                    if (dspDetails.getKey().equals(uid)) {
                        Log.i(TAG, "onComplete: GETTING CURRENT TRIP DETAILS...");
                        Log.i(TAG, "onComplete: current_trip " + dspDetails.child("current_trip").getValue());
                        String destination = "";
                        String trip_id = "";
                        for (DataSnapshot dspCurrentTrip : dspDetails.child("current_trip").getChildren()) {
                            Log.i(TAG, "onComplete: GETTING CURRENT_STOP...");
                            Log.i(TAG, "onComplete: current_stop " + dspCurrentTrip.child("current_stop").getValue());
                            Log.i(TAG, "onComplete: current_trip_id " + dspCurrentTrip.getKey());
                             trip_id = dspCurrentTrip.getKey();
                            destination = String.valueOf(dspCurrentTrip.child("destination_stop").getValue());
                            Log.i(TAG, "onComplete: UPDATING CURRENT_STOP...");
                            dbCommuter.child(uid).child("current_trip").child(trip_id).child("current_stop").setValue(current_stop);
                        }
                        Log.i(TAG, "onComplete: GETTING CURRENT_");
                        Log.i(TAG, "onComplete: wheelchair " + dspDetails.child("wheelchair").getValue());

                        boolean wheelchair = (boolean) dspDetails.child("wheelchair").getValue();
                        boolean mobility = (boolean) dspDetails.child("mobility").getValue();
                        boolean auditory = (boolean) dspDetails.child("auditory").getValue();
                        String username = String.valueOf(dspDetails.child("username").getValue());
                        Log.i(TAG, "onComplete: wheelchair variable " + wheelchair);
                        Log.i(TAG, "onComplete: geofence in parent function " + current_stop);
                        addCommuterData(current_stop, wheelchair, mobility, auditory, destination, username);
                    }
                }
            }
        });
//
    }

    public void addCommuterData(String current_stop, boolean wheelchair, boolean mobility, boolean auditory, String destination, String username) {
        String TAG = "addCommuterData";
        Log.i("ClassCalled", "addCommuterData: is running");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dbCommuter;

        //==== WHAT DO WE WANT FROM TO ADD INTO THE COMMUTER IN GEOFENCE? ====//
        // = commuter_uid
        // = commuter_username
        // = destination_stop_id
        // = current_stop_id
        // = wheelchairUser
        dbCommuter = FirebaseDatabase.getInstance().getReference();

        if (wheelchair) {
            Log.i(TAG, "addCommuterData: COMMUTER HAS A WHEELCHAIR, ADDING TO GEOFENCE...");

            dbCommuter.child("Commuter_In_Geofence").child(current_stop).child("has_wheelchair").child(uid).child("mobility").setValue(mobility);
            dbCommuter.child("Commuter_In_Geofence").child(current_stop).child("has_wheelchair").child(uid).child("auditory").setValue(auditory);
            dbCommuter.child("Commuter_In_Geofence").child(current_stop).child("has_wheelchair").child(uid).child("destination").setValue(destination);
            dbCommuter.child("Commuter_In_Geofence").child(current_stop).child("has_wheelchair").child(uid).child("username").setValue(username);

        } else {
            Log.i(TAG, "addCommuterData: Commuter HAS NO WHEELCHAIR, ADDING TO GEOFENCE...");
            dbCommuter.child("Commuter_In_Geofence").child(current_stop).child("no_wheelchair").child(uid).child("mobility").setValue(mobility);
            dbCommuter.child("Commuter_In_Geofence").child(current_stop).child("no_wheelchair").child(uid).child("auditory").setValue(auditory);
            dbCommuter.child("Commuter_In_Geofence").child(current_stop).child("no_wheelchair").child(uid).child("destination").setValue(destination);
            dbCommuter.child("Commuter_In_Geofence").child(current_stop).child("no_wheelchair").child(uid).child("username").setValue(username);
        }

    }

    // Function to remove commuter visibility on the map
    public void commuterLeavesGeofence() {
        Log.i("ClassCalled", "removeCommuterVisibility: is running...");
        String TAG = "commuterLeavesGeofence";

        DatabaseReference dbCurrentTrip = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName()).child(uid).child("current_trip");
        dbCurrentTrip.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                try {
                    for (DataSnapshot dspCommuter : task.getResult().getChildren()) {
                        dbCurrentTrip.child(dspCommuter.getKey()).child("current_stop").removeValue();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onComplete: exception ", e);
                }
            }
        });

    }

    public void deleteCommuterData() {
        String TAG = "deleteCommuterData";
        Log.i("ClassCalled", "deleteCommuterData: is running");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        // Get commuter information
        DatabaseReference dbCommuter = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName()).child(user.getUid()).child("current_trip");
        dbCommuter.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                boolean wheelchair = false;
                String origin = "";
                for (DataSnapshot dspCommuter : task.getResult().getChildren()){
                    try {
                        wheelchair = (boolean) dspCommuter.child("wheelchair").getValue();
                        origin = dspCommuter.child("origin_stop").getValue().toString();
                        DatabaseReference dbCommuterInGeofence = FirebaseDatabase.getInstance().getReference(Commuter_in_Geofence.class.getSimpleName()).child(origin);
                        // Deleting Commuter_In_Geofence record
                        if (wheelchair) {
                            dbCommuterInGeofence.child("has_wheelchair").child(user.getUid()).removeValue();
                            Log.i(TAG, "onComplete: removing wheelchair user from database... " + dbCommuterInGeofence);
                        } else {
                            dbCommuterInGeofence.child("no_wheelchair").child(user.getUid()).removeValue();
                            Log.i(TAG, "onComplete: removing non wheelchair user from database...");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onComplete: exception ", e);
                    }

                }
            }
        });
    }

}

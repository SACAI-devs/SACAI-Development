package com.example.sacai.commuter;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.Commuter_in_Geofence;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class CommGeofenceActions {

    String triggered_geofence;
    Commuter_in_Geofence commuter = new Commuter_in_Geofence();
    boolean wheelchair_user;

    public CommGeofenceActions() {
    }

    // Function to set commuter visibility from the map
    public void setCommuterVisibility() {

        Log.i("ClassCalled", "setCommuterVisibility: is running");
        String TAG = "setCommuterVisibility";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        // Get the user information
        // uid, username, impairments
        // Get the user's uid first

        // Get record of user
        DatabaseReference drUserInfo = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());

        // check if reference is correct
        Log.i(TAG, "setCommuterVisibility: dbReference " + drUserInfo);

        // Get user information
        drUserInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "onDataChange: looping through each record");
                for (DataSnapshot dsp : snapshot.getChildren()) {
                    if (uid.equals(dsp.getKey())){
                        try {
                            commuter.setUsername(String.valueOf(dsp.child("username").getValue()));
                            commuter.setMobile_impairment(String.valueOf(dsp.child("mobility").getValue()));
                            commuter.setAuditory_impairment(String.valueOf(dsp.child("auditory").getValue()));
                            wheelchair_user = (boolean) dsp.child("wheelchair").getValue();
                        } catch (Exception e) {
                            Log.i(TAG, "onDataChange: exception " + e);
                        }
                    }
                }

                Log.i(TAG, "onDataChange: commuter username" + commuter.getUsername());
                DatabaseReference drTripInformation = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName()).child(uid).child("current_trip");

                // check if reference is correct
                Log.i(TAG, "setCommuterVisibility: dbReference " + drTripInformation);

                drTripInformation.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i(TAG, "onDataChange: looping through records");
                        for (DataSnapshot dsp : snapshot.getChildren()) {

                            try{
                                commuter.setOrigin(String.valueOf(dsp.child("pickup_station").getValue()));
                                commuter.setDestination(String.valueOf(dsp.child("dropoff_station").getValue()));
                            } catch (Exception e) {
                                Log.i(TAG, "onDataChange: exception " + e);
                            }
                        }
                        addCommuterData();
                        return;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i(TAG, "onCancelled: database error " + error);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "onCancelled: database error " + error);
            }
        });
    }

    public void addCommuterData() {
        String TAG = "addCommuterData";
        Log.i("ClassCalled", "addCommuterData: is running");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        // Create hashmap object for injection
        HashMap Data = new HashMap();
        Data.put("uid", commuter.getUid());
        Data.put("username", commuter.getUsername());
        Data.put("origin", commuter.getOrigin());
        Data.put("destination", commuter.getDestination());
        Data.put("mobility", commuter.getMobile_impairment());
        Data.put("auditory", commuter.getAuditory_impairment());

        Log.i(TAG, "addCommuterData: username " + commuter.getUsername());
        Log.i(TAG, "addCommuterData: origin"  + commuter.getOrigin());
        // Check if user has wheelchair
        if (wheelchair_user) {
            Log.i(TAG, "addCommuterData: adding to has_wheelchair...");
            // Store into has_wheelchair node
            DatabaseReference drCommInGeofence = FirebaseDatabase.getInstance().getReference("Commuter_in_Geofence");
            Log.i(TAG, "addCommuterData: dbReference " + drCommInGeofence);
            drCommInGeofence.child(commuter.getOrigin()).child("has_wheelchair").child(uid).updateChildren(Data);
            Log.i(TAG, "addCommuterData: passed");
        } else {

            Log.i(TAG, "addCommuterData: adding to no_wheelchair");

            DatabaseReference drCommInGeofence = FirebaseDatabase.getInstance().getReference("Commuter_in_Geofence");
            Log.i(TAG, "addCommuterData: dbReference " + drCommInGeofence);
            drCommInGeofence.child(commuter.getOrigin()).child("no_wheelchair").child(uid).updateChildren(Data);
        }
    }

    // Function to remove commuter visibility on the map
    public void removeCommuterVisibility() {
        Log.i("ClassCalled", "removeCommuterVisibility: is running");
        String TAG = "removeCommuterVisibility";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        // Get the user information
        // uid, username, impairments
        // Get the user's uid first

        // Get database reference
        DatabaseReference drUserInfo = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());

        // check if reference is correct
        Log.i(TAG, "dbReference :" + drUserInfo);

        // Get user information
        drUserInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "onDataChange: looping through each record");
                for (DataSnapshot dsp : snapshot.getChildren()) {
                    if (uid.equals(dsp.getKey())){
                        try {
                            wheelchair_user = (boolean) dsp.child("wheelchair").getValue();
                        } catch (Exception e) {
                            Log.i(TAG, "onDataChange: exception " + e);
                        }
                    }
                }

                DatabaseReference drTripInformation = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName()).child(uid).child("current_trip");

                // check if reference is correct
                Log.i(TAG, "setCommuterVisibility: dbReference " + drTripInformation);

                drTripInformation.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i(TAG, "onDataChange: looping through records");
                        for (DataSnapshot dsp : snapshot.getChildren()) {
                            try {
                                commuter.setOrigin(String.valueOf(dsp.child("pickup_station").getValue()));
                                Log.i(TAG, "onDataChange: obtained commuter origin");
                            } catch (Exception e) {
                                Log.i(TAG, "onDataChange: exception " + e);
                            }
                        }
                        deleteCommuterData();
                        return;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i(TAG, "onCancelled: database error " + error);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "onCancelled: database error " + error);
            }
        });
    }

    public void deleteCommuterData() {
        String TAG = "deleteCommuterData";
        Log.i("ClassCalled", "deleteCommuterData: is running");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        Log.i(TAG, "deleteCommuterData: wheelchair " + wheelchair_user);
        Log.i(TAG, "deleteCommuterData: origin " + commuter.getOrigin());

        // Check if user has wheelchair
        if (wheelchair_user) {
            Log.i(TAG, "deleteCommuterData: deleting from has_wheelchair...");
            // Store into has_wheelchair node
            DatabaseReference drCommInGeofence = FirebaseDatabase.getInstance().getReference("Commuter_in_Geofence");
            Log.i(TAG, "deleteCommuterData: dbReference " + drCommInGeofence);

            drCommInGeofence.child(commuter.getOrigin()).child("has_wheelchair").child(uid).removeValue();
        } else {
            Log.i(TAG, "deleteCommuterData: deleting from no_wheelchair");
            DatabaseReference drCommInGeofence = FirebaseDatabase.getInstance().getReference("Commuter_in_Geofence");
            Log.i(TAG, "deleteCommuterData: dbReference " + drCommInGeofence);
            drCommInGeofence.child(commuter.getOrigin()).child("no_wheelchair").child(uid).removeValue();
        }
    }
}

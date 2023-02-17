package com.example.sacai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
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

public class CommuterGeofenceBroadcastReceiver extends BroadcastReceiver {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();
    Commuter_in_Geofence commuter = new Commuter_in_Geofence();



    boolean wheelchair_user;




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
            Log.i(TAG, "onReceive: " + geofence.getRequestId());
         }
//        Location location = geofencingEvent.getTriggeringLocation();    // get the location of a triggered geofence

        int transitionType = geofencingEvent.getGeofenceTransition();
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.i(TAG, "onReceive: user entered the geofence");
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.i(TAG, "onReceive: user is in geofence");

                Toast.makeText(context, "You are within the range of a bus stop. Operators in the same range will now be notified.", Toast.LENGTH_SHORT).show();
                setCommuterVisibility();
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.i(TAG, "onReceive: user exited geofence");

                removeCommuterVisibility();
                Toast.makeText(context, "You are not within the range of a bus stop. Go to the nearest bus stop so operators in the area can be notified.", Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.i(TAG, "onReceive: user is not in a geofence");
                Toast.makeText(context, "Please approach the bus station so we can alert operators in the area.", Toast.LENGTH_SHORT).show();
                break;
        }
//        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Function to set commuter visibility from the map
    private void setCommuterVisibility() {
        Log.i("ClassCalled", "setCommuterVisibility: is running");
        String TAG = "setCommuterVisibility";
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

    private void addCommuterData() {
        String TAG = "addCommuterData";
        Log.i("ClassCalled", "addCommuterData: is running");

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
    private void removeCommuterVisibility() {
        Log.i("ClassCalled", "removeCommuterVisibility: is running");
        String TAG = "removeCommuterVisibility";
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

    private void deleteCommuterData() {
        String TAG = "deleteCommuterData";
        Log.i("ClassCalled", "deleteCommuterData: is running");
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
package com.example.sacai.commuter;

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

    String triggered_geofence;
    Commuter_in_Geofence commuter = new Commuter_in_Geofence();
    boolean wheelchair_user;

    public CommGeofenceActions() {
    }

    // Function to set commuter visibility from the map
    public void commuterEntersGeofence(String current_stop) {
        Log.i("ClassCalled", "setCommuterVisibility: is running");
        String TAG = "setCommuterVisibility";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName()).child(uid).child("current_trip");
        db.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                try {
                    for (DataSnapshot dspCommuter : task.getResult().getChildren()) {
                        db.child(dspCommuter.getKey()).child("current_stop").setValue(current_stop);

                    }
                } catch (Exception e) {
                    Log.e(TAG, "onComplete: exception ", e);
                }
            }
        });
//
        // Get record of user
        DatabaseReference dbCommuter = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());

        // Check if the user has a scanned operator_id in their current_trip
        dbCommuter.child(uid).child("current_trip").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for (DataSnapshot dspCurrentTrip : task.getResult().getChildren()) {
                    try {
                        if (dspCurrentTrip.child("operator_id").exists()) {
                            Log.i(TAG, "onComplete: this commuter has already embarked on a bus");
                            Log.i(TAG, "onComplete: logging current_stop to current_trip...");
                            dbCommuter.child(uid).child("current_trip").child(dspCurrentTrip.getKey()).child("current_stop").setValue(triggered_geofence);
                        }

                        Log.i(TAG, "onComplete: this commuter is not embarked on a bus");
                        // Get user information
                        dbCommuter.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                try {
                                    for (DataSnapshot dspCurrentTrip : task.getResult().getChildren()) {
                                        if (uid.equals(dspCurrentTrip.getKey())) {
                                            commuter.setUsername(String.valueOf(dspCurrentTrip.child("username").getValue()));
                                            commuter.setMobile_impairment(String.valueOf(dspCurrentTrip.child("mobility").getValue()));
                                            commuter.setAuditory_impairment(String.valueOf(dspCurrentTrip.child("auditory").getValue()));
                                            wheelchair_user = (boolean) dspCurrentTrip.child("wheelchair").getValue();
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.i(TAG, "onDataChange: exception " + e);
                                }

                                // Getting origin and destinaiton from current_trip
                                DatabaseReference drTripInformation = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName()).child(uid).child("current_trip");
                                drTripInformation.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        try {
                                            for (DataSnapshot dsp : task.getResult().getChildren()) {
                                                commuter.setOrigin(dsp.child("origin_stop").getValue().toString());
                                                commuter.setDestination(dsp.child("destination_stop").getValue().toString());
                                            }
                                            addCommuterData(triggered_geofence);
                                            return;
                                        } catch (Exception e) {
                                            Log.e(TAG, "onDataChange: exception ", e);
                                        }
                                    }
                                });
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "onComplete: exception ", e);
                    }
                }
            }
        });
    }

    public void addCommuterData(String triggered_geofence) {
        String TAG = "addCommuterData";
        Log.i("ClassCalled", "addCommuterData: is running");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dbCommuter;

        if (wheelchair_user) {
            Log.i(TAG, "addCommuterData: Commuter HAS WHEELCHAIR");
            dbCommuter = FirebaseDatabase.getInstance().getReference(Commuter_in_Geofence.class.getSimpleName()).child(triggered_geofence).child("has_wheelchair").child(user.getUid());
            Log.i(TAG, "addCommuterData: adding commuter information to geofence record");
            dbCommuter.child("username").setValue(commuter.getUsername());
            dbCommuter.child("origin_stop").setValue(commuter.getOrigin());
            dbCommuter.child("destination_stop").setValue(commuter.getDestination());
            dbCommuter.child("mobility").setValue(commuter.getMobile_impairment());
            dbCommuter.child("auditory").setValue(commuter.getAuditory_impairment());
        } else {
            Log.i(TAG, "addCommuterData: Commuter HAS NO WHEELCHAIR");
            dbCommuter = FirebaseDatabase.getInstance().getReference();
//            dbCommuter = FirebaseDatabase.getInstance().getReference(Commuter_in_Geofence.class.getSimpleName()).child(triggered_geofence).child("no_wheelchair").child(user.getUid());
            Log.i(TAG, "addCommuterData: adding commuter information to geofence record");
            Log.i(TAG, "addCommuterData: CHECKING DATABASE REFERENCE...");
            Log.i(TAG, "addCommuterData: " + dbCommuter);
            dbCommuter.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    try {
                        Log.i(TAG, "addCommuterData: " + dbCommuter.child(Commuter_in_Geofence.class.getSimpleName()).child(triggered_geofence).child("no_wheelchair").child(user.getUid()).child("username"));
                        dbCommuter.child(Commuter_in_Geofence.class.getSimpleName()).child(triggered_geofence).child("no_wheelchair").child(user.getUid()).child("username").setValue(commuter.getUsername());
                        dbCommuter.child(Commuter_in_Geofence.class.getSimpleName()).child(triggered_geofence).child("no_wheelchair").child(user.getUid()).child("origin_stop").setValue(commuter.getOrigin());
                        dbCommuter.child(Commuter_in_Geofence.class.getSimpleName()).child(triggered_geofence).child("no_wheelchair").child(user.getUid()).child("destination_stop").setValue(commuter.getDestination());
                        dbCommuter.child(Commuter_in_Geofence.class.getSimpleName()).child(triggered_geofence).child("no_wheelchair").child(user.getUid()).child("mobility").setValue(commuter.getMobile_impairment());
                        dbCommuter.child(Commuter_in_Geofence.class.getSimpleName()).child(triggered_geofence).child("no_wheelchair").child(user.getUid()).child("auditory").setValue(commuter.getAuditory_impairment());
                    } catch (Exception e) {
                        Log.e(TAG, "onComplete: exception ", e);
                    }

                }
            });
        }

//        try {
//            Log.i(TAG, "addCommuterData: adding commuter information to geofence record");
//            dbCommuter.child("username").setValue(commuter.getUsername());
//            dbCommuter.child("origin_stop").setValue(commuter.getOrigin());
//            dbCommuter.child("destination_stop").setValue(commuter.getDestination());
//            dbCommuter.child("mobility").setValue(commuter.getMobile_impairment());
//            dbCommuter.child("auditory").setValue(commuter.getAuditory_impairment());
//        } catch (Exception e) {
//            Log.i(TAG, "addCommuterData: could not add commuter information to goefence record");
//            Log.e(TAG, "addCommuterData: exception ", e);
//        }

    }

    // Function to remove commuter visibility on the map
    public void commuterLeavesGeofence() {
        Log.i("ClassCalled", "removeCommuterVisibility: is running...");
        String TAG = "commuterLeavesGeofence";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName()).child(uid).child("current_trip");
        db.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                try {
                    for (DataSnapshot dspCommuter : task.getResult().getChildren()) {
                        db.child(dspCommuter.getKey()).child("current_stop").removeValue();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onComplete: exception ", e);
                }
            }
        });
        // check if commuter has an operator_id in their current_trip
//        DatabaseReference dbCommuter = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName()).child(uid).child("current_trip");
//        Log.i(TAG, "commuterLeavesGeofence: DB REFERENCE CHECKING...");
//        Log.i(TAG, "commuterLeavesGeofence: " + dbCommuter);
//        dbCommuter.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                Log.i(TAG, "onComplete: looping through records of a current_trip...");
//                for (DataSnapshot dspCurrentTrip : task.getResult().getChildren()) {
//                    Log.i(TAG, "onComplete: checking snapshot key...");
//                    Log.i(TAG, "onComplete: " + dspCurrentTrip.getKey());
//                    try {
//                        if (dspCurrentTrip.child("operator_id").exists()) {
//                            Log.i(TAG, "onComplete: this commuter has already embarked on a bus");
//                            Log.i(TAG, "onComplete: logging current_stop to current_trip...");
//                            dbCommuter.child(dspCurrentTrip.getKey()).child("current_stop").setValue(triggered_geofence);
//                        } else {
//                            Log.i(TAG, "onComplete: this commuter is not embarked on a bus");
//
//                            // get origin stop of the commuter
//                            Log.i(TAG, "onComplete: removing commuter form geofence...");
//                            dbCommuter.child(dspCurrentTrip.getKey()).child("origin_stop").removeValue();
//                            deleteCommuterData();
//                        }
//                    } catch (Exception e) {
//                        Log.e(TAG, "onComplete: exception ", e);
//                    }
//                }
//            }
//
//        });
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

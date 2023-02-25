package com.example.sacai.operator;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.sacai.dataclasses.Operator;
import com.example.sacai.dataclasses.Operator_Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class OperGeofenceActions {

    public OperGeofenceActions() {
    }

    public void setOperatorVisibility(ArrayList<String> current_stop) {
        String TAG = "setOperatorVisibility";
        Log.i("ClassCalled", "setOperatorVisibility: is running");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        // Get the operator's route information
        // Get the current geofence that it is in
        // Update current trip
        // Add current geofence to current_stop
        DatabaseReference db = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName()).child(user.getUid()).child("current_trip");
        Log.i(TAG, "setOperatorVisibility: db reference " + db);


        Operator_Trip trip = new Operator_Trip();
        db.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isComplete()) {
                    if (task.isSuccessful()) {
                        for (DataSnapshot dsp : task.getResult().getChildren()) {
                            Log.i(TAG, "onComplete: dsp.getkey " + dsp.getKey());
                            for (int i = 0; i < current_stop.size(); i++) {
                                Log.i(TAG, "onComplete: current stop array item " + current_stop.get(i));
                                db.child(dsp.getKey()).child("current_stop").setValue(current_stop.get(i));
                                Log.i(TAG, "onComplete: db reference VERIFY " + db.child(dsp.getKey()).child("current_stop"));
                            }
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

    public void addOperatorInGeofence() {
        String TAG = "addOperatorInGeofence";
        Log.i("ClassCalled", "addOperatorInGeofence: is running");
        // Adds the operator into the current geofence it is in
    }

    public void removeOperatorVisibility() {
        String TAG = "removeOperatorVisibility";
        Log.i("ClassCalled", "removeOperatorVisibility: is running");
        // Removes operator visibility in specific geofence
    }
}

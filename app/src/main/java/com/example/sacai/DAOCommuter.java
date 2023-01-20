package com.example.sacai;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class DAOCommuter {
    private DatabaseReference databaseReference;

    public DAOCommuter() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(Commuter.class.getSimpleName());
    }

    public boolean add(Commuter commuter) {
        if ((databaseReference.child(commuter.getUsername()).setValue(commuter)).isSuccessful()){
            return true;
        } else {
            return false;
        }
    }
}

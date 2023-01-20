package com.example.sacai.dao;

import com.example.sacai.dataclasses.Commuter;
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

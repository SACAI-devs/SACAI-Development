package com.example.sacai.dao;

import com.example.sacai.dataclasses.Operator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DAOOperator {
    private DatabaseReference databaseReference;

    public DAOOperator() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(Operator.class.getSimpleName());
    }

    public boolean add(Operator operator) {
        if ((databaseReference.child(operator.getUsername()).setValue(operator)).isSuccessful()){
            return true;
        } else {
            return false;
        }
    }
}

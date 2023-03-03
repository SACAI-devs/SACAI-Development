package com.example.sacai.commuter.dao;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.sacai.dataclasses.Commuter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class DAOCommuter {
    private DatabaseReference databaseReference;

    public DAOCommuter() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(Commuter.class.getSimpleName());
    }

//    public boolean add(Commuter commuter) {
//        if ((databaseReference.child(commuter.getUid()).setValue(commuter)).isSuccessful()){
//            return true;
//        } else {
//            return false;
//        }
//    }
}

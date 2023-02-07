package com.example.sacai.commuter.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sacai.R;
import com.example.sacai.databinding.FragmentCommRideHistoryBinding;
import com.example.sacai.dataclasses.Commuter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CommRideHistoryFrag extends Fragment {

    // Bind fragment to layout
    FragmentCommRideHistoryBinding binding;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCommRideHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Read user's ride history and display
        readData(currentUser.getUid());
    }

    private void readData(String uid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        databaseReference.child(uid).child("ride_history").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        // Get each ride information node
                        
                    }
                }
            }
        });
    }
}
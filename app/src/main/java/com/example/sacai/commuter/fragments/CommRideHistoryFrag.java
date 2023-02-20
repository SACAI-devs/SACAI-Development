package com.example.sacai.commuter.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sacai.R;
import com.example.sacai.commuter.adapter.CommRideHistoryAdapter;
import com.example.sacai.databinding.FragmentCommRideHistoryBinding;
import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.Commuter_Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CommRideHistoryFrag extends Fragment {

    // Bind fragment to layout
    FragmentCommRideHistoryBinding binding;
    RecyclerView recyclerView;
    RecyclerView.Adapter rideHistoryAdapter;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<Commuter_Trip> commuterTrip;

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
        recyclerView = view.findViewById(R.id.rideHistoryList);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        commuterTrip = new ArrayList<>();

        // Initialize Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Read user's ride history and display
        readData(currentUser.getUid());
    }

    private void readData(String uid) {
        String TAG = "readData";
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        databaseReference.child(uid).child("ride_history").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String id;
                String date;
                String time_started;
                String time_ended;
                String origin;
                String destination;
                String operatorid;
                commuterTrip = new ArrayList<Commuter_Trip>();
                for (DataSnapshot dsp: snapshot.getChildren()) {
                    try {
                        id = "Trip Tracking ID " + dsp.getKey();
                        date = String.valueOf(dsp.child("date").getValue());
                        time_started = "Started: " + dsp.child("time_started").getValue();
                        time_ended = "Ended: " + dsp.child("time_ended").getValue();
                        origin = String.valueOf(dsp.child("pickup_station").getValue());
                        destination = String.valueOf(dsp.child("dropoff_station").getValue());
                        operatorid = String.valueOf(dsp.child("operator_id").getValue());
                        commuterTrip.add(new Commuter_Trip(id, date, time_started, time_ended, origin, destination, operatorid));
                        rideHistoryAdapter = new CommRideHistoryAdapter(getActivity(), commuterTrip);
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: exception ", e);
                    }


                }
                recyclerView.setAdapter(rideHistoryAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "onCancelled: database error " + error);
            }
        });
    }
}
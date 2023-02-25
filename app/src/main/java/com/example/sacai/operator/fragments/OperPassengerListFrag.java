package com.example.sacai.operator.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sacai.R;
import com.example.sacai.databinding.FragmentOperPassengerListBinding;
import com.example.sacai.dataclasses.Commuter_in_Geofence;
import com.example.sacai.dataclasses.Operator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OperPassengerListFrag extends Fragment {

    // Bind fragment to layout
    FragmentOperPassengerListBinding binding;
    RecyclerView recyclerView;
    RecyclerView.Adapter passengerListAdapter;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<Commuter_in_Geofence> passenger;   // Commuter's information as a passenger


    public OperPassengerListFrag() {
        // Required empty public constructor
    }






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_oper_passenger_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerPassengers);
        recyclerView.setHasFixedSize(true);;

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        passenger = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        // Read operator's data and display
        readData(uid);
    }

    // Function to read data from operator
    private void readData(String uid){
        String TAG = "readData";
        Log.i("ClassCalled", "readData: is running");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName());
        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String id;          // ID of the commuter in the passengerlist
                String username;    // Commuter username
                String mobility;    // Commuter mobility needs
                String auditory;    // Commuter auditory needs
                String wheelchair;  // Commuter wheelchair needs    // TODO: Determine if this is necessary to add
                String origin;      // Commuter origin bus stop
                String destination; // Commuter destination bus stop
                passenger = new ArrayList<>();

                for (DataSnapshot dsp : snapshot.getChildren()) {
                    try {
                        id = "Trip Tracking ID: " + dsp.getKey();
                        username = "Username: " + dsp.child("username");
//                        origin = dsp.child("pickup_station")
                        // TODO Add adapter

                        Log.i(TAG, "onDataChange: id " + id);
                        Log.i(TAG, "onDataChange: date " + id);
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: exception ", e);
                    }
                }
                recyclerView.setAdapter(passengerListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "onCancelled: database error " + error);
            }
        });
    }
}
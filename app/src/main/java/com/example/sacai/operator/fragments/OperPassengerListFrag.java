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
import android.widget.Toast;

import com.example.sacai.R;
import com.example.sacai.databinding.FragmentOperPassengerListBinding;
import com.example.sacai.dataclasses.Commuter_in_Geofence;
import com.example.sacai.dataclasses.Operator;
import com.example.sacai.dataclasses.Passenger_List;
import com.example.sacai.operator.adapter.PassengerListAdapter;
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

    ArrayList<Passenger_List> passenger;   // Commuter's information as a passenger

    private String id;          // ID of the commuter in the passengerlist
    private String username;    // Commuter username
    private String mobility;    // Commuter mobility needs
    private String auditory;    // Commuter auditory needs
    private String wheelchair;  // Commuter wheelchair needs    // TODO: Determine if this is necessary to add
    private String origin;      // Commuter origin bus stop
    private String destination; // Commuter destination bus stop
    private String para_status; // Commuter if sasakay or bababa

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

        recyclerView = view.findViewById(R.id.passengerList);
        recyclerView.setHasFixedSize(true);

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

        DatabaseReference dbOperator = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName()).child(uid).child("current_trip");
        dbOperator.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                passenger = new ArrayList<>();

                for (DataSnapshot dspCurrentTrip : snapshot.getChildren()) {
                    Log.i(TAG, "onDataChange: CHECKING VALUES...");
                    Log.i(TAG, "onDataChange: dspCurrentTrip.getKey " + dspCurrentTrip.getKey());
                    Log.i(TAG, "onDataChange: CHECKING VALUES...");
                    Log.i(TAG, "onDataChange: dspCurrentTrip.child().getChildren " + dspCurrentTrip.child("passenger_list").getChildrenCount());
                    try {
                        if (dspCurrentTrip.child("passenger_list").exists()) {
                            for (DataSnapshot dspPassengers : dspCurrentTrip.child("passenger_list").getChildren()) {
                                Log.i(TAG, "onDataChange: CHECKING VALUES...");
                                Log.i(TAG, "onDataChange: passenger.getkey " + dspPassengers.getKey());
                                id = dspPassengers.getKey();
                                username = dspPassengers.child("username").getValue().toString();
                                auditory = dspPassengers.child("auditory").getValue().toString();
                                mobility = dspPassengers.child("mobility").getValue().toString();
                                wheelchair = dspPassengers.child("wheelchair").getValue().toString();
                                origin = dspPassengers.child("origin").getValue().toString();
                                destination = dspPassengers.child("destination").getValue().toString();
                                if (dspPassengers.child("para").getValue().equals("true")) {
                                    para_status = "PARA! Bababa.";
                                    Toast.makeText(getActivity(), username + getString(R.string.msg_para_bababa), Toast.LENGTH_SHORT).show();
                                } else {
                                    para_status = "Waiting...";
                                }

                                passenger.add(new Passenger_List(id, username, auditory, mobility, wheelchair, origin, destination, para_status));
                                passengerListAdapter = new PassengerListAdapter(getActivity(), passenger);
                            }
                        }
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
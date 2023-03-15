package com.example.sacai.commuter.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sacai.R;
import com.example.sacai.commuter.adapter.CommRideHistoryAdapter;
import com.example.sacai.databinding.FragmentCommRideHistoryBinding;
import com.example.sacai.dataclasses.Bus_Stop;
import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.Commuter_Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class CommRideHistoryFrag extends Fragment {

    // Bind fragment to layout
    FragmentCommRideHistoryBinding binding;
    RecyclerView recyclerView;
    RecyclerView.Adapter rideHistoryAdapter;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<Commuter_Trip> commuterTrip;
    ArrayList<String> stopName = new ArrayList<>();
    ArrayList<String> stopId = new ArrayList<>();
    String origin_stop;
    String destination_stop;
    String id;
    String date;
    String time_started;
    String time_ended;
    String operatorid;

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
        getStations();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        databaseReference.child(uid).child("ride_history").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                commuterTrip = new ArrayList<Commuter_Trip>();

                for (DataSnapshot dsp: snapshot.getChildren()) {
                    try {
                        id = "Trip Tracking ID " + dsp.getKey();
                        date = String.valueOf(dsp.child("date").getValue());
                        time_started = "Started: " + dsp.child("time_started").getValue();
                        time_ended= "Ended: " + dsp.child("time_ended").getValue();
                        operatorid = "Operator ID: \n" + dsp.child("operator_id").getValue().toString();

                        for (int i = 0; i < stopName.size(); i++) {
                            if (stopId.get(i).equals(dsp.child("origin").getValue().toString())) {
                                origin_stop = stopName.get(i);
                            }
                            if (stopId.get(i).equals(dsp.child("destination").getValue().toString())) {
                                destination_stop = stopName.get(i);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: exception ", e);
                    }
                    commuterTrip.add(new Commuter_Trip(date, time_started, time_ended, origin_stop, destination_stop, operatorid, id));
                    rideHistoryAdapter = new CommRideHistoryAdapter(getActivity(), commuterTrip);
                }
                recyclerView.setAdapter(rideHistoryAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "onCancelled: database error " + error);
            }
        });
    }

    private void getStations() {
        Log.i("ClassCalled", "getStations is running");

        // This method gets the stations registered from Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bus_Stop");

        // Get bus stops
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                stopName.clear();
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    // Get data from each node
                    String id = dsp.getKey();
                    String name = Objects.requireNonNull(dsp.child("busStopName").getValue()).toString();
                    Double lat = Double.parseDouble(Objects.requireNonNull(dsp.child("center_lat").getValue()).toString());
                    Double lon = Double.parseDouble(Objects.requireNonNull(dsp.child("center_long").getValue()).toString());
                    Log.i("CHECK VALUE", "onDataChange: " + name);
                    // Adds data to array list

                    stopName.add(name);
                    stopId.add(id);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("getStations", "onCancelled: action was cancelled. error" + error);
                Toast.makeText(getActivity(), R.string.err_couldntRetrieveStops, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
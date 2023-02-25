package com.example.sacai.operator.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.DefaultTaskExecutor;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sacai.R;

import com.example.sacai.databinding.FragmentOperRideHistoryBinding;
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

public class OperRideHistoryFrag extends Fragment {

    // Bind fragment to layout
    FragmentOperRideHistoryBinding binding;
    RecyclerView recyclerView;
    RecyclerView.Adapter rideHistoryAdapter;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<Operator_Trip> operatorTrip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate thelayout for this fragment
        binding = FragmentOperRideHistoryBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rideHistoryList);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        operatorTrip = new ArrayList<>();

        // Initialize firebase auth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        readData(uid);
    }

    private void readData(String uid) {
        String TAG = "readData";
        Log.i("ClassCalled", "readData: is running");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        DatabaseReference rideHistory = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName()).child(user.getUid()).child("ride_history");
        rideHistory.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                try {
                    for (DataSnapshot dsp : task.getResult().getChildren()) {
                        Log.i(TAG, "onComplete: Ride History ID: " + dsp.getKey());
                        Log.i(TAG, "onComplete: route_name: " + dsp.child("route_name"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onComplete: exception ", e);
                }
            }
        });
    }
}


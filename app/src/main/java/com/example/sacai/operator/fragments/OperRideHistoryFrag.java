package com.example.sacai.operator.fragments;

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
import com.example.sacai.databinding.FragmentOperPassengerListBinding;
import com.example.sacai.dataclasses.Operator;
import com.example.sacai.dataclasses.Operator_Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class OperRideHistoryFrag extends Fragment {

    // Bind fragment to layout
    FragmentOperPassengerListBinding binding;
    RecyclerView recyclerView;
    RecyclerView.Adapter rideHistoryAdapter;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<Operator_Trip> operatorTrip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate thelayout for this fragment
        binding = FragmentOperPassengerListBinding.inflate(inflater,container,false);
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

        // Get Operator information
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName());
//        databaseReference.child(uid).child("ride_history").addValueEventListener()
    }
}


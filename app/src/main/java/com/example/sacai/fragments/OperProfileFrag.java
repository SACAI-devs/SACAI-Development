package com.example.sacai.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sacai.R;
import com.example.sacai.databinding.FragmentOperProfileBinding;
import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.Operator;
import com.example.sacai.viewmodels.OperMainViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class OperProfileFrag extends Fragment {

//BIND FRAGMENT TO LAYOUT
    FragmentOperProfileBinding binding;

//    TODO: Create viewModel
    OperMainViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOperProfileBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        INITIALIZE FIREBASE AUTH
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

//        READ USER DATA AND DISPLAY
        readData(currentUser.getUid());
    }

    private void readData(String uid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName());
        databaseReference.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
//                        GET THE DATA FROM THE DATABASE
                        DataSnapshot dataSnapshot = task.getResult();
                        String driverName = String.valueOf(dataSnapshot.child("drivername").getValue());
                        String conductorName = String.valueOf(dataSnapshot.child("conductorname").getValue());
                        String username = String.valueOf(dataSnapshot.child("username").getValue());
                        String email = String.valueOf(dataSnapshot.child("email").getValue());

                        if (username.equals("null")) {
                            username = "";
                        }

//                        BIND VALUES TO COMPONENTS
                        binding.etDriverName.setText(driverName);
                        binding.etConductorName.setText(conductorName);
                        binding.etUsername.setText(username);
                        binding.etEmail.setText(email);
                    } else {
                        viewModel.setData(false);
                    }
                } else {
                    viewModel.setData(false);
                }
            }
        });
    }

    private void saveChanges(String uid) {
        String username = binding.etUsername.getText().toString().trim();
        String driverName = binding.etDriverName.getText().toString().trim();
        String conductorName = binding.etConductorName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

//        CHECK IF REQUIRED FIELDS ARE EMPTY
        if (driverName.isEmpty() || conductorName.isEmpty()) {
            viewModel.setData(false);
            viewModel.setMsg(getString(R.string.err_emptyRequiredFields));
            if (driverName.isEmpty()) {
                binding.etDriverName.setError(getString(R.string.err_fieldRequired));
                binding.etDriverName.requestFocus();
            }
            if (conductorName.isEmpty()) {
                binding.etConductorName.setError(getString(R.string.err_fieldRequired));
                binding.etConductorName.requestFocus();
            }
        } else {
            HashMap User = new HashMap();
            User.put("driverName", driverName);
            User.put("conductorName", conductorName);
            User.put("username", username);


            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
            databaseReference.child(uid).updateChildren(User).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        viewModel.setData(true);
                    } else {
                        viewModel.setData(false);
                    }
                }
            });
        }
    }
}
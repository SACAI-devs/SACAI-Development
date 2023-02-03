package com.example.sacai.operator.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sacai.R;
import com.example.sacai.databinding.FragmentOperProfileBinding;
import com.example.sacai.dataclasses.Operator;
import com.example.sacai.operator.OperUpdateEmailActivity;
import com.example.sacai.operator.viewmodels.OperMainViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class OperProfileFrag extends Fragment {

    // Bind fragment to layout
    FragmentOperProfileBinding binding;

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

        viewModel = new ViewModelProvider(requireActivity()).get(OperMainViewModel.class);

        // Initialize Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        binding.etEmail.setFocusableInTouchMode(false);
        binding.etEmail.setFocusable(false);

        // Read data and display
        readData(currentUser.getUid());

        // User re-authentication to change email
        binding.etEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateEmail();
            }
        });

        // Save changes when btn is clicked
        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges(currentUser.getUid());
            }
        });

    }
    private void showUpdateEmail() {
        Intent intent = new Intent(getActivity(), OperUpdateEmailActivity.class);
        startActivity(intent);
    }

    private void readData(String uid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName());
        databaseReference.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        // Get datasnapshot from Firebase and map them to local variables
                        DataSnapshot dataSnapshot = task.getResult();
                        String driverName = String.valueOf(dataSnapshot.child("driver").getValue());
                        String conductorName = String.valueOf(dataSnapshot.child("conductor").getValue());
                        String username = String.valueOf(dataSnapshot.child("username").getValue());
                        String email = String.valueOf(dataSnapshot.child("email").getValue());
                        String wheelchair = String.valueOf(dataSnapshot.child("wheelchairCapacity").getValue());

                        if (username.equals("null")) {
                            username = "";
                        }

                        // Bind values to components
                        binding.etDriverName.setText(driverName);
                        binding.etConductorName.setText(conductorName);
                        binding.etUsername.setText(username);
                        binding.etEmail.setText(email);
                        binding.cbWheelchair.setChecked(Boolean.parseBoolean(wheelchair));
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
        String driver = binding.etDriverName.getText().toString().trim();
        String conductor = binding.etConductorName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

        // Field validation
        if (driver.isEmpty() || conductor.isEmpty()) {
            viewModel.setData(false);
            viewModel.setMsg(getString(R.string.err_emptyRequiredFields));
            if (driver.isEmpty()) {
                binding.etDriverName.setError(getString(R.string.err_fieldRequired));
                binding.etDriverName.requestFocus();
            }
            if (conductor.isEmpty()) {
                binding.etConductorName.setError(getString(R.string.err_fieldRequired));
                binding.etConductorName.requestFocus();
            }
        } else if (!isAlphabetical(driver) || !isAlphabetical(conductor) || !isAlphabetical(username)) {
            if (!isAlphabetical(driver)){
                binding.etDriverName.setError(getString(R.string.err_invalidCharacterInput));
                binding.etDriverName.requestFocus();
            }
            if (!isAlphabetical(conductor)){
                binding.etConductorName.setError(getString(R.string.err_invalidCharacterInput));
                binding.etConductorName.requestFocus();
            }
            if (!isAlphabetical(username)){
                binding.etUsername.setError(getString(R.string.err_invalidCharacterInput));
                binding.etUsername.requestFocus();
            }
        } else {
            // Map variables to nodes in Firebase
            HashMap User = new HashMap();
            User.put("driver", driver);
            User.put("conductor", conductor);
            User.put("username", username);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName());
            databaseReference.child(uid).updateChildren(User).addOnCompleteListener(new OnCompleteListener() {
                // Signals Host activity to display a toast
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

    public static boolean isAlphabetical(String s){
        return s != null && s.matches("^[a-zA-Z ]*$");

    }
}
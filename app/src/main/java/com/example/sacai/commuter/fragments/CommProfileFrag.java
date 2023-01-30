package com.example.sacai.commuter.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sacai.R;
import com.example.sacai.databinding.FragmentCommProfileBinding;
import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.commuter.viewmodels.CommMainViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class CommProfileFrag extends Fragment {

//    BIND FRAGMENT TO LAYOUT
    FragmentCommProfileBinding binding;

    CommMainViewModel viewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCommProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CommMainViewModel.class);

//        INITIALIZE FIREBASE AUTH
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        DISABLES EMAIL EDITING TEMPORARILY
        binding.etEmail.setFocusableInTouchMode(false);
        binding.etEmail.setFocusable(false);

//        CHECKS MOBILITY IMPAIRMENT WHENEVER WHEELCHAIR USER IS CHECKED
        binding.cbWheelchair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.cbWheelchair.isChecked()) {
                    binding.cbMobility.setChecked(true);
                }
            }
        });

//        READ USER DATA AND DISPLAY
        readData(currentUser.getUid());

//        SAVE CHANGES TO PROFILE WHEN BTN IS CLICKED
        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges(currentUser.getUid());
            }
        });
    }

    private void readData(String uid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        databaseReference.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        String firstname = String.valueOf(dataSnapshot.child("firstname").getValue());
                        String lastname = String.valueOf(dataSnapshot.child("lastname").getValue());
                        String username = String.valueOf(dataSnapshot.child("username").getValue());
                        String homeAddress = String.valueOf(dataSnapshot.child("homeAddress").getValue());
                        String workAddress = String.valueOf(dataSnapshot.child("workAddress").getValue());
                        String mobility = String.valueOf(dataSnapshot.child("mobility").getValue());
                        String auditory = String.valueOf(dataSnapshot.child("auditory").getValue());
                        String wheelchair = String.valueOf(dataSnapshot.child("wheelchair").getValue());
//                        TODO: UPDATE EMAIL FEATURE
                        String email = String.valueOf(dataSnapshot.child("email").getValue());

                        if (username.equals("null")) {
                            username = "";
                        }

                        if (homeAddress.equals("null")) {
                            homeAddress = "";
                        }

                        if (workAddress.equals("null")) {
                            workAddress = "";
                        }

                        binding.etFirstname.setText(firstname);
                        binding.etLastname.setText(lastname);
                        binding.etEmail.setText(email);
                        binding.etUsername.setText(username);
                        binding.etHomeAddress.setText(homeAddress);
                        binding.etWorkAddress.setText(workAddress);
                        binding.cbMobility.setChecked(Boolean.parseBoolean(mobility));
                        binding.cbAuditory.setChecked(Boolean.parseBoolean(auditory));
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
        String firstname = binding.etFirstname.getText().toString().trim();
        String lastname = binding.etLastname.getText().toString().trim();
        String home = binding.etHomeAddress.getText().toString().trim();
        String work = binding.etWorkAddress.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

        boolean mobility = binding.cbMobility.isChecked();
        boolean auditory = binding.cbAuditory.isChecked();
        boolean wheelchair = binding.cbWheelchair.isChecked();

//        CHECK IF REQUIRED INPUTS ARE EMPTY
        if (firstname.isEmpty() || lastname.isEmpty()) {
            viewModel.setData(false);
            viewModel.setMsg(getString(R.string.err_emptyRequiredFields));
            if (firstname.isEmpty()) {
                binding.etFirstname.setError(getString(R.string.err_fieldRequired));
                binding.etFirstname.requestFocus();
            }
            if (lastname.isEmpty()) {
                binding.etLastname.setError(getString(R.string.err_fieldRequired));
                binding.etLastname.requestFocus();
            }
//        CHECK FOR INVALID CHARACTERS IN THE FIRST AND LAST NAME FIELDS
        } else if (!isAlphabetical(firstname) || !isAlphabetical(lastname) || !isAlphabetical(username)) {
                if (!isAlphabetical(firstname)){
                    binding.etFirstname.setError(getString(R.string.err_invalidCharacterInput));
                    binding.etFirstname.requestFocus();
                }
                if (!isAlphabetical(lastname)){
                    binding.etLastname.setError(getString(R.string.err_invalidCharacterInput));
                    binding.etLastname.requestFocus();
                }
                if (!isAlphabetical(username)){
                    binding.etUsername.setError(getString(R.string.err_invalidCharacterInput));
                    binding.etUsername.requestFocus();
                }
            }
        else {
            HashMap User = new HashMap();
            User.put("firstname", firstname);
            User.put("lastname", lastname);
            User.put("username", username);
            User.put("homeAddress", home);
            User.put("workAddress", work);
            User.put("mobility", mobility);
            User.put("auditory", auditory);
            User.put("wheelchair", wheelchair);

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
    public static boolean isAlphabetical(String s){
        return s != null && s.matches("^[a-zA-Z ]*$");
    }
}

package com.example.sacai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.sacai.databinding.ActivityEditProfileBinding;
import com.example.sacai.dataclasses.Commuter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.ref.Reference;

public class EditProfileActivity extends AppCompatActivity {


    ActivityEditProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        Initialize FirebaseAuth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.msg_loginToEdit, Toast.LENGTH_SHORT).show();
            showCommLogin();
        }

//        READ USER DATA AND DISPLAY
        readData(currentUser.getUid());

        binding.commEditBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
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
                        String email = String.valueOf(dataSnapshot.child("email").getValue());
                        String homeAddress = String.valueOf(dataSnapshot.child("homeAddress").getValue());
                        String workAddress = String.valueOf(dataSnapshot.child("workAddress").getValue());
                        String mobility = String.valueOf(dataSnapshot.child("mobility").getValue());
                        String auditory = String.valueOf(dataSnapshot.child("auditory").getValue());
                        String wheelchair = String.valueOf(dataSnapshot.child("wheelchair").getValue());

                        if (uid.equals(username)) {
                            username = " ";
                        }

                        binding.commEditEtFirstname.setText(firstname);
                        binding.commEditEtLastname.setText(lastname);
                        binding.commEditEtEmail.setText(email);
                        binding.commEditEtUsername.setText(username);
                        binding.commEditEtHomeAddress.setText(homeAddress);
                        binding.commEditEtWorkAddress.setText(workAddress);
                        binding.commEditCbMobility.setChecked(Boolean.parseBoolean(mobility));
                        binding.commEditCbAuditory.setChecked(Boolean.parseBoolean(auditory));
                        binding.commEditCbWheelchair.setChecked(Boolean.parseBoolean(wheelchair));
                    } else {
                        Toast.makeText(EditProfileActivity.this, R.string.err_userDoesNotExist, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, R.string.err_failedToReadData, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveChanges() {

    }

    private void showCommLogin() {
        Intent intent = new Intent(this, CommLoginActivity.class);
        startActivity(intent);
        finish();
    }
}
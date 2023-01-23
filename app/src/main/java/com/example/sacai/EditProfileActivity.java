package com.example.sacai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.sacai.dao.DAOCommuter;
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
import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

//    BIND ACTIVITY TO LAYOUT
    ActivityEditProfileBinding binding;

    DAOCommuter daoCommuter = new DAOCommuter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        INITIALIZE FIREBASE AUTH AND CHECK IF USER IS LOGGED IN
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.msg_loginToEdit, Toast.LENGTH_SHORT).show();
            showCommLogin();
        }

//        READ USER DATA AND DISPLAY
        readData(currentUser.getUid());

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
                        String email = String.valueOf(dataSnapshot.child("email").getValue());
                        String homeAddress = String.valueOf(dataSnapshot.child("homeAddress").getValue());
                        String workAddress = String.valueOf(dataSnapshot.child("workAddress").getValue());
                        String mobility = String.valueOf(dataSnapshot.child("mobility").getValue());
                        String auditory = String.valueOf(dataSnapshot.child("auditory").getValue());
                        String wheelchair = String.valueOf(dataSnapshot.child("wheelchair").getValue());

                        if (username.equals(uid)) {
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
                        Toast.makeText(EditProfileActivity.this, R.string.err_userDoesNotExist, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, R.string.err_failedToReadData, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(EditProfileActivity.this, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showCommLogin() {
        Intent intent = new Intent(this, CommLoginActivity.class);
        startActivity(intent);
        finish();
    }
}
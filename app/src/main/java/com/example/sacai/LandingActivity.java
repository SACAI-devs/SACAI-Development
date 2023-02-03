package com.example.sacai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.sacai.commuter.CommLoginActivity;
import com.example.sacai.commuter.CommMainActivity;
import com.example.sacai.commuter.CommSignupActivity;
import com.example.sacai.databinding.ActivityLandingBinding;
import com.example.sacai.operator.OperLoginActivity;
import com.example.sacai.operator.OperMainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LandingActivity extends AppCompatActivity {

    // Bind activity to layout
    ActivityLandingBinding binding;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLandingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth and check if user is logged in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            showMainActivity(currentUser.getUid());
        }

        // Show LOGIN PAGE when btn is clicked
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogin();
            }
        });

        // Show SIGNUP PAGE when btn is clicked
        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignup();
            }
        });

        // Switch to OPERATOR LOGIN when btn is clicked
        binding.btnSwitchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOperLogin();
            }
        });
    }

    private void showMainActivity (String uid){
        // Get usertype from table
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = db.getReference("Users");
        databaseReference.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot dataSnapshot = task.getResult();
                String usertype = String.valueOf(dataSnapshot.child("userType").getValue());
                // Redirect user to respective screens
                if (usertype.equalsIgnoreCase(getString(R.string.label_commuter))){
                    showMainComm();
                } else if (usertype.equalsIgnoreCase(getString(R.string.label_operator))) {
                    showMainOper();
                }
            }
        });
    }

    private void showLogin() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, CommLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showSignup() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, CommSignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void showOperLogin() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(LandingActivity.this, OperLoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void showMainComm(){
        // Check if user is logged in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.msg_loginToEdit, Toast.LENGTH_SHORT).show();
            logout();
        // Check if email is verified
        } else if (!currentUser.isEmailVerified()){
            Toast.makeText(this, R.string.msg_checkEmailForVerifyLink, Toast.LENGTH_SHORT).show();
            logout();
        } else {
            Intent intent = new Intent(LandingActivity.this, CommMainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void showMainOper(){
        // Check if user is logged in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.msg_loginToEdit, Toast.LENGTH_SHORT).show();
            logout();
        // Check if email is verified
        } else if (!currentUser.isEmailVerified()){
            Toast.makeText(this, R.string.msg_checkEmailForVerifyLink, Toast.LENGTH_SHORT).show();
            logout();
        } else {
            Intent intent = new Intent(LandingActivity.this, OperMainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void logout() {
        FirebaseAuth.getInstance().signOut();
    }
}
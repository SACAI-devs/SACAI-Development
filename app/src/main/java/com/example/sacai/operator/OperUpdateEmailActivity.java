package com.example.sacai.operator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.sacai.LandingActivity;
import com.example.sacai.R;
import com.example.sacai.commuter.CommUpdateEmailActivity;
import com.example.sacai.databinding.ActivityOperUpdateEmailBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class OperUpdateEmailActivity extends AppCompatActivity {

    ActivityOperUpdateEmailBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperUpdateEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEmail();
            }
        });
    }

    private void updateEmail() {
        // Retrieve values from text fields
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Field Validation
        if (email.isEmpty() || password.isEmpty()) {
            if (email.isEmpty()) {
                binding.etEmail.setError(getString(R.string.err_fieldRequired));
                binding.etEmail.requestFocus();
            }
            if (password.isEmpty()) {
                binding.etPassword.setError(getString(R.string.err_fieldRequired));
                binding.etPassword.requestFocus();
            }
            Toast.makeText(this, R.string.err_emptyRequiredFields, Toast.LENGTH_SHORT).show();
        } else if (!currentUser.getEmail().equals(email)) {
            binding.etEmail.setError("Please enter your currently registered email!");
            binding.etEmail.requestFocus();
        } else {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            // Get auth credentials from user for re-authentication
            AuthCredential credential = EmailAuthProvider
                    .getCredential(email, password); // Should be the user's current login credentials
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                applyUpdatedEmail();
                            } else {
                                Toast.makeText(OperUpdateEmailActivity.this, "Authentication Error. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void applyUpdatedEmail(){
        // Disable previous input
        binding.etEmail.setFocusableInTouchMode(false);
        binding.etPassword.setFocusableInTouchMode(false);
        binding.etEmail.setFocusable(false);
        binding.etPassword.setFocusable(false);
        binding.btnSubmit.setClickable(false);

        // Show new email entry and button
        binding.etEmail.setVisibility(View.GONE);
        binding.etPassword.setVisibility(View.GONE);
        binding.etEmail.setHint("");
        binding.etPassword.setHint("");
        binding.btnSubmit.setVisibility(View.GONE);
        binding.etNewEmail.setVisibility(View.VISIBLE);
        binding.btnSave.setVisibility(View.VISIBLE);

        // Have user enter new email
        binding.tvChangeEmailInstructions.setText(R.string.label_enterNewEmail);

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get values from text fields
                String newEmail =  binding.etNewEmail.getText().toString().trim();

                // Field validation
                if (newEmail.isEmpty()) {
                    binding.etEmail.setError(getString(R.string.err_fieldRequired));
                    binding.etEmail.requestFocus();
                }
                else {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    user.updateEmail(newEmail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Update records in realtime database
                                        updateUserProfile(newEmail, user.getUid());
                                        Toast.makeText(OperUpdateEmailActivity.this, R.string.msg_success, Toast.LENGTH_SHORT).show();
                                        logout();
                                    } else {
                                        Toast.makeText(OperUpdateEmailActivity.this, R.string.err_authentication, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void updateUserProfile(String newEmail, String uid) {
        HashMap User = new HashMap();
        User.put("email", newEmail);

        // Update records under Operators
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.label_operator));
        databaseReference.child(uid).updateChildren(User).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(OperUpdateEmailActivity.this, R.string.err_unknown, Toast.LENGTH_SHORT).show();;
                }
            }
        });

        // Update records under Users
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(uid).updateChildren(User).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(OperUpdateEmailActivity.this, R.string.err_unknown, Toast.LENGTH_SHORT).show();;
                } else {

                }
            }
        });
    }

    public void logout(){
        // Logout user
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LandingActivity.class);
        startActivity(intent);
        finish();
    }
}
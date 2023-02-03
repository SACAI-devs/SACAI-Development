package com.example.sacai.commuter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.sacai.operator.OperSignupActivity;
import com.example.sacai.R;
import com.example.sacai.commuter.dao.DAOCommuter;
import com.example.sacai.databinding.ActivityCommSignup2Binding;
import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class CommSignup2Activity extends AppCompatActivity {
    // Bind activity to layout
    ActivityCommSignup2Binding binding;
    private FirebaseAuth mAuth;
    DAOCommuter daoCommuter = new DAOCommuter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommSignup2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth and check if user is logged in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            return;
        }

        // Syncs mobility checkbox to wheelchair user checkbox
        binding.cbWheelchair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.cbWheelchair.isChecked()) {
                    binding.cbMobility.setChecked(true);
                    binding.cbMobility.setEnabled(false);
                } else {
                    binding.cbMobility.setEnabled(true);
                }
            }
        });

        binding.cbMobility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.cbWheelchair.isChecked()) {
                    binding.cbMobility.setChecked(true);
                    binding.cbMobility.setEnabled(false);
                } else {
                    binding.cbMobility.setEnabled(true);
                }
            }
        });

        // Registers user when btn is clicked
        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        // Show previous page when btn is clicked
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrevious();
            }
        });

        // Toolbar action handling
        Toolbar toolbar = (Toolbar) binding.toolbar;
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    private void showPrevious() {
        Intent intent = new Intent(this, CommSignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void registerUser() {
        Intent intent = getIntent();
        String firstname = intent.getStringExtra(CommSignupActivity.EXTRA_FIRST);
        String lastname = intent.getStringExtra(CommSignupActivity.EXTRA_LAST);
        String email = intent.getStringExtra(CommSignupActivity.EXTRA_EMAIL);
        String password = intent.getStringExtra(CommSignupActivity.EXTRA_PASS);
        String userType = getString(R.string.userCommuter);

        boolean mobility = binding.cbMobility.isChecked();
        boolean auditory = binding.cbAuditory.isChecked();
        boolean wheelchair = binding.cbWheelchair.isChecked();

        // Field validation
        if ((mobility == false) && (auditory == false) && (wheelchair == false)) {
            Toast.makeText(this, R.string.err_emptyRequiredFields, Toast.LENGTH_SHORT).show();
            return;
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Create user and store it into Firebase
                                User user = new User(email, userType);
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                // If USER CREATION is SUCCESSFUL then it sends a verification email
                                                if (task.isSuccessful()) {
                                                        // Adds a new commuter record
                                                        Commuter commuter = new Commuter(firstname, lastname, email, "", mobility, auditory, wheelchair,"", "", currentUser.getUid());
                                                        daoCommuter.add(commuter);
                                                        sendVerificationEmail(email, password);
                                                        Toast.makeText(CommSignup2Activity.this, R.string.msg_checkEmailForVerifyLink, Toast.LENGTH_LONG).show();
                                                        showCommLogin();
                                                } else {
                                                    Toast.makeText(CommSignup2Activity.this, R.string.err_authentication, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                // If user's email is already registered to another user
                                Toast.makeText(CommSignup2Activity.this, R.string.err_emailExists, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void sendVerificationEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.sendEmailVerification();
                            FirebaseAuth.getInstance().signOut();
                        } else {
                            Toast.makeText(CommSignup2Activity.this, R.string.err_unknown, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showCommLogin() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, CommLoginActivity.class);
        startActivity(intent);
        finish();
    }
}
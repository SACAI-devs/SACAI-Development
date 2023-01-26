package com.example.sacai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.sacai.databinding.ActivityOperLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OperLoginActivity extends AppCompatActivity {

//    BIND ACTIVITY TO LAYOUT
    ActivityOperLoginBinding binding;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        INITIALIZE FIREBASE AUTH AND CHECK IF USER IS LOGGED IN
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            return;
        }

//        SHOW OPERATOR SIGNUP PAGE WHEN BTN IS CLICKED
        binding.btnSwitchSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOperSignup();
            }
        });

//        LOGIN USER WHEN BTN IS CLICKED
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

//        SHOWS PASSWORD RESET PAGE WHEN BTN IS CLICKED
        binding.btnForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPassReset();
            }
        });

//        SHOWS COMMUTER SIGNUP WHEN BTN IS CLICKED
        binding.btnSwitchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSwitchComm();
            }
        });
    }

    private void showSwitchComm() {
        Intent intent = new Intent(this, CommLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showPassReset() {
        Intent intent  = new Intent(this, ForgotPassActivity.class);
        startActivity(intent);
        finish();
    }

    private void showOperSignup() {
        Intent intent = new Intent(this, OperSignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void showMainActivity(String uid) {
//        GET USERTYPE FROM USER TABLE
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = db.getReference("Users");
        databaseReference.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot dataSnapshot = task.getResult();
                String usertype = String.valueOf(dataSnapshot.child("userType").getValue());
//                REDIRECT USER TO RESPECTIVE SCREENS
                if (usertype.equalsIgnoreCase(getString(R.string.choice_commuter))){
                    Toast.makeText(OperLoginActivity.this, R.string.err_wrongUserType, Toast.LENGTH_SHORT).show();
                } else if (usertype.equalsIgnoreCase(getString(R.string.label_operator))) {
                    Toast.makeText(OperLoginActivity.this, R.string.msg_loginSuccess, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OperLoginActivity.this, CommMainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

//        CHECK IF FIELDS EMPTY
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
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user.isEmailVerified()) {
                                    showMainActivity(user.getUid());
                                } else {
                                    user.sendEmailVerification();
                                    Toast.makeText(OperLoginActivity.this, R.string.msg_checkEmailForVerifyLink, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(OperLoginActivity.this, R.string.err_authentication, Toast.LENGTH_SHORT).show();
                                binding.etEmail.setError("");
                                binding.etEmail.requestFocus();
                                binding.etPassword.setError("");
                                binding.etPassword.requestFocus();
                            }
                        }
                    });
        }
    }
}
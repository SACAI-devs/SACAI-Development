package com.example.sacai.commuter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.example.sacai.ForgotPassActivity;
import com.example.sacai.operator.OperLoginActivity;
import com.example.sacai.R;
import com.example.sacai.databinding.ActivityCommLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CommLoginActivity extends AppCompatActivity {

    // Bind activity to layout
    ActivityCommLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase auth and check if User is logged in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            return;
        }

        // Login user when btn is clicked
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        // Show PASSWORD RESET when btn is clicked
        binding.btnForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPass();
            }
        });

        // Switch to COMMUTER SIGNUP when btn is clicked
        binding.btnSwitchSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommSignup();
            }
        });

//        Toolbar action handling
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

    // Toolbar menu actions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.switch_to_oper_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_switch:
                Intent intent = new Intent (this, OperLoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showCommSignup() {
        Intent intent = new Intent(this, CommSignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void showForgotPass() {
        Intent intent = new Intent (this, ForgotPassActivity.class);
        startActivity(intent);
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        // Check if important fields are empty
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
                                // Check if user email is verified
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user.isEmailVerified()) {
                                    showMainActivity(user.getUid());
                                } else {
                                    // Sends a new verification link to email
                                    user.sendEmailVerification();
                                    Toast.makeText(CommLoginActivity.this, R.string.msg_checkEmailForVerifyLink, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                // Unknown error. Cannot authenticate user
                                Toast.makeText(CommLoginActivity.this, R.string.err_authentication, Toast.LENGTH_SHORT).show();
                                binding.etEmail.setError("");
                                binding.etEmail.requestFocus();
                                binding.etPassword.setError("");
                                binding.etPassword.requestFocus();
                            }
                        }
                    });
        }
    }

    private void showMainActivity(String uid) {
        // Get usertype from user table
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = db.getReference("Users");
        databaseReference.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot dataSnapshot = task.getResult();
                String usertype = String.valueOf(dataSnapshot.child("userType").getValue());
                // Redirect users accordingly
                if (usertype.equalsIgnoreCase(getString(R.string.label_commuter))){
                    Toast.makeText(CommLoginActivity.this, R.string.msg_loginSuccess, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(CommLoginActivity.this, CommMainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (usertype.equalsIgnoreCase(getString(R.string.label_operator))) {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(CommLoginActivity.this, R.string.err_wrongUserType, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
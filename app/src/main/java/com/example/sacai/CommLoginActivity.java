package com.example.sacai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sacai.databinding.ActivityCommLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class CommLoginActivity extends AppCompatActivity {

//    BIND ACTIVITY TO LAYOUT
    ActivityCommLoginBinding binding;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        INITIALIZE FIREBASE AUTH AND CHECK IF USER IS LOGGED IN
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            return;
        }


//        SHOW OPERATOR LOGIN WHEN BTN IS CLICKED
        binding.btnSwitchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOperLogin();
            }
        });

//        LOGIN USER WHEN BTN IS CLICKED
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

//        SHOW PASSWORD RESET PAGE WHEN BTN IS CLICKED
        binding.btnForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPass();
            }
        });

//        SHOW COMMUTER SIGNUP WHEN BTN IS CLICKED
        binding.btnSwitchSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommSignup();
            }
        });
    }

    private void showCommSignup() {
        Intent intent = new Intent(this, CommSignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void showForgotPass() {
        Intent intent = new Intent (this, ForgotPassActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

//        CHECK IF FIELDS ARE EMPTY
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
//                                CHECK IF USER EMAIL IS VERIFIED
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user.isEmailVerified()) {
                                    Toast.makeText(CommLoginActivity.this, R.string.msg_loginSuccess, Toast.LENGTH_LONG).show();
                                    showMainActivity();
                                    finish();
                                } else {
                                    user.sendEmailVerification();
                                    Toast.makeText(CommLoginActivity.this, R.string.msg_checkEmailForVerifyLink, Toast.LENGTH_LONG).show();
                                }
                            } else {
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

    private void showMainActivity() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void showOperLogin() {
        Intent intent = new Intent(this, OperLoginActivity.class);
        startActivity(intent);
        finish();
    }
}
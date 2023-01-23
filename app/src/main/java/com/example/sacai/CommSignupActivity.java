package com.example.sacai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sacai.databinding.ActivityCommSignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CommSignupActivity extends AppCompatActivity {


    //    BIND ACTIVITY TO LAYOUT
    ActivityCommSignupBinding binding;

    private FirebaseAuth mAuth;

    public static final String EXTRA_FIRST = "firstname";
    public static final String EXTRA_LAST = "lastname";
    public static final String EXTRA_EMAIL = "email";
    public  static final String EXTRA_PASS = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommSignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        INITIALIZE FIREBASE AUTH AND CHECK IF USER IS LOGGED IN
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            return;
        }

//          SHOW OPERATOR SIGNUP WHEN BTN IS CLICKED
        binding.btnSwitchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOpSignup();
            }
        });

//        SHOW NEXT PAGE WHEN BTN IS CLICKED
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNextPage();
            }
        });

//        SHOW COMMUTER LOGIN WHEN BTN IS CLICKED
        binding.btnSwitchLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommLogin();
            }
        });

    }

    private void showCommLogin() {
        Intent intent = new Intent(this, CommLoginActivity.class);
        startActivity(intent);
        finish();
    }

    //    SHOW NEXT PAGE
    private void showNextPage() {
        String firstname = binding.etFirstname.getText().toString().trim();
        String lastname = binding.etLastname.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
//        CHECK FOR EMPTY REQUIRED FIELDS
        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty()){
            if (firstname.isEmpty()) {
                binding.etFirstname.setError(getString(R.string.err_fieldRequired));
                binding.etFirstname.requestFocus();
            }
            if (lastname.isEmpty()) {
                binding.etLastname.setError(getString(R.string.err_fieldRequired));
                binding.etLastname.requestFocus();
            }
            if (email.isEmpty()) {
                binding.etEmail.setError(getString(R.string.err_fieldRequired));
                binding.etEmail.requestFocus();
            }
            if (password.isEmpty()) {
                binding.etPassword.setError(getString(R.string.err_fieldRequired));
                binding.etPassword.requestFocus();
            }
        } else if ((password.length() < 6)) {
//            CHECK IF THE PASSWORD LENGTH IS AT LEAST 6 CHARACTERS
            binding.etPassword.setError(getString(R.string.err_passCharCount));
            binding.etPassword.requestFocus();
        } else if (!isAlphaNumeric(password)) {
//            CHECK IF PASSWORD HAS BOTH LETTERS AND NUMBERS
            binding.etPassword.setError(getString(R.string.err_passShouldBeAlphanumeric));
            binding.etPassword.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            CHECK IF EMAIL IS A VALID EMAIL FORMAT
            Toast.makeText(this, R.string.err_authentication, Toast.LENGTH_SHORT).show();
            binding.etEmail.setError(getString(R.string.err_invalidEmail));
            binding.etEmail.requestFocus();
        } else {
            Intent intent = new Intent(this, CommSignup2Activity.class);
            intent.putExtra(EXTRA_FIRST, firstname);
            intent.putExtra(EXTRA_LAST, lastname);
            intent.putExtra(EXTRA_EMAIL,email);
            intent.putExtra(EXTRA_PASS, password);
            startActivity(intent);
            finish();
        }
    }
    public static boolean isAlphaNumeric(String s) {
        return s != null && s.matches("^(?=.*[0-9])(?=.*[a-zA-Z])[a-zA-Z0-9]+$");
    }

    private void showOpSignup() {
        Intent intent = new Intent(this, OperSignupActivity.class);
        startActivity(intent);
        finish();
    }
}
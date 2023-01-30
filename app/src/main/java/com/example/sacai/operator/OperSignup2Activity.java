package com.example.sacai.operator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.sacai.R;
import com.example.sacai.databinding.ActivityOperSignup2Binding;
import com.example.sacai.operator.dao.DAOOperator;
import com.example.sacai.dataclasses.Operator;
import com.example.sacai.dataclasses.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class OperSignup2Activity extends AppCompatActivity {

//    BIND ACTIVITY TO LAYOUT
    ActivityOperSignup2Binding binding;
    private FirebaseAuth mAuth;
    DAOOperator daoOperator = new DAOOperator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperSignup2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        INITIALIZE FIREBASE AUTH AND CHECK IF USER IS LOGGED IN
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            return;
        }

//        REGISTER USER WHEN BTN IS CLICKED
        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

//        SHOWS PREVIOUS ACTIVITY WHEN BTN IS CLICKED
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrevious();
            }
        });

//        TOOLBAR ACTION HANDLING
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
        Intent intent = new Intent(this, OperSignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void registerUser() {
        String driver = getIntent().getStringExtra(OperSignupActivity.EXTRA_DR_NAME);
        String conductor = getIntent().getStringExtra(OperSignupActivity.EXTRA_CON_NAME);
        String franchise = getIntent().getStringExtra(OperSignupActivity.EXTRA_FRANCHISE);
        String plate = getIntent().getStringExtra(OperSignupActivity.EXTRA_PLATE);
        boolean wheelchair = getIntent().getBooleanExtra("wheelchair", OperSignupActivity.EXTRA_WHEELCHAIR);
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        String userType = getString(R.string.label_operator);

//        CHECK IF FIELDS ARE EMPTY
        if (franchise.isEmpty() || plate.isEmpty() || email.isEmpty() || password.isEmpty()) {
            if (email.isEmpty()) {
                binding.etEmail.setError(getString(R.string.err_fieldRequired));
                binding.etEmail.requestFocus();
            }
            if (password.isEmpty()) {
                binding.etPassword.setError(getString(R.string.err_fieldRequired));
                binding.etPassword.requestFocus();
            }
//        CHECK IF THE PASSWORD LENGTH IS AT LEAST 6 CHARACTERS
        } else if ((password.length() < 6)) {
            binding.etPassword.setError(getString(R.string.err_passCharCount));
            binding.etPassword.requestFocus();
//        CHECK IF PASSWORD HAS BOTH LETTERS AND NUMBERS
        } else if (!isAlphaNumeric(password)) {
            binding.etPassword.setError(getString(R.string.err_passShouldBeAlphanumeric));
            binding.etPassword.requestFocus();
//        CHECK IF EMAIL IS A VALID EMAIL FORMAT
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.err_authentication, Toast.LENGTH_SHORT).show();
            binding.etEmail.setError(getString(R.string.err_invalidEmail));
            binding.etEmail.requestFocus();
        } else {
//        PROCEED WITH USER REGISTRATION
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                           if (task.isSuccessful()){
//                            CREATE A NEW USER AND STORE IT INTO FIREBASE
                               User user = new User(email, userType);
                               FirebaseUser currentUser = mAuth.getCurrentUser();
                               FirebaseDatabase.getInstance().getReference("Users")
                                       .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                       .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {
//                                               IF USER CREATION IS SUCCESSFULL THEN IT SENDS AN EMAIL VERIFICATION LINK TO THE USER
                                               if (task.isSuccessful()) {
//                                                ADD NEW OPERATOR RECORD
                                                   Operator operator = new Operator(driver, conductor, franchise, plate, wheelchair, email,"" , currentUser.getUid());
                                                   daoOperator.add(operator);
                                                   sendVerificationEmail(email, password);
                                                   Toast.makeText(OperSignup2Activity.this, R.string.msg_checkEmailForVerifyLink, Toast.LENGTH_LONG).show();
                                                   showOperLogin();
                                               } else {
                                                   Toast.makeText(OperSignup2Activity.this, R.string.err_authentication, Toast.LENGTH_SHORT).show();
                                               }
                                           }
                                       });
                           } else {
                               Toast.makeText(OperSignup2Activity.this, R.string.err_emailExists, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(OperSignup2Activity.this, R.string.err_unknown, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static boolean isAlphaNumeric(String s) {
//        return s != null && s.matches("^(?=.*[0-9])(?=.*[a-zA-Z])[a-zA-Z0-9]+$");
        return s != null && s.matches("^(?=.*[0-9])(?=.*[a-zA-Z])[a-zA-Z0-9!@#$&()\\-`.+,/\"]+$");
    }

    private void showOperLogin() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, OperLoginActivity.class);
        startActivity(intent);
    }
}
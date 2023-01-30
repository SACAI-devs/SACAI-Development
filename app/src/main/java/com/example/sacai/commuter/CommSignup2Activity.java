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

//    BIND ACTIVITY TO LAYOUT
    ActivityCommSignup2Binding binding;
    private FirebaseAuth mAuth;
    DAOCommuter daoCommuter = new DAOCommuter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommSignup2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        INITIALIZE FIREBASE AUTH AND CHECK IF USER IS LOGGED IN
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            return;
        }
//        CHECKS MOBILITY IMPAIRMENT WHENEVER WHEELCHAIR USER IS CHECKED
        binding.cbWheelchair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.cbWheelchair.isChecked()) {
                    binding.cbMobility.setChecked(true);
                }
            }
        });

//        REGISTER USER WHEN BTN IS CLICKED
        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

//        SHOW OPERATOR SIGNUP
        binding.btnSwitchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOpSignup();
            }
        });

//        SHOW PREVIOUS PAGE
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
        Intent intent = new Intent(this, CommSignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void showOpSignup() {
        Intent intent = new Intent(this, OperSignupActivity.class);
        startActivity(intent);
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

//        CHECK IF FIELDS ARE EMPTY
        if ((mobility == false) && (auditory == false) && (wheelchair == false)) {
            Toast.makeText(this, R.string.err_emptyRequiredFields, Toast.LENGTH_SHORT).show();
            return;
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
//                          CREATE A NEW USER AND STORE IT INTO FIREBASE
                                User user = new User(email, userType);
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
//                                                IF USER CREATION IS SUCCESSFULL THEN IT SENDS AN EMAIL VERIFICATION LINK TO THE USER
                                                if (task.isSuccessful()) {
//                                                    ADDS A NEW COMMUTER RECORD
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
                                Toast.makeText(CommSignup2Activity.this, R.string.err_emailExists, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public static boolean isAlphaNumeric(String s) {
        return s != null && s.matches("/^[A-Za-z]+$/");
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
    }
}
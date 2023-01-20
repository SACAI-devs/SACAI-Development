package com.example.sacai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.sacai.dao.DAOCommuter;
import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class CommSignup2Activity extends AppCompatActivity {

    CheckBox cbMobility, cbAuditory, cbWheelchair;
    Button btnSignup, btnSwitch, btnBack;

    DAOCommuter daoCommuter = new DAOCommuter();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comm_signup2);
//        Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            return;
        }

        cbMobility = findViewById(R.id.commSignup_cbMobility);
        cbAuditory = findViewById(R.id.commSignup_cbAuditory);
        cbWheelchair = findViewById(R.id.commSignup_cbWheelchair);


//        REGISTER USER WHEN BTN IS CLICKED
        btnSignup = findViewById(R.id.commSignup_btnSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

//        SHOW OPERATOR SIGNUP
        btnSwitch = findViewById(R.id.commSignup_btnSwitchUser);
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOpSignup();
            }
        });

//        SHOW PREVIOUS PAGE
//        TODO: DO THIS BETTER
        btnBack = findViewById(R.id.commSignup_btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrevious();
            }
        });
    }

    private void showPrevious() {
        Intent intent = new Intent(this, CommSignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void showOpSignup() {
        Intent intent = new Intent(this, OperSignupActivity.class);
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

        boolean mobility = cbMobility.isChecked();
        boolean auditory = cbAuditory.isChecked();
        boolean wheelchair = cbWheelchair.isChecked();

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
                                User user = new User(email, password, userType);
                                FirebaseDatabase.getInstance().getReference("users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
//                                                IF USER CREATION IS SUCCESSFULL THEN IT SENDS AN EMAIL VERIFICATION LINK TO THE USER
                                                if (task.isSuccessful()) {
//                                                    ADDS A NEW COMMUTER RECORD
                                                        Commuter commuter = new Commuter(firstname, lastname, email, mobility, auditory, wheelchair);
                                                        daoCommuter.add(commuter);
                                                        showCommLogin();
                                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                        user.sendEmailVerification();
                                                        Toast.makeText(CommSignup2Activity.this, R.string.msg_checkEmailForVerifyLink, Toast.LENGTH_LONG).show();
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

    private void showCommLogin() {
        Intent intent = new Intent(this, CommLoginActivity.class);
        startActivity(intent);
        finish();
    }
}
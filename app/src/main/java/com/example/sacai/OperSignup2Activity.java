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

import com.example.sacai.dao.DAOOperator;
import com.example.sacai.dataclasses.Operator;
import com.example.sacai.dataclasses.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class OperSignup2Activity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnBack, btnSignup;

    private FirebaseAuth mAuth;

    DAOOperator daoOperator = new DAOOperator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oper_signup2);

//        Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            return;
        }

        etEmail = findViewById(R.id.operSignup_etEmail);
        etPassword = findViewById(R.id.operSignup_etPassword);

//        REGISTER USER WHEN BTN IS CLICKED
        btnSignup = findViewById(R.id.operSignup_btnSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

//        SHOWS PREVIOUS ACTIVITY WHEN BTN IS CLICKED
        btnBack = findViewById(R.id.operSignup_btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrevious();
            }
        });

    }

    private void showPrevious() {
        Intent intent = new Intent(this, OperSignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void registerUser() {
        String drivername = getIntent().getStringExtra(OperSignupActivity.EXTRA_DR_NAME);
        String conductorname = getIntent().getStringExtra(OperSignupActivity.EXTRA_CON_NAME);
        String franchise = getIntent().getStringExtra(OperSignupActivity.EXTRA_FRANCHISE);
        String plate = getIntent().getStringExtra(OperSignupActivity.EXTRA_PLATE);
        boolean wheelchair = getIntent().getBooleanExtra("wheelchair", OperSignupActivity.EXTRA_WHEELCHAIR);
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String userType = getString(R.string.label_operator);


//        CHECK IF FIELDS ARE EMPTY
        if (franchise.isEmpty() || plate.isEmpty() || email.isEmpty() || password.isEmpty()) {
            if (email.isEmpty()) {
                etEmail.setError(getString(R.string.err_fieldRequired));
                etEmail.requestFocus();
            }
            if (password.isEmpty()) {
                etPassword.setError(getString(R.string.err_fieldRequired));
                etPassword.requestFocus();
            }
        } else if ((password.length() < 6)) {
//            CHECK IF THE PASSWORD LENGTH IS AT LEAST 6 CHARACTERS
            etPassword.setError(getString(R.string.err_passCharCount));
            etPassword.requestFocus();
        } else if (!isAlphaNumeric(password)) {
//            CHECK IF PASSWORD HAS BOTH LETTERS AND NUMBERS
            etPassword.setError(getString(R.string.err_passShouldBeAlphanumeric));
            etPassword.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            CHECK IF EMAIL IS A VALID EMAIL FORMAT
            Toast.makeText(this, R.string.err_authentication, Toast.LENGTH_SHORT).show();
            etEmail.setError(getString(R.string.err_invalidEmail));
            etEmail.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                           if (task.isSuccessful()){
//                            CREATE A NEW USER AND STORE IT INTO FIREBASE
                               User user = new User(email, password, userType);
                               FirebaseDatabase.getInstance().getReference("users")
                                       .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                       .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {
//                                               IF USER CREATION IS SUCCESSFULL THEN IT SENDS AN EMAIL VERIFICATION LINK TO THE USER
                                               if (task.isSuccessful()) {
//                                                ADD NEW OPERATOR RECORD
                                                   Operator operator = new Operator(drivername, conductorname, franchise, plate, wheelchair, email);
                                                   daoOperator.add(operator);
                                                   showOperLogin();
                                                   FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                   user.sendEmailVerification();
                                                   Toast.makeText(OperSignup2Activity.this, R.string.msg_checkEmailForVerifyLink, Toast.LENGTH_LONG).show();
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

    public static boolean isAlphaNumeric(String s) {
        return s != null && s.matches("^(?=.*[0-9])(?=.*[a-zA-Z])[a-zA-Z0-9]+$");
    }

    private void showOperLogin() {
        Intent intent = new Intent(this, OperLoginActivity.class);
        startActivity(intent);
        finish();
    }
}
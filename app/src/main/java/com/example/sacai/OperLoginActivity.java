package com.example.sacai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OperLoginActivity extends AppCompatActivity {

    EditText etEmail;
    EditText etPassword;
    Button btnLogin;
    Button btnSwitchSignup;
    Button btnPassReset;
    Button btnSwitchComm;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oper_login);

        etEmail = findViewById(R.id.operLogin_etEmail);
        etPassword = findViewById(R.id.operLogin_etPassword);

//        Initialization Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            return;
        }

//        SHOW OPERATOR SIGNUP PAGE WHEN BTN IS CLICKED
        btnSwitchSignup = findViewById(R.id.nav_btnSwitchSignup);
        btnSwitchSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOperSignup();
            }
        });

//        LOGIN USER WHEN BTN IS CLICKED
        btnLogin = findViewById(R.id.operLogin_btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

//        SHOWS PASSWORD RESET PAGE WHEN BTN IS CLICKED
        btnPassReset = findViewById(R.id.nav_btnPassReset);
        btnPassReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPassReset();
            }
        });

//        SHOWS COMMUTER SIGNUP WHEN BTN IS CLICKED
        btnSwitchComm = findViewById(R.id.operLogin_btnSwitchCommuter);
        btnSwitchComm.setOnClickListener(new View.OnClickListener() {
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

    private void showMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

//        CHECK IF FIELDS EMPTY
        if (email.isEmpty() || password.isEmpty()) {
            if (email.isEmpty()) {
                etEmail.setError(getString(R.string.err_fieldRequired));
                etEmail.requestFocus();
            }
            if (password.isEmpty()) {
                etPassword.setError(getString(R.string.err_fieldRequired));
                etPassword.requestFocus();
            }
            Toast.makeText(this, R.string.err_emptyRequiredFields, Toast.LENGTH_SHORT).show();
            return;
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user.isEmailVerified()) {
                                    Toast.makeText(OperLoginActivity.this, R.string.msg_loginSuccess, Toast.LENGTH_SHORT).show();
                                    showMainActivity();
                                    finish();
                                }
                            } else {
                                Toast.makeText(OperLoginActivity.this, R.string.err_authentication, Toast.LENGTH_SHORT).show();
                                etEmail.setError("");
                                etEmail.requestFocus();
                                etPassword.setError("");
                                etPassword.requestFocus();
                                return;
                            }
                        }
                    });
        }
    }
}
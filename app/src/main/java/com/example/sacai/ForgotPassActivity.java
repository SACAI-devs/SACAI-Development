package com.example.sacai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassActivity extends AppCompatActivity {

    EditText etEmail;
    Button btnReset;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

//        Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.forgot_etEmail);

//        SEND RESET LINK WHEN BTN CLICKED
        btnReset = findViewById(R.id.forgot_btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sentResetLink();
            }
        });
    }

    private void sentResetLink() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.err_fieldRequired));
            etEmail.requestFocus();
            Toast.makeText(this, R.string.err_emptyRequiredFields, Toast.LENGTH_SHORT).show();
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.err_pleaseEnterEmail));
            return;
        }

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPassActivity.this, R.string.msg_resetLinkSent, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ForgotPassActivity.this, R.string.err_unknown, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
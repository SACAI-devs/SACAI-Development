package com.example.sacai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sacai.databinding.ActivityForgotPassBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassActivity extends AppCompatActivity {

//    BIND ACTIVITY TO LAYOUT
    ActivityForgotPassBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        INITIALIZE FIREBASE AUTH
        mAuth = FirebaseAuth.getInstance();


//        SEND RESET LINK WHEN BTN CLICKED
        binding.btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sentResetLink();
            }
        });
    }

    private void sentResetLink() {
        String email = binding.etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            binding.etEmail.setError(getString(R.string.err_fieldRequired));
            binding.etEmail.requestFocus();
            Toast.makeText(this, R.string.err_emptyRequiredFields, Toast.LENGTH_SHORT).show();
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError(getString(R.string.err_pleaseEnterEmail));
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
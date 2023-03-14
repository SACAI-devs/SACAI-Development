package com.example.sacai.operator;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sacai.databinding.ActivityCommUpdatePassBinding;
import com.example.sacai.databinding.ActivityOperUpdatePasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class OperUpdatePassword extends AppCompatActivity {

    ActivityOperUpdatePasswordBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperUpdatePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String TAG = "onCreateCommUpdatePassword";

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = binding.etEmail.getText().toString();

                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(OperUpdatePassword.this, "Password reset link has been sent!", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Email sent.");
                                }
                            }
                        });
            }
        });
    }
}
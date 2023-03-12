package com.example.sacai.commuter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.sacai.databinding.ActivityCommUpdateEmailBinding;
import com.example.sacai.databinding.ActivityCommUpdatePassBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class CommUpdatePassword extends AppCompatActivity {

    ActivityCommUpdatePassBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommUpdatePassBinding.inflate(getLayoutInflater());
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
                                    Toast.makeText(CommUpdatePassword.this, "Password reset link has been sent!", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Email sent.");
                                }
                            }
                        });
            }
        });
    }
}
package com.example.sacai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    Button btnLogin;
    Button btnSignup;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//      Initialize Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            showMainActivity(currentUser.getUid());
            Toast.makeText(this, "It executed", Toast.LENGTH_LONG).show();
        }

//        OPEN LOGIN PAGE WHEN BTN IS CLICKED
        btnLogin = findViewById(R.id.nav_btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogin();
            }
        });

//        OPEN SIGN PAGE WHEN BTN IS CLICKED
        btnSignup = findViewById(R.id.nav_btnSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignup();
            }
        });
    }

    private void showMainActivity (String uid){
//        GET USERTYPE FROM USER TABLE
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("Users");
        databaseReference.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot dataSnapshot = task.getResult();
                String usertype = String.valueOf(dataSnapshot.child("userType").getValue());
//                REDIRECT USER TO RESPECTIVE SCREENS
                if (usertype.equalsIgnoreCase(getString(R.string.choice_commuter))){
                    Intent intent = new Intent(MainActivity.this, CommMapActivity.class);
                    startActivity(intent);
                    finish();
                } else if (usertype.equalsIgnoreCase(getString(R.string.label_operator))) {
                    Intent intent = new Intent(MainActivity.this, OperMapActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void showLogin() {
        Intent intent = new Intent(this, CommLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showSignup() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, CommSignupActivity.class);
        startActivity(intent);
        finish();
    }
}
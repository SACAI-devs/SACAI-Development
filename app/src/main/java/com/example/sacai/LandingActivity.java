package com.example.sacai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.sacai.commuter.CommLoginActivity;
import com.example.sacai.commuter.CommMainActivity;
import com.example.sacai.commuter.CommSignupActivity;
import com.example.sacai.databinding.ActivityLandingBinding;
import com.example.sacai.operator.OperLoginActivity;
import com.example.sacai.operator.OperMainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LandingActivity extends AppCompatActivity {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2223;
    private Boolean mLocationPermissionsGranted = false;
    private Boolean mBackgroundPermissionsGranted = false;


    // Bind activity to layout
    ActivityLandingBinding binding;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLandingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Initialize Firebase Auth and check if user is logged in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            getLocationPermission();
            if (mLocationPermissionsGranted && mBackgroundPermissionsGranted) {
                showMainActivity(currentUser.getUid());
            }

        }




        // Show LOGIN PAGE when btn is clicked
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationPermission();
                if (mLocationPermissionsGranted && mBackgroundPermissionsGranted) {
                    showLogin();
                }
            }
        });

        // Show SIGNUP PAGE when btn is clicked
        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationPermission();
                if (mLocationPermissionsGranted && mBackgroundPermissionsGranted) {
                    showSignup();
                }
            }
        });

        // Switch to OPERATOR LOGIN when btn is clicked
        binding.btnSwitchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationPermission();
                if (mLocationPermissionsGranted && mBackgroundPermissionsGranted) {
                    showOperLogin();
                }
            }
        });
    }

    private void showMainActivity (String uid){
        // Get usertype from table
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = db.getReference("Users");
        databaseReference.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot dataSnapshot = task.getResult();
                String usertype = String.valueOf(dataSnapshot.child("userType").getValue());
                // Redirect user to respective screens
                if (usertype.equalsIgnoreCase(getString(R.string.label_commuter))){
                    showMainComm();
                } else if (usertype.equalsIgnoreCase(getString(R.string.label_operator))) {
                    showMainOper();
                }
            }
        });
    }

    private void showLogin() {
        FirebaseAuth.getInstance().signOut();
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

    private void showOperLogin() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(LandingActivity.this, OperLoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void showMainComm(){
        // Check if user is logged in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.msg_loginToEdit, Toast.LENGTH_SHORT).show();
            logout();
        // Check if email is verified
        } else if (!currentUser.isEmailVerified()){
            Toast.makeText(this, R.string.msg_checkEmailForVerifyLink, Toast.LENGTH_SHORT).show();
            logout();
        } else {
            Intent intent = new Intent(LandingActivity.this, CommMainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void showMainOper(){
        // Check if user is logged in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.msg_loginToEdit, Toast.LENGTH_SHORT).show();
            logout();
        // Check if email is verified
        } else if (!currentUser.isEmailVerified()){
            Toast.makeText(this, R.string.msg_checkEmailForVerifyLink, Toast.LENGTH_SHORT).show();
            logout();
        } else {
            Intent intent = new Intent(LandingActivity.this, OperMainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    // Request for location permissions
    private void getLocationPermission() {
        Log.i("ClassCalled", "getLocationPermission: is running");
        String TAG = "mainActivity_getLocationPermission";
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};

        // FOR FINE LOCATION ACCESS PERMISSIONS
        if ((ContextCompat.checkSelfPermission(Objects.requireNonNull(this),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) || ContextCompat.checkSelfPermission(this, BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLocationPermissionsGranted = true;
            mBackgroundPermissionsGranted = true;
            // sets location permissions as granted

            Log.i(TAG, "mLocationPermissionGranted: true");
            Log.i(TAG, "mBackgroundPermissionsGranted: true");
        }else {
            // Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show user a dialog on why the permission is needed
                // then ask for the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                Toast.makeText(this, "SACAI needs fine locations permissions to work. Enable them in your settings.", Toast.LENGTH_LONG).show();

                Log.i("getLocationPermission", "displayRationale: executed");

            } else {
                // no dialogue explanation needed
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);

                Log.i("getLocationPermission", "noRationale: executed");
                Toast.makeText(this, "SACAI needs fine locations permissions to work. Enable them in your settings.", Toast.LENGTH_LONG).show();

            }

            // FOR BACKGROUND LOCATION ACCESS PERMISSIONS
            // Check if FINE location permission is GRANTED
            if ((ContextCompat.checkSelfPermission(Objects.requireNonNull(this),
                    BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                mBackgroundPermissionsGranted = true;
                Log.i("getLocationPermission", "backgroundPermissionsCheck: background location access granted");
            }else {

                // Ask for permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Show user a dialog on why the permission is needed
                    // then ask for the permission

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    Toast.makeText(this, "SACAI needs background locations access permissions to work. Enable them in your settings.", Toast.LENGTH_LONG).show();


                    Log.i("getLocationPermission", "displayRationale: executed");


                } else {
                    // no dialogue explanation needed
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);

                    Log.i("getLocationPermission", "backgroundAccess.noRationale: executed");
                    Toast.makeText(this, "SACAI needs background locations access permissions to work. Enable them in your settings.", Toast.LENGTH_LONG).show();
                }
            }
        }

    }


}


package com.example.sacai.commuter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.sacai.LandingActivity;
import com.example.sacai.R;
import com.example.sacai.commuter.fragments.CommMapFrag;
import com.example.sacai.databinding.ActivityCommMainBinding;
import com.example.sacai.commuter.fragments.CommProfileFrag;
import com.example.sacai.commuter.viewmodels.CommMainViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CommMainActivity extends AppCompatActivity {

    // Bind activity to layout
    ActivityCommMainBinding binding;


    // Version checking if user has Google Play Services installed
    private static final String TAG = "CommMapFrag";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2223;
    private Boolean mLocationPermissionsGranted = false;


    private CommMainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase and check if user is logged in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.msg_loginToEdit, Toast.LENGTH_SHORT).show();
            logout();
        // Check if email is verified
        } else if (!currentUser.isEmailVerified()){
            Toast.makeText(this, R.string.msg_checkEmailForVerifyLink, Toast.LENGTH_SHORT).show();
            logout();
        }

        // ViewModel Logic: Gets signal from fragment for toast messages
//        viewModel = new ViewModelProvider(this).get(CommMainViewModel.class);
//        viewModel.getResult().observe(this, item -> {
//            if (item == true) {
//                Toast.makeText(this, R.string.msg_success, Toast.LENGTH_SHORT).show();
//            } else {
//                viewModel.getMsg().observe(this, msg -> {
//                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//                });
//            }
//        });

        // Location Services
        getLocationPermission();

        // Loads map if services are OK
        if (isServicesOK()) {
            init();
        }

        // Toolbar action handling
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    // Toolbar menu actions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.comm_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_showHome:
                replaceFragment(new CommMapFrag());
                return true;
            case R.id.action_showEditProfile:
                replaceFragment(new CommProfileFrag());
                return true;
            case R.id.action_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(CommMainActivity.this, LandingActivity.class);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void init() {
        replaceFragment(new CommMapFrag());
    }

    // Check if GoogleApiServices is TRUE
    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: Checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(CommMainActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            // Everything is fine and user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            // An error occurred but is RESOLVABLE
            Log.d(TAG, "isServicesOK: An error occured. Resolvable.");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(CommMainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            // An error occurred that cannot be fixed
            Toast.makeText(this, "You can't make map requests.", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    // Request for location permissions
    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    // Look for request permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionsGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    // Initialize the map
                }
            }
        }
    }
}
package com.example.sacai.operator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.sacai.LandingActivity;
import com.example.sacai.R;
import com.example.sacai.databinding.ActivityOperMainBinding;
import com.example.sacai.operator.fragments.OperMapFrag;
import com.example.sacai.operator.fragments.OperPassengerListFrag;
import com.example.sacai.operator.fragments.OperProfileFrag;
import com.example.sacai.operator.fragments.OperQRCodeWithSaveFrag;
import com.example.sacai.operator.fragments.OperRideHistoryFrag;
import com.example.sacai.operator.viewmodels.OperMainViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OperMainActivity extends AppCompatActivity {

    // Bind activity to layout
    ActivityOperMainBinding binding;

    private OperMainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth and check if user is logged in
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


        // ViewModel Logic
        viewModel = new ViewModelProvider(this).get(OperMainViewModel.class);
        viewModel.getResult().observe(this, item -> {
            if (item == true) {
                Toast.makeText(this, R.string.msg_success, Toast.LENGTH_SHORT).show();
            } else {
                viewModel.getMsg().observe(this, msg -> {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });
            }
        });

        // Load map fragment
        replaceFragment(new OperMapFrag());

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
        inflater.inflate(R.menu.oper_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_showHome:
                replaceFragment(new OperMapFrag());
                return true;
            case R.id.action_showEditProfile:
                replaceFragment(new OperProfileFrag());
                return true;
//            case R.id.action_showRideHistory:
//                replaceFragment(new OperRideHistoryFrag());
//                return true;
            case R.id.action_showPassengerList:
                replaceFragment(new OperPassengerListFrag());
                return true;
            case R.id.action_scanQR:
                replaceFragment(new OperQRCodeWithSaveFrag());
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
        Intent intent = new Intent(this, LandingActivity.class);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}
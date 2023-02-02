package com.example.sacai;

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

import com.example.sacai.databinding.ActivityCommMainBinding;
import com.example.sacai.fragments.CommMapFrag;
import com.example.sacai.fragments.CommProfileFrag;
import com.example.sacai.viewmodels.CommMainViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CommMainActivity extends AppCompatActivity {

//    BIND ACTIVITY TO LAYOUT
    ActivityCommMainBinding binding;

    private CommMainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        INITIALIZE FIREBASE AUTH AND CHECK IF USER IS LOGGED IN
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    if (currentUser == null) {
        Toast.makeText(this, R.string.msg_loginToEdit, Toast.LENGTH_SHORT).show();
        logout();
    }

//    VIEWMODEL LOGIC
        viewModel = new ViewModelProvider(this).get(CommMainViewModel.class);
        viewModel.getResult().observe(this, item -> {
            if (item == true) {
                Toast.makeText(this, R.string.msg_success, Toast.LENGTH_SHORT).show();
            } else {
                viewModel.getMsg().observe(this, msg -> {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });
            }
        });

//        LOAD MAP FRAGMENT
        replaceFragment(new CommMapFrag());

//        TOOLBAR ACTION HANDLING
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    //  TOOLBAR MENU ACTIONS
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
}
package com.example.sacai.operator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.sacai.R;
import com.example.sacai.commuter.CommSignupActivity;
import com.example.sacai.databinding.ActivityOperSignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OperSignupActivity extends AppCompatActivity {

    // Bind activity to layout
    ActivityOperSignupBinding binding;

    // Initialize Firebase Auth
    private FirebaseAuth mAuth;

    public static String EXTRA_DR_NAME = "driver's name";
    public static String EXTRA_CON_NAME = "conductor's name";
    public static String EXTRA_FRANCHISE = "franchise name";
    public static String EXTRA_PLATE = "plate number";
    public static boolean EXTRA_WHEELCHAIR = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperSignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check if user is logged in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            return;
        }


        // Switch to COMMUTER SIGNUP when btn is clicked
        binding.btnSwitchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommSignup();
            }
        });

        // Show NEXT PAGE when btn is clicked
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNextPage();
            }
        });

        // Switch to OPERATOR LOGIN when btn is clicked
        binding.btnSwitchLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOperLogin();
            }
        });

        // Toolbar action handling
        Toolbar toolbar = (Toolbar) binding.toolbar;
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    // Toolbar menu actions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.switch_to_comm_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_switch:
                Intent intent = new Intent (this, CommSignupActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showOperLogin() {
        Intent intent = new Intent(this, OperLoginActivity.class);
        startActivity(intent);
    }

    private void showNextPage() {
        String driverName = binding.etDriverName.getText().toString().trim();
        String conductorName = binding.etConductorName.getText().toString().trim();
        String franchise = binding.etFranchise.getText().toString().trim();
        String plate = binding.etPlate.getText().toString().trim();

        boolean wheelchair = binding.cbWheelchair.isChecked();

        // Field validation
        if (driverName.isEmpty() || conductorName.isEmpty() ) {
            if (driverName.isEmpty()) {
                binding.etDriverName.setError(getString(R.string.err_fieldRequired));
                binding.etDriverName.requestFocus();
            }
            if (conductorName.isEmpty()) {
                binding.etDriverName.setError(getString(R.string.err_fieldRequired));
                binding.etDriverName.requestFocus();
            }
            if (franchise.isEmpty()) {
                binding.etFranchise.setError(getString(R.string.err_fieldRequired));
                binding.etFranchise.requestFocus();
            }
            if (plate.isEmpty()) {
                binding.etPlate.setError(getString(R.string.err_fieldRequired));
                binding.etPlate.requestFocus();
            }
            Toast.makeText(this, R.string.err_emptyRequiredFields, Toast.LENGTH_SHORT).show();
        } else if (!isAlphabetical(driverName) || !isAlphabetical(conductorName)) {
            if (!isAlphabetical(driverName)){
                binding.etDriverName.setError(getString(R.string.err_invalidCharacterInput));
                binding.etDriverName.requestFocus();
            }
            if (!isAlphabetical(conductorName)){
                binding.etConductorName.setError(getString(R.string.err_invalidCharacterInput));
                binding.etConductorName.requestFocus();
            }
        } else {
            Intent intent = new Intent(this, OperSignup2Activity.class);

            intent.putExtra(EXTRA_DR_NAME, driverName);
            intent.putExtra(EXTRA_CON_NAME, conductorName);
            intent.putExtra(EXTRA_FRANCHISE, franchise);
            intent.putExtra(EXTRA_PLATE, plate);
            intent.putExtra(String.valueOf(EXTRA_WHEELCHAIR),wheelchair);
            startActivity(intent);
            finish();
        }
    }

    public static boolean isAlphabetical(String s){
        return s != null && s.matches("^[a-zA-Z ]*$");

    }

    private void showCommSignup() {
        Intent intent = new Intent(this, CommSignupActivity.class);
        startActivity(intent);
        finish();
    }
}
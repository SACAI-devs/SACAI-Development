package com.example.sacai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sacai.databinding.ActivityOperSignup2Binding;
import com.example.sacai.databinding.ActivityOperSignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OperSignupActivity extends AppCompatActivity {

//    BIND ACTIVITY TO LAYOUT
    ActivityOperSignupBinding binding;
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

//        INITIALIZE FIREBASE AUTH AND CHECK IF USER IS LOGGED IN
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            return;
        }


//        SWITCH TO COMMUTER SIGNUP WHEN BTN IS CLICKED
        binding.btnSwitchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommSignup();
            }
        });

//        SHOW SECOND PAGE OF OPERATOR SIGNUP WHEN BTN IS CLICKED
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNextPage();
            }
        });

//        SHOW OPERATOR LOGIN PAGE WHEN BTN IS CLICKED
        binding.btnSwitchLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOperLogin();
            }
        });
    }

    private void showOperLogin() {
        Intent intent = new Intent(this, OperLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showNextPage() {
        String driverFn = binding.etDriverName.getText().toString().trim();
        String conductorFn = binding.etConductorName.getText().toString().trim();
        String franchise = binding.etFranchise.getText().toString().trim();
        String plate = binding.etPlate.getText().toString().trim();

        boolean wheelchair = binding.cbWheelchair.isChecked();

//        CHECK FOR EMPTY REQUIRED FIELDS
        if (driverFn.isEmpty() || conductorFn.isEmpty() ) {
            if (driverFn.isEmpty()) {
                binding.etDriverName.setError(getString(R.string.err_fieldRequired));
                binding.etDriverName.requestFocus();
            }
            if (conductorFn.isEmpty()) {
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
        } else {
            Intent intent = new Intent(this, OperSignup2Activity.class);

            intent.putExtra(EXTRA_DR_NAME, driverFn);
            intent.putExtra(EXTRA_CON_NAME, conductorFn);
            intent.putExtra(EXTRA_FRANCHISE, franchise);
            intent.putExtra(EXTRA_PLATE, plate);
            intent.putExtra(String.valueOf(EXTRA_WHEELCHAIR),wheelchair);
            startActivity(intent);
            finish();
        }
    }

    private void showCommSignup() {
        Intent intent = new Intent(this, CommSignupActivity.class);
        startActivity(intent);
        finish();
    }
}
package com.example.sacai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OperSignupActivity extends AppCompatActivity {

    EditText etDriverName,  etCondName, etFranchise, etPlate;

    CheckBox cbWheelchair;
    Button btnSwitchUser, btnNext, btnSwitchLogin;

    public static String EXTRA_DR_NAME = "driver's name";
    public static String EXTRA_CON_NAME = "conductor's name";

    public static String EXTRA_FRANCHISE = "franchise name";

    public static String EXTRA_PLATE = "plate number";

    public static boolean EXTRA_WHEELCHAIR = false;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oper_signup);

//        Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
            return;
        }

        etDriverName = findViewById(R.id.operSignup_etDriverName);
        etCondName = findViewById(R.id.operSignup_etCondName);
        etFranchise = findViewById(R.id.operSignup_etFranchise);
        etPlate = findViewById(R.id.operSignup_etPlate);
        cbWheelchair = findViewById(R.id.operSignup_cbWheelchair);

//        SWITCH TO COMMUTER SIGNUP WHEN BTN IS CLICKED
        btnSwitchUser = findViewById(R.id.operSignup_btnSwitchUser);
        btnSwitchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommSignup();
            }
        });

//        SHOW SECOND PAGE OF OPERATOR SIGNUP WHEN BTN IS CLICKED
        btnNext = findViewById(R.id.operSignup_btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNextPage();
            }
        });

//        SHOW OPERATOR LOGIN PAGE WHEN BTN IS CLICKED
        btnSwitchLogin = findViewById(R.id.operSignup_btnSwitchLogin);
        btnSwitchLogin.setOnClickListener(new View.OnClickListener() {
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
        String driverFn = etDriverName.getText().toString();
        String conductorFn = etCondName.getText().toString();
        String franchise = etFranchise.getText().toString();
        String plate = etPlate.getText().toString();

        boolean wheelchair = cbWheelchair.isChecked();

//        CHECK FOR EMPTY REQUIRED FIELDS
        if (driverFn.isEmpty() || conductorFn.isEmpty() ) {
            if (driverFn.isEmpty()) {
                etDriverName.setError(getString(R.string.err_fieldRequired));
                etDriverName.requestFocus();
            }
            if (conductorFn.isEmpty()) {
                etDriverName.setError(getString(R.string.err_fieldRequired));
                etDriverName.requestFocus();
            }
            if (franchise.isEmpty()) {
                etFranchise.setError(getString(R.string.err_fieldRequired));
                etFranchise.requestFocus();
            }
            if (plate.isEmpty()) {
                etPlate.setError(getString(R.string.err_fieldRequired));
                etPlate.requestFocus();
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
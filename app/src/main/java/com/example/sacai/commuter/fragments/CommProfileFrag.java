package com.example.sacai.commuter.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.sacai.R;
import com.example.sacai.commuter.CommUpdateEmailActivity;
import com.example.sacai.databinding.FragmentCommProfileBinding;
import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.commuter.viewmodels.CommMainViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class CommProfileFrag extends Fragment {

    // Bind fragment to layout
    FragmentCommProfileBinding binding;

    String[] items;
    ArrayAdapter<String> homeAddress;                    // For the drop down
    ArrayAdapter<String> workAddress;               // For the drop down
    ArrayList<String> stopName = new ArrayList<>();     // Store station names here
    String chosenHomeAddress;
    String chosenWorkAddress;
    View mView;
    AutoCompleteTextView etHomeAddress,etWorkAddress;


//    CommMainViewModel viewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCommProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Read user data and display
        readData(currentUser.getUid());

        getStations();
        // Syncs Mobility Impairment when Wheelchair User is checked
        binding.cbWheelchair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.cbWheelchair.isChecked()) {
                    binding.cbMobility.setChecked(true);
                    binding.cbMobility.setEnabled(false);
                } else {
                    binding.cbMobility.setEnabled(true);
                }
            }
        });
        binding.cbMobility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.cbWheelchair.isChecked()) {
                    binding.cbMobility.setChecked(true);
                    binding.cbMobility.setEnabled(false);
                } else {
                    binding.cbMobility.setEnabled(true);
                }
            }
        });

        // User re-authentication to change email
        binding.btnUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateEmail();
            }
        });

        // Loads selection items into home and work address drop downs
        binding.etHomeAddress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getStations();
                String station = parent.getItemAtPosition(position).toString();
                binding.etHomeAddress.setText(station);
            }
        });

        binding.etWorkAddress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getStations();
                String station = parent.getItemAtPosition(position).toString();
                binding.etWorkAddress.setText(station);
            }
        });

        // Save changes to profile when btn is clicked
        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges(currentUser.getUid());
            }
        });
    }

    private void showUpdateEmail() {
        Intent intent = new Intent(getActivity(), CommUpdateEmailActivity.class);
        startActivity(intent);
    }

    // Function to get all the stations to put in the array list
    private void getStations() {
        Log.i("ClassCalled", "getStations is running");
        String TAG = "getStations";

        // Get database reference to retrieve bus stops
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bus_Stop");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stopName.clear();

                for (DataSnapshot dsp : snapshot.getChildren()) {
                    try {
                        String name = dsp.child("busStopName").getValue().toString();
                        Log.i(TAG, "onDataChange: busStopName" + name);
                        stopName.add(name);
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: exception ", e);
                    }
                }

                // Convert arraylist to a string[]
                items  = new String[stopName.size()];
                for (int i = 0; i < stopName.size(); i++) {
                    items[i] = stopName.get(i);
                    Log.i(TAG, "onDataChange: stopName " + stopName.get(i));
                }
                Log.i(TAG, "onDataChange: items " + items);

                homeAddress = new ArrayAdapter<>(getActivity(), R.layout.dropdown_list, items);
                binding.etHomeAddress.setAdapter(homeAddress);

                workAddress = new ArrayAdapter<>(getActivity(), R.layout.dropdown_list, items);
                binding.etWorkAddress.setAdapter(workAddress);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "onCancelled: database error " + error);
            }
        });
    }

    private void readData(String uid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        databaseReference.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        // Get the data from each node
                        DataSnapshot dataSnapshot = task.getResult();
                        String firstname = String.valueOf(dataSnapshot.child("firstname").getValue());
                        String lastname = String.valueOf(dataSnapshot.child("lastname").getValue());
                        String username = String.valueOf(dataSnapshot.child("username").getValue());
                        String homeAddress = String.valueOf(dataSnapshot.child("homeAddress").getValue());
                        String workAddress = String.valueOf(dataSnapshot.child("workAddress").getValue());
                        String mobility = String.valueOf(dataSnapshot.child("mobility").getValue());
                        String auditory = String.valueOf(dataSnapshot.child("auditory").getValue());
                        String wheelchair = String.valueOf(dataSnapshot.child("wheelchair").getValue());
                        // TODO: UPDATE EMAIL FEATURE
                        String email = String.valueOf(dataSnapshot.child("email").getValue());

                        if (username.equals("null")) {
                            username = "";
                        }

                        if (homeAddress.equals("null")) {
                            homeAddress = "";
                        }

                        if (workAddress.equals("null")) {
                            workAddress = "";
                        }

                        // Set the retrieved
                        binding.etFirstname.setText(firstname);
                        binding.etLastname.setText(lastname);
                        binding.etEmail.setText(email);
                        binding.etUsername.setText(username);
                        binding.etHomeAddress.setText(homeAddress);
                        binding.etWorkAddress.setText(workAddress);
                        binding.cbMobility.setChecked(Boolean.parseBoolean(mobility));
                        binding.cbAuditory.setChecked(Boolean.parseBoolean(auditory));
                        binding.cbWheelchair.setChecked(Boolean.parseBoolean(wheelchair));
                    } else {
//                        viewModel.setData(false);
                        Toast.makeText(getActivity(), R.string.err_failedToReadData, Toast.LENGTH_SHORT).show();
                    }
                } else {
//                        viewModel.setData(false);
                    Toast.makeText(getActivity(), R.string.err_failedToReadData, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveChanges(String uid) {

        String username = binding.etUsername.getText().toString().trim();
        String firstname = binding.etFirstname.getText().toString().trim();
        String lastname = binding.etLastname.getText().toString().trim();
        String home = binding.etHomeAddress.getText().toString();
        String work = binding.etWorkAddress.getText().toString();

        boolean mobility = binding.cbMobility.isChecked();
        boolean auditory = binding.cbAuditory.isChecked();
        boolean wheelchair = binding.cbWheelchair.isChecked();

        // Check if required inputs are empty
        if (firstname.isEmpty() || lastname.isEmpty()) {
            Toast.makeText(getActivity(), R.string.err_emptyRequiredFields, Toast.LENGTH_SHORT).show();
            if (firstname.isEmpty()) {
                binding.etFirstname.setError(getString(R.string.err_fieldRequired));
                binding.etFirstname.requestFocus();
            }
            if (lastname.isEmpty()) {
                binding.etLastname.setError(getString(R.string.err_fieldRequired));
                binding.etLastname.requestFocus();
            }
//        CHECK FOR INVALID CHARACTERS IN THE FIRST AND LAST NAME FIELDS
        } else if (!isAlphabetical(firstname) || !isAlphabetical(lastname) || !isAlphabetical(username)) {
            if (!isAlphabetical(firstname)){
                binding.etFirstname.setError(getString(R.string.err_invalidCharacterInput));
                binding.etFirstname.requestFocus();
            }
            if (!isAlphabetical(lastname)){
                binding.etLastname.setError(getString(R.string.err_invalidCharacterInput));
                binding.etLastname.requestFocus();
            }
            if (!isAlphabetical(username)){
                binding.etUsername.setError(getString(R.string.err_invalidCharacterInput));
                binding.etUsername.requestFocus();
            }
        } else if (binding.etHomeAddress.equals(binding.etWorkAddress)) {
            binding.etHomeAddress.setError("Can't be the same as work address");
            binding.etWorkAddress.setError("Can't be the same as home address");

            binding.etHomeAddress.requestFocus();
            binding.etWorkAddress.requestFocus();
        } else if (mobility == false && auditory == false) {
            Toast.makeText(getActivity(), R.string.err_emptyRequiredFields, Toast.LENGTH_SHORT).show();
        } else {
            // Maps the variables to the nodes where values should be stored
            HashMap User = new HashMap();
            User.put("firstname", firstname);
            User.put("lastname", lastname);
            User.put("username", username);
            User.put("homeAddress", home);
            User.put("workAddress", work);
            User.put("mobility", mobility);
            User.put("auditory", auditory);
            User.put("wheelchair", wheelchair);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
            databaseReference.child(uid).updateChildren(User).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    // Signals host activity for an appropriate toast message
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), R.string.msg_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.err_unknown, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Just to check if string is alphabetical
    public static boolean isAlphabetical(String s){
        return s != null && s.matches("^[a-zA-Z ]*$");
    }
}

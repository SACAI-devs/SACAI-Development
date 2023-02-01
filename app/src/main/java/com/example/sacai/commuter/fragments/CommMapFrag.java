package com.example.sacai.commuter.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.sacai.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CommMapFrag extends Fragment implements OnMapReadyCallback {
    // Call variables based on Google Documentation
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;

    // Components
    Button btnSetRoute;
    AutoCompleteTextView etPickup, etDropoff;

    // Array to store stops
    ArrayAdapter<String> pickUpStations;
    ArrayAdapter<String> dropOffStations;

    // Store choices
    String choicePickup;
    String choiceDropoff;

    // Required empty public construction
    public CommMapFrag() {
    }

    // Create a map view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_comm_map, container, false);
        return mView;
    }

    // Should have the same MAP ID as the XML file
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSetRoute = (Button) mView.findViewById(R.id.btnSetRoute);
        etPickup = (AutoCompleteTextView) mView.findViewById(R.id.etPickup);
        etDropoff = (AutoCompleteTextView) mView.findViewById(R.id.etDropoff);

        mMapView = (MapView) mView.findViewById(R.id.commuter_map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

        getStations();
        
        btnSetRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCommuterRoute();
            }
        });

    }

    private void setCommuterRoute() {
        String pickup = etPickup.getText().toString();
        String dropoff = etDropoff.getText().toString();
        
        // Field validation
        if (pickup.equals(dropoff)) {
            Toast.makeText(getActivity(), R.string.err_cant_have_same_pickup_dropoff, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getActivity(), "No back-end yet.", Toast.LENGTH_SHORT).show();

    }

    // Custom map logic and configurations
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        getStations();

        //=============Testing==============//
        googleMap.addMarker(new MarkerOptions().position(new LatLng(14.574970139259474, 121.09785961494917)).title("Rainforest Park"));
        CameraPosition rainforestPark = CameraPosition.builder().target(new LatLng(14.574970139259474, 121.09785961494917)).zoom(16).bearing(0).tilt(0).build();
        googleMap.moveCamera((CameraUpdateFactory.newCameraPosition(rainforestPark)));
    }

    private void getStations() {
        ArrayList<String> results = new ArrayList<String>(); // Store results here
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bus_Stop");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp: dataSnapshot.getChildren()) {
                    String name = dsp.child("busStopName").getValue().toString();
                    results.add(name); // adds results to array list
                }
                // Convert arraylist to a string[]
                String[] pickupItems = new String[results.size()];
                for (int i = 0; i < results.size(); i++) {
                    pickupItems[i] = results.get(i);
                }
                pickUpStations = new ArrayAdapter<String>(getActivity(), R.layout.component_list_item, pickupItems);
                // Selecting from pickup
                etPickup.setAdapter(pickUpStations);
                etPickup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        choicePickup = parent.getItemAtPosition(position).toString();
                    }
                });
                // Convert arraylist to a string[]
                String[] dropoffItems = new String[results.size()];
                for (int i = 0; i < results.size(); i++) {
                    dropoffItems[i] = results.get(i);
                }
                dropOffStations = new ArrayAdapter<String>(getActivity(), R.layout.component_list_item, dropoffItems);
                // Selecting from dropoff
                etDropoff.setAdapter(dropOffStations);
                etDropoff.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        choiceDropoff = parent.getItemAtPosition(position).toString();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Couldn't retrieve bus stops. Please refresh.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
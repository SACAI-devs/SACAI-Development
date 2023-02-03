package com.example.sacai.commuter.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.Trip;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class CommMapFrag extends Fragment implements OnMapReadyCallback {
    // Call variables based on Google Documentation
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;

    // Components
    Button btnSetRoute;
    AutoCompleteTextView etPickup, etDropoff;

    // Arrays
    String[] items;
    ArrayAdapter<String> pickUpStations; // For the drop down
    ArrayAdapter<String> dropOffStations; // For the drop down
    ArrayList<String> stationId = new ArrayList<>(); // Store station id
    ArrayList<String> stationName = new ArrayList<>(); // Store station names here
    ArrayList<Double> latitude = new ArrayList<>(); // Store latitude of stations
    ArrayList<Double> longitude = new ArrayList<>(); // Store longitude of stations

    // Store choices
    String choicePickup;
    String choiceDropoff;

    // CONSTANTS
    private int MAP_ZOOM = 18;


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
        getStations();
        return mView;
    }

    // Should have the same MAP ID as the XML file
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind components to layout
        btnSetRoute = (Button) mView.findViewById(R.id.btnSetRoute);
        etPickup = (AutoCompleteTextView) mView.findViewById(R.id.etPickup);
        etDropoff = (AutoCompleteTextView) mView.findViewById(R.id.etDropoff);

        mMapView = (MapView) mView.findViewById(R.id.commuter_map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

        // Saves ride data when btn is clicked
        btnSetRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentTrip();
                setTripMarkers();
                // SEND INFORMATION TO AN OPERATOR THAT THEY ARE THERE
                // AN OPERATOR SHOULD ACCEPT
            }
        });
    }

    private void setTripMarkers() {
        // Clear map existing markers
        mGoogleMap.clear();
        int width = 100;
        int height = 100;
        BitmapDrawable bus_icon = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_bus_stop);
        Bitmap b = bus_icon.getBitmap();
        Bitmap iconified = Bitmap.createScaledBitmap(b, width, height, false);
        // Set new marker points
        for (int i = 0; i < stationName.size(); i++) {
            if (stationName.get(i).equals(choicePickup)) {
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude.get(i), longitude.get(i)))
                        .title(stationName.get(i))
                        .icon(BitmapDescriptorFactory.fromBitmap(iconified)));
            }
            if (stationName.get(i).equals(choiceDropoff)) {
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude.get(i), longitude.get(i)))
                        .title(stationName.get(i))
                        .icon(BitmapDescriptorFactory.fromBitmap(iconified)));
            }
        }
    }

    // Custom map logic and configurations
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap = googleMap;

        getStations();
        generateStationMarkers();


        // Moves camera to where the station is at
        etPickup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getStations();
                generateStationMarkers();
                String station = parent.getItemAtPosition(position).toString();
                etPickup.setText(station);
                // Pan camera to selected station
                CameraPosition mapCam = CameraPosition.builder()
                        .target(new LatLng(latitude.get(position), longitude.get(position)))
                        .zoom(MAP_ZOOM).bearing(0).tilt(0).build();
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mapCam));
            }
        });

        // Moves camera to where the station is at
        etDropoff.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getStations();
                generateStationMarkers();
                String station = parent.getItemAtPosition(position).toString();
                etDropoff.setText(station);
                // Pan camera to selected station
                CameraPosition mapCam = CameraPosition.builder()
                    .target(new LatLng(latitude.get(position), longitude.get(position)))
                    .zoom(MAP_ZOOM).bearing(0).tilt(0).build();
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mapCam));
            }
        });

        // Default camera placement
        CameraPosition rainforestPark = CameraPosition.builder()
                .target(new LatLng(14.554705128006361, 121.09289228652042))
                .zoom(15).bearing(0).tilt(0).build();
        googleMap.moveCamera((CameraUpdateFactory.newCameraPosition(rainforestPark)));
    }

    private void generateStationMarkers() {
        mGoogleMap.clear(); // Clear existing markers
        int width = 100;
        int height = 100;
        BitmapDrawable bus_icon = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_bus_stop);
        Bitmap b = bus_icon.getBitmap();
        Bitmap iconified = Bitmap.createScaledBitmap(b, width, height, false);
        // Generate new markers for each station
        for (int i = 0; i < stationId.size(); i++) {
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude.get(i), longitude.get(i)))
                    .title(stationName.get(i))
                    .icon(BitmapDescriptorFactory.fromBitmap(iconified)));
        }
    }

    private void getStations() {
        // This method gets the stations registered from Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bus_Stop");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stationId.clear();
                stationName.clear();
                latitude.clear();
                longitude.clear();

                for (DataSnapshot dsp: dataSnapshot.getChildren()) {
                    // Get data from each node
                    String id = dsp.getKey();
                    String name = dsp.child("busStopName").getValue().toString();
                    Double lat = Double.parseDouble(dsp.child("center_lat").getValue().toString());
                    Double lon = Double.parseDouble(dsp.child("center_long").getValue().toString());
                    // Adds data to array list
                    stationId.add(id);
                    stationName.add(name);
                    latitude.add(lat);
                    longitude.add(lon);
                }

                // Convert arraylist to a string[]
                items = new String[stationName.size()];
                for (int i = 0; i < stationName.size(); i++) {
                    items[i] = stationName.get(i);
                }
                // Selecting from pickup
                pickUpStations = new ArrayAdapter<String>(getActivity(), R.layout.component_list_item, items);
                etPickup.setAdapter(pickUpStations);
                // Selecting from dropoff
                dropOffStations = new ArrayAdapter<String>(getActivity(), R.layout.component_list_item, items);
                etDropoff.setAdapter(dropOffStations);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Couldn't retrieve bus stops. Please refresh.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setCurrentTrip() {
        // Get the current date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        String date = sdf.format(c.getTime());
        String time = String.valueOf(Calendar.getInstance().getTime());

        choicePickup= etPickup.getText().toString();
        choiceDropoff= etDropoff.getText().toString();

        // Field validation
        if (choicePickup.isEmpty() || choiceDropoff.isEmpty()) {
            if (choicePickup.isEmpty()) {
                etPickup.setError(getString(R.string.err_fieldRequired));
            }
            if (choiceDropoff.isEmpty()) {
                etDropoff.setError(getString(R.string.err_fieldRequired));
            }
            Toast.makeText(getActivity(), R.string.err_emptyRequiredFields, Toast.LENGTH_SHORT).show();
            return;
        } else if (choicePickup.equals(choiceDropoff)) {
            Toast.makeText(getActivity(), R.string.err_cant_have_same_pickup_dropoff, Toast.LENGTH_SHORT).show();
            return;
        }
        
        findMidpoint(choicePickup, choiceDropoff); // Finding the midpoint between the pickup and drop off
        Toast.makeText(getActivity(), "Now waiting for bus.", Toast.LENGTH_SHORT).show();

        String pickupId = null;
        String dropoffId = null;

        // Get station ids
        for (int i = 0; i < stationName.size(); i++) {
            if (stationName.get(i).equals(choicePickup)) {
                pickupId = stationId.get(i);
            }
            if (stationName.get(i).equals(choiceDropoff)) {
                dropoffId = stationId.get(i);
            }
        }

        // Create the node for a current trip
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Trip current_trip = new Trip(date, time, "", pickupId, dropoffId);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());

        // Temporary for testing
        if (databaseReference != null) {
            // Supposed to happen whenever the trip ends
            DatabaseReference nodeToRemove = databaseReference.child(user.getUid()).child("current_trip");
            nodeToRemove.removeValue();
        }
        databaseReference.child(user.getUid()).child("current_trip").push().setValue(current_trip);

    }

    private void findMidpoint(String pickup, String dropoff) {
        Double pickupLatitude = 0.0;
        Double pickupLongitude = 0.0;
        Double dropoffLatitude = 0.0;
        Double dropoffLongitude = 0.0;

        // Get the center of two points
        for (int i = 0; i < stationName.size(); i++) {
            if (stationName.get(i).equals(pickup)) {
                pickupLatitude = latitude.get(i);
                pickupLongitude = longitude.get(i);
            }
            if (stationName.get(i).equals(dropoff)) {
                dropoffLatitude = latitude.get(i);
                dropoffLongitude = longitude.get(i);
            }
        }
        // Calculate for the midpoint between the two locations
        Double midLat = (pickupLatitude + dropoffLatitude)/2;
        Double midLong = (pickupLongitude + dropoffLongitude)/2;


        // Clear the map of the other markers
        // Move the camera to new midpoint location
        CameraPosition midpoint = CameraPosition.builder()
                .target(new LatLng(midLat, midLong))
                .zoom(13).bearing(0).tilt(0).build();
        mGoogleMap.moveCamera((CameraUpdateFactory.newCameraPosition(midpoint)));

    }


}
package com.example.sacai.commuter.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
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
import java.util.HashMap;
import java.util.Objects;

public class CommMapFrag extends Fragment implements OnMapReadyCallback {
    // Call variables based on Google Documentation
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;
    private GeofencingClient geofencingClient;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2223;
    private Boolean mLocationPermissionsGranted = false;
    private String[] permissions = {FINE_LOCATION, COARSE_LOCATION};
    // Components
    Button btnSetRoute;
    Button btnDisembarked;
    AutoCompleteTextView etOrigin, etDestination;
    TextInputLayout selectOrigin, selectDestination;
    Marker originMark;
    Marker destinationMark;

    // Arrays
    String[] items;
    ArrayAdapter<String> originStop;                    // For the drop down
    ArrayAdapter<String> destinationStop;               // For the drop down
    ArrayList<String> stopId = new ArrayList<>();       // Store station id
    ArrayList<String> stopName = new ArrayList<>();     // Store station names here
    ArrayList<Double> latitude = new ArrayList<>();     // Store latitude of stations
    ArrayList<Double> longitude = new ArrayList<>();    // Store longitude of stations
    ArrayList<Geofence> geofenceList = new ArrayList<>(); // Store geofences

    // Store choices
    String chosenOrigin;
    String chosenDestination;

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

        // For geofencing
        geofencingClient = LocationServices.getGeofencingClient(Objects.requireNonNull(getActivity()));

        // Bind components to layout
        btnSetRoute = (Button) mView.findViewById(R.id.btnSetRoute);
        btnDisembarked = (Button) mView.findViewById(R.id.btnDisembarked); // TODO: REMOVE ONCE DONE TESTING
        etOrigin = (AutoCompleteTextView) mView.findViewById(R.id.etPickup);
        etDestination = (AutoCompleteTextView) mView.findViewById(R.id.etDropoff);
        selectOrigin = (TextInputLayout) mView.findViewById(R.id.containerSelectOrigin);
        selectDestination = (TextInputLayout) mView.findViewById(R.id.containerSelectDestination);

        mMapView = (MapView) mView.findViewById(R.id.commuter_map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
        btnDisembarked.setVisibility(View.GONE);


        // Saves ride data when btn is clicked
        btnSetRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentTrip();
                setTripMarkers();
                buildGeofence();

                // Configure UI to disable origin
                etOrigin.setEnabled(false);
                etOrigin.setClickable(false);
                etOrigin.setFocusable(false);
                etOrigin.setFocusableInTouchMode(false);
                selectOrigin.setEnabled(false);
                selectOrigin.setClickable(false);
                selectOrigin.setFocusable(false);
                selectOrigin.setFocusableInTouchMode(false);
                etDestination.setEnabled(false);
                etDestination.setClickable(false);
                etDestination.setFocusable(false);
                etDestination.setFocusableInTouchMode(false);
                selectDestination.setEnabled(false);
                selectDestination.setClickable(false);
                selectDestination.setFocusable(false);
                selectDestination.setFocusableInTouchMode(false);

                // SEND INFORMATION TO AN OPERATOR THAT THEY ARE THERE
                // AN OPERATOR SHOULD ACCEPT
            }
        });

        // TODO: TESTING FOR SAVING RIDE HISTORY
        btnDisembarked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                saveRideHistory();

                // Configure UI to enable origin and destination selections again
                etOrigin.setEnabled(true);
                etOrigin.setClickable(true);
                etOrigin.setFocusable(true);
                etOrigin.setFocusableInTouchMode(true);
                selectOrigin.setEnabled(true);
                selectOrigin.setClickable(true);
                selectOrigin.setFocusable(true);
                selectOrigin.setFocusableInTouchMode(true);
                etDestination.setEnabled(true);
                etDestination.setClickable(true);
                etDestination.setFocusable(true);
                etDestination.setFocusableInTouchMode(true);
                selectDestination.setEnabled(true);
                selectDestination.setClickable(true);
                selectDestination.setFocusable(true);
                selectDestination.setFocusableInTouchMode(true);
            }
        });
        getStations();

    }

    // Custom map logic and configurations
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap = googleMap;

        // Moves camera to where the station is at
        etOrigin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getStations();
                getStationsInRoute(etOrigin.getText().toString());
//                generateStationMarkers();
                String station = parent.getItemAtPosition(position).toString();
                etOrigin.setText(station);
                // Pan camera to selected station
                CameraPosition mapCam = CameraPosition.builder()
                        .target(new LatLng(latitude.get(position), longitude.get(position)))
                        .zoom(MAP_ZOOM).bearing(0).tilt(0).build();
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mapCam));

            }
        });

        // Moves camera to where the station is at
        etDestination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                getStations();
                String pickup = etOrigin.getText().toString();

//                generateStationMarkers();
                String station = parent.getItemAtPosition(position).toString();
                etDestination.setText(station);
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

    // Function to generate the markers showing the origin and the destination
    private void setTripMarkers() {
        // Clear map existing markers
        mGoogleMap.clear();
        int width = 100;
        int height = 100;
        BitmapDrawable bus_icon = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_bus_stop);
        Bitmap iconified = bus_icon.getBitmap();
        // Set new marker points
        for (int i = 0; i < stopName.size(); i++) {
            if (stopName.get(i).equals(chosenOrigin)) {
                originMark = mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude.get(i), longitude.get(i)))
                        .title(stopName.get(i))
                        .icon(BitmapDescriptorFactory.fromBitmap(iconified)));
            }
            if (stopName.get(i).equals(chosenDestination)) {
                destinationMark = mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude.get(i), longitude.get(i)))
                        .title(stopName.get(i))
                        .icon(BitmapDescriptorFactory.fromBitmap(iconified)));
            }
        }
    }

    // Function to generate the station markers on the map
    private void generateStationMarkers() {
        mGoogleMap.clear(); // Clear existing markers
        // Variables
        int width = 100;
        int height = 100;

        // For the icon of the bus stops
        BitmapDrawable bus_icon = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_bus_stop);
        Bitmap iconified = bus_icon.getBitmap();

        // Generate new markers for each station
        for (int i = 0; i < stopId.size(); i++) {
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude.get(i), longitude.get(i)))
                    .title(stopName.get(i))
                    .icon(BitmapDescriptorFactory.fromBitmap(iconified)));
        }
    }

    // Function to get all the stations to put in the array list
    private void getStations() {
        // This method gets the stations registered from Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bus_Stop");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stopId.clear();
                stopName.clear();
                latitude.clear();
                longitude.clear();

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    // Get data from each node
                    String id = dsp.getKey();
                    String name = Objects.requireNonNull(dsp.child("busStopName").getValue()).toString();
                    Double lat = Double.parseDouble(Objects.requireNonNull(dsp.child("center_lat").getValue()).toString());
                    Double lon = Double.parseDouble(Objects.requireNonNull(dsp.child("center_long").getValue()).toString());

                    // Adds data to array list
                    stopId.add(id);
                    stopName.add(name);
                    latitude.add(lat);
                    longitude.add(lon);
                }

                // Convert arraylist to a string[]
                items = new String[stopName.size()];
                for (int i = 0; i < stopName.size(); i++) {
                    items[i] = stopName.get(i);
                }

                // Selecting from pickup
                originStop = new ArrayAdapter<String>(getActivity(), R.layout.dropdown_list, items);
                etOrigin.setAdapter(originStop);

                // Selecting from dropoff
//                dropOffStations = new ArrayAdapter<String>(getActivity(), R.layout.component_list_item, items);
//                etDropoff.setAdapter(dropOffStations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), R.string.err_couldntRetrieveStops, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to get all the stations within a route
    private void getStationsInRoute(String origin) {
        String TAG = "getStationsInRoute";

        // Get routes of the pickup station
        ArrayList<String> routes = new ArrayList<>();  // Holds the routes that includes the origin/pickup station
        ArrayList<String> stations = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Routes");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dsp : snapshot.getChildren()) {
                    String thisRoute = String.valueOf(dsp.child("routeName").getValue());
                    // Get the stops
                    for (int i = 1; i < dsp.getChildrenCount(); i++) {
                        String data = String.valueOf(dsp.child("stop".concat(String.valueOf(i))).child("busStopName").getValue()); // holds the bus stop name from this subnode
                        // Check if the origin is equal to any of the stops in this route
                        if (origin.equals(data)) {
                            routes.add(thisRoute); // if the origin shows up in that route, then it adds it to the array list
                            String originOrder = String.valueOf(dsp.child("stop".concat(String.valueOf(i))).child("order").getValue()); // holds the order of the origin bus stop
                            // gets the stations in the same routes as origin
                            for (int x = 1; x < dsp.getChildrenCount(); x++) {
                                data = String.valueOf(dsp.child("stop".concat(String.valueOf(x))).child("busStopName").getValue());
                                String stopOrder = String.valueOf(dsp.child("stop".concat(String.valueOf(x))).child("order").getValue());
                                // only bus stops that come after the origin stop will be added
                                if (Integer.parseInt(originOrder) < Integer.parseInt(stopOrder)) {
                                    stations.add(data);
                                }
                            }
                        }
                    }
                }

                // Convert arraylist to a string[]
                items = new String[stations.size()];
                for (int i = 0; i < stations.size(); i++) {
                    items[i] = stations.get(i);
                }
                destinationStop = new ArrayAdapter<String>(getActivity(), R.layout.dropdown_list, items);
                etDestination.setAdapter(destinationStop);
                Log.i(TAG, "stations: " + stations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "onCancelled: Action cancelled");
            }
        });
    }

    // Function to set the current trip of the commuter
    private void setCurrentTrip() {
        // Get the current date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        String date = sdf.format(c.getTime());
        String time = String.valueOf(Calendar.getInstance().getTime());

        chosenOrigin = etOrigin.getText().toString();
        chosenDestination = etDestination.getText().toString();

        // Field validation
        if (chosenOrigin.isEmpty() || chosenDestination.isEmpty()) {
            if (chosenOrigin.isEmpty()) {
                etOrigin.setError(getString(R.string.err_fieldRequired));
            }
            if (chosenDestination.isEmpty()) {
                etDestination.setError(getString(R.string.err_fieldRequired));
            }
            Toast.makeText(getActivity(), R.string.err_emptyRequiredFields, Toast.LENGTH_SHORT).show();
            return;
        } else if (chosenOrigin.equals(chosenDestination)) {
            Toast.makeText(getActivity(), R.string.err_cant_have_same_pickup_dropoff, Toast.LENGTH_SHORT).show();
            return;
        }

        findMidpoint(chosenOrigin, chosenDestination); // Finding the midpoint between the pickup and drop off

        String pickup = null;
        String dropoff = null;

        // Get station ids
        for (int i = 0; i < stopName.size(); i++) {
            if (stopName.get(i).equals(chosenOrigin)) {
                pickup = stopName.get(i);
            }
            if (stopName.get(i).equals(chosenDestination)) {
                dropoff = stopName.get(i);
            }
        }
        // Create the node for a current trip
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Trip current_trip = new Trip("", date, time, "", pickup, dropoff, "");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        databaseReference.child(user.getUid()).child("current_trip").push().setValue(current_trip);

        btnSetRoute.setVisibility(View.GONE);
        btnDisembarked.setVisibility(View.VISIBLE);
    }

    // Function to save current trip to ride history
    private void saveRideHistory() {
        // You can either pass the trip info somehow here or get the current_trip info into this method itself. eitherway,
        // it should be stored in a Trip object
        // Get the instance of firebase to take a snapshot of
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference[] databaseReference = {FirebaseDatabase.getInstance().getReference("Commuter").child(user.getUid()).child("current_trip")};
        databaseReference[0].get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        // Variables
                        String id = "";
                        String date = "";
                        String time_started = "";
                        String time_ended = "";
                        String operator_id = "";
                        String pickup_station = "";
                        String dropoff_station = "";

                        // Creating a hashmap to store a new node into ride
                        for (DataSnapshot dsp : task.getResult().getChildren()) { // loop through all current_trip records (there should only be one every time)
                            id = dsp.getKey();
                            date = String.valueOf(dsp.child("date").getValue());
                            time_started = String.valueOf(dsp.child("time_started").getValue());
                            time_ended = String.valueOf(dsp.child("time_ended").getValue());
                            operator_id = String.valueOf(dsp.child("operator_id").getValue());
                            pickup_station = String.valueOf(dsp.child("pickup_station").getValue());
                            dropoff_station = String.valueOf(dsp.child("dropoff_station").getValue());
                        }
                        // Save the information to firebase
                        HashMap Ride = new HashMap();
                        Trip trip = new Trip(id, date, time_started, time_ended, pickup_station, dropoff_station, operator_id);
                        Ride.put("id", trip.getId());
                        Ride.put("date", trip.getDate());
                        Ride.put("time_started", trip.getTime_started());
                        Ride.put("time_ended", String.valueOf(Calendar.getInstance().getTime()));
                        Ride.put("operator_id", trip.getOperator_id());
                        Ride.put("pickup_station", pickup_station);
                        Ride.put("dropoff_station", dropoff_station);

                        databaseReference[0] = FirebaseDatabase.getInstance().getReference("Commuter").child(user.getUid()).child("ride_history").child(id);
                        databaseReference[0].updateChildren(Ride);
                    } else {
                        Toast.makeText(getActivity(), R.string.err_failedToReadData, Toast.LENGTH_SHORT).show();
                    }
                } else {
//                    Toast.makeText(getActivity(), R.string.err_unknown, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Configure the UI
        btnDisembarked.setVisibility(View.GONE);
        btnSetRoute.setVisibility(View.VISIBLE);

        // Remove the current_trip information as commuter confirms disembark
        DatabaseReference current_trip = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName()).child(user.getUid()).child("current_trip");
        current_trip.removeValue();
        mGoogleMap.clear(); // Clear the existing markers on the map
    }

    // Function to find the midpoint of two stations
    private void findMidpoint(String pickup, String dropoff) {
        Double pickupLatitude = 0.0;
        Double pickupLongitude = 0.0;
        Double dropoffLatitude = 0.0;
        Double dropoffLongitude = 0.0;

        // Get the center of two points
        for (int i = 0; i < stopName.size(); i++) {
            if (stopName.get(i).equals(pickup)) {
                pickupLatitude = latitude.get(i);
                pickupLongitude = longitude.get(i);
            }
            if (stopName.get(i).equals(dropoff)) {
                dropoffLatitude = latitude.get(i);
                dropoffLongitude = longitude.get(i);
            }
        }
        // Calculate for the midpoint between the two locations
        Double midLat = (pickupLatitude + dropoffLatitude) / 2;
        Double midLong = (pickupLongitude + dropoffLongitude) / 2;


        // Clear the map of the other markers
        // Move the camera to new midpoint location
        CameraPosition midpoint = CameraPosition.builder()
                .target(new LatLng(midLat, midLong))
                .zoom(13).bearing(0).tilt(0).build();
        mGoogleMap.moveCamera((CameraUpdateFactory.newCameraPosition(midpoint)));

    }

    // Function to create the instance of the Geofencing client
    private void buildGeofence() {

        String TAG = "buildGeofence";
        geofenceList.add(new Geofence.Builder()
                .setRequestId("Origin Geofence")
                .setCircularRegion(
                        originMark.getPosition().latitude,
                        originMark.getPosition().longitude,
                        400   // TODO: TESTING ONLY
                )
                .setExpirationDuration(60l * 10l) // simulater 10 minutes wait time for commuter to embark on a bus
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());
//        getActivity().requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
            getLocationPermission();
            // here to request the missing permissions, and then overriding

            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent()).addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(TAG, "onSuccess: success");
            }
        }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: failed. something happened. " + e);
            }
        });
    }

    // Function to specify the geofences to monitor and to set how related geofence events are triggered
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        // Specifying INITIAL_TRIGGER_ENTER tells Location services that GEOFENCE_TRANSITION_ENTER should be triggered if the device is already inside the geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    // Function to define a broadcast receiver for geofence transitions
    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(getActivity(), BroadcastReceiver.class);
        PendingIntent geofencePendingIntent = PendingIntent.getBroadcast(getActivity(), 0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    // Request for location permissions
    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(getActivity(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
            } else {
                ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
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
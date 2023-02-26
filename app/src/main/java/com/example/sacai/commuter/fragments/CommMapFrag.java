package com.example.sacai.commuter.fragments;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sacai.R;
import com.example.sacai.commuter.CommGeofenceActions;
import com.example.sacai.commuter.CommMainActivity;
import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.Commuter_Trip;
import com.example.sacai.commuter.CommGeofenceHelper;
import com.example.sacai.dataclasses.Commuter_in_Geofence;
import com.example.sacai.dataclasses.Operator;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import com.google.maps.android.PolyUtil;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class CommMapFrag extends Fragment implements OnMapReadyCallback {
    // Call variables based on Google Documentation
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;
    FusedLocationProviderClient fusedLocationProviderClient;

    // Global Variables
    private GeofencingClient geofencingClient;
    private CommGeofenceHelper commGeofenceHelper;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 10002;
    // Variables for looping location updates
    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();

    // Components
    Button btnSetRoute, btnDisembarked, btnSetHome, btnSetWork, btnScanQr;
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
    ArrayList<Marker> busInBusStop = new ArrayList<>(); // Store bus stop ids in bus stop
    // Store choices
    String chosenOrigin;
    String chosenDestination;
    String currentRoute;
    ArrayList<String> temp = new ArrayList<>();
    String qrResultsOperatorUID;
    String usernameDb, auditoryDb, mobilityDb, wheelchairDb;
    String currentTripOrigin, currentTripDestination;
    String currentRouteKey;
    boolean wheelchair_user = false;

    //Polyline
    String encodedPolyline = "";
    List<LatLng> decodedPolyline;
    Polyline polyInit;
    List<Polyline> testPoly = new ArrayList<Polyline>();


    // CONSTANTS
    private int MAP_ZOOM = 20;
    private float GEOFENCE_RADIUS = 100; // TODO: QUERY FROM FIREBASE
    private int width = 100;
    private int height = 100;

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
        String TAG = "onViewCreated";
        super.onViewCreated(view, savedInstanceState);

        // For geofencing
        geofencingClient = LocationServices.getGeofencingClient(requireActivity()); // TODO: check
        commGeofenceHelper = new CommGeofenceHelper(getActivity());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Bind components to layout
        btnSetRoute =  mView.findViewById(R.id.btnSetRoute);
        btnDisembarked =  mView.findViewById(R.id.btnDisembarked);
        btnScanQr = mView.findViewById(R.id.btnScanQr);
        btnSetHome = mView.findViewById(R.id.btnSetHome);
        btnSetWork = mView.findViewById(R.id.btnSetWork);
        etOrigin =  mView.findViewById(R.id.etPickup);
        etDestination =  mView.findViewById(R.id.etDropoff);
        selectOrigin =  mView.findViewById(R.id.containerSelectOrigin);
        selectDestination =  mView.findViewById(R.id.containerSelectDestination);

        // Generate stations to select
        getStations();
        mMapView = (MapView) mView.findViewById(R.id.commuter_map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }


        // Sets button to invisible if commuter is not yet in a trip
        btnDisembarked.setVisibility(View.GONE);
        btnScanQr.setVisibility(View.GONE);

        // Saves ride data when btn is clicked
        btnSetRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String TAG = "btnSetRoute";
                Log.i(TAG, "onClick: TRUE");
                //Activate this function if Commuter has input in both Origin and Destination
                if (chosenDestination != "" && chosenOrigin != "") {
                    // Save current trip into database
                    findRoute();
                    setCurrentTrip();
                }
            }
        });

        // set origin to home address
        btnSetHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etOrigin.isFocused()) {
                    setOriginHome();
                } else if (etDestination.isFocused()) {
                    if(etOrigin.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), "Destination is not within route of selected origin or origin is not set.", Toast.LENGTH_SHORT).show();
                    } else {
                        setDestinationHome();
                    }
                } else {
                    Toast.makeText(getActivity(), "Select between origin or destination before setting your home or work address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSetWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etOrigin.isFocused()) {
                    setOriginWork();
                } else if (etDestination.isFocused()) {
                    if(etOrigin.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), "Destination is not within route of selected origin or origin is not set.", Toast.LENGTH_SHORT).show();
                    } else {
                        setDestinationWork();
                    }
                } else {
                    Toast.makeText(getActivity(), "Select between origin or destination before setting your home or work address", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnScanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = IntentIntegrator.forSupportFragment(CommMapFrag.this);

                integrator.setOrientationLocked(false);
                integrator.setPrompt("Scan QR code");
                integrator.setBeepEnabled(false);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);

                integrator.initiateScan();
            }
        });


        // TODO: Move to when the commuter is already in a ride
        btnDisembarked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommGeofenceActions action = new CommGeofenceActions();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                DatabaseReference db = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName()).child(user.getUid()).child("current_trip");

                db.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isComplete()) {
                            String operator_id;
                            for (DataSnapshot dsp : task.getResult().getChildren()) {
                                Log.i(TAG, "onComplete: GETTING CURRENT TRIP " + dsp.getKey());
                                operator_id = dsp.child("operator_id").getValue().toString();
                                if (!operator_id.isEmpty()){
                                    Log.i(TAG, "onComplete: OPERATOR ID " + operator_id);
                                    saveRideHistory();
                                }
                                removeFromCurrentTrip();
                            }
                        }
                    }
                });

                getStationsInRoute("");
                // Configure UI to enable origin and destination selections again
                etOrigin.setEnabled(true);
                etOrigin.setClickable(true);
                etOrigin.setFocusable(true);
                etOrigin.setFocusableInTouchMode(true);
                etOrigin.setText(null);
                etDestination.setText(null);
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
                btnScanQr.setVisibility(View.GONE);
            }
        });
    }

    // Custom map logic and configurations
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        String TAG = "onMapReady";
        MapsInitializer.initialize(getContext());

        // Declarations and initializations
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap = googleMap;
        BitmapDrawable bus_icon = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_bus_stop);
        Bitmap iconified = bus_icon.getBitmap();

        checkForOngoingTrip();

        Log.i(TAG, "onMapReady: removing existing geofences...");
        geofencingClient.removeGeofences(commGeofenceHelper.getPendingIntent())
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        Log.i("Remove Geofences", "onSuccess: geofences removed");
                        // ...
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                    }
                });

        mGoogleMap.setMyLocationEnabled(true);  // sets the user location to be enabled
        Log.i("getLocationPermission", "mLocationPermissionGranted: TRUE");
        zoomToUserLocation();   //

        // Default camera placement
        Log.i("onMapReady", "onMapReady: camera moved to default");
        CameraPosition rainforestPark = CameraPosition.builder()
                .target(new LatLng(14.554705128006361, 121.09289228652042))
                .zoom(15).bearing(0).tilt(0).build();
        googleMap.moveCamera((CameraUpdateFactory.newCameraPosition(rainforestPark)));


        //====== everything is user interaction based ======//
        // Moves camera to where the station is at
        etOrigin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mGoogleMap.clear();
                getStations();  // Generate stations to select
                etDestination.setText(null);

                getStationsInRoute(etOrigin.getText().toString());
                String station = parent.getItemAtPosition(position).toString();
                etOrigin.setText(station);

                Toast.makeText(getActivity(), "Make sure you are near the bus stop.", Toast.LENGTH_SHORT).show();

                // If there is an existing ORIGIN marker on the map then remove that marker
                try {
                    originMark.remove();
                    Log.i("onMapReady", "originMark.remove: successful");
                } catch (Exception e) {
                    Log.i("onMapReady", "originMark.remove: no originMarker to remove. exception " + e);
                }

                // Create the marker for this station
                chosenOrigin = String.valueOf(etOrigin.getText());
                for (int i = 0; i < stopName.size(); i++) { // Loop through the list of stops that was queried
                    if (stopName.get(i).equals(chosenOrigin)) {
                        // Create the marker object for originMark
                        originMark = mGoogleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude.get(i), longitude.get(i)))
                                .title(stopName.get(i))
                                .icon(BitmapDescriptorFactory.fromBitmap(iconified)));
                        Log.i("onMapReady", "originMark.addMarker: Success.");

                        // Pan camera to selected station
                        CameraPosition mapCam = CameraPosition.builder()
                                .target(new LatLng(latitude.get(position), longitude.get(position)))
                                .zoom(MAP_ZOOM).bearing(0).tilt(0).build();
                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mapCam));
                        // Queries stations in the route/s the origin stop is in
                        getStationsInRoute(chosenOrigin);
                    }
                }
                //Activate this function if Commuter has input in both Origin and Destination
                if (chosenDestination != "" && chosenOrigin != "") {
                    //Identify the route for drawing route encoded polyline
                    findRoute();
                }
            }
        });
        // Moves camera to where the station is at
        etDestination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String station = parent.getItemAtPosition(position).toString();
                etDestination.setText(station);

                // If there is an existing DESTINATION marker on the map then remove that marker
                try {
                    destinationMark.remove();
                    Log.i("destinationMark", "remove: successfull");
                } catch (Exception e) {
                    Log.i("destinationMark", "remove: error occurred. exception" + e);
                }

                // Create the marker for this station
                chosenDestination = String.valueOf(etDestination.getText());
                for (int i = 0; i < stopName.size(); i++) {     // Loop through the stations that was queried
                    if (stopName.get(i).equals(chosenDestination)) {
                        destinationMark = mGoogleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude.get(i), longitude.get(i)))
                                .title(stopName.get(i))
                                .icon(BitmapDescriptorFactory.fromBitmap(iconified)));
                        Log.i("destinationMark", "addMarker: success");
                        // Pan camera to selected station
                        CameraPosition mapCam = CameraPosition.builder()
                                .target(new LatLng(latitude.get(i), longitude.get(i)))
                                .zoom(MAP_ZOOM).bearing(0).tilt(0).build();
                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mapCam));
                        chosenDestination = String.valueOf(etDestination.getText());
                        // Queries stations in the route/s the origin stop is in
                        getStationsInRoute(chosenOrigin);
                    }
                }
                //Activate this function if Commuter has input in both Origin and Destination
                if (chosenDestination != "" && chosenOrigin != "") {
                    //Identify the route for drawing route encoded polyline
                    findRoute();
                }
            }
        });
    }



    // Function to get all the stations to put in the array list
    private void getStations() {
        Log.i("ClassCalled", "getStations is running");

        // This method gets the stations registered from Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bus_Stop");

        // Get bus stops
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("getStations", "onCancelled: action was cancelled. error" + error);
                Toast.makeText(getActivity(), R.string.err_couldntRetrieveStops, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to get all the stations within a route
    private void getStationsInRoute(String origin) {
        Log.i("ClassCalled", "getStationsInRoute is running");
        // Get routes of the pickup station
        ArrayList<String> routes = new ArrayList<>();  // Holds the routes that includes the origin/pickup station
        ArrayList<String> stations = new ArrayList<>(); // Holds the bus stops within aforementioned routes

        // Get routes
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
                Log.i("getStationsInRoute", "onDataChange: success");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), R.string.err_couldntRetrieveStops, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function to set the current trip of the commuter
    private void setCurrentTrip() {
        Log.i("ClassCalled", "setCurrentTrip is running");
        String TAG = "setCurrentTrip";
        // Get the current date and time
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        String date = sdf.format(c.getTime());
        String time = String.valueOf(Calendar.getInstance().getTime());

        // Get the values from the dropdown menu
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
        Commuter_Trip current_trip = new Commuter_Trip("", date, time, "", pickup, dropoff, "");

        // Saving the current trip into the database
        DatabaseReference db = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        Log.i(TAG, "setCurrentRide: verify database " + db);

        // Clear current trip
        db.child(user.getUid()).child("current_trip").removeValue();
        Log.i(TAG, "setCurrentTrip: cleared current_trip");

        db.child(user.getUid()).child("current_trip").push().setValue(current_trip).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()) {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "onComplete: current trip information has been added to the database");

                        // Configure UI to disable when trip has started
                        toggleViewNoQrScanned();

                        Log.i(TAG, "btnSetRoute.onClick: success");
                    } else {
                        Toast.makeText(getActivity(), "Error: Could not start a trip.", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onComplete: retrieve data unsuccessful");
                    }
                } else {
                    Toast.makeText(getActivity(), "Test", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onComplete: retrieve data could not be completed");
                }
            }
        });
        Log.i(TAG, "setCurrentTrip: trying to add geofences...");
        tryAddingGeofence();
    }

    // Acquire CommuterDetails
    private void acquireCommuterDetails() {
        Log.i("Debug", "Running acquireCommuterDetails() Class");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("Commuter").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    //Acquire single items
                    String firebaseValues = String.valueOf(task.getResult().getValue());
                    String[] arrOfStr = firebaseValues.split(",");

                    for (String a : arrOfStr) {
                        if (a.contains("username")) {
                            String[] b = a.split("=");
                            if (b[1].replaceAll("[\\[\\](){}]","")!=""){
                                usernameDb = b[1].replaceAll("[\\[\\](){}]","");
                                Log.d("firebase", usernameDb);
                            }
                        }
                        if (a.contains("mobility")) {
                            String[] b = a.split("=");
                            if (b[1].replaceAll("[\\[\\](){}]","")!=""){
                                mobilityDb = b[1].replaceAll("[\\[\\](){}]","");
                                Log.d("firebase", mobilityDb);
                            }
                        }
                        if (a.contains("auditory")) {
                            String[] b = a.split("=");
                            if (b[1].replaceAll("[\\[\\](){}]","")!=""){
                                auditoryDb = b[1].replaceAll("[\\[\\](){}]","");
                                Log.d("firebase", auditoryDb);
                            }
                        }
                        if (a.contains("wheelchair")) {
                            String[] b = a.split("=");
                            if (b[1].replaceAll("[\\[\\](){}]","")!=""){
                                wheelchairDb = b[1].replaceAll("[\\[\\](){}]","");
                                Log.d("firebase", wheelchairDb);
                            }
                        }
                    }
                }
            }
        });
    }

    private void checkForOngoingTrip() {
        String TAG = "checkForOngoingTrip";
        Log.i("ClassCalled", "checkForOngoingTrip: is running");

        BitmapDrawable bus_icon = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_bus_stop);
        Bitmap iconified = bus_icon.getBitmap();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        Log.i(TAG, "checkForOngoingTrip: db reference " + databaseReference);

        getStations();

        databaseReference.child(uid).child("current_trip").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.getResult().exists()) {
                    Log.i(TAG, "onDataChange: snapshot " + task.getResult());
                    Log.i(TAG, "onDataChange: a current trip exists");

                    toggleViewNoQrScanned();

                    for (DataSnapshot dsp : task.getResult().getChildren()) {
                        try {
                            etOrigin.setText(dsp.child("pickup_station").getValue().toString());
                            etDestination.setText(dsp.child("dropoff_station").getValue().toString());
                            chosenOrigin = etOrigin.getText().toString();
                            chosenDestination = etDestination.getText().toString();

                            //Activate this function if Commuter has input in both Origin and Destination
                            if (chosenDestination != "" && chosenOrigin != "") {
                                //Identify the route for drawing route encoded polyline
                                findRoute();
                            }


                            for (int i = 0; i < stopName.size(); i++) {
                                Log.i(TAG, "onDataChange: going through stops");
                                Log.i(TAG, "onDataChange: Stop Name " + stopName.get(i));
                                if (stopName.get(i).equals(chosenOrigin)) {
                                    originMark = mGoogleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(latitude.get(i), longitude.get(i)))
                                            .title(stopName.get(i))
                                            .icon(BitmapDescriptorFactory.fromBitmap(iconified)));
                                } else if (stopName.get(i).equals(chosenDestination)) {
                                    destinationMark = mGoogleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(latitude.get(i), longitude.get(i)))
                                            .title(stopName.get(i))
                                            .icon(BitmapDescriptorFactory.fromBitmap(iconified)));
                                }
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: exception ", e);
                        }
                    }
                    Log.i(TAG, "onDataChange: chosenOrigin " + chosenOrigin);
                    Log.i(TAG, "onDataChange: chosenDestination " + chosenDestination);
                    Log.i(TAG, "onDataChange: stopname size " + stopName.size());
                    Log.i(TAG, "onDataChange: trying to add geofences...");
                    tryAddingGeofence();
                } else {
                    Log.i(TAG, "onDataChange: a current trip does not exist");
                }
            }
        });

        acquireCommuterDetails();
    }

    // Retrieve the user's saved home address
    private void setOriginHome() {
        String TAG = "setOriginHome";
        Log.i(TAG, "setOriginHome: is running");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        // Get user information from firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());

        Log.i(TAG, "setOriginHome: dbReference " + databaseReference);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dsp : snapshot.getChildren()) {
                    try {
                        if (dsp.getKey().equals(uid)) {
                            String home = dsp.child("homeAddress").getValue().toString();
                            Log.i(TAG, "onDataChange: homeAddress " + home);
                            etOrigin.setText(home);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: exception ", e);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "onCancelled: database error " + error);
            }
        });
    }

    private void setOriginWork() {
        String TAG = "setOriginWork";
        Log.i("ClassCalled", "setOriginWork: is running");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        Log.i(TAG, "setOriginWork: dbReference " + databaseReference);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dsp : snapshot.getChildren()) {
                    try {
                        if (dsp.getKey().equals(uid)) {
                            String work = dsp.child("workAddress").getValue().toString();
                            Log.i(TAG, "onDataChange: workAddress" + work);
                            etOrigin.setText(work);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: exception ", e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "onCancelled: database error " + error);
            }
        });

        Log.i(TAG, "setOriginWork: work is set");
    }

    private void setDestinationWork() {
        String TAG = "setDestination";
        Log.i("ClassCalled", "setDestination: is running");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        temp.clear();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        Log.i(TAG, "setDestinationWork: dbReference " + databaseReference);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dsp : snapshot.getChildren()) {
                    try {
                        if (dsp.getKey().equals(uid)) {
                            String work = dsp.child("workAddress").getValue().toString();
                            for (int i = 0; i < items.length; i++) {
                                if (work.equals(items[i])) {
                                    etDestination.setText(work);
                                    temp.add(work);
                                }
                                Log.i(TAG, "onDataChange: work " + work);
                                Log.i(TAG, "onDataChange: items " + items[i]);
                            }

                        }
                    } catch (Exception e) {
                        Log.i(TAG, "onDataChange: exception ", e);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "onCancelled: database error " + error);
            }
        });
        Log.i(TAG, "setOriginWork: home is set");
    }

    private void setDestinationHome() {
        String TAG = "setDestinationHome";
        Log.i("ClassCalled", "setDestinationHome: is running");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        temp.clear();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean inRoute =true;

                for (DataSnapshot dsp : snapshot.getChildren()) {
                    try {
                        if (dsp.getKey().equals(uid)) {
                            String home = dsp.child("homeAddress").getValue().toString();
                            for (int i = 0 ; i < items.length; i++) {
                                if (home.equals(items[i])) {
                                    etDestination. setText(home);
                                    temp.add(home);
                                }
                            }

                        }
                    } catch (Exception e) {
                        Log.i(TAG, "onDataChange: exception ", e);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "onCancelled: cancel " + error);
            }
        });
        Log.i(TAG, "setDestinationHome: home destination set");
    }

    private void removeFromCurrentTrip() {
        String TAG = "removeFromCurrentTrip";
        Log.i(TAG, "removeFromCurrentTrip: is running");
        stopLocationUpdates();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        geofencingClient.removeGeofences(commGeofenceHelper.getPendingIntent())
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        Log.i("Remove Geofences", "onSuccess: geofences removed");
                        // ...
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                    }
                });

        // Configure the UI
        btnDisembarked.setVisibility(View.GONE);
        btnSetRoute.setVisibility(View.VISIBLE);
        btnSetWork.setVisibility(View.VISIBLE);
        btnSetHome.setVisibility(View.VISIBLE);

        Log.i("UI_Changes", "saveRideHistory: btnDisembarked set to GONE");
        Log.i("UI_Changes", "saveRideHistory: btnSetRoute set to VISIBLE");

        // Remove the current_trip information as commuter confirms disembark
        DatabaseReference current_trip = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName()).child(user.getUid()).child("current_trip");
        current_trip.removeValue();
        Log.i("current_trip", "removeValue: successful");
        // Clear the existing markers on the map
        mGoogleMap.clear();
        removeCommuterVisibility();
    }

    // Function to save current trip to ride history
    private void saveRideHistory() {
        Log.i("ClassCalled", "saveRideHistory is running");

        // Get the current date and time
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        String date = sdf.format(c.getTime());
        String time = String.valueOf(Calendar.getInstance().getTime());

        // You can either pass the trip info somehow here or get the current_trip info into this method itself. eitherway,
        // it should be stored in a Trip object
        // Get the instance of firebase to take a snapshot of
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final DatabaseReference[] db = {FirebaseDatabase.getInstance().getReference("Commuter").child(user.getUid()).child("current_trip")};

        db[0].get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        // Variables
                        String id = "";
                        String date = "";
                        String time_started = "";
                        String time_ended = time;
                        String operator_id = "";
                        String pickup_station = "";
                        String dropoff_station = "";

                        // Creating a hashmap to store a new node into ride
                        for (DataSnapshot dsp : task.getResult().getChildren()) { // loop through all current_trip records (there should only be one every time)
                            id = dsp.getKey();
                            date = String.valueOf(dsp.child("date").getValue());
                            time_started = String.valueOf(dsp.child("time_started").getValue());
                            operator_id = String.valueOf(dsp.child("operator_id").getValue());
                            pickup_station = String.valueOf(dsp.child("pickup_station").getValue());
                            dropoff_station = String.valueOf(dsp.child("dropoff_station").getValue());
                        }

                        DatabaseReference dbSave = FirebaseDatabase.getInstance().getReference("Commuter").child(user.getUid()).child("ride_history").child(id);

                        dbSave.child("date").setValue(date);
                        dbSave.child("time_started").setValue(time_started);
                        dbSave.child("time_ended").setValue(time);
                        dbSave.child("operator_id").setValue(operator_id);
                        dbSave.child("origin").setValue(pickup_station);
                        dbSave.child("destination").setValue(dropoff_station);

                    } else {
                        Toast.makeText(getActivity(), R.string.err_failedToReadData, Toast.LENGTH_SHORT).show();
                        Log.i("saveRideHistory", "onComplete: data from record does not exist");
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.err_unknown, Toast.LENGTH_SHORT).show();
                    Log.i("saveRideHistory", "onComplete: retrieve data could not be completed");
                }
            }
        });

        geofencingClient.removeGeofences(commGeofenceHelper.getPendingIntent())
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        Log.i("Remove Geofences", "onSuccess: geofences removed");
                        // ...
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                    }
                });

        // Configure the UI
        btnDisembarked.setVisibility(View.GONE);
        btnSetRoute.setVisibility(View.VISIBLE);
        btnSetWork.setVisibility(View.VISIBLE);
        btnSetHome.setVisibility(View.VISIBLE);

        // Remove the current_trip information as commuter confirms disembark
        DatabaseReference current_trip = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName()).child(user.getUid()).child("current_trip");
        current_trip.removeValue();
        Log.i("current_trip", "removeValue: successful");
        // Clear the existing markers on the map
        mGoogleMap.clear();
    }


    // Function to zoom into the user's current location (not the built in zoom to location)
    private void zoomToUserLocation() {
        Log.i("ClassCall", "zoomToUserLocation: is running");

        // get the last location of the user (SUPPRESSED BECAUSE WE SHOULD ALREADY HAVE THAT EXECUTE IN THE FUNCTION THAT WOULD CALL IT)`
        @SuppressLint("MissingPermission") Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                try {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM));
                    Log.i("zoomToUserLocation", "onSuccess: camera moved to user location");
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Please turn on your location services", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    // Function that draws a circle to indicate the geofence boundaries of a bus stop
    private void addCircle(LatLng latLng, float geofence_radius) {
        String TAG = "addCircle";
        Log.i(TAG, "addCircle: is running");

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(geofence_radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(65, 255, 0, 0));
        circleOptions.strokeColor(4);
        mGoogleMap.addCircle(circleOptions);
    }

    // Function to try adding geofence
    private void tryAddingGeofence() {
        String TAG = "tryAddingGeofences";
        Log.i(TAG, "tryAddingGeofence: is running");

        // remove existing geofences
        Log.i(TAG, "tryAddingGeofence: removing existing geofences");
        geofencingClient.removeGeofences(commGeofenceHelper.getPendingIntent())
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        Log.i("Remove Geofences", "onSuccess: geofences removed");
                        // ...
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                    }
                });

        // get the list of bus stops available
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bus_Stop");
        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String origin = null;          // stores id of origin bus stop
                String destination = null;      // stores id of destination bus stop
                for (DataSnapshot dsp : task.getResult().getChildren()) {
                    if ((dsp.child("busStopName").getValue().toString()).equals(chosenOrigin))  {
                        origin = dsp.getKey();
                    } else if ((dsp.child("busStopName").getValue().toString()).equals(chosenDestination)) {
                        destination = dsp.getKey();
                        Log.i(TAG, "onDataChange.destination: " + destination);
                    } else {
                        Log.i(TAG, "onDataChange: nothing matches");
                    }
                }
                Log.i(TAG, "onDataChange: Matched Origin " + origin);
                Log.i(TAG, "onDataChange: Matched Destination " + destination);
                try {
                    Log.i(TAG, "onComplete: adding geofences...");
                    addGeofence(chosenOrigin, new LatLng(originMark.getPosition().latitude, originMark.getPosition().longitude), GEOFENCE_RADIUS);                     // make a geofence at the origin of the trip
                    addGeofence(chosenDestination   , new LatLng(destinationMark.getPosition().latitude, destinationMark.getPosition().longitude), GEOFENCE_RADIUS);      // make a geofence at the destination of the trip
                    startLocationUpdates();
                } catch (Exception e) {
                    Log.i(TAG, "onDataChange: tried adding geofences, failed...");
                    Log.i(TAG, "onDataChange: exception " + e);
                }
            }
        });
    }

    // Function to add a geofence
    @SuppressLint("MissingPermission")
    private void addGeofence(String geofence_id, LatLng latLng, float radius) {
        String TAG = "addGeofence";
        Log.i("ClassCalled", "addGeofence: is running");
        Log.i(TAG, "addGeofence: geofence id " + geofence_id);

        Geofence geofence = commGeofenceHelper.getGeofence(geofence_id, latLng, radius,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = commGeofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = commGeofenceHelper.getPendingIntent();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i("addGeofence", "onSuccess: geofence added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("addGeofence", "onFailure: " + e);
                    }
                });
    }

    private void findRoute() {
        //Refer to the Route_Drawing branch
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Route_Drawing");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Get data under child
                for (DataSnapshot getRouteSnapshot: dataSnapshot.getChildren()) {
                    String startBusDb = etOrigin.getText().toString();
                    String endBusDb = "";
                    currentRoute = "";

                    for (DataSnapshot getBusStopInfoSnapshot: getRouteSnapshot.getChildren()) {
                        if (getBusStopInfoSnapshot.child("startBusStopName").getValue().toString().equals(startBusDb)) {

                            endBusDb = getBusStopInfoSnapshot.child("endBusStopName").getValue().toString();

                            if (!startBusDb.equals(etDestination.getText().toString())) {
                                startBusDb = endBusDb;
                            }
                            if (startBusDb.equals(etDestination.getText().toString())) {
                                currentRoute = getRouteSnapshot.getKey();
                                drawRoutes();
                                break; //stop query database once Route has been acquired
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Couldn't retrieve routes. Please refresh.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoutes() {
        //Refer to the Route_Drawing branch
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Route_Drawing").child(currentRoute);
        databaseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Get data under child
                String startBusDb = etOrigin.getText().toString();
                String endBusDb = "";

                //Remove existing polylines
                try {
                    for(Polyline line : testPoly)
                    {
                        line.remove();
                    }
                    testPoly.clear();
                }
                catch (Exception e) {

                }

                for (DataSnapshot getBusStopInfoSnapshot: dataSnapshot.getChildren()) {
                    Log.i("DRAW ROUTES", "onDataChange: startBusDb " + startBusDb);
                    if (getBusStopInfoSnapshot.child("startBusStopName").getValue().toString().equals(startBusDb)) {
                        // This polyline color is currently black
                        endBusDb = getBusStopInfoSnapshot.child("endBusStopName").getValue().toString();

                        //Loop through dB branches until it matches the text on the Destination
                        if (!startBusDb.equals(etDestination.getText().toString())) {
                            startBusDb = endBusDb;

                            encodedPolyline = getBusStopInfoSnapshot.child("polyline").getValue().toString();
                            decodedPolyline = PolyUtil.decode(encodedPolyline);

                            try {
                                polyInit = mGoogleMap.addPolyline(new PolylineOptions().addAll(decodedPolyline));
                                testPoly.add(polyInit);
                            }
                            catch (Exception e) {
                                //should definitely add something here for debugging
                            }

                        }
                        if (startBusDb.equals(etDestination.getText().toString())) {
                            encodedPolyline = getBusStopInfoSnapshot.child("polyline").getValue().toString();
                            decodedPolyline = PolyUtil.decode(encodedPolyline);

                            try {
                                polyInit = mGoogleMap.addPolyline(new PolylineOptions().addAll(decodedPolyline));
                                testPoly.add(polyInit);
                            }
                            catch (Exception e) {
                                //should def add something here for debugging
                            }
                            break;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Couldn't retrieve polylines. Please refresh.", Toast.LENGTH_SHORT).show();
                throw error.toException();
            }
        });
    }

    // Function to find the midpoint of two stations
    private void findMidpoint(String pickup, String dropoff) {
        Log.i("ClassCalled", "findMidpoint is running");

        Double originLatitude = 0.0;
        Double originLongitude = 0.0;
        Double destinationLatitude = 0.0;
        Double destinationLongitude = 0.0;

        // Get the center of two points
        for (int i = 0; i < stopName.size(); i++) {
            if (stopName.get(i).equals(pickup)) {
                originLatitude = latitude.get(i);
                originLongitude = longitude.get(i);
            }
            if (stopName.get(i).equals(dropoff)) {
                destinationLatitude = latitude.get(i);
                destinationLongitude = longitude.get(i);
            }
        }
        // Calculate for the midpoint between the two locations
        Double midLat = (originLatitude + destinationLatitude) / 2;
        Double midLong = (originLongitude + destinationLongitude) / 2;

        Log.i("findMidpoint", "midLat: " + originLatitude);
        Log.i("findMidpoint", "midLong: " + originLongitude);

        // Clear the map of the other markers
        // Move the camera to new midpoint location
        CameraPosition midpoint = CameraPosition.builder()
                .target(new LatLng(midLat, midLong))
                .zoom(13).bearing(0).tilt(0).build();
        mGoogleMap.moveCamera((CameraUpdateFactory.newCameraPosition(midpoint)));
        Log.i("findMidpoint", "moveCamera: successful");

        // Will zoom and pan the camera to the location of the user after 3 seconds
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                zoomToUserLocation();
            }
        }, 3000);
    }


 // Function to remove commuter visibility on the map
    public void removeCommuterVisibility() {
        Log.i("ClassCalled", "removeCommuterVisibility: is running");
        String TAG = "removeCommuterVisibility";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        // Get the user information
        // uid, username, impairments
        // Get the user's uid first

        // Get database reference
        DatabaseReference drUserInfo = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());


        // check if reference is correct
        Log.i(TAG, "dbReference :" + drUserInfo);

        // Get user information
        drUserInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "onDataChange: looping through each record");
                for (DataSnapshot dsp : snapshot.getChildren()) {
                    if (uid.equals(dsp.getKey())){
                        try {
                            wheelchair_user = (boolean) dsp.child("wheelchair").getValue();
                        } catch (Exception e) {
                            Log.i(TAG, "onDataChange: exception " + e);
                        }
                    }
                }

                DatabaseReference drTripInformation = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName()).child(uid).child("current_trip");

                // check if reference is correct
                Log.i(TAG, "setCommuterVisibility: dbReference " + drTripInformation);

                drTripInformation.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i(TAG, "onDataChange: looping through records");
                        for (DataSnapshot dsp : snapshot.getChildren()) {
                            try {
                                chosenOrigin = (String.valueOf(dsp.child("pickup_station").getValue()));
                                Log.i(TAG, "onDataChange: obtained commuter origin");
                            } catch (Exception e) {
                                Log.i(TAG, "onDataChange: exception " + e);
                            }
                        }
                        deleteCommuterData();
                        return;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i(TAG, "onCancelled: database error " + error);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "onCancelled: database error " + error);
            }
        });

    }

    public void deleteCommuterData() {
        String TAG = "deleteCommuterData";
        Log.i("ClassCalled", "deleteCommuterData: is running");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        Log.i(TAG, "deleteCommuterData: wheelchair " + wheelchair_user);
        Log.i(TAG, "deleteCommuterData: origin " + chosenOrigin);

        String origin = chosenOrigin.replaceAll("[^a-zA-Z0-9]", " ");
        // Check if user has wheelchair
        if (wheelchair_user) {
            Log.i(TAG, "deleteCommuterData: deleting from has_wheelchair...");
            // Store into has_wheelchair node
            DatabaseReference drCommInGeofence = FirebaseDatabase.getInstance().getReference("Commuter_in_Geofence");
            Log.i(TAG, "deleteCommuterData: dbReference " + drCommInGeofence);

            drCommInGeofence.child(origin).child("has_wheelchair").child(uid).removeValue();
        } else {
            Log.i(TAG, "deleteCommuterData: deleting from no_wheelchair");
            DatabaseReference drCommInGeofence = FirebaseDatabase.getInstance().getReference("Commuter_in_Geofence");
            Log.i(TAG, "deleteCommuterData: dbReference " + drCommInGeofence);
            drCommInGeofence.child(origin).child("no_wheelchair").child(uid).removeValue();
        }
    }

    // Function to find all operators in the same bus stop
    private void showOperatorInBusStop() {
        String TAG = "showOperatorInBusStop";
        Log.i("ClassCalled", "showOperatorInBusStop: is running");



        DatabaseReference db = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName());
        // get the operators information
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dsp : snapshot.getChildren()) {
                    Log.i(TAG, "onComplete: OPERATOR ID " + dsp.getKey()); {
                        if (dsp.child("current_trip").exists()) {
                            Log.i(TAG, "onComplete: THIS OPERATOR HAS AN ONGOING TRIP");
                            // Get the current trip of the operator with this ID
                            getOperatorTripInformation(dsp.getKey());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getOperatorTripInformation(String key) {
        String TAG = "getOperatorTripInformation";
        Log.i("ClassCalled", "getOperatorTripInformation: is running");

        DatabaseReference db = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName()).child(key).child("current_trip");
        Log.i(TAG, "getOperatorTripInformation: db reference " + db);

        db.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isComplete()) {
                    try {
                        for (DataSnapshot dsp : task.getResult().getChildren()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            DatabaseReference dbCommuter = FirebaseDatabase.getInstance().getReference(Commuter_in_Geofence.class.getSimpleName());
                            if (wheelchair_user) {
                                dbCommuter.child("has_wheelchair").child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        for (DataSnapshot dsp : task.getResult().getChildren()) {
                                            chosenOrigin = dsp.child("current_stop").getValue().toString();
                                            Log.i(TAG, "onComplete: chosen origin changed");
                                            Log.i(TAG, "onComplete: " + chosenOrigin);
                                        }
                                    }
                                });
                            } else {
                                dbCommuter.child("no_wheelchair").child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        for (DataSnapshot dsp : task.getResult().getChildren()) {
                                            chosenOrigin = dsp.child("current_stop").getValue().toString();
                                            Log.i(TAG, "onComplete: chosen origin changed");
                                            Log.i(TAG, "onComplete: " + chosenOrigin);
                                        }
                                    }
                                });
                            }
                            Log.i(TAG, "onComplete: commuter's route " + currentRoute);
                            Log.i(TAG, "onComplete: commuter's origin " + chosenOrigin);

                            // check if the current stop is equal to the chosen origin
                            if (dsp.child("current_stop").getValue().toString().equals(chosenOrigin)) {
                                Log.i(TAG, "onComplete: THE BUS IS IN THE SAME STOP");
                                showOperatorLocation(dsp.getKey(), Double.parseDouble(dsp.child("current_lat").getValue().toString()), Double.parseDouble(dsp.child("current_long").getValue().toString()), dsp.child("current_stop").exists());
                            } else {
                                for (int i = 0; i < busInBusStop.size(); i++) {
                                    Log.i(TAG, "showOperatorLocation: markers " + busInBusStop.get(i));
                                    Log.i(TAG, "showOperatorLocation: removing markers");
                                    busInBusStop.get(i).remove();
                                    busInBusStop.remove(i);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onComplete: exception ", e);
                    }
                }
            }
        });

    }

    private void showOperatorLocation(String key, Double current_lat, Double current_long, boolean current_stopExists) {
        String TAG = "showOperatorLocation";
        Log.i(TAG, "showOperatorLocation: is running");

        LatLng busLocation = new LatLng(current_lat, current_long);

        for (int i = 0; i < busInBusStop.size(); i++) {
            Log.i(TAG, "showOperatorLocation: markers " + busInBusStop.get(i));
            Log.i(TAG, "showOperatorLocation: removing markers");
            busInBusStop.get(i).remove();
            busInBusStop.remove(i);
        }

        Log.i(TAG, "showOperatorLocation: number of bus stop markers in area " + busInBusStop.size());
        busInBusStop.add(mGoogleMap.addMarker(new MarkerOptions()
        .position(busLocation)));

    }

    // FUNCTIONS FOR LOCATION UPDATES
    private void stopLocationUpdates(){
        String TAG = "stopTimer";
        Log.i("ClassCalled", "stopTimer: is running");
        if(timer != null){
            timer.cancel();
            timer.purge();
            Log.i(TAG, "stopTimer: timer loop stopped");
        }
    }

    //To start timer
    private void startLocationUpdates(){
        String TAG = "startTimer";
        Log.i("ClassCalled", "startTimer: is running");
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run(){
                        // update location every set interval
                        showOperatorInBusStop();
                        Log.i(TAG, "run: location updated");
                    }
                });
            }
        };
        timer.schedule(timerTask, 5000, 5000);
    }

    //For QR Code Scanning
    // Checks whether the  QR scanned completed and obtained an Operator ID
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
            } else  {
                Log.i("ScanResults", result.getContents());
                qrResultsOperatorUID = result.getContents();
                Toast.makeText(getContext(), "Successfully Scanned", Toast.LENGTH_LONG).show();
            }
            Log.i("Debug", qrResultsOperatorUID);
            addCurrentTripToOperator();
        } else {
            Intent intent = new Intent(getActivity(), CommMainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    //Add to current_trip of operator
    private void addCurrentTripToOperator() {
        Log.i("Debug", "Running addCurrentTrip() Class");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();


        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference dbInput = db.child("Operator").child(qrResultsOperatorUID).child("current_trip").child("passenger_list").child(uid);
        DatabaseReference dbInput = db.child("Operator").child(qrResultsOperatorUID).child("current_trip").child("passenger_list").child(uid);

        dbInput.child("username").setValue(usernameDb);
        dbInput.child("wheelchair").setValue(wheelchairDb);
        dbInput.child("auditory").setValue(auditoryDb);
        dbInput.child("mobility").setValue(mobilityDb);
        dbInput.child("origin").setValue(currentTripOrigin);
        dbInput.child("destination").setValue(currentTripDestination);
        dbInput.child("para").setValue("false");

        addOperatorIDToCommuterCurrentTrip();
    }

    private void addOperatorIDToCommuterCurrentTrip() {
        Log.i("Debug", "Running addOperatorIDToCommuterCurrentTrip() Class");
        checkForOngoingTrip();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        try {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
            DatabaseReference dbInput = db.child("Commuter").child(uid).child("current_trip");
            Log.i("TESTING", "addOperatorIDToCommuterCurrentTrip: " + dbInput.getKey());
            dbInput.child("operator_id").setValue(qrResultsOperatorUID);

            toggleOnGoingTripView();
        } catch (Exception e){
            Log.e("ERROR", "addOperatorIDToCommuterCurrentTrip: exception ",e );
        }


    }

    private void toggleOnGoingTripView() {
        String TAG = "toggleOnGoingTripView";
        Log.i(TAG, "toggleOnGoingTripView: is running");

        btnDisembarked.setText("Para! BABABA");
        btnScanQr.setVisibility(View.GONE);
    }
    private void toggleViewNoQrScanned() {

        // UI when the COMMUTER is still waiting for a bus and has NOT scanned an operator's QR code
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
        btnSetRoute.setVisibility(View.GONE);
        btnDisembarked.setVisibility(View.VISIBLE);
        btnSetHome.setVisibility(View.GONE);
        btnSetWork.setVisibility(View.GONE);
        btnScanQr.setVisibility(View.VISIBLE);
    }

    private void toggleViewQRScanned() {
        Toast.makeText(commGeofenceHelper, "Ride has started.", Toast.LENGTH_SHORT).show();
        btnScanQr.setVisibility(View.GONE);
    }

}
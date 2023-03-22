package com.example.sacai.operator.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.PathSegment;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sacai.R;
import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.Commuter_in_Geofence;
import com.example.sacai.dataclasses.Operator;
import com.example.sacai.dataclasses.Operator_Trip;
import com.example.sacai.dataclasses.Passenger_List;
import com.example.sacai.operator.OperGeofenceHelper;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class OperMapFrag extends Fragment implements OnMapReadyCallback {
    // Call variables based on GoogleMaps Documentation
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;
    FusedLocationProviderClient fusedLocationProviderClient;
    AlertDialog.Builder builder;


    // Gobal Variables
    private GeofencingClient geofencingClient;
    private OperGeofenceHelper operGeofenceHelper;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 10002;
    private static final Location[] lastLocation = new Location[1];

    // Variables for looping location updates
    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();
    private long DELAY = 15000;
    private long PERIOD = 500;
    // Components
    AutoCompleteTextView routeSelects;
    TextInputLayout etRouteSelects;
    TextView commuterCount;
    Button btnStartRoute, btnEndRoute;
    Switch switchSeating;

    String routeIDWithNoBrackets;
    //String routeSnapID; // Store station name in the Routes branch
    String selectedRouteName;
    String chosenRoute;
    String existingCurrentRoute;



    // Arrays
    String[] routeItems;
    String[] stationNameInRoutesItems;
    ArrayAdapter<String> routeChoices; // For the drop down
    ArrayList<String> routeId = new ArrayList<>(); // Store route id
    ArrayList<String> routeNames = new ArrayList<>(); // Store route names here
    ArrayList<String> stationInRoutesName = new ArrayList<>(); // Store station name in the Routes branch
    ArrayList<String> stationInRoutesOrder = new ArrayList<>(); // Store station order in the Routes branch
    ArrayList<String> routeSnapID = new ArrayList<>(); // Store route id
    ArrayList<String> routeSnapName = new ArrayList<>(); // Store station order in the Routes branch
    ArrayList<String> stationInBusStopID = new ArrayList<>(); // Store station name in the Bus_Stop branch
    ArrayList<String> stationInBusStopName = new ArrayList<>(); //Store matched Bus Stop Names
    ArrayList<Double> latitude = new ArrayList<>(); // Store latitude of stations
    ArrayList<Double> longitude = new ArrayList<>(); // Store longitude of stations
    ArrayList<Marker> stationMarkers = new ArrayList<>(); // Store station markers

    // CONSTANTS
    private int MAP_ZOOM = 14;
    private int width = 100;
    private int height = 100;
    private int GEOFENCE_RADIUS = 150;

    // Required public constructor
    public OperMapFrag() {
    }

    // Create a map view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_oper_map, container, false);
        return mView;
    }

    // Should have the same MAP ID as XML file
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String TAG = "onViewCreated";
        Log.i("ClassCalled", "onViewCreated: is running");

        geofencingClient = LocationServices.getGeofencingClient(requireActivity());
        operGeofenceHelper = new OperGeofenceHelper(getActivity());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Bind components to layout
        routeSelects = (AutoCompleteTextView) mView.findViewById(R.id.routeSelect);
        etRouteSelects = (TextInputLayout) mView.findViewById(R.id.etRouteSelect);
        btnStartRoute = mView.findViewById(R.id.btnStartRoute);
        btnEndRoute = mView.findViewById(R.id.btnEndRoute);
        switchSeating = mView.findViewById(R.id.toggleSeating);
        commuterCount = mView.findViewById(R.id.tvCommuterCount);


        builder = new AlertDialog.Builder (getActivity());

        mMapView = (MapView) mView.findViewById(R.id.operator_map);

        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

        checkForCurrentTrip();

        btnStartRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btnStartRoute.onClick: is running");
                startLocationUpdates();
                setCurrentTrip();
                Log.i(TAG, "btnSetRoute.onClick: is running");
            }
        });

        btnEndRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showEndTripDialog();
            }
        });

        switchSeating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSeatingAvailability();
            }
        });
    }

    private void showEndTripDialog () {
        String TAG = "showCancelPara";
        Log.i("ClassCalled", "showCancelPara: is running...");
        builder.setTitle("Ending current trip")
                .setMessage("Would you like to end your current trip? \n\n" +
                        "Make sure you no longer have passengers in your list.")
                .setCancelable(false)
                .setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(TAG, "onClick: commuter is cancelling PARA request");
                        endCurrentTrip();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(R.string.label_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(TAG, "onClick: commuter is not cancelling PARA request");
                        dialogInterface.cancel();
                    }
                })
                .show();

    }

    private void toggleSeatingAvailability() {
        String TAG = "toggleSeatingAvailability";
        Log.i(TAG, "toggleSeatingAvailability: is running...");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        // Get current key id
        DatabaseReference dbOperator = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName());
        dbOperator.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
               for (DataSnapshot dspOperator : task.getResult().getChildren()) {
                   if (dspOperator.getKey().equals(uid)) {
                       for (DataSnapshot dspCurrentTrip : dspOperator.child("current_trip").getChildren()) {
                           dbOperator.child(uid).child("current_trip").child(dspCurrentTrip.getKey()).child("seating_availability").setValue(switchSeating.isChecked());
                       }
                   }
               }
            }
        });
        if (switchSeating.isChecked()) {
            Toast.makeText(getActivity(), R.string.msg_you_are_now_visible, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), R.string.msg_you_are_no_longer_visible, Toast.LENGTH_SHORT).show();
        }

    }

    // Custom map logic and configuration
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        String TAG = "onMapReady";
        MapsInitializer.initialize(getContext());

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap = googleMap;

        getRoutes();

        Log.i(TAG, "onMapReady: removing existing geofences...");
        geofencingClient.removeGeofences(operGeofenceHelper.getPendingIntent())
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

        // TODO check for ongoing trips
        mGoogleMap.setMyLocationEnabled(true);

        // Moves camera to where the route is at
        routeSelects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mGoogleMap.clear();
                selectedRouteName = routeSelects.getText().toString();

                Log.i("VerifyValue", selectedRouteName);

                //From onItemClickListener > matchRouteNames > acquireBusStopsUnderRoutes > matchBusStopsUnderBusStops > generateRouteMarkers
                matchRouteNames();

                Log.i("VerifyValueLatitude", latitude.toString());
                Log.i("OnClick", "User has selected");
            }
        });


        // Remove existing geofences
        Log.i(TAG, "onMapReady: removing existing geofences");
        geofencingClient.removeGeofences(operGeofenceHelper.getPendingIntent())
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

        // Move the camera to a default location
        CameraPosition defaultCamera = CameraPosition.builder()
                .target(new LatLng(14.554705128006361, 121.09289228652042))
                .zoom(15).bearing(0).tilt(0).build();
        googleMap.moveCamera((CameraUpdateFactory.newCameraPosition(defaultCamera)));


    }

    private void checkForCurrentTrip() {
        // This method gets the operator current trip registered from Firebase
        Log.i("ClassCalled", "checkForCurrentTrip is running");
        try {
            FirebaseUser operatorUID = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Operator").child(operatorUID.getUid()).child("current_trip");

            Log.i("Existing Current Route", dbRef.toString());

            if (dbRef.getKey() != null) {
                toggleViewNoTripStarted();
            }
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    for (DataSnapshot dsp : task.getResult().getChildren()) {
                        try {
                            Log.i("Existing Current Route", dsp.child("route_name").getValue().toString());
                            if (dsp.hasChild("route_name")) {
                                existingCurrentRoute = dsp.child("route_name").getValue().toString();
                                selectedRouteName = existingCurrentRoute;
                                routeSelects.setText(existingCurrentRoute);
                                switchSeating.isChecked();
                                mGoogleMap.clear();
                                toggleViewTripStarted();
                                checkForExistingRouteValues();
                                startLocationUpdates();
                                drawRoutes();
//                        tryAddingGeofences();
                            }
                            else {
                                Log.i("Existing Current Route", "No existing current route");
                                toggleViewNoTripStarted();
                            }
                        } catch (Exception e) {
                            Log.e("CHECK FOR CURRENT TRIP", "onDataChange: exception ", e);
                        }
                    }
                }
            });

            //Get existing current trip
//        dbRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
//                    try {
//                        Log.i("Existing Current Route", dsp.child("route_name").getValue().toString());
//                        if (dsp.hasChild("route_name")) {
//                            existingCurrentRoute = dsp.child("route_name").getValue().toString();
//                            selectedRouteName = existingCurrentRoute;
//                            routeSelects.setText(existingCurrentRoute);
//                            switchSeating.isChecked();
//                            mGoogleMap.clear();
//                            toggleViewTripStarted();
//                            checkForExistingRouteValues();
//                            startLocationUpdates();
//                            drawRoutes();
////                        tryAddingGeofences();
//                        }
//                        else {
//                            Log.i("Existing Current Route", "No existing current route");
//                            toggleViewNoTripStarted();
//                        }
//                    } catch (Exception e) {
//                        Log.e("CHECK FOR CURRENT TRIP", "onDataChange: exception ", e);
//                    }
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getActivity(), "Couldn't retrieve any existing trip. Please refresh.", Toast.LENGTH_SHORT).show();
//            }
//        });
        } catch (Exception e) {
            Log.e("CRASH", "checkForCurrentTrip: EXCEPTION", e);
        }

    }

    ArrayList<String> busStopNameExistingRoute = new ArrayList<>();
    //String busStopNameExistingRoute;
    private void checkForExistingRouteValues() {
        //Get Markers
        DatabaseReference dbRoutes = FirebaseDatabase.getInstance().getReference("Routes");
        dbRoutes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                busStopNameExistingRoute.clear();
                String currBusStopName = "";
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Log.i("getExistingRoute()", dsp.child("routeName").getValue().toString());
                    if (dsp.child("routeName").getValue().toString().equals(existingCurrentRoute)) {
                        for (DataSnapshot dspRef : dsp.getChildren()) {
                            Log.i("getExistingRoute()", dspRef.toString());
                            if (dspRef.child("busStopName").exists()) {
                                currBusStopName = dspRef.child("busStopName").getValue().toString();
                                //busStopNameExistingRoute = dspRef.child("busStopName").getValue().toString();
                                //getExistingRouteDrawing();
                                busStopNameExistingRoute.add(currBusStopName);
                                getExistingRouteLatLong();
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

    ArrayList<Double> currentLat = new ArrayList<>();
    ArrayList<Double> currentLong = new ArrayList<>();
    ArrayList<Marker> currentMarkers = new ArrayList<>();

    private void getExistingRouteLatLong() {
        try {
            BitmapDrawable bus_icon = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_bus_stop);
            Bitmap iconified = bus_icon.getBitmap();
            //Get Markers
            DatabaseReference dbRoutes = FirebaseDatabase.getInstance().getReference("Bus_Stop");
            dbRoutes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        String currLat = "";
                        String currLong = "";
                        if (busStopNameExistingRoute.contains(dsp.child("busStopName").getValue().toString())) {
                            currLat = dsp.child("center_lat").getValue().toString();
                            currLong = dsp.child("center_long").getValue().toString();

                            currentMarkers.add(mGoogleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(Double.valueOf(currLat), Double.valueOf(currLong)))
                                    .title(dsp.child("busStopName").getValue().toString())
                                    .icon(BitmapDescriptorFactory.fromBitmap(iconified))));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Couldn't retrieve routes. Please refresh.", Toast.LENGTH_SHORT).show();
                }
            });
//        tryAddingGeofences();
        } catch (Exception e) {
            Log.e("getExistingRouteLatLong", "getExistingRouteLatLong: exception ", e);
        }

    }

    // FUNCTIONS FOR SELECTING, AND VIEWING A ROUTE
    // First, get the Route again that is equivalent to the option that the user chose [Routes node]
    // Then, acquire the bus stop info from the database by matching the routeName [Routes node]
    // Next, get the bus stop info by referring to the [Bus_Stop node] then matching the busStopName
    // After that, if busStopName [Bus_Stop node] matches with busStopName [Routes node] then acquire long lat
    // Profit
    private void getRoutes() {
        // This method gets the routes registered from Firebase
        Log.i("ClassCalled", "getRoutes is running");
        DatabaseReference dbRoutes = FirebaseDatabase.getInstance().getReference("Routes");

        //Get Routes
        dbRoutes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                routeId.clear();
                routeNames.clear();

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    // Get data from each node
                    String routeid = dsp.getKey();
                    String routename = dsp.child("routeName").getValue().toString();
                    //Debug
                    Log.i("DBValue", routeid);
                    Log.i("DBValue", routename);
                    // Adds data to array list
                    routeId.add(routeid);
                    routeNames.add(routename);
                }

                // Convert arraylist to a string[]
                routeItems = new String[routeNames.size()];
                for (int i = 0; i < routeNames.size(); i++) {
                    routeItems[i] = routeNames.get(i);
                }

                // Selecting from route autocompleteview
                routeChoices = new ArrayAdapter<String>(getActivity(), R.layout.dropdown_list, routeItems);
                routeSelects.setAdapter(routeChoices);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Couldn't retrieve routes. Please refresh.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    String current_stop;
    private void getCurrentStop() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference dbOperator = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName()).child(user.getUid()).child("current_trip");
            dbOperator.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    try {
                        for (DataSnapshot dspCurrentTrip : task.getResult().getChildren()) {
                            if (dspCurrentTrip.child("current_stop").exists()) {
                                current_stop = dspCurrentTrip.child("current_stop").getValue().toString();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("GETCURRENTROUTE", "onComplete: ", e);
                    }
                }
            });

            getCommutersInCurrentStop();
        } catch (Exception e) {
            Log.e("CRASH REPORT", "getCurrentStop: exception ", e);
        }

    }

    ArrayList<String> Commuters = new ArrayList<>();
    private void getCommutersInCurrentStop() {
        String TAG = "getCommutersInCurrentStop";
        Log.i(TAG, "getCommutersInCurrentStop: is running");


        DatabaseReference dbCommuter = FirebaseDatabase.getInstance().getReference(Commuter.class.getSimpleName());
        dbCommuter.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                try {
                    for (DataSnapshot dspCommuter : task.getResult().getChildren()) {
                        if (dspCommuter.child("current_trip").exists()) {
                            Log.i(TAG, "onComplete: dspCommuter.key " + dspCommuter.getKey());
                            for (DataSnapshot dspCurrentTrip : dspCommuter.child("current_trip").getChildren()) {
                                Log.i(TAG, "onComplete: dspCurrentTrip.key " + dspCurrentTrip.getKey());
                                Log.i(TAG, "onComplete: dspCurrentTrip.current_stop " + dspCurrentTrip.child("current_stop").getValue().toString());
                                Log.i(TAG, "onComplete: current_stop " + current_stop);
                                if (dspCurrentTrip.child("current_stop").getValue().toString().equals(current_stop)) {
                                    Log.i(TAG, "onComplete: current_stop matches");
                                    Log.i(TAG, "onComplete: commuter key " + dspCommuter.getKey());

                                    if (!Commuters.contains(dspCommuter.getKey())) {
                                        Commuters.add(dspCommuter.getKey());
                                    }
                                    Log.i(TAG, "onComplete: Commuters in Current Stop = " + Commuters.size());
                                }
                            }
                        }
                    }
//                    Toast.makeText(getActivity(), "There are " + Commuters.size() + " in this bus stop.", Toast.LENGTH_SHORT).show();
                    commuterCount.setText(getString(R.string.label_commuters_in_bus_stop) + Commuters.size());
                    Commuters.clear();
                } catch (Exception e) {
                    Log.e(TAG, "onComplete: exception", e);
                }
            }
        });
    }

    private Timer updateCommuters = new Timer();
    private TimerTask timerTask2;
//    private void stopUpdates() {
//        if(updateCommuters != null){
//            updateCommuters.cancel();
//            updateCommuters.purge();
//            Log.i("ClassCalled", "stopUpdates: timer loop stopped");
//            Commuters.clear();
//        }
//    }
//
//    private void updateCommutersInStop() {
//        String TAG = "updateCommutersInStop";
//        Log.i("ClassCalled", "updateCommutersInStop: is running");
//
//
//        timerTask2 = new TimerTask() {
//            public void run() {
//                handler.post(new Runnable() {
//                    public void run(){
//                        //update commuters in stop
//                        getCurrentStop();
//                        Log.i(TAG, "run: location updated");
//                    }
//                });
//            }
//        };
//        updateCommuters.schedule(timerTask2, 5000, 5000);
//    }



    private void generateRouteMarkers() {
        Log.i("ClassCalled", "generateReouteMarkers is running");
        Log.i("VerifyValueLatitude", latitude.toString());
        mGoogleMap.clear(); // Clear existing markers
        BitmapDrawable bus_icon = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_bus_stop);
        Bitmap iconified = bus_icon.getBitmap();


        // Generate new markers for each station
        for (int i = 0; i < stationInBusStopID.size(); i++) {
            stationMarkers.add(mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude.get(i), longitude.get(i)))
                    .title(String.valueOf(stationInRoutesName.get(i)))
                    .icon(BitmapDescriptorFactory.fromBitmap(iconified))));
        }
        drawRoutes();
    }

    private void matchRouteNames() {
        Log.i("VerifyValue",selectedRouteName);
        Log.i("Verify",routeId.toString());
        Log.i("ClassCalled","matchRouteNames is running");

        DatabaseReference dbRoutes = FirebaseDatabase.getInstance().getReference("Routes");

        // Acquiring Routes and matching with user selected Route. If match, then acquire busStopName
        // Get Routes and Bus Stop Info under Routes
        dbRoutes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Debug
                Log.i("DatabaseRunning","Going through Routes tree");
                routeSnapID.clear();
                routeSnapName.clear();
                //Get data under child
                for (DataSnapshot getRouteSnapshot: dataSnapshot.getChildren()) {
                    Log.i("DatabaseRunning","Going through children of Routes tree");
                    // Get data from each node
                    String routeid = getRouteSnapshot.getKey();
                    String routename = getRouteSnapshot.child("routeName").getValue().toString();
                    //Debug
                    Log.i("VerifyValue",selectedRouteName);
                    Log.i("DBValue",routeid);
                    //test if matching input with routeName
                    boolean test = selectedRouteName.equals(routename);
                    //if test is true
                    if (test) {
                        routeSnapID.add(routeid);
                        Log.i("Verify",routeSnapID.toString());
                    }
                }
                acquireBusStopsUnderRoutes();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Couldn't retrieve bus stops. Please refresh.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acquireBusStopsUnderRoutes() {
        String trmStr = routeSnapID.toString();
        trmStr = trmStr.replaceAll("\\[", "").replaceAll("\\]","");
        Log.i("Verify",routeSnapID.toString());
        Log.i("Verify",trmStr);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference dbRoutes = ref.child("Routes").child(trmStr);

        //Acquiring busStopName under the specific route selected by the user
        dbRoutes.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshots) {
                stationInRoutesName.clear();
                stationInRoutesOrder.clear();

                //Get data under child
                for (DataSnapshot getRouteSnapshot: dataSnapshots.getChildren()) {
                    if (getRouteSnapshot.child("busStopName").exists()) {
                        Log.i("Verify",getRouteSnapshot.child("busStopName").getValue().toString());
                        // Get data from each node
                        String stationname = "";
                        String stationorder = "";
                        stationname = getRouteSnapshot.child("busStopName").getValue().toString();
                        stationorder = getRouteSnapshot.child("order").getValue().toString();
                        //test if matching input with routeName
                        stationInRoutesName.add(stationname);
                        stationInRoutesOrder.add(stationorder);
                        Log.i("Test",stationInRoutesName.toString());
                    }
                }
                matchBusStopsUnderBusStops();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Couldn't retrieve bus stops. Please refresh.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void matchBusStopsUnderBusStops(){
        // This method gets the stations registered from Firebase
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Bus_Stop");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stationInBusStopID.clear();
                stationInBusStopName.clear();
                latitude.clear();
                longitude.clear();

                //Get data under child
                for (DataSnapshot getRouteSnapshot: dataSnapshot.getChildren()) {
                    Log.i("DatabaseRunning", "Going through children of Routes tree");
                    // Get data from each node
                    String busstationid = getRouteSnapshot.getKey();
                    String busstationname = getRouteSnapshot.child("busStopName").getValue().toString();
                    Double lat = 0.0;
                    Double lon = 0.0;
                    // test if matching input with routeName
                    boolean test = stationInRoutesName.contains(busstationname);

                    if (test) {
                        // if test is true
                        if (getRouteSnapshot.child("center_lat").exists() && getRouteSnapshot.child("center_long").exists()) {
                            lat = Double.parseDouble(getRouteSnapshot.child("center_lat").getValue().toString());
                            lon = Double.parseDouble(getRouteSnapshot.child("center_long").getValue().toString());
                        }

                        try {
                            stationInBusStopID.add(busstationid);
                            stationInBusStopName.add(busstationname);
                            latitude.add(lat);
                            longitude.add(lon);
                        } catch (Exception e) {
                            // handle the exception here
                        }
                    }
                }
                generateRouteMarkers();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Couldn't retrieve bus stops. Please refresh.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoutes(){
        // This method gets the route drawings from Firebase
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Route_Drawing").child(selectedRouteName);
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Get data under child
                for (DataSnapshot getRouteDrawingSnapshot: dataSnapshot.getChildren()) {
                    String encodedPolyline = "";

                    // if test is true
                    if (getRouteDrawingSnapshot.child("polyline").exists()) {
                        encodedPolyline = getRouteDrawingSnapshot.child("polyline").getValue().toString();
                        List<LatLng> decodedPolyline = PolyUtil.decode(encodedPolyline);
                        mGoogleMap.addPolyline(new PolylineOptions().addAll(decodedPolyline));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Couldn't retrieve bus stops. Please refresh.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // FUNCTIONS FOR MANAGING A TRIP
    private void setCurrentTrip() {
        String TAG = "startRide";
        Log.i("ClassCalled", "startRide: is running");

        // Create the node for the current trip
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Get operator user database record
        // Create a data object for the information on the ride
        // Save the ride to a new ride_history node

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        String date = sdf.format(c.getTime());
        String time = String.valueOf(Calendar.getInstance().getTime());


        // Get the values from the drop down menu
        chosenRoute = routeSelects.getText().toString();

        if (chosenRoute.isEmpty()) {
            routeSelects.setError(getString(R.string.err_fieldRequired));
            return;
        }

        // Get origin and destination
        Log.i(TAG, "setCurrentRide: route bus stops " + stationInRoutesName);
        Log.i(TAG, "setCurrentRide: route bus stop order " + stationInRoutesOrder);

        // Find the midpoint of two stations
        findMidPoint();
        Operator_Trip current_trip = new Operator_Trip(chosenRoute, true);
        switchSeating.setChecked(true);
        // Saving the current trip into the database
        DatabaseReference db = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName());
        Log.i(TAG, "setCurrentRide: verify database " + db);

        // Clear current trip record if any
        try {
            db.child(user.getUid()).child("current_trip").removeValue();
            Log.i(TAG, "setCurrentTrip: cleared current_trip");
        } catch (Exception e) {
            Log.i(TAG, "setCurrentTrip: current trip cleared " + e);
        }

        DatabaseReference dbOperator = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName()).child(user.getUid()).child("current_trip");
        dbOperator.push().setValue(current_trip);

        toggleViewTripStarted();
        // try adding geofences
        try {
            if (ContextCompat.checkSelfPermission((requireActivity()), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                Log.i(TAG, "btnSetRoute.tryAddingGeofence: passed");
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    // We should show a dialog explaining why we need this
                    // TODO Dialog popup?
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
                }
                tryAddingGeofences();
                Log.i(TAG, "btnSetRoute.tryAddingGeofence: passed");
            }
        } catch (Exception e) {
            Log.e(TAG, "onComplete: ", e);
        }


        tryAddingGeofences();
    }

    private void endCurrentTrip() {
        String TAG = "endCurrentRoute";
        Log.i("ClassCalled", "endCurrentRoute: is running");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference[] db = {FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName()).child(user.getUid()).child("current_trip")};
        Log.i(TAG, "endCurrentRoute: verify db reference " + db[0]);

        db[0].get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String trip_id = "";
                        String id = "";
                        String route = "";
                        String username = "";
                        String auditory = "";
                        String mobility = "";
                        String wheelchair = "";
                        String origin = "";
                        String destination = "";
                        ArrayList <Passenger_List> passenger_list = new ArrayList<>();


                        for (DataSnapshot dspCurrentTrip : task.getResult().getChildren()) {
                            trip_id = dspCurrentTrip.getKey();
                            route = dspCurrentTrip.child("route_name").getValue().toString();

                            if (dspCurrentTrip.child("current_stop").exists()) {
                                current_stop = dspCurrentTrip.child("current_stop").getValue().toString();
                            }

                            Log.i(TAG, "onComplete: CHECKING NUMBER OF PASSENGERS...");
                            Log.i(TAG, "onComplete: " + dspCurrentTrip.child("passenger_list").getChildrenCount());
                            Passenger_List passenger = new Passenger_List();
                            ArrayList<String> times_disembarked = new ArrayList<>();

                            for (DataSnapshot dspPassengerList : dspCurrentTrip.child("passenger_list").getChildren()) {
                                Log.i(TAG, "onComplete: CHECKING PASSENGER LIST...");
                                Log.i(TAG, "onComplete: " + dspPassengerList.getKey());
                                id = dspPassengerList.getKey();
                                username = dspPassengerList.child("username").getValue().toString();
                                auditory = dspPassengerList.child("auditory").getValue().toString();
                                mobility = dspPassengerList.child("mobility").getValue().toString();
                                wheelchair = dspPassengerList.child("wheelchair").getValue().toString();
                                origin = dspPassengerList.child("origin").getValue().toString();
                                destination = dspPassengerList.child("destination").getValue().toString();
                                try {
                                    for (DataSnapshot dspTimes : dspPassengerList.child("times_disembarked").getChildren()) {
                                        times_disembarked.add(dspPassengerList.child("times_disembarked").child(dspTimes.getKey()).getValue().toString());
                                    }
                                    passenger_list.add(new Passenger_List(id, username, auditory, mobility, wheelchair, origin, destination, times_disembarked));
                                } catch (Exception e) {
                                    Log.e(TAG, "onComplete: CRASHED " , e);
                                }
                            }
                        }
                        DatabaseReference dbOperator = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName()).child(user.getUid()).child("ride_history").child(trip_id);

                        dbOperator.child("route_name").setValue(route);
                        dbOperator.child("passenger_list").setValue(passenger_list);
                        dbOperator.child("last_stop").setValue(current_stop);
                    } else {
                        Toast.makeText(getActivity(), R.string.err_failedToReadData, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onComplete: data does not exist");
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.err_unknown, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onComplete: retrieve data could not be completed");
                }
            }
        });
        geofencingClient.removeGeofences(operGeofenceHelper.getPendingIntent())
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
                        Log.e(TAG, "onFailure: exception ", e);
                    }
                });

        db[0] = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName());
        db[0].child(user.getUid()).child("current_trip").removeValue();
        Log.i(TAG, "setCurrentTrip: cleared current_trip");
        mGoogleMap.clear();

        stationMarkers.clear();
        Log.i(TAG, "endCurrentRoute: station markers removed " + stationMarkers);
        // Stop looping location updates
        Log.i(TAG, "endCurrentRoute: stopping location updates...");
        stopLocationUpdates();
        toggleViewNoTripStarted();
    }

    // FUNCTIONS FOR LOCATION UPDATES
    private void stopLocationUpdates(){
        String TAG = "stopTimer";
        Log.i("ClassCalled", "stopTimer: is running");

        if(timer != null){
            timer.cancel();
            timer.purge();
            Log.i(TAG, "stopTimer: timer loop stopped");
            //toggleViewNoTripStarted();
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
                        //update location every set interval
                        updateLocation();
                        updatePassengerList();
                        getCurrentStop();

                        Log.i(TAG, "run: location updated");
                    }
                });
            }
        };
        timer.schedule(timerTask, 500, 10000);
    }


    int limiter = 0;
    // Function for updating operator that commuter wants to para
    private void updatePassengerList() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.i("SEE HERE","Update Passenger List is working");

        try {
            // This method gets the route drawings from Firebase
            DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Operator").child(user.getUid()).child("current_trip");
            db.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //Get data under child
                    for (DataSnapshot getPassengerSnapshot : dataSnapshot.getChildren()) {
                        if (getPassengerSnapshot.hasChild("passenger_list")) {
                            Log.i("SEE HERE", "Passenger list exists");
                            Log.i("SEE HERE", getPassengerSnapshot.getValue().toString());
                            DataSnapshot passengerListDSP = getPassengerSnapshot.child("passenger_list");
                            for (DataSnapshot getPassengerListSnapshot : passengerListDSP.getChildren()) {
                                // if test is true
                                if (getPassengerListSnapshot.child("para").exists()) {
                                    Log.i("SEE HERE", "Para Exists");
                                    Log.i("SEE HERE", getPassengerListSnapshot.child("para").toString());
                                    Log.i("SEE HERE", getPassengerListSnapshot.child("para").getValue().toString());
                                    if (getPassengerListSnapshot.child("para").getValue().toString().equals("true")) {
                                        Log.i("SEE HERE", "Para is now true");
                                        Log.i("SEE HERE", getPassengerListSnapshot.child("para").toString());
                                        if (limiter == 0) {
                                            builder.setTitle("PARA! Bababa")
                                                    .setMessage("Someone wants to disembark. Please assist passengers at the designated bus stop.")
                                                    .setCancelable(true)
                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            startLocationUpdates();
                                                            dialogInterface.dismiss();
                                                            limiter = 0;
                                                        }
                                                    })
                                                    .setNegativeButton("", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                                    .show();
                                            limiter = 1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            Log.e("CRASHED DURING LOGOUT FROM OPERATOR", "updatePassengerList: exception ", e);
        }
    }

    // Function to update location in firebase every set time interval
    @SuppressLint("MissingPermission")
    private void updateLocation() {
        String TAG = "updateLocation";
        Log.i("ClassCalled", "updateLocation: is running");
        try {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isComplete()) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "onComplete: " + task.getResult());
                            lastLocation[0] = new Location(task.getResult());
                            Log.i(TAG, "verify latitude: " + lastLocation[0].getLatitude());
                            Log.i(TAG, "verify longitude: " + lastLocation[0].getLongitude());
                            saveLocationToDatabase(lastLocation[0].getLatitude(), lastLocation[0].getLongitude());
                        }
                    }
                }
            });
        }  catch (Exception e) {
            Log.e("CRASH", "updateLocation: ", e);
        }


    }

    public void saveLocationToDatabase(double latitude, double longitude) {
        String TAG = "saveLocationToDatabase";
        Log.i("ClassCalled", "saveLocationToDatabase: is running");


        // Check current_lat and current_long from pass values
        Log.i(TAG, "saveLocationToDatabase: latitude verify " + latitude);
        Log.i(TAG, "saveLocationToDatabase:  longitude verify " + longitude);


        try {
            // Get current user
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            DatabaseReference dbInput = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName()).child(user.getUid()).child("current_trip");
            dbInput.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    try {
                        for (DataSnapshot dsp : task.getResult().getChildren()) {
                            dbInput.child(dsp.getKey()).child("current_lat").setValue(latitude);
                            dbInput.child(dsp.getKey()).child("current_long").setValue(longitude);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onComplete: exception ", e);
                    }

                }
            });
        } catch (Exception e) {
            Log.e(TAG, "saveLocationToDatabase: CRASH ", e);
        }

    }

    // FUNCTIONS FOR GEOFENCING
    @SuppressLint("MissingPermission")
    private void addGeofence(String geofence_id, LatLng latLng, float radius) {
        String TAG = "addGeofence";
        Log.i("ClassCalled", "addGeofence: is running");

        Geofence geofence = operGeofenceHelper.getGeofence(geofence_id, latLng, radius,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = operGeofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = operGeofenceHelper.getPendingIntent();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i(TAG, "onSuccess: geofences added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                    }
                });

    }

    private void tryAddingGeofences() {
        String TAG = "tryAddingGeofencese";
        Log.i("ClassCalled", "tryAddingGeofencese: is running");


        Log.i(TAG, "tryAddingGeofences: removing existing geofences...");
        geofencingClient.removeGeofences(operGeofenceHelper.getPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i(TAG, "onSuccess: existing geofences removed successfully");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onFailure: could not remove existing geofences");
                    }
                });


        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Bus_Stop");
        Log.i(TAG, "tryAddingGeofences: database reference " + db);


        try {
            for (int i = 0; i < stationMarkers.size(); i++) {
                addGeofence(stationInBusStopID.get(i), new LatLng(stationMarkers.get(i).getPosition().latitude, stationMarkers.get(i).getPosition().longitude), GEOFENCE_RADIUS);
            }
            Log.i(TAG, "tryAddingGeofences: stations in route geofences added");
//            updateCommutersInStop();
        } catch (Exception e) {
            Log.e(TAG, "tryAddingGeofences: exception ", e);
        }

    }


    private void findMidPoint() {
        Log.i("ClassCalled", "findMidpoint is running");

        try{
            Double originLatitude = stationMarkers.get(0).getPosition().latitude;
            Double originLongitude = stationMarkers.get(0).getPosition().longitude;
            Double destinationLatitude = stationMarkers.get(stationMarkers.size()-1).getPosition().latitude;
            Double destinationLongitude = stationMarkers.get(stationMarkers.size()-1).getPosition().longitude;

            // Calculate for the midpoint between the two locations
            Double midLat = (originLatitude + destinationLatitude) / 2;
            Double midLong = (originLongitude + destinationLongitude) / 2;

            Log.i("findMidpoint", "midLat: " + originLatitude);
            Log.i("findMidpoint", "midLong: " + originLongitude);

            // Clear the map of the other markers
            // Move the camera to new midpoint location
            CameraPosition midpoint = CameraPosition.builder()
                    .target(new LatLng(midLat, midLong))
                    .zoom(12).bearing(0).tilt(0).build();
            mGoogleMap.moveCamera((CameraUpdateFactory.newCameraPosition(midpoint)));
            Log.i("findMidpoint", "moveCamera: successful");

            // Will zoom and pan the camera to the location of the user after 3 seconds
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    zoomToUserLocation();
                }
            }, 3000);
        } catch (Exception e) {
            Log.e("findMidpoint", "Exception ", e);
        }
    }

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

    private void toggleViewNoTripStarted() {
        switchSeating.setVisibility(View.GONE);
        commuterCount.setVisibility(View.GONE);
        btnStartRoute.setVisibility(View.VISIBLE);
        btnEndRoute.setVisibility(View.GONE);
        switchSeating.setChecked(false);
        routeSelects.setFocusable(true);
        routeSelects.setClickable(true);
        routeSelects.setEnabled(true);
        routeSelects.setFocusableInTouchMode(true);
        etRouteSelects.setFocusable(true);
        etRouteSelects.setClickable(true);
        etRouteSelects.setEnabled(true);
        etRouteSelects.setFocusableInTouchMode(true);
        routeSelects.setText(null);

//        stopUpdates();
    }
    private void toggleViewTripStarted() {
        switchSeating.setVisibility(View.VISIBLE);
        btnEndRoute.setVisibility(View.VISIBLE);
        btnStartRoute.setVisibility(View.GONE);
        commuterCount.setVisibility(View.VISIBLE);
        routeSelects.setFocusable(false);
        routeSelects.setClickable(false);
        routeSelects.setFocusableInTouchMode(false);
        routeSelects.setEnabled(false);
        etRouteSelects.setFocusable(false);
        etRouteSelects.setClickable(false);
        etRouteSelects.setEnabled(false);
        etRouteSelects.setFocusableInTouchMode(false);
    }

//    // Function that draws a circle to indicate the geofence boundaries of a bus stop
//    private void addCircle(LatLng latLng, float geofence_radius) {
//        String TAG = "addCircle";
//        Log.i(TAG, "addCircle: is running");
//
//        CircleOptions circleOptions = new CircleOptions();
//        circleOptions.center(latLng);
//        circleOptions.radius(geofence_radius);
//        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
//        circleOptions.fillColor(Color.argb(65, 255, 0, 0));
//        circleOptions.strokeColor(4);
//        mGoogleMap.addCircle(circleOptions);
//    }
}
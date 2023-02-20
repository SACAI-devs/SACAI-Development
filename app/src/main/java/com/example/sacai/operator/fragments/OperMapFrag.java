package com.example.sacai.operator.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import com.example.sacai.dataclasses.Operator;
import com.example.sacai.dataclasses.Operator_Trip;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class OperMapFrag extends Fragment implements OnMapReadyCallback {
    // Call variables based on GoogleMaps Documentation
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;
    FusedLocationProviderClient fusedLocationProviderClient;

    // Gobal Variables
    private GeofencingClient geofencingClient;
    private OperGeofenceHelper operGeofenceHelper;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 10002;
    
    // Components
    AutoCompleteTextView routeSelects;
    Button btnStartRoute, btnEndRoute;

    String routeIDWithNoBrackets;
    //String routeSnapID; // Store station name in the Routes branch
    String selectedRouteName;
    String chosenRoute;


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
    private int MAP_ZOOM = 2;
    private int width = 100;
    private int height = 100;
    private int GEOFENCE_RADIUS = 500;

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
        btnStartRoute = mView.findViewById(R.id.btnStartRoute);
        btnEndRoute = mView.findViewById(R.id.btnEndRoute);

        mMapView = (MapView) mView.findViewById(R.id.operator_map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }


        btnStartRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btnStartRoute.onClick: is running");
                setCurrentTrip();
                Log.i(TAG, "btnSetRoute.onClick: is running");
            }
        });

        btnEndRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endCurrentRoute();
            }
        });

    }

    private void endCurrentRoute() {
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
                        String id = "";
                        String route = "";
                        String origin = "";
                        String destination = "";
                        String passenger_list_id = "";  // Must get passenger list but temporarily empty

                        for (DataSnapshot dsp : task.getResult().getChildren()) {
                            id = dsp.getKey();
                            route = dsp.child("route_name").getValue().toString();
                            origin = dsp.child("origin_stop").getValue().toString();
                            destination = dsp.child("destination_stop").getValue().toString();
                            // passenger_list_id = dsp.child("passenger_list_id").getValue().toString();
                        }

                        // Save the information to firebase
                        HashMap Ride = new HashMap<>();
                        Operator_Trip operatorTrip = new Operator_Trip(id, route, origin, destination, "", "");

                        Ride.put("id", operatorTrip.getId());
                        Ride.put("route_name", operatorTrip.getRoute_name());
                        Ride.put("origin_stop", operatorTrip.getOrigin_stop());
                        Ride.put("destination_stop", operatorTrip.getDestination_stop());
                        Ride.put("passenger_list_id", "TEMP ID");

                        db[0] = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName()).child(user.getUid()).child("ride_history").child(id);

                        db[0].updateChildren(Ride);
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
                        // ...
                    }
                });

        db[0] = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName());
        db[0].child(user.getUid()).child("current_trip").removeValue();
        Log.i(TAG, "setCurrentTrip: cleared current_trip");
        mGoogleMap.clear();
    }

    // Custom map logic and configuration
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap = googleMap;

        getRoutes();
        mGoogleMap.setMyLocationEnabled(true);

        // Moves camera to where the route is at
        routeSelects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedRouteName = routeSelects.getText().toString();

                Log.i("VerifyValue",selectedRouteName);

                //From onItemClickListener > matchRouteNames > acquireBusStopsUnderRoutes > matchBusStopsUnderBusStops > generateRouteMarkers
                matchRouteNames();


                Log.i("VerifyValueLatitude",latitude.toString());
                Log.i("OnClick","User has selected");
            }
        });

        CameraPosition rainforestPark = CameraPosition.builder().target(new LatLng(14.574970139259474, 121.09785961494917)).zoom(16).bearing(0).tilt(0).build();
        googleMap.moveCamera((CameraUpdateFactory.newCameraPosition(rainforestPark)));

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

    }

    // Function to generate route markers
    private void generateRouteMarkers() {
        Log.i("ClassCalled","generateReouteMarkers is running");
        Log.i("VerifyValueLatitude",latitude.toString());
        mGoogleMap.clear(); // Clear existing markers
        BitmapDrawable bus_icon = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_bus_stop);
        Bitmap iconified = bus_icon.getBitmap();
        // Generate new markers for each station
        for (int i = 0; i < stationInBusStopID.size(); i++) {
            stationMarkers.add(mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude.get(i), longitude.get(i)))
                    .title(stationInBusStopName.get(i))
                    .icon(BitmapDescriptorFactory.fromBitmap(iconified))));
        }
        drawRoutes();
    }

    // Function to get routes from database
    private void getRoutes() {
        // This method gets the routes registered from Firebase
        Log.i("ClassCalled","getRoutes is running");
        DatabaseReference dbRoutes = FirebaseDatabase.getInstance().getReference("Routes");

        //Get Routes
        dbRoutes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                routeId.clear();
                routeNames.clear();

                for (DataSnapshot dsp: dataSnapshot.getChildren()) {
                    // Get data from each node
                    String routeid = dsp.getKey();
                    String routename = dsp.child("routeName").getValue().toString();
                    //Debug
                    Log.i("DBValue",routeid);
                    Log.i("DBValue",routename);
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

    // Function to set a current trip
    private void setCurrentTrip() {
        String TAG = "startRide";
        Log.i("ClassCalled", "startRide: is running");

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

        // Create the node for the current trip
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Operator_Trip current_trip = new Operator_Trip("", chosenRoute, stationInBusStopName.get(0), stationInBusStopName.get(stationInBusStopName.size()-1), "", "");

        // Saving the current trip into the database
        DatabaseReference db = FirebaseDatabase.getInstance().getReference(Operator.class.getSimpleName());
        Log.i(TAG, "setCurrentRide: verify database " + db);

        db.child(user.getUid()).child("current_trip").push().setValue(current_trip).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()) {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "onComplete: current trip information has been added to the database");

                        // Configure UI
                        toggleMapUI();
                        // try adding geofence
                        // We need background location services permission
                        try {
                            if (ContextCompat.checkSelfPermission((requireActivity()), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                tryAddingGeofences();
                                Log.i(TAG, "btnSetRoute.tryAddingGeofence: passed");
                            } else {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                                    // We should show a dialog explaining why we need this
                                    // TODO Dialog popup?
                                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
                                } else {
                                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
                                }
                                tryAddingGeofences();
                                Log.i(TAG, "btnSetRoute.tryAddingGeofence: passed");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "onComplete: ", e);
                        }
                    } else {
                        Log.i(TAG, "onComplete: task is not successful");
                    }
                } else {
                    Log.i(TAG, "onComplete: task could not be completed");
                }
            }
        });
    }

    private void toggleMapUI() {
        String TAG = "toggleMapUI";
        Log.i("ClassCalled", "toggleMapUI: is running");
    }

    // Function to find the midpoint of the first and last station in the route
    // and then redirect the camera back to the user
    private void findMidPoint() {
        Log.i("ClassCalled", "findMidpoint is running");

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


        // Get the bus stops in the route
        Log.i(TAG, "tryAddingGeofencese: " + stationInRoutesName);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Bus_Stop");
        Log.i(TAG, "tryAddingGeofences: database reference " + db);
    
        
        try {
            for (int i = 0; i < stationMarkers.size(); i++) {
                addGeofence(stationInBusStopName.get(i), new LatLng(stationMarkers.get(i).getPosition().latitude, stationMarkers.get(i).getPosition().longitude), GEOFENCE_RADIUS);
            }
            Log.i(TAG, "tryAddingGeofences: stations in route geofences added");
        } catch (Exception e) {
            Log.e(TAG, "tryAddingGeofences: exception ", e);
        }

    }

    // First, get the Route again that is equivalent to the option that the user chose [Routes node]
    // Then, acquire the bus stop info from the database by matching the routeName [Routes node]
    // Next, get the bus stop info by referring to the [Bus_Stop node] then matching the busStopName
    // After that, if busStopName [Bus_Stop node] matches with busStopName [Routes node] then acquire long lat
    // Profit
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

    //Draw routes
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

}
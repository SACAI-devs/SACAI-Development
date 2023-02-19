package com.example.sacai.operator.fragments;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.example.sacai.commuter.CommGeofenceHelper;
import com.example.sacai.R;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class OperMapFrag extends Fragment implements OnMapReadyCallback {
    // Call variables based on GoogleMaps Documentation
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;
    FusedLocationProviderClient fusedLocationProviderClient;

    // Gobal Variables
    private GeofencingClient geofencingClient;
    private OperGeofenceHelper geofenceHelper;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 10002;
    
    // Components
    AutoCompleteTextView routeSelects;
    Button btnStartRoute;

    String routeIDWithNoBrackets;
    //String routeSnapID; // Store station name in the Routes branch
    String selectedRouteName;

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
        geofenceHelper = new OperGeofenceHelper(getActivity());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Bind components to layout
        routeSelects = (AutoCompleteTextView) mView.findViewById(R.id.routeSelect);
        btnStartRoute = mView.findViewById(R.id.btnStartRoute);

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
                tryAddingGeofences();
                Log.i(TAG, "btnSetRoute.onClick: is running");
            }
        });

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

        geofencingClient.removeGeofences(geofenceHelper.getPendingIntent())
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



    private void getRoutes() {
        // This method gets the routes registered from Firebase
        Log.i("ClassCalled","getRoutes is running");
        DatabaseReference databaseReferenceRoutes = FirebaseDatabase.getInstance().getReference("Routes");

        //Get Routes
        databaseReferenceRoutes.addValueEventListener(new ValueEventListener() {
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
    
    @SuppressLint("MissingPermission")
    private void addGeofence(String geofence_id, LatLng latLng, float radius) {
        String TAG = "addGeofence";
        Log.i("ClassCalled", "addGeofence: is running");
        
        Geofence geofence = geofenceHelper.getGeofence(geofence_id, latLng, radius,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bus_Stop");
        Log.i(TAG, "tryAddingGeofences: database reference " + databaseReference);
    
        
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

        DatabaseReference databaseReferenceRoutes = FirebaseDatabase.getInstance().getReference("Routes");

        // Acquiring Routes and matching with user selected Route. If match, then acquire busStopName
        // Get Routes and Bus Stop Info under Routes
        databaseReferenceRoutes.addValueEventListener(new ValueEventListener() {
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
        DatabaseReference databaseReferenceRoutes = ref.child("Routes").child(trmStr);
        //Acquiring busStopName under the specific route selected by the user
        databaseReferenceRoutes.addValueEventListener(new ValueEventListener() {

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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bus_Stop");
        databaseReference.addValueEventListener(new ValueEventListener() {
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Route_Drawing").child(selectedRouteName);
        databaseReference.addValueEventListener(new ValueEventListener() {
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
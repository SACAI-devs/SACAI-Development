package com.example.sacai.operator.fragments;

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

import com.example.sacai.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class OperMapFrag extends Fragment implements OnMapReadyCallback {
    // Call variables based on GoogleMaps Documentation
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;

    // Components
    AutoCompleteTextView routeSelects;
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

    // CONSTANTS
    private int MAP_ZOOM = 2;
    private int width = 100;
    private int height = 100;

    // Required public constructor
    public OperMapFrag() {
    }

    // Create a map view
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
//        Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_oper_map, container, false);

        return mView;
    }

    // Should have the same MAP ID as XML file
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind components to layout
        routeSelects = (AutoCompleteTextView) mView.findViewById(R.id.routeSelect);

        mMapView = (MapView) mView.findViewById(R.id.operator_map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }


    // Custom map logic and configuration
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap = googleMap;

        getRoutes();

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

        //=============Testing==============//
        // TODO: RETRIEVE STATIONS FROM DATABASE AND MAP THEM TO MARKERS TO DISPLAY ON MAPS
        /* googleMap.addMarker(new MarkerOptions().position(new LatLng(14.574970139259474, 121.09785961494917)).title("Rainforest Park"));
        CameraPosition rainforestPark = CameraPosition.builder().target(new LatLng(14.574970139259474, 121.09785961494917)).zoom(16).bearing(0).tilt(0).build();
        googleMap.moveCamera((CameraUpdateFactory.newCameraPosition(rainforestPark))); */
    }

    private void generateRouteMarkers() {
        Log.i("ClassCalled","generateReouteMarkers is running");
        Log.i("VerifyValueLatitude",latitude.toString());
        mGoogleMap.clear(); // Clear existing markers
        int width = 100;
        int height = 100;
        BitmapDrawable bus_icon = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_bus_stop);
        Bitmap b = bus_icon.getBitmap();
        Bitmap iconified = Bitmap.createScaledBitmap(b, width, height, false);
        // Generate new markers for each station
        for (int i = 0; i < stationInBusStopID.size(); i++) {
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude.get(i), longitude.get(i)))
                    .title(stationInBusStopName.get(i))
                    .icon(BitmapDescriptorFactory.fromBitmap(iconified)));
        }
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
                routeChoices = new ArrayAdapter<String>(getActivity(), R.layout.component_list_item, routeItems);
                routeSelects.setAdapter(routeChoices);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Couldn't retrieve routes. Please refresh.", Toast.LENGTH_SHORT).show();
            }
        });
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
        /*DatabaseReference databaseReferenceStations = FirebaseDatabase.getInstance().getReference("Bus_Stop");
        // Match Bus_Stop and Routes busStopName then
        databaseReferenceStations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("DatabaseRunning","Going through Bus_Stop tree");
                stationInBusStopID.clear();
                matchBusStopName.clear();
                latitude.clear();
                longitude.clear();
                for (DataSnapshot dsp: dataSnapshot.getChildren()) {
              *//*      *//*//*//* Get data from each node
                    String busstopid = dsp.getKey();
                    String busstopname = dsp.child("busStopName").getValue().toString();

                    boolean test = stationInRoutesName.contains(busstopname);

                    if (test) {
                        try {
                            Double lat = Double.parseDouble(dsp.child("center_lat").getValue().toString());
                            Double lon = Double.parseDouble(dsp.child("center_long").getValue().toString());
                            Log.i("DBValue",busstopname);
                            Log.i("DBValue",lat.toString());
                            Log.i("DBValue",lon.toString());
                            stationInBusStopID.add(busstopid);
                            matchBusStopName.add(busstopname);
                            latitude.add(lat);
                            longitude.add(lon);
                        }
                        catch (Exception e) {
                            Log.i("Skipped","No Output");
                        }
                    }*//*
                    // Get data from each node
                    String busstopid = dsp.getKey();
                    String busstopname = "";
                    Double lat = 0.0;
                    Double lon = 0.0;

                    if (dsp.child("busStopName").exists()) {
                        busstopname = dsp.child("busStopName").getValue().toString();
                    }

                    if (dsp.child("center_lat").exists() && dsp.child("center_long").exists()) {
                        lat = Double.parseDouble(dsp.child("center_lat").getValue().toString());
                        lon = Double.parseDouble(dsp.child("center_long").getValue().toString());
                    }

                    boolean test = stationInRoutesName.contains(busstopname);

                    if (test) {
                        try {
                            stationInBusStopID.add(busstopid);
                            matchBusStopName.add(busstopname);
                            latitude.add(lat);
                            longitude.add(lon);
                        } catch (Exception e) {
                            // handle the exception here
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Couldn't retrieve bus stops. Please refresh.", Toast.LENGTH_SHORT).show();
            }
        });*/
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
}
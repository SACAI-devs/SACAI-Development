package com.example.sacai.operator.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sacai.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

public class OperMapFrag extends Fragment implements OnMapReadyCallback {

//    CALL VARIABLES BASED ON GOOGLE MAPS DOCUMENTATION
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

//    REQUIRED EMPTY PUBLIC CONSTRUCTOR
    public OperMapFrag() {
    }

//    CREATE A MAP VIEW INSTANCE
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
//        Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_comm_map, container, false);
        return mView;
    }

//    SHOULD HAVE THE SAME MAP ID FROM THE XML FILE
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView = (MapView) mView.findViewById(R.id.operator_map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

//    CUSTOM CONFIGURATIONS ON MAPS
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

//        TESTING STUFF
        // TODO: RETRIEVE STATIONS FROM DATABASE AND MAP THEM TO MARKERS TO DISPLAY ON MAPS
    }
}
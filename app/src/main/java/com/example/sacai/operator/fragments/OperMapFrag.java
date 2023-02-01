package com.example.sacai.operator.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sacai.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class OperMapFrag extends Fragment implements OnMapReadyCallback {
    // Call variables based on GoogleMaps Documentation
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;

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

        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //=============Testing==============//
        // TODO: RETRIEVE STATIONS FROM DATABASE AND MAP THEM TO MARKERS TO DISPLAY ON MAPS
        googleMap.addMarker(new MarkerOptions().position(new LatLng(14.574970139259474, 121.09785961494917)).title("Rainforest Park"));
        CameraPosition rainforestPark = CameraPosition.builder().target(new LatLng(14.574970139259474, 121.09785961494917)).zoom(16).bearing(0).tilt(0).build();
        googleMap.moveCamera((CameraUpdateFactory.newCameraPosition(rainforestPark)));
    }
}
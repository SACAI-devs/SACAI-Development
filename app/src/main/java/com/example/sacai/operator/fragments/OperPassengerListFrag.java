package com.example.sacai.operator.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sacai.R;
import com.example.sacai.databinding.FragmentCommRideHistoryBinding;
import com.example.sacai.databinding.FragmentOperPassengerListBinding;

import java.util.ArrayList;

public class OperPassengerListFrag extends Fragment {

    // Bind fragment to layout
    FragmentOperPassengerListBinding binding;
    RecyclerView recyclerView;
    RecyclerView.Adapter passengerListAdapter;
    RecyclerView.LayoutManager layoutManager;



    public OperPassengerListFrag() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_oper_passenger_list, container, false);
    }
}
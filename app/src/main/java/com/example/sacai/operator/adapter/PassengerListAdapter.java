package com.example.sacai.operator.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sacai.dataclasses.Commuter_in_Geofence;

import java.util.ArrayList;

public class PassengerListAdapter extends RecyclerView.Adapter<PassengerListAdapter.ViewHolder> {
    private ArrayList<Commuter_in_Geofence> passenger;
    public PassengerListAdapter(Context context, ArrayList<Commuter_in_Geofence> list) {
        passenger = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public PassengerListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull PassengerListAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


}

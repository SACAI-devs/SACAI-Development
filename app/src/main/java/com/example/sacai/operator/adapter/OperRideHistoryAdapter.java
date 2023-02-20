package com.example.sacai.operator.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sacai.dataclasses.Operator_Trip;

import java.util.ArrayList;

public class OperRideHistoryAdapter extends RecyclerView.Adapter<OperRideHistoryAdapter.ViewHolder> {
    private ArrayList<Operator_Trip> operatorTrip;
    public OperRideHistoryAdapter (Context context, ArrayList<Operator_Trip> list) {
        operatorTrip = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public OperRideHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return 0;
    }
}

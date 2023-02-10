package com.example.sacai.commuter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sacai.R;
import com.example.sacai.dataclasses.Trip;

import java.util.ArrayList;

public class RideHistoryAdapter extends RecyclerView.Adapter<RideHistoryAdapter.ViewHolder> {
    private ArrayList<Trip> trip;
    public RideHistoryAdapter(Context context, ArrayList<Trip> list) {
        trip = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvPlate, tvDate, tvOrigin, tvDestination, tvTimeStart, tvTimeEnd, tvTripId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlate = itemView.findViewById(R.id.tvPlateNumber);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvOrigin = itemView.findViewById(R.id.tvOrigin);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvTimeStart = itemView.findViewById(R.id.tvTimeStart);
            tvTimeEnd = itemView.findViewById(R.id.tvTimeEnd);
            tvTripId = itemView.findViewById(R.id.tvTripId);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    @NonNull
    @Override
    public RideHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ride_history_list, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RideHistoryAdapter.ViewHolder viewHolder, int position) {
        viewHolder.itemView.setTag(trip.get(position));
        viewHolder.tvDate.setText(trip.get(position).getDate());
        viewHolder.tvOrigin.setText(trip.get(position).getPickup_station());
        viewHolder.tvDestination.setText(trip.get(position).getDropoff_station());
        viewHolder.tvTimeEnd.setText(trip.get(position).getTime_ended());
        viewHolder.tvTimeStart.setText(trip.get(position).getTime_started());
        viewHolder.tvTripId.setText(trip.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return trip.size();
    }
}

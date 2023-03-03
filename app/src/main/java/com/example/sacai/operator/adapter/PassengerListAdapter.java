package com.example.sacai.operator.adapter;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sacai.R;
import com.example.sacai.dataclasses.Commuter_in_Geofence;

import java.util.ArrayList;

public class PassengerListAdapter extends RecyclerView.Adapter<PassengerListAdapter.ViewHolder> {
    private ArrayList<Commuter_in_Geofence> passenger;
    public PassengerListAdapter(Context context, ArrayList<Commuter_in_Geofence> list) {
        passenger = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvOrigin, tvDestination, tvUid;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvOrigin = itemView.findViewById(R.id.tvSource);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvUid = itemView.findViewById(R.id.tvUserId);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("PassengerList ViewHolder", "onClick: item was clicked");
                }
            });
        }
    }

    @NonNull
    @Override
    public PassengerListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.passenger_list, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PassengerListAdapter.ViewHolder viewHolder, int position) {
        viewHolder.itemView.setTag(passenger.get(position));
        viewHolder.tvUsername.setText(passenger.get(position).getUsername());
        viewHolder.tvOrigin.setText(passenger.get(position).getDestination());
//        viewHolder.tvUid.setText(passenger.get(position).getUid());
    }

    @Override
    public int getItemCount() {
        return 0;
    }


}

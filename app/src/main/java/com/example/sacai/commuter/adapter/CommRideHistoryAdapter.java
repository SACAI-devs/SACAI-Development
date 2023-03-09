package com.example.sacai.commuter.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sacai.R;
import com.example.sacai.dataclasses.Commuter;
import com.example.sacai.dataclasses.Commuter_Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CommRideHistoryAdapter extends RecyclerView.Adapter<CommRideHistoryAdapter.ViewHolder> {
    private ArrayList<Commuter_Trip> commuterTrip;
    public CommRideHistoryAdapter(Context context, ArrayList<Commuter_Trip> list) {
        commuterTrip = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvOperatorId, tvDate, tvOrigin, tvDestination, tvTimeStart, tvTimeEnd, tvTripId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripId = itemView.findViewById(R.id.tvTripId);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvOrigin = itemView.findViewById(R.id.tvOrigin);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvTimeStart = itemView.findViewById(R.id.tvTimeStart);
            tvTimeEnd = itemView.findViewById(R.id.tvTimeEnd);
            tvOperatorId = itemView.findViewById(R.id.tvOperatorId);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("CommuterRideHistory ViewHolder", "onClick: item was clicked");

                }
            });
        }
    }

    @NonNull
    @Override
    public CommRideHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ride_history_list, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommRideHistoryAdapter.ViewHolder viewHolder, int position) {


        viewHolder.itemView.setTag(commuterTrip.get(position));
        viewHolder.tvTripId.setText(commuterTrip.get(position).getId());
        viewHolder.tvDate.setText(commuterTrip.get(position).getDate());
        viewHolder.tvOrigin.setText(commuterTrip.get(position).getOrigin_stop());
        viewHolder.tvDestination.setText(commuterTrip.get(position).getDestination_stop());
        viewHolder.tvTimeEnd.setText(commuterTrip.get(position).getTime_ended());
        viewHolder.tvTimeStart.setText(commuterTrip.get(position).getTime_started());
        viewHolder.tvOperatorId.setText(commuterTrip.get(position).getOperator_id());

    }

    @Override
    public int getItemCount() {
        return commuterTrip.size();
    }
}

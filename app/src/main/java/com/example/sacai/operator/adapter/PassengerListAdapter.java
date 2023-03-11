package com.example.sacai.operator.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.Visibility;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sacai.R;
import com.example.sacai.dataclasses.Passenger_List;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PassengerListAdapter extends RecyclerView.Adapter<PassengerListAdapter.ViewHolder> {
    private ArrayList<Passenger_List> passenger;
    AlertDialog.Builder builder;

    public PassengerListAdapter(Context context, ArrayList<Passenger_List> list) {
        passenger = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvOrigin, tvDestination, tvUid, tvParaStatus;
        ImageView profPic, pwd_id;
        ConstraintLayout pwd_id_view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            builder = new AlertDialog.Builder (itemView.getContext());
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvUid = itemView.findViewById(R.id.tvUserId);
            tvParaStatus = itemView.findViewById(R.id.tvParaStatus);
            tvOrigin = itemView.findViewById(R.id.tvSource);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            profPic = itemView.findViewById(R.id.profPic);
            pwd_id_view = itemView.findViewById(R.id.pwd_id_view);
            pwd_id = itemView.findViewById(R.id.pwd_id);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("PassengerList ViewHolder", "onClick: item was clicked");
                    Passenger_List item = passenger.get(getAdapterPosition());
                    item.setVisibility(!item.isVisibility());
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }
    }

    private void loadProfPic(ImageView profPic, int position) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference profilePicture = storageRef.child("Profile-Picture_Uploads/" + passenger.get(position).getId() + "/");
        try {
            File localfile2 = File.createTempFile("tempfile", ".jpg");
            profilePicture.getFile(localfile2)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap2 = BitmapFactory.decodeFile(localfile2.getAbsolutePath());
                            profPic.setImageBitmap(bitmap2);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("IMAGE_PWD", "onFailure: Image retrieve FAILED...");
                            Log.e("IMAGE_PWD", "onFailure: exception ", e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadID(ImageView id, int position) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference pwdID = storageRef.child("ID_Uploads/" + passenger.get(position) + "/");
        Log.i("loadID", "loadID: storage ref " + pwdID);
        try {
            File localfile = File.createTempFile("tempfile", ".jpg");
            pwdID.getFile(localfile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                            id.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("IMAGE_PWD_ID", "onFailure: Image retrieve FAILED...");
                            Log.e("IMAGE_PWD_ID", "onFailure: exception ", e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
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

        loadProfPic(viewHolder.profPic, position);
        loadID(viewHolder.pwd_id, position);
        viewHolder.itemView.setTag(passenger.get(position));
        viewHolder.tvUsername.setText(passenger.get(position).getUsername());
        viewHolder.tvParaStatus.setText(passenger.get(position).getPara_status());
        viewHolder.tvOrigin.setText(passenger.get(position).getOrigin());
        viewHolder.tvDestination.setText(passenger.get(position).getDestination());
        viewHolder.tvUid.setText(passenger.get(position).getId());

        Passenger_List item = passenger.get(position);
        boolean isVisible = item.isVisibility();
        viewHolder.pwd_id_view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return passenger.size();
    }


}

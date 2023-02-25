package com.example.sacai.operator.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sacai.R;
import com.example.sacai.databinding.FragmentOperQrcodeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class OperQRCodeWithSaveFrag extends Fragment {

    String acquiredUID;
    Bitmap bitmapUID;
    Bitmap bitmapUIDToSave;
    ImageView imageView;
    Button saveToDeviceBtn;

    //Image size
    int width = 1000;
    int length = 1000;

    View mView;
    FragmentOperQrcodeBinding binding;

    public OperQRCodeWithSaveFrag() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_oper_qrcode, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Read data and display
        acquiredUID = currentUser.getUid();

        imageView = mView.findViewById(R.id.imageView_QR);

        try {
            bitmapUID = encodeAsBitmap(acquiredUID);
            imageView.setImageBitmap(bitmapUID);
        } catch (WriterException ex) {
            ex.printStackTrace();
        }

        saveToDeviceBtn = mView.findViewById(R.id.btnSaveQR);

        saveToDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    saveQRCodeToDevice();
            }
        });
    }

    Bitmap encodeAsBitmap(@NonNull String str) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, width, length);

        int w = bitMatrix.getWidth();
        int h = bitMatrix.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pixels[y * w + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

    public void saveQRCodeToDevice() {

        BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
        bitmapUIDToSave = draw.getBitmap();

        FileOutputStream outStream = null;
        File sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File dir = new File(sdCard.getAbsolutePath() + "/SACAI");
        dir.mkdirs();
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);

        Log.i("File", dir.toString());

        try {
            outStream = new FileOutputStream(outFile);
            bitmapUIDToSave.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();

            Toast.makeText(getActivity(), "Saved to Gallery", Toast.LENGTH_SHORT).show();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
       catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Storage Permissions was deleted

}
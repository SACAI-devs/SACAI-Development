package com.example.sacai.operator.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.sacai.R;
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
    private int EXTERNAL_STORAGE_PERMISSION_CODE = 23;

    File acquiredDirectory = null;

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
            //saveToDeviceBtn.setEnabled(true);

        } catch (WriterException ex) {
            ex.printStackTrace();
        }

        saveToDeviceBtn = mView.findViewById(R.id.btnSaveQR);

        saveToDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //saveQRCodeToDevice();

                if (Build.VERSION.SDK_INT >= 23)
                {   Log.e("Test", "Permission Granted: " + PackageManager.PERMISSION_GRANTED);
                    Log.e("Test", "CheckSelfPermission: " + ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE));
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
                    }
                    else {
                        acquireDirectory();
                    }
                }
                else
                {
                    acquireDirectory();
                    Log.e("Test", "No need to Request Permission");
                }
            }
        });
    }

    private void acquireDirectory() {
        try {
            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
                saveToDeviceBtn.setEnabled(false);
                Log.e("File", "Read-Only");
                Toast.makeText(getActivity(), "Storage is Read-Only. Please check permissions.", Toast.LENGTH_SHORT).show();
            } else {
                acquiredDirectory = checkVersionPermissions("SACAI");
                if (acquiredDirectory != null){
                    saveQRCodeToDevice();
                    Log.e("File", "Acquired Directory:" + acquiredDirectory);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static File checkVersionPermissions(String FolderName) {
        File dir = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + FolderName);
            Log.e("File", ">=R File:" + dir);
        }
        else
        {
            try {
                //dir = new File(OperQRCodeWithSaveFrag.getAppContext().getFilesDir().getPath() + "/" + FolderName);
                dir = new File(Environment.getExternalStorageDirectory() + "/Pictures/" + FolderName);
                Log.e("MountedMedia", Environment.getExternalStorageDirectory().toString());
                Log.e("MountedMedia", Environment.MEDIA_MOUNTED);
                Log.e("File", "<R File:" + dir);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Make sure the path directory exists.
        if (!dir.exists())
        {
            // Make it, if it doesn't exit
            boolean success = dir.mkdirs();
            if (!success)
            {
                dir = null;
                Log.e("File", "Directory not created");
            }
        }
        return dir;
    }

    private void saveQRCodeToDevice() {
        BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
        bitmapUIDToSave = draw.getBitmap();

        FileOutputStream outStream = null;

        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(acquiredDirectory, fileName);


        Log.e("File", acquiredDirectory.toString());
        try {
            outStream = new FileOutputStream(outFile);
            bitmapUIDToSave.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();

            Log.e("File", "Saved to Gallery");
            Toast.makeText(getActivity(), "Saved to Gallery", Toast.LENGTH_SHORT).show();
            closeFragment();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeFragment() {
        //TODO: Close the Fragment Once File has been saved
        //PRIORITY: LOW
        getParentFragmentManager().popBackStackImmediate();
    }
}
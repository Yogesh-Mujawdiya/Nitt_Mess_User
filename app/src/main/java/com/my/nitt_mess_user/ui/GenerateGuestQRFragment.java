package com.my.nitt_mess_user.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.my.nitt_mess_user.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class GenerateGuestQRFragment extends Fragment {

    TextInputEditText GuestName;
    AppCompatButton GenerateQR, DownloadQR;
    TextView textViewGuestName;
    ImageView ImageQRCode;
    LinearLayout layoutQR;
    String MessId;
    FirebaseUser FUser;
    GregorianCalendar calendar;
    int Year, Month, Day;
    DatabaseReference reference;
    String RollNo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_generate_guest_q_r, container, false);
        FUser = FirebaseAuth.getInstance().getCurrentUser();
        RollNo = FUser.getEmail().split("@")[0];
        GenerateQR = root.findViewById(R.id.generateQR);
        GuestName = root.findViewById(R.id.GuestName);
        textViewGuestName = root.findViewById(R.id.textViewGuestName);
        DownloadQR = root.findViewById(R.id.downloadQR);
        ImageQRCode = root.findViewById(R.id.imageViewQRCode);
        layoutQR = root.findViewById(R.id.layoutQR);
        calendar = new GregorianCalendar();
        Year = calendar.get(Calendar.YEAR);
        Month = calendar.get(Calendar.MONTH);
        Day = calendar.get(Calendar.DAY_OF_MONTH);
        getUserData();



        GenerateQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(GuestName.getText().toString().equals(""))
                    GuestName.setError("Invalid Name");
                else{
                    textViewGuestName.setText("");
                    ImageQRCode.setImageResource(0);
                    DownloadQR.setVisibility(View.GONE);
                    generateQrCode(QrCodeData(MessId));
                }
            }
        });


        DownloadQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Permission ask
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
                } else {
                    DownloadQR();
                }
            }
        });

        return root;
    }
    private void getUserData() {
        RollNo = FUser.getEmail().split("@")[0];
        String Key = Year+"/"+Month;
        String key = Year+"/"+Month+"/"+Day;
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Data/User").child(RollNo).child("AllocateMess/"+Key);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    MessId = dataSnapshot.getValue().toString();
                    reference = FirebaseDatabase.getInstance().getReference("Data/GuestQR").child(MessId).child(key).push();
                }
                else {
                    Toast.makeText(getContext(),"No Mess Allocated", Toast.LENGTH_LONG);
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void DownloadQR() {
        File file = saveBitMap(getContext(), layoutQR);    //which view you want to pass that view as parameter
        if (file != null) {
            Log.i("TAG", "Drawing saved to the gallery!");
            Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file);
            String mime = getContext().getContentResolver().getType(uri);
            // Open file with user selected app
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(),"Try Again! QR could not be saved.", Toast.LENGTH_LONG).show();
            Log.i("TAG", "Oops! Image could not be saved.");
        }
    }

    private File saveBitMap(Context context, View drawView) {
        Environment.getExternalStorageState();
        File pictureFileDir = new File(Environment.getExternalStorageDirectory(), "NITT");
        if (!pictureFileDir.exists()) {
            boolean isDirectoryCreated = pictureFileDir.mkdirs();
            if (!isDirectoryCreated)
                Log.i("ATG", "Can't create directory to save the image");
            return null;
        }
        String filename = pictureFileDir.getPath() + "/" + textViewGuestName.getText() +Day+"-"+Month+"-"+Year+ "_QR.jpg";
        File pictureFile = new File(filename);
        Bitmap bitmap = getBitmapFromView(drawView);
        try {
            pictureFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
            oStream.flush();
            oStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TAG", "There was an issue saving the image.");
        }
        scanGallery(context, pictureFile.getAbsolutePath());
        return pictureFile;
    }

    //create bitmap from view and returns it
    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    // used for scanning gallery
    private void scanGallery(Context cntx, String path) {
        try {
            MediaScannerConnection.scanFile(cntx, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 111) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                DownloadQR();
            }
        }
    }



    private String QrCodeData(String MessId){
        String Data = MessId+"\n"+reference.getKey();
        String EncryptData = "";
        for(int i=0;i<Data.length();i++){
            int c = Data.charAt(i);
            EncryptData += (char)(c^4);
        }
        return EncryptData;
    }

    private void generateQrCode(String data) {
        try {
            Bitmap bitmap = TextToImageEncode(data);
            ImageQRCode.setImageBitmap(bitmap);
            DownloadQR.setVisibility(View.VISIBLE);
            textViewGuestName.setText(GuestName.getText().toString());
            reference.child("GenerateBy").setValue(RollNo);
            reference.child("Name").setValue(GuestName.getText().toString());

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    500, 500, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.QRCodeBlackColor):getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }


}
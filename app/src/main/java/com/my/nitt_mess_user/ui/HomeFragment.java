package com.my.nitt_mess_user.ui;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.my.nitt_mess_user.Adapter.MessListAdapter;
import com.my.nitt_mess_user.Class.LastTakenFood;
import com.my.nitt_mess_user.Class.Mess;
import com.my.nitt_mess_user.Class.MessMenu;
import com.my.nitt_mess_user.HomeActivity;
import com.my.nitt_mess_user.InvalidUserActivity;
import com.my.nitt_mess_user.LoginActivity;
import com.my.nitt_mess_user.R;
import com.my.nitt_mess_user.RegisterActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    LastTakenFood lastTakenFood;
    RecyclerView recyclerView;
    MessListAdapter recyclerAdapter;
    TextView AllocatedMess, FoodType, TodayMenu, textViewData;
    boolean IsMessAllocate = false;
    List<Mess> messList;
    Button submit;
    FirebaseUser FUser;
    Dictionary AllMess;
    ImageView ImageQRCode;
    String weekDay;
    int Year, Month, Day, Hours, Minutes;
    Calendar calendar;
    ProgressDialog progressDialog;
    Mess MyMess;
    Uri FilePath;
    RelativeLayout layoutAllocatedMess, layoutNotAllocatedMess, layoutFee;
    private boolean IsOnLeave;
    private String Gender;
    AppCompatButton UploadFee, SelectFile;
    TextView textView, selectedFile;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://nitt-mess.appspot.com/UserFee");


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        AllMess = new Hashtable();
        FUser = FirebaseAuth.getInstance().getCurrentUser();
        layoutAllocatedMess = root.findViewById(R.id.layoutAllocatedMess);
        layoutNotAllocatedMess = root.findViewById(R.id.layoutNotAllocatedMess);
        layoutFee = root.findViewById(R.id.layoutNotUploadFee);
        textViewData = root.findViewById(R.id.textViewData);
        recyclerView = root.findViewById(R.id.recyclerViewMessList);
        AllocatedMess = root.findViewById(R.id.AllocatedMess);
        FoodType = root.findViewById(R.id.textViewFoodType);
        TodayMenu = root.findViewById(R.id.textViewMenu);
        submit = root.findViewById(R.id.SubmitButton);
        ImageQRCode = root.findViewById(R.id.ImageQRCode);
        UploadFee = root.findViewById(R.id.UploadFee);
        SelectFile = root.findViewById(R.id.SelectFile);
        textView = root.findViewById(R.id.textView);
        selectedFile = root.findViewById(R.id.selectedFile);

        calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        Year = calendar.get(Calendar.YEAR);
        Month = calendar.get(Calendar.MONTH);
        Day = calendar.get(Calendar.DAY_OF_MONTH);
        Hours = calendar.get(Calendar.HOUR_OF_DAY);
        Minutes = calendar.get(Calendar.MINUTE);

        weekDay = "";
        if (Calendar.MONDAY == dayOfWeek) weekDay = "Monday";
        else if (Calendar.TUESDAY == dayOfWeek) weekDay = "Tuesday";
        else if (Calendar.WEDNESDAY == dayOfWeek) weekDay = "Wednesday";
        else if (Calendar.THURSDAY == dayOfWeek) weekDay = "Thursday";
        else if (Calendar.FRIDAY == dayOfWeek) weekDay = "Friday";
        else if (Calendar.SATURDAY == dayOfWeek) weekDay = "Saturday";
        else if (Calendar.SUNDAY == dayOfWeek) weekDay = "Sunday";
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitMessInfo();
            }
        });

        SelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent,1212 );
            }
        });

        UploadFee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FilePath==null){
                    Toast.makeText(getContext(), "Please select File", Toast.LENGTH_LONG).show();
                }
                else{
                    uploadFile();
                }
            }
        });

        getFeeApprove();
        return root;
    }

    private void uploadFile() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.show();
        String RollNo = FUser.getEmail().split("@")[0];
        int Y = Integer.parseInt("20"+RollNo.substring(4,6));
        Log.d("KKKKKKKKKKKKKKKKKKKKKKK",Y+"");
        Log.d("KKKKKKKKKKKKKKKKKKKKKKK",calendar.get(Calendar.YEAR)+"");
        String sam="0";
        if(calendar.get(Calendar.YEAR)-Y == 0)
            sam="1";
        else if(calendar.get(Calendar.YEAR)-Y == 1){
            if(calendar.get(Calendar.MONTH)<6)
                sam = "2";
            else
                sam = "3";
        }
        else if(calendar.get(Calendar.YEAR)-Y == 2){
            if(calendar.get(Calendar.MONTH)<6)
                sam = "4";
            else
                sam = "5";
        }
        else if(calendar.get(Calendar.YEAR)-Y == 3){
            if(calendar.get(Calendar.MONTH)<6)
                sam = "6";
            else
                sam = "7";
        }else if(calendar.get(Calendar.YEAR)-Y == 4) {
            if (calendar.get(Calendar.MONTH) < 6)
                sam = "8";
        }
        String Semester = sam;
        StorageReference riversRef = storageRef.child(RollNo+"_"+sam+"_Semester"+".pdf");
        riversRef.putFile(FilePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        FirebaseDatabase.getInstance().getReference("Data/User/").child(RollNo).child("Fee").child(Semester).child("File").setValue(RollNo+"_"+Semester+"_Semester"+".pdf");
                        FirebaseDatabase.getInstance().getReference("Data/User/").child(RollNo).child("Fee").child(Semester).child("Status").setValue("Pending");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploading File" + ((int) progress) + "%...");
                    }
                });
    }

    private void getFeeApprove() {
        String RollNo = FUser.getEmail().split("@")[0];
        int Y = Integer.parseInt("20"+RollNo.substring(4,6));
        String sam="0";
        if(calendar.get(Calendar.YEAR)-Y == 0)
            sam="1";
        else if(calendar.get(Calendar.YEAR)-Y == 1){
            if(calendar.get(Calendar.MONTH)<6)
                sam = "2";
            else
                sam = "3";
        }
        else if(calendar.get(Calendar.YEAR)-Y == 2){
            if(calendar.get(Calendar.MONTH)<6)
                sam = "4";
            else
                sam = "5";
        }
        else if(calendar.get(Calendar.YEAR)-Y == 3){
            if(calendar.get(Calendar.MONTH)<6)
                sam = "6";
            else
                sam = "7";
        }else if(calendar.get(Calendar.YEAR)-Y == 4) {
            if (calendar.get(Calendar.MONTH) < 6)
                sam = "8";
        }
        if(sam!="0") {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Data/User").child(RollNo).child("Fee").child(sam);
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    layoutFee.setVisibility(View.VISIBLE);
                    if (dataSnapshot.exists()) {
                        Log.d("KKKKKKKKKK",dataSnapshot.getValue().toString());
                        if(dataSnapshot.hasChild("Status")) {
                            if (dataSnapshot.child("Status").getValue(String.class).equals("Accept")) {
                                getUserGender();
                                layoutFee.setVisibility(View.GONE);
                            } else if (dataSnapshot.child("Status").getValue(String.class).equals("Pending")) {
                                textView.setText("File Uploaded But Not Approve Yet");
                                UploadFee.setVisibility(View.GONE);
                                selectedFile.setVisibility(View.GONE);
                                SelectFile.setVisibility(View.GONE);
                            } else {
                                textView.setText("File Not Approve");
                                UploadFee.setVisibility(View.VISIBLE);
                                selectedFile.setVisibility(View.VISIBLE);
                                SelectFile.setVisibility(View.VISIBLE);
                                UploadFee.setText("ReUpload File");
                            }
                        }
                    } else {
                        UploadFee.setVisibility(View.VISIBLE);
                        selectedFile.setVisibility(View.VISIBLE);
                        SelectFile.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                        textView.setText("Need to Upload Fee Receipt");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void submitMessInfo() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Data/Mess");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (int i = 0; i < messList.size(); i++) {
                        Mess mess = messList.get(i);
                        if (!IsMessAllocate) {
                            int Count = Integer.parseInt(dataSnapshot.child(messList.get(i).getId()).child("Total").getValue().toString());
                            int Allocate = Integer.parseInt(dataSnapshot.child(messList.get(i).getId()).child("Allocate").getValue().toString());
                            if (Count > Allocate) {
                                IsMessAllocate = true;
                                String Key = Year + "/" + Month;
                                String RollNo = FUser.getEmail().split("@")[0];
                                FirebaseDatabase.getInstance().getReference("Data/Mess").child(mess.getId()).child("Allocate").setValue(Allocate + 1);
                                FirebaseDatabase.getInstance().getReference("Data/AllocatedMessHistory").child(mess.getId()).child(Key).child(RollNo).setValue(FUser.getDisplayName());
                                FirebaseDatabase.getInstance().getReference("Data/User").child(RollNo).child("AllocateMess").child(Key).setValue(mess.getId());
                                layoutAllocatedMess.setVisibility(View.VISIBLE);
                                layoutNotAllocatedMess.setVisibility(View.GONE);
                                AllocatedMess.setText(mess.getName());
                                MyMess = mess;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUserGender() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        String RollNo = FUser.getEmail().split("@")[0];
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("AllUser").child(RollNo).child("Gender");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Gender = dataSnapshot.getValue(String.class);
                    getUserData();
                } else {
                    startActivity(new Intent(getContext(), InvalidUserActivity.class));
                    getActivity().finish();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void getUserData() {
        String RollNo = FUser.getEmail().split("@")[0];
        String Key = Year + "/" + Month;
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Data/User").child(RollNo).child("AllocateMess/" + Key);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String MessId = dataSnapshot.getValue().toString();
                    IsMessAllocate = true;
                    layoutAllocatedMess.setVisibility(View.VISIBLE);
                    layoutNotAllocatedMess.setVisibility(View.GONE);
                    getMessData(MessId);
                } else {
                    getAllMessData();
                    layoutAllocatedMess.setVisibility(View.GONE);
                    layoutNotAllocatedMess.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void getAllMessData() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Data/Mess");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    messList = new ArrayList<Mess>();
                    for (final DataSnapshot mess : dataSnapshot.getChildren()) {
                        if (mess.child("Type").getValue().equals(Gender) || mess.child("Type").getValue().equals("Unisex")) {
                            Dictionary Menu = new Hashtable();
                            for (final DataSnapshot menu : mess.child("Menu").getChildren()) {
                                Menu.put(menu.getKey(), new MessMenu(
                                        menu.getKey(),
                                        menu.child("Breakfast").getValue().toString(),
                                        menu.child("Lunch").getValue().toString(),
                                        menu.child("Dinner").getValue().toString(),
                                        menu.child("Snack").getValue().toString()
                                ));
                            }
                            Mess M = new Mess(
                                    mess.getKey(),
                                    mess.child("Name").getValue().toString(),
                                    Integer.parseInt(mess.child("Total").getValue().toString()),
                                    Integer.parseInt(mess.child("Allocate").getValue().toString()),
                                    Menu);
                            AllMess.put(M.getId(), M);
                            if (!M.isFull())
                                messList.add(M);
                        }
                    }
                    if (MyMess != null) {
                        MessMenu menu = (MessMenu) MyMess.getMenu().get(weekDay);
                        if (menu != null)
                            TodayMenu.setText(menu.getDinner());
                    }
                }
                recyclerAdapter = new MessListAdapter(messList, getContext());
                recyclerView.setAdapter(recyclerAdapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void getMessData(final String messId) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Data/Mess").child(messId);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot mess) {
                if (mess.exists()) {
                    Dictionary Menu = new Hashtable();
                    for (final DataSnapshot menu : mess.child("Menu").getChildren()) {
                        Menu.put(menu.getKey(), new MessMenu(
                                menu.getKey(),
                                menu.child("Breakfast").getValue().toString(),
                                menu.child("Lunch").getValue().toString(),
                                menu.child("Dinner").getValue().toString(),
                                menu.child("Snack").getValue().toString()
                        ));
                    }
                    MyMess = new Mess(
                            mess.getKey(),
                            mess.child("Name").getValue().toString(),
                            Integer.parseInt(mess.child("Total").getValue().toString()),
                            Integer.parseInt(mess.child("Allocate").getValue().toString()),
                            Menu);
                    AllocatedMess.setText(MyMess.getName());
                    isOnLeave(MyMess.getId());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void isOnLeave(String MessId) {
        GregorianCalendar today = new GregorianCalendar();
        int Year = today.get(Calendar.YEAR);
        int Month = today.get(Calendar.MONTH);
        int Day = today.get(Calendar.DAY_OF_MONTH);
        String Key = Year + "/" + Month + "/" + Day;
        String RollNo = FUser.getEmail().split("@")[0];
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Data/StudentOnLeave").child(MessId).child(Key).child(RollNo);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    IsOnLeave = true;
                    ImageQRCode.setVisibility(View.INVISIBLE);
                    textViewData.setText("On Leave");
                    progressDialog.dismiss();
                } else {
                    getLastTakenFood(MyMess.getId());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void getLastTakenFood(final String MessId) {
        String RollNo = FUser.getEmail().split("@")[0];
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Data/User").child(RollNo).child("LastTakenFood");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    lastTakenFood = new LastTakenFood(
                            dataSnapshot.child("type").getValue(String.class),
                            dataSnapshot.child("date").getValue(String.class),
                            dataSnapshot.child("time").getValue(String.class),
                            dataSnapshot.child("feedback").getValue(Boolean.class)
                    );
                    if (lastTakenFood.isFeedBack()) {
                        MessMenu menu = (MessMenu) MyMess.getMenu().get(weekDay);
                        if (Hours < 11) {
                            if (lastTakenFood.isBreakfast() && lastTakenFood.isToday()) {
                                ImageQRCode.setVisibility(View.INVISIBLE);
                                textViewData.setText("Done at Time : " + lastTakenFood.getTime());
                            }
                            FoodType.setText("Breakfast");
                            TodayMenu.setText(menu.getBreakfast());
                        } else if (Hours < 15) {
                            if (lastTakenFood.isLunch() && lastTakenFood.isToday()) {
                                ImageQRCode.setVisibility(View.INVISIBLE);
                                textViewData.setText("Done at Time : " + lastTakenFood.getTime());
                            }
                            FoodType.setText("Lunch");
                            TodayMenu.setText(menu.getLunch());
                        } else if (Hours < 18) {
                            if (lastTakenFood.isSnack() && lastTakenFood.isToday()) {
                                ImageQRCode.setVisibility(View.INVISIBLE);
                                textViewData.setText("Done at Time : " + lastTakenFood.getTime());
                            }
                            FoodType.setText("Snack");
                            TodayMenu.setText(menu.getSnack());
                        } else {
                            if (lastTakenFood.isDinner() && lastTakenFood.isToday()) {
                                ImageQRCode.setVisibility(View.INVISIBLE);
                                textViewData.setText("Done at Time : " + lastTakenFood.getTime());
                            }
                            FoodType.setText("Dinner");
                            TodayMenu.setText(menu.getDinner());
                        }
                        generateQrCode(QrCodeData(MessId));
                        progressDialog.dismiss();
                    } else
                        textViewData.setText("Please Complete Your Previous Food Feedback");
                } else {
                    MessMenu menu = (MessMenu) MyMess.getMenu().get(weekDay);
                    if (Hours < 11) {
                        FoodType.setText("Breakfast");
                        TodayMenu.setText(menu.getBreakfast());
                    } else if (Hours < 15) {
                        FoodType.setText("Lunch");
                        TodayMenu.setText(menu.getLunch());
                    } else if (Hours < 18) {
                        FoodType.setText("Snack");
                        TodayMenu.setText(menu.getSnack());
                    } else {
                        FoodType.setText("Dinner");
                        TodayMenu.setText(menu.getDinner());
                    }
                    progressDialog.dismiss();
                    generateQrCode(QrCodeData(MessId));
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private String QrCodeData(String MessId) {
        String Food;
        if (Hours < 11) {
            Food = "Breakfast";
        } else if (Hours < 15) {
            Food = "Lunch";
        } else if (Hours < 18) {
            Food = "Snack";
        } else {
            Food = "Dinner";
        }
        String RollNo = FUser.getEmail().split("@")[0];
        String Data = MessId + "\n" + Food + "\n" + RollNo + "\n" + FUser.getDisplayName();
        String EncryptData = "";
        for (int i = 0; i < Data.length(); i++) {
            int c = Data.charAt(i);
            EncryptData += (char) (c ^ 4);
        }
        return EncryptData;
    }

    private void generateQrCode(String data) {
        try {
            Bitmap bitmap1 = TextToImageEncode(data);
            ImageQRCode.setImageBitmap(bitmap1);
            //to get the image from the ImageView (say iv)
            BitmapDrawable draw = (BitmapDrawable) ImageQRCode.getDrawable();
            Bitmap bitmap = draw.getBitmap();

            Environment.getExternalStorageState();
            File pictureFileDir = new File(Environment.getExternalStorageDirectory(), "NITT");
            if (!pictureFileDir.exists()) {
                boolean isDirectoryCreated = pictureFileDir.mkdirs();
                if (!isDirectoryCreated)
                    return;
            }
            String filename = pictureFileDir.getPath() + "/" + "Current_QR_Code.jpg";
            File filePath = new File(filename);
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (WriterException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
                        getResources().getColor(R.color.QRCodeBlackColor) : getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            Collections.swap(messList, fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 1212:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    FilePath = data.getData();
                    Log.d("DDDDDDDd",FilePath.toString());
                    String uriString = FilePath.toString();
                    File myFile = new File(uriString);
                    String path = myFile.getAbsolutePath();
                    String displayName = null;

                    if (uriString.startsWith("content://")) {
                        Cursor cursor = null;
                        try {
                            cursor = getActivity().getContentResolver().query(FilePath, null, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            }
                        } finally {
                            cursor.close();
                        }
                    } else if (uriString.startsWith("file://")) {
                        displayName = myFile.getName();
                    }
                    selectedFile.setText(displayName);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
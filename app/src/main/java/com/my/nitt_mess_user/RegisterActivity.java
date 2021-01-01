package com.my.nitt_mess_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
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

public class RegisterActivity extends AppCompatActivity {
    TextInputLayout Email, Password;
    int SELECT_PICTURE = 500;
    private Button BtnRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private ImageView ImageUserProfile;
    private Uri FilePath = null;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://nitt-mess.appspot.com/User/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // taking FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        getSupportActionBar().setTitle("Register");
        Email = findViewById(R.id.editTextEmail);
        Password = findViewById(R.id.editTextPassword);
        BtnRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressbar);
        ImageUserProfile = findViewById(R.id.UserProfile);

        // Set on Click Listener on Registration button
        BtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                registerNewUser();
            }
        });

        ImageUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Profile Picture"),SELECT_PICTURE );
            }
        });
    }
    private void registerNewUser()
    {

        // show the visibility of progress bar to show loading
        progressBar.setVisibility(View.VISIBLE);

        // Take the value of two edit texts in Strings
        final String name, rollNo, mobile, email, password;
        email = Email.getEditText().getText().toString().trim();
        password = Password.getEditText().getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(),
                    "Please enter email!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(),
                    "Please enter password!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();
        // create new user or register new user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),
                                    "Registration successful!",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // hide the progress bar
                            progressBar.setVisibility(View.GONE);

                            // if the user created intent to login activity
                            final FirebaseUser User = FirebaseAuth.getInstance().getCurrentUser();
                            if(User!=null) {
                                StorageReference riversRef = storageRef.child(User.getUid());
                                riversRef.putFile(FilePath)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                progressDialog.dismiss();
                                                if(User.isEmailVerified()) {
                                                    Intent intent
                                                            = new Intent(RegisterActivity.this,
                                                            HomeActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                else {
                                                    String RollNo = User.getEmail().split("@")[0];
                                                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("User").child(RollNo).child("Student Name");
                                                    mDatabase.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.exists()) {
                                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                                        .setDisplayName(dataSnapshot.getValue(String.class)).build();
                                                                User.updateProfile(profileUpdates);
                                                            }
                                                        }
                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                            progressDialog.dismiss();
                                                        }
                                                    });
                                                    User.sendEmailVerification();
                                                    FirebaseAuth.getInstance().signOut();
                                                    Toast.makeText(getApplicationContext(),
                                                            "Verification Link is Sent on your Email Id",
                                                            Toast.LENGTH_LONG)
                                                            .show();
                                                    Intent intent
                                                            = new Intent(RegisterActivity.this,
                                                            LoginActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                progressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                                progressDialog.setMessage("Uploading Profile Pic" + ((int) progress) + "%...");
                                            }
                                        });
                            }
                        }
                        else {
                            Log.d("Error",task.getException().toString());
                            // Registration failed
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Registration failed!!"
                                            + " Please try again later",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // hide the progress bar
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode == 1){
            Bundle extras = data.getExtras();
            Bitmap thePic = (Bitmap) extras.get("data");
            ImageUserProfile.setImageBitmap(thePic);
        }
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            performCrop(uri);
        }
    }

    private void performCrop(Uri uri){

        // Image Crop Code
        try {
            Intent CropIntent = new Intent("com.android.camera.action.CROP");

            CropIntent.setDataAndType(uri, "image/*");

            CropIntent.putExtra("crop", "true");
            CropIntent.putExtra("outputX", 180);
            CropIntent.putExtra("outputY", 180);
            CropIntent.putExtra("aspectX", 3);
            CropIntent.putExtra("aspectY", 4);
            CropIntent.putExtra("scaleUpIfNeeded", true);
            CropIntent.putExtra("return-data", true);

            startActivityForResult(CropIntent, 1);

        } catch (ActivityNotFoundException e) {

        }
    }
}
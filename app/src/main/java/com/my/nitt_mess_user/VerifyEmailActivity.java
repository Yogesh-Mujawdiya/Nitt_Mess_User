package com.my.nitt_mess_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class VerifyEmailActivity extends AppCompatActivity {

    TextInputLayout Email, Password;
    Button button;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);
        mAuth = FirebaseAuth.getInstance();

        // initialising all views through id defined above
        Email = findViewById(R.id.editTextEmail);
        Password = findViewById(R.id.editTextPassword);
        button = findViewById(R.id.buttonVerifyLink);

        getSupportActionBar().setTitle("Verify Email");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationLink();
            }
        });

    }

    private void sendVerificationLink(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        String email = Email.getEditText().getText().toString();
        String password = Password.getEditText().getText().toString();
        // validations for input email and password
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email!!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password!!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override public void onComplete(
                                    @NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                        Toast.makeText(getApplicationContext(), "Email Already Verified", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                                        Toast.makeText(getApplicationContext(), "Verification Link Sent!!", Toast.LENGTH_LONG).show();
                                    }
                                    FirebaseAuth.getInstance().signOut();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Invalid User Info!!", Toast.LENGTH_LONG).show();
                                }
                                progressDialog.dismiss();
                            }
                        });
    }
}
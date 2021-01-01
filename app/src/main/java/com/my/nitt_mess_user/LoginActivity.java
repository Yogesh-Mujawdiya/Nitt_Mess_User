package com.my.nitt_mess_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    TextInputLayout Email, Password;
    private Button Btn;
    private ProgressBar progressBar;
    private TextView BtnRegister, BtnResendVerification, BtnForgetPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // taking instance of FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        getSupportActionBar().setTitle("Login");
        // initialising all views through id defined above
        Email = findViewById(R.id.editTextEmail);
        Password = findViewById(R.id.editTextPassword);
        Btn = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.loading);
        BtnRegister = findViewById(R.id.buttonRegister);
        BtnResendVerification = findViewById(R.id.buttonVerifyLink);
        BtnForgetPassword = findViewById(R.id.buttonForgetPassword);

        // Set on Click Listener on Sign-in button
        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserAccount();
            }
        });

        BtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent
                        = new Intent(LoginActivity.this,
                        RegisterActivity.class);
                startActivity(intent);
            }
        });
        BtnResendVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent
                        = new Intent(LoginActivity.this,
                        VerifyEmailActivity.class);
                startActivity(intent);
            }
        });
        BtnForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent
                        = new Intent(LoginActivity.this,
                        ForgetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUserAccount() {

        // show the visibility of progress bar to show loading
        progressBar.setVisibility(View.VISIBLE);

        // Take the value of two edit texts in Strings
        String email, password;
        email = Email.getEditText().getText().toString();
        password = Password.getEditText().getText().toString();

        // validations for input email and password
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

        // signin existing user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(
                                    @NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    final FirebaseUser User = FirebaseAuth.getInstance().getCurrentUser();
                                    if (User.isEmailVerified()) {

                                        final String UserId = User.getEmail().split("@")[0];
                                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("AllUser").child(UserId);
                                        mDatabase.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    Intent intent
                                                            = new Intent(LoginActivity.this,
                                                            HomeActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                    Toast.makeText(getApplicationContext(),
                                                            "Login successful!!",
                                                            Toast.LENGTH_LONG)
                                                            .show();
                                                } else {
                                                    Intent intent
                                                            = new Intent(LoginActivity.this,
                                                            InvalidUserActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                    Toast.makeText(getApplicationContext(),
                                                            "Login successful!!",
                                                            Toast.LENGTH_LONG)
                                                            .show();
                                                }
                                                progressBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });
                                    } else {
                                        FirebaseAuth.getInstance().signOut();
                                        Toast.makeText(getApplicationContext(),
                                                "Email Id not Verify",
                                                Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Login failed!!",
                                            Toast.LENGTH_LONG)
                                            .show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
    }
}
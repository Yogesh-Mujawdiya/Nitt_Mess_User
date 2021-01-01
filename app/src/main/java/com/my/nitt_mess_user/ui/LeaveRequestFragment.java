package com.my.nitt_mess_user.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.my.nitt_mess_user.Class.Leave;
import com.my.nitt_mess_user.R;
import java.util.Calendar;

public class LeaveRequestFragment extends Fragment {
    DatePickerDialog picker;
    Calendar From = Calendar.getInstance(), To = Calendar.getInstance();
    TextInputLayout editTextFrom, editTextTo;
    EditText editTextLeaveReason;
    ProgressBar progressBar;
    Button buttonLeave;
    Calendar c = Calendar.getInstance();
    FirebaseUser FUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_leave_request, container, false);

        FUser = FirebaseAuth.getInstance().getCurrentUser();
        editTextFrom = root.findViewById(R.id.editTextFrom);
        editTextTo = root.findViewById(R.id.editTextTo);
        editTextLeaveReason = root.findViewById(R.id.leaveReason);
        buttonLeave = root.findViewById(R.id.LeaveBtn);
        progressBar = root.findViewById(R.id.progressbar);

        editTextFrom.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFromDatePicker();
            }
        });
        editTextTo.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openToDatePicker();
            }
        });
        buttonLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                buttonLeave.setVisibility(View.GONE);
                SubmitLeaveRequest();
            }
        });
        return root;
    }

    private void SubmitLeaveRequest() {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hours = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        int milliSecond = c.get(Calendar.MILLISECOND);
        final String Key = day+"-"+month+"-"+year+" "+hours+":"+minute+":"+second+":"+milliSecond;
        final String RollNo = FUser.getEmail().split("@")[0];
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Data/User").child(RollNo).child("AllocateMess").child(year+"/"+month);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String MessId = dataSnapshot.getValue().toString();
                    Leave leave = new Leave(
                            editTextFrom.getEditText().getText().toString(),
                            editTextTo.getEditText().getText().toString(),
                            editTextLeaveReason.getText().toString(),
                            RollNo,
                            MessId,
                            "Pending"
                    );
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Data/LeaveRequest").push();
                    databaseReference.setValue(leave);
                    FirebaseDatabase.getInstance().getReference("Data/MessLeaveRequest").child(MessId).child(databaseReference.getKey()).setValue(1);
                    FirebaseDatabase.getInstance().getReference("Data/StudentLeaveRequest").child(RollNo).child(databaseReference.getKey()).setValue(1);
                    Toast.makeText(getContext(), "Request Uploaded!!", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
                buttonLeave.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                buttonLeave.setVisibility(View.VISIBLE);
            }
        });
    }

    private void openFromDatePicker(){
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        // date picker dialog
        picker = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        editTextFrom.getEditText().setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                        From.set(year,monthOfYear,dayOfMonth);
                    }
                }, year, month, day);
        Calendar calendar = Calendar.getInstance();
        picker.getDatePicker().setMinDate(calendar.getTimeInMillis() + 2*24*60*60*1000);
        picker.show();
    }
    private void openToDatePicker(){
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        // date picker dialog
        picker = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        editTextTo.getEditText().setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                        To.set(year,monthOfYear,dayOfMonth);
                    }
                }, year, month, day);
        picker.getDatePicker().setMinDate(From.getTimeInMillis());
        picker.show();
    }
}
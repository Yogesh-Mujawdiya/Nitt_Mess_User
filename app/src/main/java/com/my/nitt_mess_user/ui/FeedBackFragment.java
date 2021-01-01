package com.my.nitt_mess_user.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.my.nitt_mess_user.R;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class FeedBackFragment extends Fragment {

    TextView FeedBack;
    LinearLayout layoutFeedback;
    RatingBar Food, Quantity, Service, Cleanliness, FoodOnTime, Overall;
    TextInputLayout Remark;
    Button Reset, Submit;
    LinearLayout FeedBackLayout;
    FirebaseUser User;
    boolean IsFeedbackSubmitted = false;
    String MessId;
    String FoodType;
    String Key;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_feed_back, container, false);

        GregorianCalendar calendar = new GregorianCalendar();
        int Year = calendar.get(Calendar.YEAR);
        int Month = calendar.get(Calendar.MONTH);
        Key = Year + "/" + Month;
        FeedBack = root.findViewById(R.id.UserFeedBack);
        layoutFeedback = root.findViewById(R.id.layoutFeedback);
        Food = root.findViewById(R.id.ratingBarFood);
        Quantity = root.findViewById(R.id.ratingBarQuantity);
        FoodOnTime = root.findViewById(R.id.ratingBarFoodOnTime);
        Service = root.findViewById(R.id.ratingBarService);
        Cleanliness = root.findViewById(R.id.ratingBarCleanliness);
        Reset = root.findViewById(R.id.ResetButton);
        Submit = root.findViewById(R.id.SubmitButton);
        FeedBackLayout = root.findViewById(R.id.layout);
        User = FirebaseAuth.getInstance().getCurrentUser();
        getUserFeedback();
        Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Food.setRating((float) 2.5);
                Quantity.setRating((float) 2.5);
                FoodOnTime.setRating((float) 2.5);
                Service.setRating((float) 2.5);
                Cleanliness.setRating((float) 2.5);
                Overall.setRating((float) 2.5);
                Remark.getEditText().setText("");
            }
        });

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Data/MessRating").child(MessId).child(Key);
                databaseReference.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        double FoodData;
                        double QuantityData;
                        double FoodOnTimeData;
                        double ServiceData;
                        double CleanlinessData;
                        FoodData = QuantityData = FoodOnTimeData = ServiceData = CleanlinessData = 0;
                        int Count = 0;
                        if(currentData.child("Food").getValue(Float.class)!=null)
                            FoodData = currentData.child("Food").getValue(Float.class);
                        if(currentData.child("Quantity").getValue(Float.class)!=null)
                            QuantityData = currentData.child("Quantity").getValue(Float.class);
                        if(currentData.child("FoodOnTime").getValue(Float.class)!=null)
                            FoodOnTimeData = currentData.child("FoodOnTime").getValue(Float.class);
                        if(currentData.child("Service").getValue(Float.class)!=null)
                            ServiceData = currentData.child("Service").getValue(Float.class);
                        if(currentData.child("Cleanliness").getValue(Float.class)!=null)
                            CleanlinessData = currentData.child("Cleanliness").getValue(Float.class);
                        if(currentData.child("Count").getValue(Integer.class)!=null)
                            Count = currentData.child("Count").getValue(Integer.class);

                        Count+=1;
                        FoodData = (FoodData+Food.getRating())/Count;
                        QuantityData = (QuantityData+Quantity.getRating())/Count;
                        FoodOnTimeData = (FoodOnTimeData+FoodOnTime.getRating())/Count;
                        ServiceData = (ServiceData+Service.getRating())/Count;
                        CleanlinessData = (CleanlinessData+Cleanliness.getRating())/Count;
                        currentData.child("Food").setValue(FoodData);
                        currentData.child("Quantity").setValue(QuantityData);
                        currentData.child("FoodOnTime").setValue(FoodOnTimeData);
                        currentData.child("Service").setValue(ServiceData);
                        currentData.child("Cleanliness").setValue(CleanlinessData);
                        currentData.child("Count").setValue(Count);

                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        final String RollNo = firebaseUser.getEmail().split("@")[0];
                        FirebaseDatabase.getInstance().getReference("Data/User").child(RollNo).child("LastTakenFood").child("feedback").setValue(true);
//                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
        });
        return root;
    }

    private void getUserFeedback() {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String RollNo = firebaseUser.getEmail().split("@")[0];
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Data/User").child(RollNo).child("LastTakenFood");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    MessId = dataSnapshot.child("MessId").getValue().toString();
                    FoodType = dataSnapshot.child("type").getValue().toString();
                    Key = dataSnapshot.child("date").getValue().toString();
                    IsFeedbackSubmitted = dataSnapshot.child("feedback").getValue(Boolean.class);
                    if(IsFeedbackSubmitted){
                        FeedBack.setText("Feedback Submitted");
                        layoutFeedback.setVisibility(View.GONE);
                    }
                    else{
                        FeedBack.setText("Give Us Your " + FoodType + " FeedBack");
                        layoutFeedback.setVisibility(View.VISIBLE);
                    }
                    FeedBackLayout.setVisibility(View.VISIBLE);
                    progressDialog.dismiss();
                } else {
                    FeedBack.setText("No Data Available");
                    layoutFeedback.setVisibility(View.GONE);
                    FeedBackLayout.setVisibility(View.VISIBLE);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }
}
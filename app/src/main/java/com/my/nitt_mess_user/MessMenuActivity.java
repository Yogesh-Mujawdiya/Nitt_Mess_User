package com.my.nitt_mess_user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.my.nitt_mess_user.Adapter.MessMenuAdapter;
import com.my.nitt_mess_user.Class.MessMenu;
import com.my.nitt_mess_user.Class.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class MessMenuActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    MessMenuAdapter recyclerAdapter;
    TextView AllocatedMess;
    boolean IsMessAllocate = false;
    List<MessMenu> menuList;
    Button submit;
    User user;
    FirebaseUser FUser;
    Dictionary Week;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mess_menu);
        String MessId = getIntent().getStringExtra("MessId");
        getMessMenu(MessId);
        Week = new Hashtable();
        Week.put("Monday",1);
        Week.put("Tuesday",2);
        Week.put("Wednesday",3);
        Week.put("Thursday",4);
        Week.put("Friday",5);
        Week.put("Saturday",6);
        Week.put("Sunday",7);

        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplication());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplication()));
    }

    private void getMessMenu(String MessId){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Data/Mess/"+MessId+"/Menu");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    menuList = new ArrayList<MessMenu>();
                    for (final DataSnapshot menu : dataSnapshot.getChildren()) {
                        menuList.add( new MessMenu(
                                menu.getKey(),
                                menu.child("Breakfast").getValue().toString(),
                                menu.child("Lunch").getValue().toString(),
                                menu.child("Dinner").getValue().toString(),
                                menu.child("Snack").getValue().toString()
                        ));
                    }
                }
                sortByWeekDay();
                recyclerAdapter = new MessMenuAdapter(menuList, getApplication());
                recyclerView.setAdapter(recyclerAdapter);
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void sortByWeekDay() {
        for(int i=0;i<menuList.size();i++){
            for(int j=i+1;j<menuList.size();j++){
                int x = (int) Week.get(menuList.get(j).getWeekName());
                int y = (int) Week.get(menuList.get(i).getWeekName());
                if(x<y){
                    Collections.swap(menuList, i, j);
                }
            }
        }
    }

}
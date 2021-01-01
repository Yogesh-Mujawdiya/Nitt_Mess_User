package com.my.nitt_mess_user.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.my.nitt_mess_user.Adapter.MessListAdapter;
import com.my.nitt_mess_user.Class.Mess;
import com.my.nitt_mess_user.R;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MessMenuFragment extends Fragment {


    RecyclerView recyclerView;
    MessListAdapter recyclerAdapter;
    List<Mess> messList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_mess_menu, container, false);

        recyclerView = root.findViewById(R.id.recyclerViewMessList);


        String UserId = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("AllUser").child(UserId);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    getMessData(dataSnapshot.child("Gender").getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return root;
    }

    private void getMessData(final String Gender) {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Data/Mess");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    messList = new ArrayList<Mess>();
                    for (final DataSnapshot mess : dataSnapshot.getChildren()) {
                        if(mess.child("Type").getValue().equals(Gender) || mess.child("Type").getValue().equals("Unisex")) {
                            Mess M = new Mess(
                                    mess.getKey(),
                                    mess.child("Name").getValue().toString(),
                                    Integer.parseInt(mess.child("Total").getValue().toString()),
                                    Integer.parseInt(mess.child("Allocate").getValue().toString()),
                                    new Hashtable());
                            messList.add(M);
                        }
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

}
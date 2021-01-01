package com.my.nitt_mess_user.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.my.nitt_mess_user.Class.Leave;
import com.my.nitt_mess_user.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class StudentsLeaveRequestAdapter extends RecyclerView.Adapter<StudentsLeaveRequestAdapter.ViewHolder> implements Filterable {

    List<Leave> leaveList, fullList;
    FirebaseUser User;
    Context context;
    String Key;

    public StudentsLeaveRequestAdapter(List<Leave> leaveList, Context context)  {

        GregorianCalendar c = new GregorianCalendar();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        final String Key = year+"/"+month;
        User = FirebaseAuth.getInstance().getCurrentUser();
        this.leaveList = leaveList;
        fullList = leaveList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.leave_history, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Leave leave = leaveList.get(position);
        holder.textViewLeaveReason.setText(leave.getReason());
        holder.textViewFrom.setText(leave.getFrom());
        holder.textViewTo.setText(leave.getTo());
        holder.UserRollNo.setText(leave.getRollNo());
        holder.textViewLeaveStatus.setText(leave.getStatus());
        if(!leave.getStatus().equals("Pending"))
            holder.deleteRequest.setVisibility(View.GONE);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("AllUser").child(leave.getRollNo());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    holder.textViewUserName.setText(dataSnapshot.child("Student Name").getValue().toString());
                    holder.UserMobile.setText(dataSnapshot.child("Contact No").getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.deleteRequest.setOnClickListener(view -> {

            String RollNo = User.getEmail().split("@")[0];
            FirebaseDatabase.getInstance().getReference("Data/LeaveRequest").child(leave.getId()).removeValue();
            FirebaseDatabase.getInstance().getReference("Data/StudentLeaveRequest").child(RollNo).child(leave.getId()).removeValue();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Data/User").child(RollNo).child("AllocateMess").child(Key);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        FirebaseDatabase.getInstance().getReference("Data/MessLeaveRequest").
                                child(dataSnapshot.getValue(String.class)).child(leave.getId()).removeValue();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        });
    }

    @Override
    public int getItemCount() {
        if (leaveList==null)
            return 0;
        return leaveList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewLeaveReason, textViewFrom, textViewTo, textViewUserName;
        TextView UserRollNo, UserMobile, textViewLeaveStatus;
        CardView cardView, deleteRequest;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLeaveReason = itemView.findViewById(R.id.textViewLeaveReason);
            textViewFrom = itemView.findViewById(R.id.textViewFrom);
            textViewTo = itemView.findViewById(R.id.textViewTo);
            textViewUserName = itemView.findViewById(R.id.UserName);
            UserRollNo = itemView.findViewById(R.id.UserRollNo);
            UserMobile = itemView.findViewById(R.id.UserMobile);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            cardView = itemView.findViewById(R.id.cardView);
            deleteRequest = itemView.findViewById(R.id.deleteRequest);
            textViewLeaveStatus = itemView.findViewById(R.id.textViewLeaveStatus);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
//            Toast.makeText(view.getContext(), leaveList.get(getAdapterPosition()), Toast.LENGTH_SHORT).show();
        }
    }


    //filter class
    private class RecordFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            //Implement filter logic
            // if edittext is null return the actual list
            if (constraint == null || constraint.length() == 0) {
                //No need for filter
                results.values = fullList;
                results.count = fullList.size();

            } else {
                //Need Filter
                // it matches the text  entered in the edittext and set the data in adapter list
                ArrayList<Leave> fRecords = new ArrayList<>();

                for (Leave s : fullList) {
                    if (s.getRollNo().toUpperCase().trim().contains(constraint.toString().toUpperCase().trim())) {
                        fRecords.add(s);
                    }
                }
                results.values = fRecords;
                results.count = fRecords.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,FilterResults results) {

            //it set the data from filter to adapter list and refresh the recyclerview adapter
            leaveList = (ArrayList<Leave>) results.values;
            notifyDataSetChanged();
        }
    }


    private Filter fRecords;

    //return the filter class object
    @Override
    public Filter getFilter() {
        if(fRecords == null) {
            fRecords=new RecordFilter();
        }
        return fRecords;
    }
}
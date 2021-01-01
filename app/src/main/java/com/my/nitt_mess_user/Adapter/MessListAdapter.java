package com.my.nitt_mess_user.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.my.nitt_mess_user.Class.Mess;
import com.my.nitt_mess_user.MessMenuActivity;
import com.my.nitt_mess_user.R;

import java.util.List;

public class MessListAdapter extends RecyclerView.Adapter<MessListAdapter.ViewHolder> {

    private static final String TAG = "RecyclerAdapter";
    List<Mess> messList;
    Context context;

    public MessListAdapter(List<Mess> messList, Context context) {

        this.messList = messList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.mess, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Mess mess = messList.get(position);
        holder.textView.setText(mess.getName());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MessMenuActivity.class);
                intent.putExtra("MessId", mess.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textView;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            cardView = itemView.findViewById(R.id.cardView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
//            Toast.makeText(view.getContext(), messList.get(getAdapterPosition()), Toast.LENGTH_SHORT).show();
        }
    }
}
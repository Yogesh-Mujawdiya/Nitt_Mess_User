package com.my.nitt_mess_user.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.my.nitt_mess_user.Class.MessMenu;
import com.my.nitt_mess_user.R;

import java.util.List;

public class MessMenuAdapter extends RecyclerView.Adapter<MessMenuAdapter.ViewHolder> {

    private static final String TAG = "RecyclerAdapter";
    List<MessMenu> messMenu;
    Context context;

    public MessMenuAdapter(List<MessMenu> messMenu, Context context) {

        this.messMenu = messMenu;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.menu_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MessMenu menu = messMenu.get(position);
        holder.WeekDay.setText(menu.getWeekName());
        holder.Breakfast.setText(menu.getBreakfast());
        holder.Lunch.setText(menu.getLunch());
        holder.Snack.setText(menu.getSnack());
        holder.Dinner.setText(menu.getDinner());
    }

    @Override
    public int getItemCount() {
        return messMenu.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView WeekDay, Breakfast, Lunch, Dinner, Snack;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            WeekDay = itemView.findViewById(R.id.weekName);
            Breakfast = itemView.findViewById(R.id.Breakfast);
            Lunch = itemView.findViewById(R.id.Lunch);
            Snack = itemView.findViewById(R.id.Snack);
            Dinner = itemView.findViewById(R.id.Dinner);
            cardView = itemView.findViewById(R.id.cardView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
//            Toast.makeText(view.getContext(), messMenu.get(getAdapterPosition()), Toast.LENGTH_SHORT).show();
        }
    }
}
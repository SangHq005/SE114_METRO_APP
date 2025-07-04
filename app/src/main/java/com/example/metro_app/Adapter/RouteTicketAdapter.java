package com.example.metro_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.Activity.User.ChooseTicketActivity;
import com.example.metro_app.Model.TicketType;
import com.example.metro_app.R;

public class RouteTicketAdapter extends ListAdapter<TicketType, RouteTicketAdapter.RouteTicketViewHolder> {

    private static final String TAG = "RouteTicketAdapter";

    public RouteTicketAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TicketType> DIFF_CALLBACK = new DiffUtil.ItemCallback<TicketType>() {
        @Override
        public boolean areItemsTheSame(@NonNull TicketType oldItem, @NonNull TicketType newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TicketType oldItem, @NonNull TicketType newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId()) &&
                    oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getPrice().equals(newItem.getPrice()) &&
                    (oldItem.getStartStation() == null ? newItem.getStartStation() == null : oldItem.getStartStation().equals(newItem.getStartStation())) &&
                    (oldItem.getEndStation() == null ? newItem.getEndStation() == null : oldItem.getEndStation().equals(newItem.getEndStation())) &&
                    (oldItem.getActive() == null ? newItem.getActive() == null : oldItem.getActive().equals(newItem.getActive())) &&
                    (oldItem.getAutoActive() == null ? newItem.getAutoActive() == null : oldItem.getAutoActive().equals(newItem.getAutoActive())) &&
                    (oldItem.getType() == null ? newItem.getType() == null : oldItem.getType().equals(newItem.getType()));
        }
    };

    @NonNull
    @Override
    public RouteTicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_route_tickets, parent, false);
        return new RouteTicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteTicketViewHolder holder, int position) {
        TicketType ticket = getItem(position);
        Log.d(TAG, "Binding ticket at position " + position + ": id=" + (ticket.getId() != null ? ticket.getId() : "null") + ", name=" + ticket.getName() + ", endStation=" + ticket.getEndStation());
        holder.routeTicketTxt.setText("Đến " + (ticket.getEndStation() != null ? ticket.getEndStation() : "N/A"));
        holder.priceTxt.setText(ticket.getPrice() != null ? ticket.getPrice() : "0 VND");

        // Xử lý click trên ViewHolder
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, ChooseTicketActivity.class);
            intent.putExtra("ticket_type_id", ticket.getId() != null ? ticket.getId() : "unknown"); // Đảm bảo không null
            intent.putExtra("ticket_name", ticket.getName());
            intent.putExtra("start_station", ticket.getStartStation());
            intent.putExtra("end_station", ticket.getEndStation());
            intent.putExtra("ticket_price", ticket.getPrice());
            intent.putExtra("ticket_active", ticket.getActive() != null ? ticket.getActive() : "0");
            intent.putExtra("ticket_auto_active", ticket.getAutoActive() != null ? ticket.getAutoActive() : "0");
            intent.putExtra("type", ticket.getType());
            Log.d(TAG, "Starting ChooseTicketActivity with ticket_type_id: " + (ticket.getId() != null ? ticket.getId() : "unknown"));
            context.startActivity(intent);
        });
    }

    static class RouteTicketViewHolder extends RecyclerView.ViewHolder {
        TextView routeTicketTxt, priceTxt;

        public RouteTicketViewHolder(@NonNull View itemView) {
            super(itemView);
            routeTicketTxt = itemView.findViewById(R.id.routeTicketTxt);
            priceTxt = itemView.findViewById(R.id.priceTxt);
        }
    }
}
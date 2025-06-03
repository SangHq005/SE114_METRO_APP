package com.example.metro_app.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.Model.TicketType;
import com.example.metro_app.R;

import java.util.List;

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.RouteViewHolder> {

    private List<TicketType> ticketList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TicketType ticket);
    }

    public RouteListAdapter(List<TicketType> ticketList, OnItemClickListener listener) {
        this.ticketList = ticketList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_route_list_ticket, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        TicketType ticket = ticketList.get(position);
        holder.routeTxt.setText("Đi từ " + (ticket.getStartStation() != null ? ticket.getStartStation() : "N/A"));
        holder.detailTxt.setOnClickListener(v -> listener.onItemClick(ticket));
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView routeTxt, detailTxt;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            routeTxt = itemView.findViewById(R.id.routeTxt);
            detailTxt = itemView.findViewById(R.id.detailTxt);
        }
    }
}
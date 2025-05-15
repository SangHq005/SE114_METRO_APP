package com.example.metro_app.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.metro_app.Model.TicketType;
import com.example.metro_app.R;
import java.util.List;

public class TicketTypeAdapter extends RecyclerView.Adapter<TicketTypeAdapter.TicketViewHolder> {

    private List<TicketType> ticketList;
    private OnTicketClickListener onTicketClickListener;

    public interface OnTicketClickListener {
        void onTicketClick(TicketType ticket);
    }

    public TicketTypeAdapter(List<TicketType> ticketList, OnTicketClickListener listener) {
        this.ticketList = ticketList;
        this.onTicketClickListener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        TicketType ticket = ticketList.get(position);
        holder.tvTicketName.setText(ticket.getName());
        holder.tvTicketPrice.setText(ticket.getPrice()); // Hiển thị giá
        holder.itemView.setOnClickListener(v -> onTicketClickListener.onTicketClick(ticket));
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTicketIcon;
        TextView tvTicketName;
        TextView tvTicketPrice;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTicketIcon = itemView.findViewById(R.id.iv_ticket_icon);
            tvTicketName = itemView.findViewById(R.id.tv_ticket_name);
            tvTicketPrice = itemView.findViewById(R.id.tv_ticket_price);
        }
    }
}
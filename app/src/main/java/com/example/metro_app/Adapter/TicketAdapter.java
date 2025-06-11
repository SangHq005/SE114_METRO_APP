package com.example.metro_app.Adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.metro_app.Activity.User.CreateQRActivity;
import com.example.metro_app.Domain.TicketModel;
import com.example.metro_app.R;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<TicketModel> ticketList;
    private OnTicketClickListener onTicketClickListener;
    private String userUUID;

    public interface OnTicketClickListener {
        void onTicketClick(TicketModel ticket);
    }

    public TicketAdapter(List<TicketModel> ticketList, OnTicketClickListener listener, String userUUID) {
        this.ticketList = ticketList;
        this.onTicketClickListener = listener;
        this.userUUID = userUUID;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_your_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        TicketModel ticket = ticketList.get(position);
        if (holder.tvTicketName != null) {
            holder.tvTicketName.setText(ticket.getTicketType() != null ? ticket.getTicketType() : "Không có thông tin");
        } else {
            Log.e("TicketAdapter", "tvTicketName is null at position " + position);
        }
        if (holder.tvStatus != null) {
            holder.tvStatus.setText(ticket.getStatus() != null ? ticket.getStatus() : "Không có thông tin");
            String status = ticket.getStatus() != null ? ticket.getStatus().trim() : "";
            if ("Chưa kích hoạt".equals(status)) {
                holder.tvStatus.setBackgroundResource(R.drawable.status_pending_gradient);
                holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.pending_text));
            } else if ("Hết hạn".equals(status)) {
                holder.tvStatus.setBackgroundResource(R.drawable.status_warning_gradient);
                holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.failed_text));
            } else {
                holder.tvStatus.setBackgroundResource(R.drawable.status_success_gradient);
                // Không thay đổi textColor nếu không phải "Chưa kích hoạt" hoặc "Hết hạn"
            }
        } else {
            Log.e("TicketAdapter", "tvStatus is null at position " + position);
        }
        if (holder.tvAutoActiveDate != null) {
            if ("Đang kích hoạt".equals(ticket.getStatus())) {
                String expirationDate = ticket.getExpirationDate();
                holder.tvAutoActiveDate.setText("Hết hạn vào ngày " + (expirationDate != null ? expirationDate : "N/A"));
                holder.tvAutoActiveDate.setVisibility(View.VISIBLE);
            } else {
                String expireDate = ticket.getExpireDate();
                if (expireDate != null && !expireDate.isEmpty()) {
                    holder.tvAutoActiveDate.setText(expireDate);
                    holder.tvAutoActiveDate.setVisibility(View.VISIBLE);
                } else {
                    holder.tvAutoActiveDate.setVisibility(View.GONE);
                }
            }
        } else {
            Log.e("TicketAdapter", "tvAutoActiveDate is null at position " + position);
        }
        holder.itemView.setOnClickListener(v -> {
            onTicketClickListener.onTicketClick(ticket);

            String ticketStatus = ticket.getStatus() != null ? ticket.getStatus().trim() : null;
            Log.d("TicketAdapter", "Ticket Status onClick: " + ticketStatus);

            if (ticketStatus != null && !"Hết hạn".equals(ticketStatus)) {
                Intent intent = new Intent(v.getContext(), CreateQRActivity.class);
                intent.putExtra("ticketId", ticket.getTicketId());
                intent.putExtra("ticketType", ticket.getTicketType());
                intent.putExtra("expireDate", ticket.getExpireDate());
                intent.putExtra("status", ticket.getStatus());
                intent.putExtra("issueDate", ticket.getIssueDate() != null ? ticket.getIssueDate().getTime() : 0L);
                intent.putExtra("userName", ticket.getUserName());
                intent.putExtra("ticketCode", ticket.getTicketCode());
                intent.putExtra("userId", userUUID);
                intent.putExtra("source", "YourTicketsActivity");
                v.getContext().startActivity(intent);
            } else {
                Log.d("TicketAdapter", "Skipping CreateQRActivity: Status is " + ticketStatus);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTicketIcon;
        TextView tvTicketName;
        TextView tvStatus;
        TextView tvAutoActiveDate;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTicketIcon = itemView.findViewById(R.id.iv_ticket_icon);
            tvTicketName = itemView.findViewById(R.id.tv_ticket_name);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAutoActiveDate = itemView.findViewById(R.id.tv_ticket_auto_active_date);

            if (ivTicketIcon == null) Log.e("TicketAdapter", "ivTicketIcon is null");
            if (tvTicketName == null) Log.e("TicketAdapter", "tvTicketName is null");
            if (tvStatus == null) Log.e("TicketAdapter", "tvStatus is null");
            if (tvAutoActiveDate == null) Log.e("TicketAdapter", "tvAutoActiveDate is null");
        }
    }
}
package com.example.metro_app.Activity.Admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.Domain.TicketModel;
import com.example.metro_app.R;

import java.util.ArrayList;
import java.util.List;


// Adapter for RecyclerView
class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private List<TicketModel> ticketList;

    public TicketAdapter(List<TicketModel> ticketList) {
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        TicketModel ticket = ticketList.get(position);
        holder.ticketName.setText(ticket.getPrice());
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView ticketName;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ticketName = itemView.findViewById(R.id.tv_ticket_name);
        }
    }
}

public class AdTicketActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TicketAdapter ticketAdapter;
    private List<TicketModel> ticketList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_ticket);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view_tickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample data
        ticketList = new ArrayList<>();
        ticketList.add(new TicketModel("100,000 VND"));
        ticketList.add(new TicketModel("100,000 VND"));
        ticketList.add(new TicketModel("100,000 VND"));
        ticketList.add(new TicketModel("100,000 VND"));
        ticketList.add(new TicketModel("100,000 VND"));
        ticketList.add(new TicketModel("100,000 VND"));
        ticketList.add(new TicketModel("100,000 VND"));
        ticketList.add(new TicketModel("100,000 VND"));
        ticketList.add(new TicketModel("100,000 VND"));
        ticketList.add(new TicketModel("100,000 VND"));
        ticketList.add(new TicketModel("100,000 VND"));
        ticketList.add(new TicketModel("100,000 VND"));

        // Set up adapter
        ticketAdapter = new TicketAdapter(ticketList);
        recyclerView.setAdapter(ticketAdapter);
    }
}
package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.Domain.TicketModel;
import com.example.metro_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private List<TicketModel> filteredTicketList;
    private final OnItemClickListener listener;
    private final AppCompatActivity context;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public TicketAdapter(AppCompatActivity context, List<TicketModel> filteredTicketList, OnItemClickListener listener) {
        this.context = context;
        this.filteredTicketList = filteredTicketList;
        this.listener = listener;
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
        TicketModel ticket = filteredTicketList.get(position);
        String ticketType = ticket.getTicketType() != null ? ticket.getTicketType() : "Unknown";
        String price = ticket.getPrice() != null ? ticket.getPrice() : "N/A";
        holder.ticketName.setText(context.getString(R.string.ticket_name_format, ticketType, price));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return filteredTicketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView ticketName;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ticketName = itemView.findViewById(R.id.tv_ticket_name);
        }
    }

    public void updateList(List<TicketModel> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TicketDiffCallback(filteredTicketList, newList));
        filteredTicketList = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    static class TicketDiffCallback extends DiffUtil.Callback {
        private final List<TicketModel> oldList;
        private final List<TicketModel> newList;

        TicketDiffCallback(List<TicketModel> oldList, List<TicketModel> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition) == newList.get(newItemPosition);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TicketModel oldTicket = oldList.get(oldItemPosition);
            TicketModel newTicket = newList.get(newItemPosition);
            return (oldTicket.getTicketType() == null ? newTicket.getTicketType() == null : oldTicket.getTicketType().equals(newTicket.getTicketType())) &&
                    (oldTicket.getPrice() == null ? newTicket.getPrice() == null : oldTicket.getPrice().equals(newTicket.getPrice())) &&
                    (oldTicket.getExpireDate() == null ? newTicket.getExpireDate() == null : oldTicket.getExpireDate().equals(newTicket.getExpireDate()));
        }
    }
}

public class AdTicketActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TicketAdapter ticketAdapter;
    private List<TicketModel> ticketList;
    private List<TicketModel> filteredTicketList;

    private final ActivityResultLauncher<Intent> addTicketLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    TicketModel ticket = (TicketModel) result.getData().getSerializableExtra("ticket");
                    if (ticket != null) {
                        ticketList.add(ticket);
                        filterTickets("");
                    }
                }
            });

    private final ActivityResultLauncher<Intent> editTicketLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    TicketModel ticket = (TicketModel) result.getData().getSerializableExtra("ticket");
                    int position = result.getData().getIntExtra("position", -1);
                    if (ticket != null && position != -1) {
                        ticketList.set(position, ticket);
                        filterTickets("");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_ticket);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view_tickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize search bar
        EditText searchBar = findViewById(R.id.search_bar);

        // Sample data
        ticketList = new ArrayList<>();
        ticketList.add(new TicketModel("Vé lượt đi", "6,000 VND", "05/05/2025"));
        ticketList.add(new TicketModel("Vé 1 ngày", "15,000 VND", "06/05/2025"));
        ticketList.add(new TicketModel("Vé 3 ngày", "40,000 VND", "07/05/2025"));
        ticketList.add(new TicketModel("Vé 30 ngày", "100,000 VND", "08/05/2025"));

        // Initialize filtered list
        filteredTicketList = new ArrayList<>(ticketList);

        // Set up adapter
        ticketAdapter = new TicketAdapter(this, filteredTicketList, position -> {
            Intent intent = new Intent(AdTicketActivity.this, AdTicketDetails.class);
            intent.putExtra("ticket", filteredTicketList.get(position));
            intent.putExtra("position", ticketList.indexOf(filteredTicketList.get(position)));
            editTicketLauncher.launch(intent);
        });
        recyclerView.setAdapter(ticketAdapter);

        ImageButton addButton = findViewById(R.id.button_add_ticket);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdTicketActivity.this, AddTicketActivity.class);
            addTicketLauncher.launch(intent);
        });

        // Search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTickets(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_ad_wallet);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_ad_home) {
                startActivity(new Intent(AdTicketActivity.this, AdHomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_ad_route) {
                startActivity(new Intent(AdTicketActivity.this, AdRouteActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_ad_wallet) {
                return true;
            } else if (id == R.id.nav_ad_userlist) {
                startActivity(new Intent(AdTicketActivity.this, AdUserActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void filterTickets(String query) {
        filteredTicketList.clear();
        if (query.isEmpty()) {
            filteredTicketList.addAll(ticketList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (TicketModel ticket : ticketList) {
                if (ticket.getTicketType() != null && ticket.getTicketType().toLowerCase().contains(lowerCaseQuery)) {
                    filteredTicketList.add(ticket);
                }
            }
        }
        ticketAdapter.updateList(filteredTicketList);
    }
}
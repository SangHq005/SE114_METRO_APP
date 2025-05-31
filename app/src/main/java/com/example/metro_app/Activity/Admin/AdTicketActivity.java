package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.Model.TicketType;
import com.example.metro_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private List<TicketType> filteredTicketList;
    private final OnItemClickListener listener;
    private final AppCompatActivity context;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public TicketAdapter(AppCompatActivity context, List<TicketType> filteredTicketList, OnItemClickListener listener) {
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
        TicketType ticket = filteredTicketList.get(position);
        String ticketName = ticket.getName() != null ? ticket.getName() : "Unknown";
        String price = ticket.getPrice() != null ? ticket.getPrice() : "N/A";
        holder.ticketName.setText(ticketName);
        if (holder.ticketPrice != null) {
            holder.ticketPrice.setText(price);
        } else {
            // Fallback to single TextView with combined format
            holder.ticketName.setText(context.getString(R.string.ticket_name_format, ticketName, price));
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return filteredTicketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView ticketName;
        TextView ticketPrice; // Nullable, for backward compatibility

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ticketName = itemView.findViewById(R.id.tv_ticket_name);
            ticketPrice = itemView.findViewById(R.id.tv_ticket_price); // May be null
        }
    }

    public void updateList(List<TicketType> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TicketDiffCallback(filteredTicketList, newList));
        filteredTicketList = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    static class TicketDiffCallback extends DiffUtil.Callback {
        private final List<TicketType> oldList;
        private final List<TicketType> newList;

        TicketDiffCallback(List<TicketType> oldList, List<TicketType> newList) {
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
            return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TicketType oldTicket = oldList.get(oldItemPosition);
            TicketType newTicket = newList.get(newItemPosition);
            return (oldTicket.getName() == null ? newTicket.getName() == null : oldTicket.getName().equals(newTicket.getName())) &&
                    (oldTicket.getPrice() == null ? newTicket.getPrice() == null : oldTicket.getPrice().equals(newTicket.getPrice())) &&
                    (oldTicket.getActive() == null ? newTicket.getActive() == null : oldTicket.getActive().equals(newTicket.getActive())) &&
                    (oldTicket.getAutoActive() == null ? newTicket.getAutoActive() == null : oldTicket.getAutoActive().equals(newTicket.getAutoActive())) &&
                    (oldTicket.getStatus() == null ? newTicket.getStatus() == null : oldTicket.getStatus().equals(newTicket.getStatus()));
        }
    }
}

public class AdTicketActivity extends AppCompatActivity {
    private static final String TAG = "AdTicketActivity";
    private RecyclerView recyclerView;
    private TicketAdapter ticketAdapter;
    private List<TicketType> ticketList;
    private List<TicketType> filteredTicketList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    private final ActivityResultLauncher<Intent> addTicketLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    TicketType ticket = (TicketType) result.getData().getSerializableExtra("ticket");
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
                    TicketType ticket = (TicketType) result.getData().getSerializableExtra("ticket");
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

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view_tickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize search bar
        EditText searchBar = findViewById(R.id.search_bar);

        // Initialize ProgressBar
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        // Initialize lists
        ticketList = new ArrayList<>();
        filteredTicketList = new ArrayList<>();

        // Set up adapter
        ticketAdapter = new TicketAdapter(this, filteredTicketList, position -> {
            Intent intent = new Intent(AdTicketActivity.this, AdTicketDetails.class);
            intent.putExtra("ticket", filteredTicketList.get(position));
            intent.putExtra("position", ticketList.indexOf(filteredTicketList.get(position)));
            editTicketLauncher.launch(intent);
        });
        recyclerView.setAdapter(ticketAdapter);

        // Load data from Firestore
        loadTicketsFromFirestore();

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

        // BottomNavigationView
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

    private void loadTicketsFromFirestore() {
        db.collection("TicketType")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ticketList.clear();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (var doc : querySnapshot.getDocuments()) {
                                try {
                                    // Use Firestore document ID as id
                                    String id = doc.getId();
                                    String name = doc.getString("Name");
                                    String status = doc.getString("Status");

                                    // Handle price as String or Number
                                    String price = null;
                                    Object priceObj = doc.get("Price") != null ? doc.get("Price") : doc.get("price");
                                    if (priceObj instanceof String) {
                                        price = (String) priceObj;
                                    } else if (priceObj instanceof Number) {
                                        DecimalFormat formatter = new DecimalFormat("#,###");
                                        price = formatter.format(((Number) priceObj).longValue()) + " VND";
                                    } else {
                                        Log.w(TAG, "Invalid price format in document: " + doc.getId());
                                        continue;
                                    }

                                    // Handle active as Number (stored as days)
                                    String active = null;
                                    Object activeObj = doc.get("Active");
                                    if (activeObj instanceof Number) {
                                        active = String.valueOf(((Number) activeObj).longValue());
                                    } else if (activeObj instanceof String) {
                                        active = (String) activeObj;
                                    } else {
                                        Log.w(TAG, "Invalid active format in document: " + doc.getId());
                                        continue;
                                    }

                                    // Handle autoActive as Number (stored as days)
                                    String autoActive = null;
                                    Object autoActiveObj = doc.get("AutoActive");
                                    if (autoActiveObj instanceof Number) {
                                        autoActive = String.valueOf(((Number) autoActiveObj).longValue());
                                    } else if (autoActiveObj instanceof String) {
                                        autoActive = (String) autoActiveObj;
                                    } else {
                                        Log.w(TAG, "Invalid autoActive format in document: " + doc.getId());
                                        continue;
                                    }

                                    // Handle null values
                                    if (id == null || name == null || price == null || active == null || autoActive == null || status == null) {
                                        Log.w(TAG, "Missing fields in document: " + doc.getId() +
                                                ", id=" + id + ", name=" + name + ", price=" + price +
                                                ", active=" + active + ", autoActive=" + autoActive +
                                                ", status=" + status);
                                        continue;
                                    }

                                    TicketType ticket = new TicketType(id, name, price, active, autoActive, status);
                                    ticketList.add(ticket);
                                    Log.d(TAG, "Added ticket: " + name + ", Price: " + price);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing document: " + doc.getId(), e);
                                }
                            }
                            filteredTicketList.clear();
                            filteredTicketList.addAll(ticketList);
                            ticketAdapter.updateList(filteredTicketList);
                            Log.d(TAG, "Loaded " + ticketList.size() + " tickets from Firestore");
                        } else {
                            Log.w(TAG, "No documents found in TicketType collection");
                            Toast.makeText(AdTicketActivity.this, "Không tìm thấy dữ liệu vé.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error loading tickets: ", task.getException());
                        Toast.makeText(AdTicketActivity.this, "Lỗi khi tải dữ liệu vé: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void filterTickets(String query) {
        filteredTicketList.clear();
        if (query.isEmpty()) {
            filteredTicketList.addAll(ticketList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (TicketType ticket : ticketList) {
                if (ticket.getName() != null && ticket.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredTicketList.add(ticket);
                }
            }
        }
        ticketAdapter.updateList(filteredTicketList);
    }
}
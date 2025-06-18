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

    public List<TicketType> getFilteredList() {
        return filteredTicketList;
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
        TextView ticketPrice;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ticketName = itemView.findViewById(R.id.tv_ticket_name);
            ticketPrice = itemView.findViewById(R.id.tv_ticket_price);
        }
    }

    public void updateList(List<TicketType> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TicketDiffCallback(this.filteredTicketList, newList));
        // Dòng code này sẽ tạo ra một danh sách mới cho adapter, tách biệt khỏi danh sách của Activity
        this.filteredTicketList = new ArrayList<>(newList);
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
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TicketType oldTicket = oldList.get(oldItemPosition);
            TicketType newTicket = newList.get(newItemPosition);
            // So sánh nội dung các trường
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

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recycler_view_tickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        EditText searchBar = findViewById(R.id.search_bar);
        progressBar = findViewById(R.id.progress_bar);

        progressBar.setVisibility(View.VISIBLE);
        ticketList = new ArrayList<>();

        // *** THÊM ĐIỀU HƯỚNG KHI NHẤN VÀO MỘT ITEM ***
        ticketAdapter = new TicketAdapter(this, new ArrayList<>(), position -> {
            // Lấy đúng vé từ danh sách đã được lọc của adapter
            TicketType selectedTicket = ticketAdapter.getFilteredList().get(position);

            // Tìm vị trí của vé đó trong danh sách gốc để gửi đi, giúp việc cập nhật dễ dàng hơn
            int originalPosition = -1;
            for (int i = 0; i < ticketList.size(); i++) {
                if (ticketList.get(i).getId().equals(selectedTicket.getId())) {
                    originalPosition = i;
                    break;
                }
            }

            // Khởi động AdTicketDetails để chỉnh sửa
            Intent intent = new Intent(AdTicketActivity.this, AdTicketDetails.class);
            intent.putExtra("ticket", selectedTicket);
            intent.putExtra("position", originalPosition);
            editTicketLauncher.launch(intent);
        });
        recyclerView.setAdapter(ticketAdapter);

        loadTicketsFromFirestore();

        ImageButton addButton = findViewById(R.id.button_add_ticket);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdTicketActivity.this, AddTicketActivity.class);
            addTicketLauncher.launch(intent);
        });
        ImageButton publicButton = findViewById(R.id.btnPublic);
        publicButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdTicketActivity.this, CreateTicketActivity.class);
            startActivity(intent);
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTickets(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_ad_wallet);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == bottomNavigationView.getSelectedItemId()) {
                return false;
            }
            if (id == R.id.nav_ad_home) {
                startActivity(new Intent(AdTicketActivity.this, AdHomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_ad_route) {
                startActivity(new Intent(AdTicketActivity.this, AdRouteActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_ad_userlist) {
                startActivity(new Intent(AdTicketActivity.this, AdUserActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_ad_profile) {
                startActivity(new Intent(AdTicketActivity.this, AdDashboardActivity.class));
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
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        ticketList.clear();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (var doc : querySnapshot.getDocuments()) {
                                try {
                                    String id = doc.getId();
                                    String name = doc.getString("Name");
                                    String status = doc.getString("Status");
                                    String price;
                                    Object priceObj = doc.get("Price");
                                    if (priceObj instanceof String) {
                                        price = (String) priceObj;
                                    } else if (priceObj instanceof Number) {
                                        DecimalFormat formatter = new DecimalFormat("#,###");
                                        price = formatter.format(((Number) priceObj).longValue()) + " VND";
                                    } else {
                                        price = "N/A";
                                    }

                                    String active = doc.get("Active") != null ? String.valueOf(doc.get("Active")) : "0";
                                    String autoActive = doc.get("AutoActive") != null ? String.valueOf(doc.get("AutoActive")) : "0";

                                    if (id == null || name == null || status == null) {
                                        Log.w(TAG, "Document " + (doc != null ? doc.getId() : "null") + " has missing fields.");
                                        continue;
                                    }

                                    TicketType ticket = new TicketType(id, name, price, active, autoActive, status);
                                    ticketList.add(ticket);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing document: " + (doc != null ? doc.getId() : "null"), e);
                                }
                            }
                            Log.d(TAG, "Loaded " + ticketList.size() + " tickets from Firestore");
                            // Sau khi tải xong, gọi filterTickets để hiển thị toàn bộ danh sách
                            filterTickets("");
                        } else {
                            Log.w(TAG, "No documents found in TicketType collection");
                            Toast.makeText(AdTicketActivity.this, "Không tìm thấy dữ liệu vé.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error loading tickets: ", task.getException());
                        Toast.makeText(AdTicketActivity.this, "Lỗi khi tải dữ liệu vé: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void filterTickets(String query) {
        List<TicketType> newFilteredList = new ArrayList<>();

        if (query.isEmpty()) {
            newFilteredList.addAll(ticketList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (TicketType ticket : ticketList) {
                if (ticket.getName() != null && ticket.getName().toLowerCase().contains(lowerCaseQuery)) {
                    newFilteredList.add(ticket);
                }
            }
        }

        ticketAdapter.updateList(newFilteredList);
    }
}
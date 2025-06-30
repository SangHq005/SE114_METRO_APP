package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.Adapter.RouteTicketAdapter;
import com.example.metro_app.Model.TicketType;
import com.example.metro_app.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DetailRouteActivity extends AppCompatActivity {

    private static final String TAG = "DetailRouteActivity";
    private TextView nameRouteTxt;

    private ImageView backBtn;
    private RecyclerView recyclerViewTicket;
    private ProgressBar progressBar;
    private RouteTicketAdapter routeTicketAdapter;
    private List<TicketType> routeTickets;
    private FirebaseFirestore db;
    private DecimalFormat decimalFormat;
    private String startStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_route);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DetailRouteActivity.this, MyTicketsActivity.class));
            }
        });

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String userId = prefs.getString("UserID", null);
        Log.d(TAG, "Retrieved userId from SharedPreferences: " + userId);

        // Lấy startStation từ Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("start_station")) {
            startStation = intent.getStringExtra("start_station");
            Log.d(TAG, "Received start_station: " + startStation);
            if (startStation == null || startStation.trim().isEmpty()) {
                Toast.makeText(this, "Ga bắt đầu không hợp lệ!", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else {
            Toast.makeText(this, "Không nhận được thông tin ga bắt đầu!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        decimalFormat = new DecimalFormat("#,###");
        decimalFormat.setGroupingSize(3);

        // Ánh xạ view
        try {
            nameRouteTxt = findViewById(R.id.nameRouteTxt);
            recyclerViewTicket = findViewById(R.id.recyclerViewTicket);
            progressBar = findViewById(R.id.progressBarTicket);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error initializing UI: " + e.getMessage());
            return;
        }

        // Đặt tên route
        nameRouteTxt.setText("Vé lượt - Đi từ " + startStation);

        // Khởi tạo RecyclerView
        recyclerViewTicket.setLayoutManager(new LinearLayoutManager(this));
        routeTickets = new ArrayList<>();
        routeTicketAdapter = new RouteTicketAdapter();
        recyclerViewTicket.setAdapter(routeTicketAdapter);

        // Load tickets
        loadTickets();
    }

    private void loadTickets() {
        Log.d(TAG, "Starting Firestore query for tickets with Type='Vé lượt' and StartStation='" + startStation + "'");
        progressBar.setVisibility(View.VISIBLE);

        db.collection("TicketType")
                .whereEqualTo("Type", "Vé lượt")
                .whereEqualTo("StartStation", startStation)
                .whereEqualTo("Status", "Hoạt động")
                .get()
                .addOnCompleteListener(task -> {
                    if (isFinishing() || isDestroyed()) {
                        Log.w(TAG, "Activity is finishing or destroyed, skipping Firestore callback");
                        return;
                    }

                    Log.d(TAG, "Firestore query completed, success=" + task.isSuccessful());
                    try {
                        if (task.isSuccessful()) {
                            routeTickets.clear();
                            HashSet<String> uniqueEndStations = new HashSet<>();
                            Log.d(TAG, "Query result size: " + task.getResult().size());

                            if (task.getResult().isEmpty()) {

                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String id = document.getId();
                                    String name = document.getString("Name");
                                    String startStationDoc = document.getString("StartStation");
                                    String endStation = document.getString("EndStation");
                                    String type = document.getString("Type");
                                    Object activeObj = document.get("Active"); // Lấy Object để kiểm tra kiểu
                                    Object autoActiveObj = document.get("AutoActive"); // Lấy Object để kiểm tra kiểu

                                    String active = "0";
                                    if (activeObj != null) {
                                        if (activeObj instanceof String) {
                                            active = (String) activeObj;
                                        } else if (activeObj instanceof Number) {
                                            active = String.valueOf(((Number) activeObj).longValue());
                                        } else {
                                            Log.w(TAG, "Unsupported Active type for ticket " + id + ": " + activeObj.getClass().getName() + ", using default 0");
                                        }
                                    }

                                    String autoActive = "0";
                                    if (autoActiveObj != null) {
                                        if (autoActiveObj instanceof String) {
                                            autoActive = (String) autoActiveObj;
                                        } else if (autoActiveObj instanceof Number) {
                                            autoActive = String.valueOf(((Number) autoActiveObj).longValue());
                                        } else {
                                            Log.w(TAG, "Unsupported AutoActive type for ticket " + id + ": " + autoActiveObj.getClass().getName() + ", using default 0");
                                        }
                                    }

                                    String price = "0 VND";
                                    Object priceObj = document.get("Price");
                                    Log.d(TAG, "Raw priceObj for ticket " + id + ": " + priceObj);
                                    if (priceObj != null) {
                                        if (priceObj instanceof String) {
                                            String priceStr = ((String) priceObj).replace(",", "");
                                            if (!priceStr.endsWith("VND")) {
                                                try {
                                                    long priceValue = Long.parseLong(priceStr);
                                                    price = decimalFormat.format(priceValue) + " VND";
                                                } catch (NumberFormatException e) {
                                                    Log.e(TAG, "Error parsing price for ticket " + id + ": " + e.getMessage() + ", using default 0 VND");
                                                    price = "0 VND";
                                                }
                                            } else {
                                                price = priceStr;
                                            }
                                        } else if (priceObj instanceof Long || priceObj instanceof Integer) {
                                            long priceValue = ((Number) priceObj).longValue();
                                            price = decimalFormat.format(priceValue) + " VND";
                                        } else if (priceObj instanceof Double) {
                                            long priceValue = ((Double) priceObj).longValue();
                                            price = decimalFormat.format(priceValue) + " VND";
                                        } else {
                                            Log.w(TAG, "Unsupported price type for ticket " + id + ": " + priceObj.getClass().getName());
                                            price = "0 VND";
                                        }
                                    }
                                    Log.d(TAG, "Formatted price for ticket " + id + ": " + price);

                                    if (name == null) {
                                        Log.w(TAG, "Skipping ticket with null name, id=" + id);
                                        continue;
                                    }

                                    if (endStation != null && uniqueEndStations.add(endStation)) {
                                        TicketType ticket = new TicketType(id, name, price);
                                        ticket.setType(type);
                                        ticket.setStartStation(startStationDoc);
                                        ticket.setEndStation(endStation);
                                        ticket.setActive(active);
                                        ticket.setAutoActive(autoActive);
                                        routeTickets.add(ticket);
                                        Log.d(TAG, "Added ticket: id=" + id + ", name=" + name + ", endStation=" + endStation + ", price=" + price + ", active=" + active + ", autoActive=" + autoActive);
                                    }
                                }
                            }

                            runOnUiThread(() -> {
                                if (!isFinishing() && !isDestroyed()) {
                                    if (routeTicketAdapter != null) {
                                        routeTicketAdapter.submitList(new ArrayList<>(routeTickets));
                                        Log.d(TAG, "Adapter updated with " + routeTickets.size() + " tickets");
                                    }
                                    progressBar.setVisibility(View.GONE);
                                    Log.d(TAG, "ProgressBar hidden");
                                }
                            });
                        } else {
                            runOnUiThread(() -> {
                                if (!isFinishing() && !isDestroyed()) {
                                    Toast.makeText(this, "Lỗi tải dữ liệu: " + (task.getException() != null ? task.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                    Log.d(TAG, "ProgressBar hidden due to query failure");
                                }
                            });
                            Log.e(TAG, "Firestore query failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            if (!isFinishing() && !isDestroyed()) {
                                Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                                Log.d(TAG, "ProgressBar hidden due to exception");
                            }
                        });
                        Log.e(TAG, "Exception in Firestore callback: " + e.getMessage());
                    }
                });
    }
}
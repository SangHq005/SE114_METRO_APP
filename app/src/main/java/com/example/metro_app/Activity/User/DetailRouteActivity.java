package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.List;

public class DetailRouteActivity extends AppCompatActivity {

    private static final String TAG = "DetailRouteActivity";
    private TextView nameRouteTxt;
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
                            Log.d(TAG, "Query result size: " + task.getResult().size());

                            if (task.getResult().isEmpty()) {
                                runOnUiThread(() -> {
                                    if (!isFinishing() && !isDestroyed()) {
                                        Toast.makeText(DetailRouteActivity.this, "Không có vé lượt cho ga này", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                        Log.d(TAG, "ProgressBar hidden due to empty result");
                                    }
                                });
                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String id = document.getId();
                                    String name = document.getString("Name");
                                    String startStationDoc = document.getString("StartStation");
                                    String endStation = document.getString("EndStation");
                                    String type = document.getString("Type");
                                    String active = document.contains("Active") ? String.valueOf(document.get("Active")) : "0";
                                    String autoActive = document.contains("AutoActive") ? String.valueOf(document.get("AutoActive")) : "0";

                                    String price = "0 VND";
                                    Object priceObj = document.get("Price");
                                    if (priceObj != null) {
                                        if (priceObj instanceof String) {
                                            price = (String) priceObj;
                                            if (!price.endsWith("VND")) {
                                                try {
                                                    long priceValue = Long.parseLong(price);
                                                    price = decimalFormat.format(priceValue) + " VND";
                                                } catch (NumberFormatException e) {
                                                    Log.e(TAG, "Error parsing price: " + e.getMessage());
                                                }
                                            }
                                        } else if (priceObj instanceof Long || priceObj instanceof Integer) {
                                            long priceValue = ((Number) priceObj).longValue();
                                            price = decimalFormat.format(priceValue) + " VND";
                                        }
                                    }

                                    if (name == null) {
                                        Log.w(TAG, "Skipping ticket with null name, id=" + id);
                                        continue;
                                    }

                                    TicketType ticket = new TicketType(id, name, price);
                                    ticket.setType(type);
                                    ticket.setStartStation(startStationDoc);
                                    ticket.setEndStation(endStation);
                                    ticket.setActive(active);
                                    ticket.setAutoActive(autoActive);
                                    routeTickets.add(ticket);
                                    Log.d(TAG, "Added ticket: id=" + id + ", name=" + name + ", endStation=" + endStation + ", active=" + active + ", autoActive=" + autoActive);
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
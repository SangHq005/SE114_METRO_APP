package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.Adapter.TicketAdapter;
import com.example.metro_app.Domain.TicketModel;
import com.example.metro_app.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class YourTicketsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTickets;
    private TextView tvNoTickets, hethanTxt;
    private Button btnDangSuDung, btnChuaSuDung;
    private ImageView homeImgBtn;
    private TicketAdapter ticketAdapter;
    private List<TicketModel> ticketList;
    private FirebaseFirestore db;
    private String userId;
    private final Date currentDate = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_your_tickets);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        userId = prefs.getString("UserID", null);
        Log.d("YourTicketsActivity", "Retrieved userId from SharedPreferences: " + userId);

        db = FirebaseFirestore.getInstance();

        recyclerViewTickets = findViewById(R.id.recyclerViewTickets);
        tvNoTickets = findViewById(R.id.tv_no_tickets);
        btnDangSuDung = findViewById(R.id.btn_dang_su_dung);
        btnChuaSuDung = findViewById(R.id.btn_chua_su_dung);
        hethanTxt = findViewById(R.id.hethanTxt);
        homeImgBtn = findViewById(R.id.homeImgBtn);

        recyclerViewTickets.setLayoutManager(new LinearLayoutManager(this));
        ticketList = new ArrayList<>();

        ticketAdapter = new TicketAdapter(ticketList, item -> {}, userId);
        recyclerViewTickets.setAdapter(ticketAdapter);

        btnDangSuDung.setOnClickListener(v -> loadTicketsByStatus("Đang kích hoạt"));
        btnChuaSuDung.setOnClickListener(v -> loadTicketsByStatus("Chưa kích hoạt"));

        hethanTxt.setOnClickListener(v -> {
            startActivity(new Intent(YourTicketsActivity.this, ExpireActivity.class));
        });

        // Sự kiện nhấn homeImgBtn
        homeImgBtn.setOnClickListener(v -> {
            Log.d("YourTicketsActivity", "homeImgBtn clicked");
            startActivity(new Intent(YourTicketsActivity.this, HomeActivity.class));
            finish();
        });

        loadTicketsByStatus("Đang kích hoạt");
    }

    private void loadTicketsByStatus(String status) {
        tvNoTickets.setVisibility(View.GONE);
        ticketList.clear();
        ticketAdapter.notifyDataSetChanged();

        db.collection("Ticket")
                .whereEqualTo("userId", userId)
                .whereEqualTo("Status", status)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            tvNoTickets.setVisibility(View.VISIBLE);
                            ticketAdapter.notifyDataSetChanged();
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String ticketId = document.getId();
                            String ticketTypeId = document.getString("ticketTypeId");
                            String ticketStatus = document.getString("Status");
                            Date autoActiveDate = document.getDate("AutoActiveDate");
                            Date expirationDate = document.getDate("ExpirationDate");
                            Date issueDate = document.getDate("timestamp");
                            String userIdDoc = document.getString("userId");
                            String ticketCode = document.getString("ticketCode");

                            if (!"Hết hạn".equals(ticketStatus) && expirationDate != null && !expirationDate.after(currentDate)) {
                                db.collection("Ticket").document(ticketId)
                                        .update("Status", "Hết hạn")
                                        .addOnSuccessListener(aVoid -> {
                                            loadTicketsByStatus(status);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Lỗi cập nhật trạng thái vé: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                                continue;
                            }

                            if ("Chưa kích hoạt".equals(ticketStatus) && autoActiveDate != null && !autoActiveDate.after(currentDate)) {
                                db.collection("Ticket").document(ticketId)
                                        .update("Status", "Đang kích hoạt")
                                        .addOnSuccessListener(aVoid -> {
                                            loadTicketsByStatus(status);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Lỗi cập nhật trạng thái vé: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                                continue;
                            }

                            db.collection("TicketType").document(ticketTypeId).get()
                                    .addOnCompleteListener(typeTask -> {
                                        if (typeTask.isSuccessful() && typeTask.getResult() != null) {
                                            String ticketName = typeTask.getResult().getString("Name");
                                            String price = typeTask.getResult().get("Price") != null ? typeTask.getResult().get("Price").toString() + " VND" : "0 VND";

                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                            String formattedAutoActiveDate = autoActiveDate != null ? "Tự động kích hoạt vào: " + dateFormat.format(autoActiveDate) : "Tự động kích hoạt vào: N/A";
                                            String formattedExpirationDate = expirationDate != null ? dateFormat.format(expirationDate) : "N/A";

                                            db.collection("Account").document(userIdDoc).get()
                                                    .addOnCompleteListener(userTask -> {
                                                        String userName = "Không có thông tin";
                                                        if (userTask.isSuccessful()) {
                                                            DocumentSnapshot userDoc = userTask.getResult();
                                                            if (userDoc != null && userDoc.exists()) {
                                                                if (userDoc.contains("Name")) {
                                                                    userName = userDoc.getString("Name");
                                                                } else if (userDoc.contains("name")) {
                                                                    userName = userDoc.getString("name");
                                                                } else {
                                                                    Log.e("YourTicketsActivity", "Document exists but 'Name' or 'name' field not found for userId: " + userIdDoc);
                                                                }
                                                            } else {
                                                                Log.e("YourTicketsActivity", "Document does not exist for userId: " + userIdDoc);
                                                            }
                                                        } else {
                                                            Log.e("YourTicketsActivity", "Failed to fetch user name: " + (userTask.getException() != null ? userTask.getException().getMessage() : "Unknown error"));
                                                        }
                                                        Log.d("YourTicketsActivity", "Fetched userName: " + userName);

                                                        TicketModel ticket = new TicketModel(
                                                                ticketId,
                                                                ticketName != null ? ticketName : "Không có thông tin",
                                                                price,
                                                                formattedAutoActiveDate,
                                                                ticketStatus,
                                                                issueDate,
                                                                userName,
                                                                ticketCode,
                                                                formattedExpirationDate,
                                                                userIdDoc
                                                        );
                                                        ticketList.add(ticket);

                                                        ticketAdapter.notifyDataSetChanged();

                                                        tvNoTickets.setVisibility(ticketList.isEmpty() ? View.VISIBLE : View.GONE);
                                                    });
                                        }
                                    });
                        }
                    } else {
                        tvNoTickets.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Lỗi tải dữ liệu vé: " + (task.getException() != null ? task.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
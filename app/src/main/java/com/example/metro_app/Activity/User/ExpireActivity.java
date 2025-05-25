package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExpireActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTickets;
    private TextView tvNoTickets;
    private TicketAdapter ticketAdapter;
    private List<TicketModel> ticketList;
    private FirebaseFirestore db;
    private String userUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_your_tickets); // Tái sử dụng layout của YourTicketsActivity

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("UUID")) {
            userUUID = intent.getStringExtra("UUID");
            System.out.println("Received UUID in ExpireActivity: " + userUUID);
        }

        db = FirebaseFirestore.getInstance();

        recyclerViewTickets = findViewById(R.id.recyclerViewTickets);
        tvNoTickets = findViewById(R.id.tv_no_tickets);

        recyclerViewTickets.setLayoutManager(new LinearLayoutManager(this));
        ticketList = new ArrayList<>();

        ticketAdapter = new TicketAdapter(ticketList, item -> {}, userUUID);
        recyclerViewTickets.setAdapter(ticketAdapter);

        // Ẩn các nút không cần thiết
        findViewById(R.id.buttonToggleGroup).setVisibility(View.GONE);
        findViewById(R.id.hethanTxt).setVisibility(View.GONE);

        // Đặt tiêu đề
        TextView hetHanTv = findViewById(R.id.hetHanTv);
        hetHanTv.setText("Vé Hết Hạn");

        loadExpiredTickets();
    }

    private void loadExpiredTickets() {
        tvNoTickets.setVisibility(View.GONE);
        ticketList.clear();
        ticketAdapter.notifyDataSetChanged();

        db.collection("Ticket")
                .whereEqualTo("userId", userUUID)
                .whereEqualTo("Status", "Hết hạn")
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
                            Date issueDate = document.getDate("timestamp");
                            String userId = document.getString("userId");
                            String ticketCode = document.getString("ticketCode");

                            db.collection("TicketType").document(ticketTypeId).get()
                                    .addOnCompleteListener(typeTask -> {
                                        if (typeTask.isSuccessful() && typeTask.getResult() != null) {
                                            String ticketName = typeTask.getResult().getString("Name");
                                            String price = typeTask.getResult().get("Price") != null ? typeTask.getResult().get("Price").toString() + " VND" : "0 VND";

                                            db.collection("users").document(userId).get()
                                                    .addOnCompleteListener(userTask -> {
                                                        String userName = "Không có thông tin";
                                                        if (userTask.isSuccessful() && userTask.getResult() != null) {
                                                            userName = userTask.getResult().getString("name") != null ? userTask.getResult().getString("name") : "Không có thông tin";
                                                        }

                                                        TicketModel ticket = new TicketModel(
                                                                ticketId,
                                                                ticketName != null ? ticketName : "Không có thông tin",
                                                                price,
                                                                "", // Bỏ qua dòng "Tự động kích hoạt..."
                                                                ticketStatus,
                                                                issueDate,
                                                                userName,
                                                                ticketCode,
                                                                "",
                                                                ""
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
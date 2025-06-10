package com.example.metro_app.Activity.User;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
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
import com.google.android.material.button.MaterialButton;
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
    private MaterialButton btnDangSuDung, btnChuaSuDung;
    private ImageView homeImgBtn;
    private TicketAdapter ticketAdapter;
    private List<TicketModel> ticketList;
    private FirebaseFirestore db;
    private String userId;
    private final Date currentDate = new Date();

    // Biến để theo dõi trạng thái button
    private String currentStatus = "Đang kích hoạt";

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

        initViews();
        setupToggleButtons();

        // Thiết lập trạng thái mặc định
        updateButtonState(btnDangSuDung, btnChuaSuDung);
        loadTicketsByStatus("Đang kích hoạt");
    }

    private void initViews() {
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

        // Sự kiện nhấn hethanTxt
        hethanTxt.setOnClickListener(v -> {
            startActivity(new Intent(YourTicketsActivity.this, ExpireActivity.class));
        });

        // Sự kiện nhấn homeImgBtn
        homeImgBtn.setOnClickListener(v -> {
            Log.d("YourTicketsActivity", "homeImgBtn clicked");
            startActivity(new Intent(YourTicketsActivity.this, HomeActivity.class));
            finish();
        });
    }

    private void setupToggleButtons() {
        btnDangSuDung.setOnClickListener(v -> {
            if (!currentStatus.equals("Đang kích hoạt")) {
                currentStatus = "Đang kích hoạt";
                updateButtonState(btnDangSuDung, btnChuaSuDung);
                loadTicketsByStatus("Đang kích hoạt");
            }
        });

        btnChuaSuDung.setOnClickListener(v -> {
            if (!currentStatus.equals("Chưa kích hoạt")) {
                currentStatus = "Chưa kích hoạt";
                updateButtonState(btnChuaSuDung, btnDangSuDung);
                loadTicketsByStatus("Chưa kích hoạt");
            }
        });
    }

    private void updateButtonState(MaterialButton selectedButton, MaterialButton unselectedButton) {
        // Thay đổi trạng thái selected
        selectedButton.setSelected(true);
        unselectedButton.setSelected(false);

        // Thêm animation
        animateButtonSelection(selectedButton, true);
        animateButtonSelection(unselectedButton, false);

        // Thêm haptic feedback (rung nhẹ)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            selectedButton.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE);
        }
    }

    private void animateButtonSelection(MaterialButton button, boolean isSelected) {
        // Animation scale - button được chọn sẽ nhỏ lại một chút để tạo hiệu ứng "pressed"
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", isSelected ? 0.95f : 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", isSelected ? 0.95f : 1f);

        // Animation elevation - button được chọn sẽ có shadow
        ObjectAnimator elevation = ObjectAnimator.ofFloat(button, "elevation",
                isSelected ? 8f : 0f);

        // Chạy animation
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, elevation);
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new FastOutSlowInInterpolator());
        animatorSet.start();
    }

    private void loadTicketsByStatus(String status) {
        // Hiển thị loading state
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

                            // Kiểm tra và cập nhật trạng thái vé hết hạn
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

                            // Kiểm tra và cập nhật trạng thái vé tự động kích hoạt
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

                            // Lấy thông tin loại vé
                            db.collection("TicketType").document(ticketTypeId).get()
                                    .addOnCompleteListener(typeTask -> {
                                        if (typeTask.isSuccessful() && typeTask.getResult() != null) {
                                            String ticketName = typeTask.getResult().getString("Name");
                                            String price = typeTask.getResult().get("Price") != null ?
                                                    typeTask.getResult().get("Price").toString() + " VND" : "0 VND";

                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                            String formattedAutoActiveDate = autoActiveDate != null ?
                                                    "Tự động kích hoạt vào: " + dateFormat.format(autoActiveDate) : "Tự động kích hoạt vào: N/A";
                                            String formattedExpirationDate = expirationDate != null ?
                                                    dateFormat.format(expirationDate) : "N/A";

                                            // Lấy thông tin người dùng
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
                                                            Log.e("YourTicketsActivity", "Failed to fetch user name: " +
                                                                    (userTask.getException() != null ? userTask.getException().getMessage() : "Unknown error"));
                                                        }
                                                        Log.d("YourTicketsActivity", "Fetched userName: " + userName);

                                                        // Tạo đối tượng TicketModel
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

                                                        // Cập nhật UI
                                                        ticketAdapter.notifyDataSetChanged();
                                                        tvNoTickets.setVisibility(ticketList.isEmpty() ? View.VISIBLE : View.GONE);
                                                    });
                                        }
                                    });
                        }
                    } else {
                        tvNoTickets.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Lỗi tải dữ liệu vé: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Không xác định"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
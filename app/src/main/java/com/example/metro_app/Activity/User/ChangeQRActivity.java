package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.metro_app.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChangeQRActivity extends AppCompatActivity {

    private EditText nhapMaVeEdt;
    private Button checkBtn;
    private ImageView backBtn;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_qr);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        userId = prefs.getString("UserID", null);
        if (userId == null) {
            Toast.makeText(this, "Không nhận được userId!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        // Ánh xạ view
        nhapMaVeEdt = findViewById(R.id.nhapMaVeEdt);
        checkBtn = findViewById(R.id.checkBtn);
        backBtn = findViewById(R.id.backBtn);

        // Thiết lập sự kiện nhấn nút Kiểm tra
        checkBtn.setOnClickListener(v -> checkTicketCode());

        // Thiết lập sự kiện nhấn nút Back
        backBtn.setOnClickListener(v -> finish());
    }

    private void checkTicketCode() {
        String ticketCode = nhapMaVeEdt.getText().toString().trim();
        if (ticketCode.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã vé!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra mã vé trong Firestore
        db.collection("Ticket")
                .whereEqualTo("ticketCode", ticketCode)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(this, "Mã vé không tồn tại!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.contains("userId") && document.getString("userId") != null) {
                                Toast.makeText(this, "Mã vé đã được sử dụng!", Toast.LENGTH_LONG).show();
                                return;
                            }

                            // Mã vé hợp lệ, chuyển sang CreateQRActivity
                            Intent intent = new Intent(ChangeQRActivity.this, CreateQRActivity.class);
                            intent.putExtra("ticketId", document.getId());
                            intent.putExtra("ticketType", document.getString("ticketTypeId"));
                            intent.putExtra("status", document.getString("Status"));
                            intent.putExtra("ticketCode", ticketCode);
                            // Lấy thêm các thông tin khác nếu cần
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            Date autoActiveDate = document.getDate("AutoActiveDate");
                            Date expirationDate = document.getDate("ExpirationDate");
                            Date issueDate = document.getDate("timestamp");
                            String formattedAutoActiveDate = autoActiveDate != null ? "Tự động kích hoạt vào: " + dateFormat.format(autoActiveDate) : "Tự động kích hoạt vào: N/A";
                            String formattedExpirationDate = expirationDate != null ? dateFormat.format(expirationDate) : "N/A";
                            intent.putExtra("expireDate", formattedAutoActiveDate);
                            intent.putExtra("issueDate", issueDate != null ? issueDate.getTime() : 0L);
                            intent.putExtra("expirationDate", formattedExpirationDate);
                            intent.putExtra("source", "ChangeQRActivity");
                            startActivity(intent);
                            finish();
                            return;
                        }
                    } else {
                        Toast.makeText(this, "Lỗi kiểm tra mã vé: " + (task.getException() != null ? task.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
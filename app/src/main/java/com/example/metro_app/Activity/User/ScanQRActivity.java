package com.example.metro_app.Activity.User;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.metro_app.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanQRActivity extends AppCompatActivity {

    private static final String TAG = "ScanQRActivity";
    private static final int CAMERA_PERMISSION_CODE = 100;
    private DecoratedBarcodeView barcodeView;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan_qr);


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
        barcodeView = findViewById(R.id.barcode_scanner);

        // Kiểm tra quyền CAMERA
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            initializeScanner();
        }
    }

    private void initializeScanner() {
        BarcodeCallback callback = new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null) {
                    String ticketCode = result.getText();
                    Log.d(TAG, "Scanned ticketCode: " + ticketCode);
                    processTicketCode(ticketCode);
                    barcodeView.pause(); // Tạm dừng quét
                }
            }

            @Override
            public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) {
                // Không cần xử lý
            }
        };
        barcodeView.decodeContinuous(callback);
    }

    private void processTicketCode(String ticketCode) {
        if (ticketCode == null || ticketCode.isEmpty()) {
            Toast.makeText(this, "Mã QR không hợp lệ!", Toast.LENGTH_LONG).show();
            resumeScanning();
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
                            resumeScanning();
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.contains("userId") && document.getString("userId") != null) {
                                Toast.makeText(this, "Vé đã được sử dụng!", Toast.LENGTH_LONG).show();
                                resumeScanning();
                                return;
                            }

                            // Thêm userId vào document nếu chưa có
                            db.collection("Ticket")
                                    .document(document.getId())
                                    .update("userId", userId)
                                    .addOnSuccessListener(aVoid -> {
                                        // Lấy ticketTypeId để truy vấn Name trong TicketType\
                                        String ticketTypeId = document.getString("ticketTypeId");
                                        if (ticketTypeId != null) {
                                            db.collection("TicketType")
                                                    .document(ticketTypeId)
                                                    .get()
                                                    .addOnCompleteListener(typeTask -> {
                                                        if (typeTask.isSuccessful()) {
                                                            String ticketTypeName = typeTask.getResult().getString("Name");
                                                            if (ticketTypeName == null) ticketTypeName = "Không xác định";

                                                            // Lấy username từ SharedPreferences
                                                            SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
                                                            String username = prefs.getString("name", "Không xác định");

                                                            // Chuyển sang CreateQRActivity
                                                            Intent intent = new Intent(ScanQRActivity.this, CreateQRActivity.class);
                                                            intent.putExtra("ticketId", document.getId());
                                                            intent.putExtra("ticketType", ticketTypeName); // Sử dụng Name thay vì ticketTypeId
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
                                                            intent.putExtra("source", "ScanQRActivity");
                                                            intent.putExtra("userName", username); // Thêm username
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(this, "Lỗi tải loại vé: " + (typeTask.getException() != null ? typeTask.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
                                                            resumeScanning();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(this, "Không tìm thấy ticketTypeId!", Toast.LENGTH_LONG).show();
                                            resumeScanning();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Lỗi cập nhật userId: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        Log.e(TAG, "Error updating userId: " + e.getMessage());
                                        resumeScanning();
                                    });
                            return;
                        }
                    } else {
                        Toast.makeText(this, "Lỗi kiểm tra mã vé: " + (task.getException() != null ? task.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
                        resumeScanning();
                    }
                });
    }

    private void resumeScanning() {
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                initializeScanner();
            } else {
                Toast.makeText(this, "Quyền camera bị từ chối!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barcodeView != null) {
        }
    }
}
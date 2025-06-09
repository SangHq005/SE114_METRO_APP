package com.example.metro_app.Activity.Admin;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.metro_app.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.Calendar;
import java.util.Date;

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        userId = prefs.getString("UserID", null);
        Log.d(TAG, "Retrieved userId from SharedPreferences: " + userId);

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
                    String ticketId = result.getText();
                    Log.d(TAG, "Scanned ticketId: " + ticketId);
                    processTicketId(ticketId);
                    // Tạm dừng quét để tránh quét lại ngay lập tức
                    barcodeView.pause();
                }
            }

            @Override
            public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) {
                // Không cần xử lý
            }
        };
        barcodeView.decodeContinuous(callback);
    }

    private void processTicketId(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) {
            Toast.makeText(this, "Mã QR không hợp lệ!", Toast.LENGTH_LONG).show();
            resumeScanning();
            return;
        }

        // Kiểm tra vé trong Firestore với document ID = ticketId
        db.collection("Ticket")
                .document(ticketId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        var document = task.getResult();
                        if (!document.exists()) {
                            Toast.makeText(this, "Vé không tồn tại!", Toast.LENGTH_LONG).show();
                            resumeScanning();
                            return;
                        }

                        String status = document.getString("Status");
                        Log.d(TAG, "Ticket status: " + status);

                        if ("Đang kích hoạt".equals(status)) {
                            Toast.makeText(this, "Vé hợp lệ", Toast.LENGTH_LONG).show();
                            resumeScanning();
                        } else if ("Chưa kích hoạt".equals(status)) {
                            // Lấy ticketTypeId để truy vấn Expiration
                            String ticketTypeId = document.getString("ticketTypeId");
                            Log.d(TAG, "TicketTypeId: " + ticketTypeId);

                            if (ticketTypeId == null) {
                                Toast.makeText(this, "Lỗi: Không tìm thấy ticketTypeId!", Toast.LENGTH_LONG).show();
                                resumeScanning();
                                return;
                            }

                            // Truy vấn TicketType để lấy Expiration
                            db.collection("TicketType")
                                    .document(ticketTypeId)
                                    .get()
                                    .addOnCompleteListener(typeTask -> {
                                        if (typeTask.isSuccessful()) {
                                            var typeDocument = typeTask.getResult();
                                            if (!typeDocument.exists()) {
                                                Toast.makeText(this, "Lỗi: Không tìm thấy thông tin loại vé!", Toast.LENGTH_LONG).show();
                                                resumeScanning();
                                                return;
                                            }

                                            Object expirationObj = typeDocument.get("Active");
                                            long expirationDays = 0;
                                            if (expirationObj instanceof Number) {
                                                expirationDays = ((Number) expirationObj).longValue();
                                            } else if (expirationObj instanceof String) {
                                                try {
                                                    expirationDays = Long.parseLong((String) expirationObj);
                                                } catch (NumberFormatException e) {
                                                    Log.e(TAG, "Error parsing Expiration: " + e.getMessage());
                                                }
                                            }
                                            Log.d(TAG, "Expiration: " + expirationDays + " days");

                                            // Tính ExpirationDate
                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTime(new Date());
                                            calendar.add(Calendar.DAY_OF_YEAR, (int) expirationDays);
                                            Date newExpirationDate = calendar.getTime();
                                            Log.d(TAG, "New ExpirationDate: " + newExpirationDate);

                                            // Cập nhật Status và ExpirationDate
                                            db.collection("Ticket")
                                                    .document(ticketId)
                                                    .update(
                                                            "Status", "Đang kích hoạt",
                                                            "ExpirationDate", newExpirationDate
                                                    )
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(this, "Vé hợp lệ", Toast.LENGTH_LONG).show();
                                                        Log.d(TAG, "Updated ticket status to Đang kích hoạt and ExpirationDate to " + newExpirationDate);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Lỗi cập nhật vé: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                        Log.e(TAG, "Error updating ticket status: " + e.getMessage());
                                                    });
                                        } else {
                                            Toast.makeText(this, "Lỗi truy vấn loại vé: " + (typeTask.getException() != null ? typeTask.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
                                            Log.e(TAG, "Error querying TicketType: " + (typeTask.getException() != null ? typeTask.getException().getMessage() : "Unknown error"));
                                        }
                                        resumeScanning();
                                    });
                        } else if ("Hết hạn".equals(status)) {
                            Toast.makeText(this, "Vé đã hết hạn", Toast.LENGTH_LONG).show();
                            resumeScanning();
                        } else {
                            Toast.makeText(this, "Trạng thái vé không xác định: " + status, Toast.LENGTH_LONG).show();
                            resumeScanning();
                        }
                    } else {
                        Toast.makeText(this, "Lỗi kiểm tra vé: " + (task.getException() != null ? task.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error querying ticket: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
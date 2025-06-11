package com.example.metro_app.Activity.Admin;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.metro_app.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ScanQRActivity extends AppCompatActivity {

    // --- Giữ nguyên các biến từ file cũ ---
    private static final String TAG = "ScanQRActivity";
    private static final int CAMERA_PERMISSION_CODE = 100;
    private DecoratedBarcodeView barcodeView;
    private FirebaseFirestore db;
    private String userId;

    // --- Thêm các biến cho UI mới ---
    private ImageButton btnBack;
    private View scannerLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bỏ EdgeToEdge vì layout mới đã xử lý việc này
        setContentView(R.layout.activity_scan_qr);

        // --- Giữ nguyên logic khởi tạo từ file cũ ---
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        userId = prefs.getString("UserID", null);
        Log.d(TAG, "Retrieved userId from SharedPreferences: " + userId);

        db = FirebaseFirestore.getInstance();

        // --- Ánh xạ các view từ layout mới ---
        barcodeView = findViewById(R.id.barcode_scanner);
        scannerLine = findViewById(R.id.scanner_line);

        // --- Thêm logic cho các thành phần UI mới ---
        // 1. Xử lý nút quay lại
        btnBack.setOnClickListener(v -> {
            onBackPressed(); // Hoặc finish();
        });

        // 2. Bắt đầu animation cho đường quét
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.scanner_animation);
        scannerLine.startAnimation(animation);

        // --- Giữ nguyên logic kiểm tra quyền camera ---
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
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
                if (result != null && result.getText() != null) {
                    // Tạm dừng quét để tránh quét lại ngay lập tức
                    barcodeView.pause();
                    String ticketId = result.getText();
                    Log.d(TAG, "Scanned ticketId: " + ticketId);
                    processTicketId(ticketId);
                }
            }

            @Override
            public void possibleResultPoints(@NonNull List<com.google.zxing.ResultPoint> resultPoints) {
                // Không cần xử lý
            }
        };
        barcodeView.decodeContinuous(callback);
    }

    // --- PHƯƠNG THỨC NÀY ĐƯỢC GIỮ NGUYÊN HOÀN TOÀN ---
    private void processTicketId(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) {
            Toast.makeText(this, "Mã QR không hợp lệ!", Toast.LENGTH_LONG).show();
            resumeScanningAfterDelay();
            return;
        }

        db.collection("Ticket")
                .document(ticketId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        var document = task.getResult();
                        if (!document.exists()) {
                            Toast.makeText(this, "Vé không tồn tại!", Toast.LENGTH_LONG).show();
                            resumeScanningAfterDelay();
                            return;
                        }

                        String status = document.getString("Status");
                        Log.d(TAG, "Ticket status: " + status);

                        if ("Đang kích hoạt".equals(status)) {
                            Toast.makeText(this, "Vé hợp lệ", Toast.LENGTH_LONG).show();
                            resumeScanningAfterDelay();
                        } else if ("Chưa kích hoạt".equals(status)) {
                            String ticketTypeId = document.getString("ticketTypeId");
                            Log.d(TAG, "TicketTypeId: " + ticketTypeId);

                            if (ticketTypeId == null) {
                                Toast.makeText(this, "Lỗi: Không tìm thấy ticketTypeId!", Toast.LENGTH_LONG).show();
                                resumeScanningAfterDelay();
                                return;
                            }

                            db.collection("TicketType")
                                    .document(ticketTypeId)
                                    .get()
                                    .addOnCompleteListener(typeTask -> {
                                        if (typeTask.isSuccessful()) {
                                            var typeDocument = typeTask.getResult();
                                            if (!typeDocument.exists()) {
                                                Toast.makeText(this, "Lỗi: Không tìm thấy thông tin loại vé!", Toast.LENGTH_LONG).show();
                                                resumeScanningAfterDelay();
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

                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTime(new Date());
                                            calendar.add(Calendar.DAY_OF_YEAR, (int) expirationDays);
                                            Date newExpirationDate = calendar.getTime();
                                            Log.d(TAG, "New ExpirationDate: " + newExpirationDate);

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
                                        resumeScanningAfterDelay();
                                    });
                        } else if ("Hết hạn".equals(status)) {
                            Toast.makeText(this, "Vé đã hết hạn", Toast.LENGTH_LONG).show();
                            resumeScanningAfterDelay();
                        } else {
                            Toast.makeText(this, "Trạng thái vé không xác định: " + status, Toast.LENGTH_LONG).show();
                            resumeScanningAfterDelay();
                        }
                    } else {
                        Toast.makeText(this, "Lỗi kiểm tra vé: " + (task.getException() != null ? task.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error querying ticket: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        resumeScanningAfterDelay();
                    }
                });
    }

    /**
     * Cải tiến: Cho phép quét lại sau một khoảng trễ ngắn (ví dụ: 2 giây)
     * để người dùng có thời gian đọc thông báo.
     */
    private void resumeScanningAfterDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (barcodeView != null) {
                barcodeView.resume();
            }
        }, 2000); // 2000 milliseconds = 2 seconds
    }

    // --- PHẦN QUẢN LÝ QUYỀN VÀ VÒNG ĐỜI ACTIVITY ĐƯỢC GIỮ NGUYÊN ---
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        // Hiển thị lại và chạy animation khi quay lại màn hình
        if (scannerLine != null) {
            scannerLine.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.scanner_animation);
            scannerLine.startAnimation(animation);
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
        // Dừng animation để tiết kiệm tài nguyên
        if (scannerLine != null) {
            scannerLine.clearAnimation();
            scannerLine.setVisibility(View.GONE);
        }
    }

    // Phương thức onDestroy được giữ nguyên, không cần thay đổi
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
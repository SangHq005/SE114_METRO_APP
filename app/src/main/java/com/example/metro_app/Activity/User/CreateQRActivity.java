package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateQRActivity extends AppCompatActivity {

    private MaterialButtonToggleGroup toggleButtonGroup;
    private Button trangChuBtn;
    private View qrCodeContainer;
    private View infoContainer;
    private ImageView qrImg;
    private TextView typeTicketTxt, loaiVeTxt, hanSuDungTxt, ngayPhatHanhTxt, tenTxt, maVeTxt;
    private CountDownTimer countDownTimer;
    private final long REFRESH_INTERVAL = 30000; // 30 giây
    private final long COUNTDOWN_INTERVAL = 1000; // 1 giây
    private String ticketId;
    private String userId;
    private String initialStatus;
    private String source;
    private FirebaseFirestore db;
    private ListenerRegistration qrScanListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_qrcode);

        db = FirebaseFirestore.getInstance();

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        userId = prefs.getString("UserID", null);
        Log.d("CreateQRActivity", "Retrieved userId from SharedPreferences: " + userId);

        // Ánh xạ view
        toggleButtonGroup = findViewById(R.id.toggleButtonGroup);
        qrCodeContainer = findViewById(R.id.qrCodeContainer);
        infoContainer = findViewById(R.id.infoContainer);
        qrImg = findViewById(R.id.qrImg);
        typeTicketTxt = findViewById(R.id.typeTicketTxt);
        loaiVeTxt = findViewById(R.id.loaiVeTxt);
        hanSuDungTxt = findViewById(R.id.hanSuDungTxt);
        ngayPhatHanhTxt = findViewById(R.id.ngayPhatHanhTxt);
        tenTxt = findViewById(R.id.tenTxt);
        maVeTxt = findViewById(R.id.maVeTxt);
        trangChuBtn = findViewById(R.id.trangChuBtn);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            ticketId = intent.getStringExtra("ticketId");
            String ticketType = intent.getStringExtra("ticketType");
            String expireDate = intent.getStringExtra("expireDate");
            initialStatus = intent.getStringExtra("status");
            long issueDateMillis = intent.getLongExtra("issueDate", 0L);
            String userName = intent.getStringExtra("userName");
            String ticketCode = intent.getStringExtra("ticketCode");
            source = intent.getStringExtra("source");

            // Log giá trị userName và source để kiểm tra
            Log.d("CreateQRActivity", "Received userName: " + userName);
            Log.d("CreateQRActivity", "Received source: " + source);

            // Cập nhật TextView trong infoContainer
            typeTicketTxt.setText(ticketType != null ? ticketType : "Không có thông tin");
            loaiVeTxt.setText(ticketType != null ? ticketType : "Không có thông tin");
            hanSuDungTxt.setText(expireDate != null ? expireDate : "Không có thông tin");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            ngayPhatHanhTxt.setText(issueDateMillis != 0 ? dateFormat.format(new Date(issueDateMillis)) : "Không có thông tin");
            tenTxt.setText(userName != null ? userName : "Không có thông tin");
            maVeTxt.setText(ticketCode != null ? ticketCode : "Không có thông tin");

            // Tạo mã QR
            generateQRCode(ticketId);
        } else {
            Log.e("CreateQRActivity", "Intent is null");
            Toast.makeText(this, "Không nhận được dữ liệu vé!", Toast.LENGTH_LONG).show();
        }

        // Thiết lập toggle button
        toggleButtonGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.qrBtn) {
                    showQrCode();
                } else if (checkedId == R.id.infoBtn) {
                    showTicketInfo();
                }
            }
        });

        // Thiết lập sự kiện nhấn nút trangChuBtn
        trangChuBtn.setOnClickListener(v -> {
            if (ticketId == null) {
                Toast.makeText(this, "Không thể xử lý: Thiếu thông tin vé!", Toast.LENGTH_LONG).show();
                return;
            }

            if ("YourTicketsActivity".equals(source)) {
                // Nếu nguồn là YourTicketsActivity, chỉ chuyển lại YourTicketsActivity
                startActivity(new Intent(CreateQRActivity.this, YourTicketsActivity.class));
                finish();
            } else if ("ChangeQRActivity".equals(source)) {
                // Nếu nguồn là ChangeQRActivity, kiểm tra userId và cập nhật trước khi chuyển
                if (userId == null) {
                    Toast.makeText(this, "Không thể xử lý: Thiếu thông tin userId!", Toast.LENGTH_LONG).show();
                    return;
                }
                DocumentReference ticketRef = db.collection("Ticket").document(ticketId);
                ticketRef.update("userId", userId)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Vé đã được thêm vào tài khoản!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(CreateQRActivity.this, YourTicketsActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi cập nhật vé: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                // Trường hợp không xác định được source, chuyển lại YourTicketsActivity
                startActivity(new Intent(CreateQRActivity.this, YourTicketsActivity.class));
                finish();
            }
        });

        // Mặc định hiển thị QR code
        ((MaterialButton) findViewById(R.id.qrBtn)).setChecked(true);
        showQrCode();

        // Lắng nghe sự kiện quét QR
        setupQrScanListener();
    }

    private void showQrCode() {
        qrCodeContainer.setVisibility(View.VISIBLE);
        infoContainer.setVisibility(View.GONE);
        startCountdown();
    }

    private void showTicketInfo() {
        qrCodeContainer.setVisibility(View.GONE);
        infoContainer.setVisibility(View.VISIBLE);
        stopCountdown();
    }

    private void startCountdown() {
        stopCountdown();

        countDownTimer = new CountDownTimer(REFRESH_INTERVAL, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                ((TextView) findViewById(R.id.tvRefreshTimer)).setText(seconds + "s");
            }

            @Override
            public void onFinish() {
                refreshQrCode();
                startCountdown();
            }
        }.start();
    }

    private void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void refreshQrCode() {
        generateQRCode(ticketId);
    }

    private void generateQRCode(String ticketId) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    ticketId,
                    BarcodeFormat.QR_CODE,
                    250,
                    250
            );
            Bitmap bitmap = Bitmap.createBitmap(250, 250, Bitmap.Config.RGB_565);
            for (int x = 0; x < 250; x++) {
                for (int y = 0; y < 250; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            qrImg.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo mã QR: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupQrScanListener() {
        if (ticketId == null) return;

        DocumentReference ticketRef = db.collection("Ticket").document(ticketId);
        qrScanListener = ticketRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Toast.makeText(CreateQRActivity.this, "Lỗi lắng nghe QR: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                String status = snapshot.getString("Status");
                if ("Đang kích hoạt".equals(status) && !"Đang kích hoạt".equals(initialStatus)) {
                    Toast.makeText(CreateQRActivity.this, "Mã QR đã được quét và vé đã kích hoạt!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                ticketRef.update("Status", "Đang kích hoạt");
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCountdown();
        if (qrScanListener != null) {
            qrScanListener.remove();
        }
    }
}
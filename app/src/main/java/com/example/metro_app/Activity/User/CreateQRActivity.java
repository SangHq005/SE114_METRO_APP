package com.example.metro_app.Activity.User;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class CreateQRActivity extends AppCompatActivity {

    private MaterialButtonToggleGroup toggleButtonGroup;
    private View qrCodeContainer;
    private View infoContainer;
    private CountDownTimer countDownTimer;
    private final long REFRESH_INTERVAL = 30000; // 30 giây
    private final long COUNTDOWN_INTERVAL = 1000; // 1 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_qrcode);

        // Ánh xạ view
        toggleButtonGroup = findViewById(R.id.toggleButtonGroup);
        qrCodeContainer = findViewById(R.id.qrCodeContainer);
        infoContainer = findViewById(R.id.infoContainer);

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

        // Mặc định hiển thị QR code
        ((MaterialButton) findViewById(R.id.qrBtn)).setChecked(true);
        showQrCode();
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
        stopCountdown(); // Dừng bộ đếm cũ nếu có

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
        // TODO: Thêm logic làm mới mã QR ở đây
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCountdown();
    }
}
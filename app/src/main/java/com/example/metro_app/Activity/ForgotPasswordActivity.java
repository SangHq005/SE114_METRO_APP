package com.example.metro_app.Activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.example.metro_app.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ForgotPasswordActivity extends AppCompatActivity {

    private int currentStep = 1;
    private String userEmail;

    private ImageView btnBack;

    private LinearLayout step1View;
    private TextInputEditText etEmail;
    private Button btnSendCode;

    private LinearLayout step2View;
    private PinView pinView;
    private TextView tvResendCode;
    private Button btnVerifyCode;
    private CountDownTimer countDownTimer;

    private LinearLayout step3View;
    private TextInputEditText etNewPassword, etConfirmNewPassword;
    private Button btnResetPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnForgotPasswordBack);
        step1View = findViewById(R.id.step1_email_view);
        etEmail = findViewById(R.id.etForgotPasswordEmail);
        btnSendCode = findViewById(R.id.btnSendCode);

        step2View = findViewById(R.id.step2_passcode_view);
        pinView = findViewById(R.id.pinViewForgot);
        tvResendCode = findViewById(R.id.tvResendCodeForgot);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);

        step3View = findViewById(R.id.step3_reset_password_view);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        setupListeners();
        updateUiForStep(currentStep);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                updateUiForStep(currentStep);
            }
        });

        btnSendCode.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Vui lòng nhập email hợp lệ.");
                return;
            }
            userEmail = email;

            // TODO: Gửi mã OTP/link reset đến email ở đây
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Hướng dẫn đặt lại mật khẩu đã được gửi đến email của bạn", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Không thể gửi email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
//            currentStep = 2;
//            updateUiForStep(currentStep);
        });

        btnVerifyCode.setOnClickListener(v -> {
            String otp = pinView.getText().toString();
            if (otp.length() < 6) {
                Toast.makeText(this, "Vui lòng nhập đủ 6 số.", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Xác thực mã OTP ở đây
            Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();

            currentStep = 3;
            updateUiForStep(currentStep);
        });

        tvResendCode.setOnClickListener(v -> {
            // TODO: Gửi lại mã OTP
            Toast.makeText(this, "Đã gửi lại mã.", Toast.LENGTH_SHORT).show();
            startCountdownTimer();
        });

        btnResetPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString();
            String confirmPassword = etConfirmNewPassword.getText().toString();

            if (newPassword.length() < 6) {
                etNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự.");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                etConfirmNewPassword.setError("Mật khẩu xác nhận không khớp.");
                return;
            }

            // TODO: Cập nhật mật khẩu mới cho user
            Toast.makeText(this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updateUiForStep(int step) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        btnBack.setVisibility(step > 1 ? View.VISIBLE : View.GONE);

        step1View.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        step2View.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        step3View.setVisibility(step == 3 ? View.VISIBLE : View.GONE);

        if(step == 2) {
            startCountdownTimer();
        }
    }

    private void startCountdownTimer() {
        tvResendCode.setEnabled(false);
        long duration = TimeUnit.SECONDS.toMillis(60);

        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvResendCode.setText(String.format(Locale.ENGLISH, "Gửi lại sau (%ds)",
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)));
            }

            @Override
            public void onFinish() {
                tvResendCode.setEnabled(true);
                tvResendCode.setText("Gửi lại mã");
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
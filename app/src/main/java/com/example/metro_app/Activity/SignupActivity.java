package com.example.metro_app.Activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.chaos.view.PinView;
import com.example.metro_app.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etFullName,etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvLoginNow;

    private ConstraintLayout passcodeView;
    private PinView pinView;
    private TextView tvResendCode;
    private Button btnVerify;

    private CountDownTimer countDownTimer;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        etFullName = findViewById(R.id.etSignupFullName);
        etEmail = findViewById(R.id.etSignupEmail);
        etPhone = findViewById(R.id.etSignupPhone);
        etPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etSignupConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvLoginNow = findViewById(R.id.tvLoginNow);

        passcodeView = findViewById(R.id.passcodeView);
        pinView = findViewById(R.id.pinView);
        tvResendCode = findViewById(R.id.tvResendCode);
        btnVerify = findViewById(R.id.btnVerify);

        setupListeners();
    }

    private void setupListeners() {
        btnSignup.setOnClickListener(v -> {
            validateAndProceed();
        });

        btnVerify.setOnClickListener(v -> {
            String otp = pinView.getText().toString();
            verifyOtp(otp);
        });

        tvResendCode.setOnClickListener(v -> {
            // Logic gửi lại mã OTP
            Toast.makeText(this, "Đã gửi lại mã...", Toast.LENGTH_SHORT).show();
            startCountdownTimer();
        });

        tvLoginNow.setOnClickListener(v -> {
            finish();
        });
    }

    private void validateAndProceed() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Vui lòng nhập họ và tên");
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Gửi email xác minh
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(verifyTask -> {
                                    if (verifyTask.isSuccessful()) {
                                        saveUserToFirestore(email, phone, String.valueOf(etFullName.getText()));
                                        Toast.makeText(this,
                                                "Đăng ký thành công. Vui lòng xác minh email trước khi đăng nhập.",
                                                Toast.LENGTH_LONG).show();
                                        mAuth.signOut(); // Bắt buộc đăng xuất ngay sau khi đăng ký
                                        finish(); // Quay về màn hình đăng nhập
                                    } else {
                                        Toast.makeText(this,
                                                "Không thể gửi email xác minh: " + verifyTask.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this,
                                "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void showPasscodeView() {
        passcodeView.setVisibility(View.VISIBLE);
        startCountdownTimer();
    }

    private void verifyOtp(String otp) {
        if (otp.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 chữ số", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Xử lý logic xác thực OTP với server hoặc Firebase

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignupActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SignupActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void saveUserToFirestore(String email, String phone,String name) {
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("Email", email);
        data.put("PhoneNumber", phone);
        data.put("Role", "User");
        data.put("Name", name);
        data.put("CCCD", "");
        data.put("avatarUrl", "");
        data.put("firstTimeLogin", com.google.firebase.Timestamp.now());

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Account")
                .document(uid)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    // Optional log
                    android.util.Log.d("SignupActivity", "Thông tin người dùng đã được lưu.");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lưu thông tin lên Firestore thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void startCountdownTimer() {
        tvResendCode.setEnabled(false);
        long duration = TimeUnit.SECONDS.toMillis(60);

        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String sDuration = String.format(Locale.ENGLISH, "Gửi lại sau (%d)",
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished));
                tvResendCode.setText(sDuration);
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
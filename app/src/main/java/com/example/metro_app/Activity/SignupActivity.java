package com.example.metro_app.Activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.chaos.view.PinView;
import com.example.metro_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SignupActivity extends AppCompatActivity {

    // Khai báo các thành phần UI
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvLoginNow;

    // Các thành phần cho màn hình OTP (hiện tại không dùng trong luồng chính)
    private ConstraintLayout passcodeView;
    private PinView pinView;
    private TextView tvResendCode;
    private Button btnVerify;

    private CountDownTimer countDownTimer;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Khởi tạo Firebase Auth và Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ View
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

        // Thiết lập sự kiện click
        setupListeners();
    }

    private void setupListeners() {
        btnSignup.setOnClickListener(v -> validateAndProceed());

        btnVerify.setOnClickListener(v -> {
            String otp = pinView.getText().toString();
            verifyOtp(otp); // Logic này là một luồng riêng, không liên quan đến đăng ký chính
        });

        tvResendCode.setOnClickListener(v -> {
            Toast.makeText(this, "Đã gửi lại mã...", Toast.LENGTH_SHORT).show();
            startCountdownTimer();
        });

        tvLoginNow.setOnClickListener(v -> {
            finish(); // Quay lại màn hình trước đó (LoginActivity)
        });
    }

    /**
     * Kiểm tra tính hợp lệ của thông tin nhập vào và tiến hành đăng ký.
     * Luồng xử lý được cấu trúc lại để đảm bảo các bước diễn ra tuần tự.
     */
    private void validateAndProceed() {
        // Lấy dữ liệu từ các ô nhập liệu
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // --- Bắt đầu kiểm tra dữ liệu đầu vào ---
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Vui lòng nhập họ và tên");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return;
        }
        // --- Kết thúc kiểm tra ---

        // BƯỚC 1: TẠO TÀI KHOẢN VỚI EMAIL VÀ PASSWORD
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            Toast.makeText(this, "Không thể lấy thông tin người dùng vừa tạo.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // BƯỚC 2: CẬP NHẬT TÊN HIỂN THỊ (DISPLAYNAME) CHO TÀI KHOẢN AUTH
                        // Đây là bước quan trọng nhất để sửa lỗi
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful()) {
                                        // BƯỚC 3: GỬI EMAIL XÁC MINH
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(verifyTask -> {
                                                    if (verifyTask.isSuccessful()) {
                                                        // BƯỚC 4: LƯU TOÀN BỘ THÔNG TIN VÀO FIRESTORE
                                                        saveUserToFirestore(user.getUid(), email, phone, fullName);
                                                        Toast.makeText(this,
                                                                "Đăng ký thành công. Vui lòng xác minh email của bạn.",
                                                                Toast.LENGTH_LONG).show();
                                                        mAuth.signOut(); // Đăng xuất để yêu cầu đăng nhập lại
                                                        finish(); // Quay về màn hình đăng nhập
                                                    } else {
                                                        Toast.makeText(this,
                                                                "Không thể gửi email xác minh: " + verifyTask.getException().getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        // Nếu cập nhật profile thất bại, vẫn thông báo nhưng có thể cho phép tiếp tục
                                        Toast.makeText(this, "Lỗi cập nhật tên người dùng.", Toast.LENGTH_SHORT).show();
                                        // Vẫn tiến hành lưu vào Firestore như một phương án dự phòng
                                        saveUserToFirestore(user.getUid(), email, phone, fullName);
                                        mAuth.signOut();
                                        finish();
                                    }
                                });
                    } else {
                        // Xử lý các lỗi đăng ký phổ biến
                        Toast.makeText(this,
                                "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }


    /**
     * Lưu thông tin người dùng vào collection "Account" trong Firestore.
     * @param uid ID người dùng từ Firebase Authentication
     * @param email Email người dùng
     * @param phone Số điện thoại người dùng
     * @param name Tên đầy đủ của người dùng
     */
    private void saveUserToFirestore(String uid, String email, String phone, String name) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("Name", name);
        userData.put("Email", email);
        userData.put("PhoneNumber", phone);
        userData.put("Role", "User"); // Mặc định vai trò là "User"
        userData.put("CCCD", ""); // Khởi tạo rỗng
        userData.put("avatarUrl", ""); // Khởi tạo rỗng
        userData.put("firstTimeLogin", com.google.firebase.Timestamp.now());

        db.collection("Account")
                .document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("SignupActivity", "Thông tin người dùng đã được lưu vào Firestore thành công.");
                })
                .addOnFailureListener(e -> {
                    // Ghi lại lỗi nếu không thể lưu vào Firestore
                    android.util.Log.e("SignupActivity", "Lỗi lưu vào Firestore: ", e);
                    Toast.makeText(this, "Lỗi khi lưu dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
                });
    }

    // Các phương thức cho luồng OTP (giữ nguyên)
    private void showPasscodeView() {
        passcodeView.setVisibility(View.VISIBLE);
        startCountdownTimer();
    }

    private void verifyOtp(String otp) {
        if (otp.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 chữ số", Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO: Xử lý logic xác thực OTP thực tế ở đây
        Toast.makeText(this, "Đang xác thực OTP...", Toast.LENGTH_SHORT).show();
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
        // Hủy CountDownTimer để tránh rò rỉ bộ nhớ
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
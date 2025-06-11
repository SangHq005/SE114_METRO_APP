package com.example.metro_app.Activity.Admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.metro_app.Activity.LoginActivity;
import com.example.metro_app.Model.FireStoreHelper;
import com.example.metro_app.Model.UserModel;
import com.example.metro_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail;
    private EditText etProfileCccd;
    private ImageView profileImage;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private FireStoreHelper fireStoreHelper;
    private UserModel currentUserModel;
    private String originalCccd = ""; // Lưu lại CCCD ban đầu để so sánh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        initViews();
        mAuth = FirebaseAuth.getInstance();
        fireStoreHelper = new FireStoreHelper();

        setupListeners();
        setupBottomNavigation();
        loadUserProfile();
        etProfileCccd.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                autoSaveChanges(); // Khi mất focus
            }
        });

    }

    private void initViews() {
        profileImage = findViewById(R.id.profile_image);
        tvProfileName = findViewById(R.id.tv_profile_name);
        tvProfileEmail = findViewById(R.id.tv_profile_email);
        etProfileCccd = findViewById(R.id.et_profile_cccd);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void loadUserProfile() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Không tìm thấy người dùng.", Toast.LENGTH_LONG).show();
            goToLogin();
            return;
        }

        fireStoreHelper.getUserById(firebaseUser.getUid(), new FireStoreHelper.Callback<UserModel>() {
            @Override
            public void onSuccess(UserModel result) {
                if (result != null) {
                    currentUserModel = result;
                    updateUI();
                } else {
                    Toast.makeText(AdProfileActivity.this, "Không thể tải thông tin hồ sơ.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdProfileActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (currentUserModel == null) return;

        tvProfileName.setText(currentUserModel.getName());
        tvProfileEmail.setText(currentUserModel.getEmail());
        String avatarUrl = null;
        try {
            avatarUrl = currentUserModel.getClass().getMethod("getAvatarUrl") != null ? (String) currentUserModel.getClass().getMethod("getAvatarUrl").invoke(currentUserModel) : null;
        } catch (Exception e) {
            // Method does not exist or error, ignore
        }
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.userbtn) // fallback image
                    .error(R.drawable.userbtn)
                    .circleCrop()
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.userbtn);
        }

        // Lưu lại CCCD gốc và hiển thị
        originalCccd = currentUserModel.getCCCD() != null ? currentUserModel.getCCCD() : "";
        if (!originalCccd.isEmpty()) {
            etProfileCccd.setText(originalCccd);
        } else {
            etProfileCccd.setText("");
            etProfileCccd.setHint("Chưa cập nhật (12 số)");
        }
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Nhấn "Enter" trên bàn phím sẽ gọi autoSaveChanges()
        etProfileCccd.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {

                autoSaveChanges();
                return true; // Đã xử lý
            }
            return false;
        });

        // Optional: nếu vẫn muốn chạm ngoài để đóng bàn phím
        View rootLayout = findViewById(android.R.id.content);
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboard();
            }
            return false;
        });
    }



    // Hàm tự động lưu thay đổi
    private void autoSaveChanges() {
        hideKeyboard();
        String newCccd = etProfileCccd.getText().toString().trim();
        Log.d("CCCD_DEBUG", "autoSaveChanges() called");
        Log.d("CCCD_DEBUG", "newCccd = " + newCccd + ", original = " + originalCccd);
        // **So sánh với giá trị gốc.** Nếu không có gì thay đổi thì không làm gì cả.
        if (newCccd.equals(originalCccd)) {
            Log.d("CCCD_DEBUG", "CCCD không thay đổi, không cần cập nhật.");
            etProfileCccd.clearFocus(); // Clear focus if no changes
            return;
        }

        // **Kiểm tra dữ liệu hợp lệ.** CCCD phải là 12 chữ số.
        if (!newCccd.matches("\\d{12}")) {
            Toast.makeText(this, "CCCD không hợp lệ. Phải là 12 chữ số.", Toast.LENGTH_LONG).show();
            // Quay lại giá trị cũ để tránh lưu sai
            etProfileCccd.setText(originalCccd);
            etProfileCccd.clearFocus(); // Clear focus on invalid input
            return;
        }

        // Nếu dữ liệu hợp lệ và có thay đổi -> Bắt đầu lưu
        Toast.makeText(this, "Đang cập nhật CCCD...", Toast.LENGTH_SHORT).show();

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;

        fireStoreHelper.updateUserCccd(firebaseUser.getUid(), newCccd, new FireStoreHelper.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(AdProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                originalCccd = newCccd; // Cập nhật lại giá trị gốc sau khi lưu thành công
                etProfileCccd.clearFocus(); // Clear focus after successful save
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdProfileActivity.this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Nếu thất bại, trả lại giá trị CCCD cũ
                etProfileCccd.setText(originalCccd);
                etProfileCccd.clearFocus(); // Clear focus on failure
            }
        });
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Xác nhận Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    mAuth.signOut();
                    goToLogin();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void goToLogin() {
        Intent intent = new Intent(AdProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_ad_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ad_home) {
                startActivity(new Intent(getApplicationContext(), AdHomeActivity.class));
            } else if (id == R.id.nav_ad_userlist) {
                startActivity(new Intent(getApplicationContext(), AdUserActivity.class));
            } else if (id == R.id.nav_ad_profile) {
                return true;
            }

            if (id != R.id.nav_ad_profile) {
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            // Nếu là EditText đang focus → clear focus
            if (view instanceof EditText) {
                view.clearFocus(); // ← rất quan trọng để trigger autoSaveChanges
            }
        }
    }

}
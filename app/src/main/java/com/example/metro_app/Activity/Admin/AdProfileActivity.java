package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.example.metro_app.Activity.LoginActivity;
import com.example.metro_app.Model.UserModel;
import com.example.metro_app.R;
import com.example.metro_app.utils.FireStoreHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView tvProfileName;
    private AppCompatButton btnManageRoutes;
    private AppCompatButton btnLogout; // Thêm nút đăng xuất

    private FirebaseAuth mAuth;
    private FireStoreHelper fireStoreHelper;
    private UserModel currentUserModel;

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
    }

    private void initViews() {
        profileImage = findViewById(R.id.profile_image);
        tvProfileName = findViewById(R.id.tv_profile_name);
        btnManageRoutes = findViewById(R.id.btn_manage_routes);
        btnLogout = findViewById(R.id.btn_logout); // Ánh xạ nút đăng xuất
    }

    private void setupListeners() {
        // Sự kiện click cho nút quản lý tuyến đường
        btnManageRoutes.setOnClickListener(v -> {
            Intent intent = new Intent(AdProfileActivity.this, AdAddWayActivity.class);
            startActivity(intent);
        });

        // Sự kiện click cho nút đăng xuất
        btnLogout.setOnClickListener(v -> {
            showLogoutConfirmationDialog(); // Gọi hộp thoại xác nhận
        });
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

        String avatarUrl = null;
        try {
            avatarUrl = currentUserModel.getClass().getMethod("getAvatarUrl") != null ? (String) currentUserModel.getClass().getMethod("getAvatarUrl").invoke(currentUserModel) : null;
        } catch (Exception e) {
            // Method does not exist or error, ignore
        }
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.userbtn)
                    .error(R.drawable.userbtn)
                    .circleCrop()
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.userbtn);
        }
    }

    // Hiển thị hộp thoại xác nhận đăng xuất
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
            if (id == R.id.nav_ad_profile) {
                return true;
            } else if (id == R.id.nav_ad_route) {
                startActivity(new Intent(AdProfileActivity.this, AdRouteActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_ad_wallet) {
                startActivity(new Intent(AdProfileActivity.this, AdTicketActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_ad_userlist) {
                startActivity(new Intent(AdProfileActivity.this, AdUserActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_ad_home) {
                startActivity(new Intent(AdProfileActivity.this, AdHomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.metro_app.Activity.LoginActivity;
import com.example.metro_app.Model.UserModel;
import com.example.metro_app.R;
import com.example.metro_app.utils.FireStoreHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton; // THAY ĐỔI 1: Import MaterialButton
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.Method;

public class AdDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdDashboardActivity";

    // UI Components
    private ImageView profileImage;
    private TextView tvAdminName;

    // Management Cards
    private CardView cardManageRoutes;
    private CardView cardManageNews;

    // Logout Button
    private MaterialButton btnLogout;
    private BottomNavigationView bottomNavigationView;

    // Firebase
    private FirebaseAuth mAuth;
    private FireStoreHelper fireStoreHelper;
    private UserModel currentUserModel;

    // Handler for UI operations
    private Handler mainHandler;
    private boolean isActivityDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        try {
            setContentView(R.layout.activity_admin_dashboard);
            Log.d(TAG, "Layout set successfully");

            mainHandler = new Handler(Looper.getMainLooper());

            if (initViews()) {
                Log.d(TAG, "Views initialized successfully");
                initFirebase();
                setupListeners();
                setupBottomNavigation();

                // Delay load profile để tránh block UI thread
                mainHandler.postDelayed(this::loadUserProfile, 200);
            } else {
                Log.e(TAG, "Failed to initialize views");
                showErrorAndFinish("Lỗi khởi tạo giao diện");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorAndFinish("Lỗi khởi tạo ứng dụng: " + e.getMessage());
        }
    }

    private boolean initViews() {
        try {
            // Profile components
            profileImage = findViewById(R.id.profile_image);
            tvAdminName = findViewById(R.id.tv_admin_name);

            // Management cards
            cardManageRoutes = findViewById(R.id.card_manage_routes);
            cardManageNews = findViewById(R.id.card_manage_news);

            // Logout button
            // THAY ĐỔI 3: Việc gán giá trị bây giờ đã chính xác vì kiểu dữ liệu đã khớp
            btnLogout = findViewById(R.id.btn_logout);
            bottomNavigationView = findViewById(R.id.bottomNavigationView);

            // Set default values
            tvAdminName.setText("Admin");
            profileImage.setImageResource(R.drawable.userbtn);

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            return false;
        }
    }

    private void initFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            fireStoreHelper = new FireStoreHelper();
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
        }
    }

    private void setupListeners() {
        try {
            // Bus Stops Management
            if (cardManageRoutes != null) {
                cardManageRoutes.setOnClickListener(v -> {
                    safeNavigate(AdAddWayActivity.class);
                });
            }

            // News Management
            if (cardManageNews != null) {
                cardManageNews.setOnClickListener(v -> {
                    safeNavigate(AdNewsListActivity.class); // Assuming you'll create this
                });
            }

            // Logout
            // THAY ĐỔI 4: Không cần thay đổi ở đây, setOnClickListener hoạt động tốt trên MaterialButton
            if (btnLogout != null) {
                btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
            }

            Log.d(TAG, "Listeners setup successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners: " + e.getMessage(), e);
        }
    }

    private void safeNavigate(Class<?> activityClass) {
        try {
            if (!isActivityDestroyed && !isFinishing()) {
                Intent intent = new Intent(AdDashboardActivity.this, activityClass);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to " + activityClass.getSimpleName() + ": " + e.getMessage());
            Toast.makeText(this, "Lỗi chuyển trang", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        if (isActivityDestroyed || isFinishing()) {
            return;
        }

        try {
            FirebaseUser firebaseUser = mAuth != null ? mAuth.getCurrentUser() : null;
            if (firebaseUser == null) {
                Log.w(TAG, "No current user found");
                runOnUiThread(() -> {
                    if (!isActivityDestroyed) {
                        Toast.makeText(this, "Không tìm thấy người dùng.", Toast.LENGTH_LONG).show();
                        goToLogin();
                    }
                });
                return;
            }

            // Show loading state
            runOnUiThread(() -> {
                if (tvAdminName != null && !isActivityDestroyed) {
                    tvAdminName.setText("Đang tải...");
                }
            });

            if (fireStoreHelper != null) {
                fireStoreHelper.getUserById(firebaseUser.getUid(), new FireStoreHelper.Callback<UserModel>() {
                    @Override
                    public void onSuccess(UserModel result) {
                        if (isActivityDestroyed || isFinishing()) {
                            return;
                        }

                        mainHandler.post(() -> {
                            try {
                                if (result != null) {
                                    currentUserModel = result;
                                    updateUI();
                                } else {
                                    Toast.makeText(AdDashboardActivity.this, "Không thể tải thông tin hồ sơ.", Toast.LENGTH_SHORT).show();
                                    if (tvAdminName != null) {
                                        tvAdminName.setText("Admin");
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error in onSuccess: " + e.getMessage(), e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (isActivityDestroyed || isFinishing()) {
                            return;
                        }

                        Log.e(TAG, "Failed to load user profile: " + e.getMessage(), e);
                        mainHandler.post(() -> {
                            try {
                                Toast.makeText(AdDashboardActivity.this, "Lỗi tải hồ sơ", Toast.LENGTH_SHORT).show();
                                if (tvAdminName != null) {
                                    tvAdminName.setText("Admin");
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Error in onFailure: " + ex.getMessage(), ex);
                            }
                        });
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserProfile: " + e.getMessage(), e);
            runOnUiThread(() -> {
                if (tvAdminName != null && !isActivityDestroyed) {
                    tvAdminName.setText("Admin");
                }
            });
        }
    }

    private void updateUI() {
        if (isActivityDestroyed || currentUserModel == null) {
            return;
        }

        try {
            // Update admin name
            String adminName = currentUserModel.getName();
            if (adminName != null && !adminName.isEmpty()) {
                if (tvAdminName != null) {
                    tvAdminName.setText(adminName);
                }
            } else {
                if (tvAdminName != null) {
                    tvAdminName.setText("Admin");
                }
            }

            // Update profile image in background thread
            new Thread(() -> {
                try {
                    String avatarUrl = getAvatarUrl();

                    mainHandler.post(() -> {
                        if (!isActivityDestroyed && profileImage != null) {
                            updateProfileImage(avatarUrl);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error updating profile image: " + e.getMessage(), e);
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "Error in updateUI: " + e.getMessage(), e);
        }
    }

    private void updateProfileImage(String avatarUrl) {
        try {
            if (avatarUrl != null && !avatarUrl.isEmpty() && !isActivityDestroyed) {
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.userbtn)
                        .error(R.drawable.userbtn)
                        .circleCrop()
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.userbtn);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile image: " + e.getMessage(), e);
            if (profileImage != null) {
                profileImage.setImageResource(R.drawable.userbtn);
            }
        }
    }

    private String getAvatarUrl() {
        if (currentUserModel == null) {
            return null;
        }

        try {
            Method method = currentUserModel.getClass().getMethod("getAvatarUrl");
            Object result = method.invoke(currentUserModel);
            return result != null ? (String) result : null;
        } catch (Exception e) {
            Log.d(TAG, "getAvatarUrl method not found or error: " + e.getMessage());
            return null;
        }
    }

    private void showLogoutConfirmationDialog() {
        if (isActivityDestroyed || isFinishing()) {
            return;
        }

        try {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống quản trị?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        performLogout();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setCancelable(true)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing logout dialog: " + e.getMessage(), e);
        }
    }

    private void performLogout() {
        try {
            if (mAuth != null) {
                mAuth.signOut();
            }
            Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
            goToLogin();
        } catch (Exception e) {
            Log.e(TAG, "Error during logout: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi đăng xuất", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToLogin() {
        try {
            Intent intent = new Intent(AdDashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error going to login: " + e.getMessage(), e);
            finish();
        }
    }

    private void setupBottomNavigation() {
        try {
            if (bottomNavigationView == null) {
                Log.e(TAG, "BottomNavigationView is null");
                return;
            }

            bottomNavigationView.setSelectedItemId(R.id.nav_ad_profile);

            bottomNavigationView.setOnItemSelectedListener(item -> {
                try {
                    if (isActivityDestroyed || isFinishing()) {
                        return false;
                    }

                    int id = item.getItemId();

                    if (id == R.id.nav_ad_profile) {
                        return true;
                    } else if (id == R.id.nav_ad_home) {
                        safeNavigate(AdHomeActivity.class);
                        return true;
                    } else if (id == R.id.nav_ad_route) {
                        safeNavigate(AdRouteActivity.class);
                        return true;
                    } else if (id == R.id.nav_ad_wallet) {
                        safeNavigate(AdTicketActivity.class);
                        return true;
                    } else if (id == R.id.nav_ad_userlist) {
                        safeNavigate(AdUserActivity.class);
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    Log.e(TAG, "Error in bottom navigation: " + e.getMessage(), e);
                    return false;
                }
            });

            Log.d(TAG, "Bottom navigation setup successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation: " + e.getMessage(), e);
        }
    }

    private void showErrorAndFinish(String message) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error showing error message: " + e.getMessage(), e);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        try {
            if (mAuth != null && mAuth.getCurrentUser() != null && !isActivityDestroyed) {
                // Delay để tránh block UI
                mainHandler.postDelayed(() -> {
                    if (!isActivityDestroyed) {
                        loadUserProfile();
                    }
                }, 300);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityDestroyed = true;

        try {
            if (mainHandler != null) {
                mainHandler.removeCallbacksAndMessages(null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
        }

        Log.d(TAG, "onDestroy called");
    }

    @Override
    public void onBackPressed() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Thoát ứng dụng")
                    .setMessage("Bạn có muốn thoát khỏi ứng dụng quản trị?")
                    .setPositiveButton("Thoát", (dialog, which) -> {
                        finishAffinity(); // Thoát hoàn toàn ứng dụng
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error in onBackPressed: " + e.getMessage(), e);
            super.onBackPressed();
        }
    }
}
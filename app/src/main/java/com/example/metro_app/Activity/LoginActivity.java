package com.example.metro_app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.Activity.Admin.AdHomeActivity;
import com.example.metro_app.Activity.User.HomeActivity;
import com.example.metro_app.Model.UserModel;
import com.example.metro_app.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int RC_SIGN_IN = 9001;

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout layoutEmail, layoutPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegisterNow;
    private View googleSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegisterNow = findViewById(R.id.tvRegisterNow);
        googleSignInButton = findViewById(R.id.Google);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        setupListeners();
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginWithEmailPassword());

        tvRegisterNow.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
    }

    private void loginWithEmailPassword() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Vui lòng nhập email hợp lệ");
            return;
        } else {
            layoutEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            layoutPassword.setError("Vui lòng nhập mật khẩu");
            return;
        } else {
            layoutPassword.setError(null);
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show();
                        onLoginSuccess(user);
                    } else {
                        Toast.makeText(LoginActivity.this, "Sai email hoặc mật khẩu.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        onLoginSuccess(user);
                    } else {
                        Toast.makeText(LoginActivity.this, "Xác thực thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onLoginSuccess(FirebaseUser user) {
        if (user == null) return;

        if (!user.isEmailVerified()) {
            Toast.makeText(this, "Vui lòng xác minh địa chỉ email trước khi đăng nhập.", Toast.LENGTH_LONG).show();

            // Gợi ý gửi lại email xác minh
            user.sendEmailVerification()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã gửi email xác minh.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Gửi email xác minh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            mAuth.signOut(); // đăng xuất khỏi phiên
            return;
        }

        // Đã xác minh email
        String uid = user.getUid();
        db.collection("Account").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        handleExistingUser(user, documentSnapshot);
                    } else {
                        handleNewUser(user);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
                });
    }


    private void handleExistingUser(FirebaseUser user, DocumentSnapshot documentSnapshot) {
        String uid = user.getUid();
        String role = documentSnapshot.getString("Role");

        Map<String, Object> updates = new HashMap<>();
        updates.put("Name", user.getDisplayName());
        updates.put("Email", user.getEmail());
        if (user.getPhotoUrl() != null) {
            updates.put("avatarUrl", user.getPhotoUrl().toString());
        }

        db.collection("Account").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    UserModel userModel = new UserModel();
                    userModel.setUid(uid);
                    userModel.setName(user.getDisplayName());
                    userModel.setEmail(user.getEmail());
                    userModel.setAvatarUrl(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                    userModel.setPhoneNumber(documentSnapshot.getString("PhoneNumber"));
                    userModel.setCCCD(documentSnapshot.getString("CCCD"));
                    userModel.setRole(role);

                    saveUserInfo(userModel);
                    navigateToHome(role, uid);
                })
                .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Lỗi cập nhật thông tin.", Toast.LENGTH_SHORT).show());
    }

    private void handleNewUser(FirebaseUser user) {
        String uid = user.getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("Name", user.getDisplayName());
        data.put("Email", user.getEmail());
        data.put("Role", "User");
        data.put("PhoneNumber", "");
        data.put("CCCD", "");
        data.put("avatarUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        data.put("firstTimeLogin", com.google.firebase.Timestamp.now());

        db.collection("Account").document(uid).set(data)
                .addOnSuccessListener(aVoid -> {
                    UserModel userModel = new UserModel();
                    userModel.setUid(uid);
                    userModel.setName(user.getDisplayName());
                    userModel.setEmail(user.getEmail());
                    userModel.setAvatarUrl(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                    userModel.setPhoneNumber("");
                    userModel.setCCCD("");
                    userModel.setRole("User");

                    saveUserInfo(userModel);
                    navigateToHome("User", uid);
                })
                .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Lỗi tạo tài khoản.", Toast.LENGTH_SHORT).show());
    }

    private void navigateToHome(String role, String uid) {
        if ("Admin".equals(role)) {
            startActivity(new Intent(LoginActivity.this, AdHomeActivity.class));
        } else {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("UUID", uid);
            startActivity(intent);
        }
        finish();
    }

    private void saveUserInfo(UserModel user) {
        if (user != null) {
            SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("UserID", user.getUid());
            editor.putString("name", user.getName());
            editor.putString("email", user.getEmail());
            editor.putString("photo", user.getAvatarUrl());
            editor.putString("phoneNumber", user.getPhoneNumber());
            editor.putString("CCCD", user.getCCCD());
            editor.putString("role", user.getRole());

            editor.apply();
        }
    }
}
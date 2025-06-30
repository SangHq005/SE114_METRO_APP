package com.example.metro_app.Activity.User;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.metro_app.Activity.LoginActivity;
import com.example.metro_app.R;
import com.example.metro_app.databinding.ActivityInfoAcitivityBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class InfoAcitivity extends AppCompatActivity {

    ActivityInfoAcitivityBinding binding;
    ImageView backbtn;
    Button btnLogout;
    LinearLayout PN, CCCDButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityInfoAcitivityBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        backbtn = findViewById(R.id.backBtn);
        btnLogout = findViewById(R.id.btnLogout);
        PN = findViewById(R.id.PNButton);
        CCCDButton = findViewById(R.id.CCCDButton);

        backbtn.setOnClickListener(v -> startActivity(new Intent(InfoAcitivity.this, HomeActivity.class)));

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        SharedPreferences.Editor editor = getSharedPreferences("UserInfo", MODE_PRIVATE).edit();
                        editor.clear();
                        editor.apply();
                        startActivity(new Intent(this, LoginActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        CCCDButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
            String currentCCCD = prefs.getString("CCCD", "");
            showCustomInputDialog("Nhập CCCD mới", currentCCCD, this::saveCCCDToFirestoreAndPrefs);
        });

        PN.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
            String currentPN = prefs.getString("phoneNumber", "");
            showCustomInputDialog("Nhập số điện thoại mới", currentPN, this::savePhoneNumberToFirestoreAndPrefs);
        });

        loadUserInfo();
        setupTextAnimation();
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String name = prefs.getString("name", "");
        String email = prefs.getString("email", "");
        String CCCD = prefs.getString("CCCD", "Chưa cập nhật");
        String photoUrl = prefs.getString("photo", "");
        String phone = prefs.getString("phoneNumber", "Chưa cập nhật");

        binding.nameTxt.setText(name);
        binding.userName.setText(name);
        binding.tvEmail.setText(email);
        binding.nationalIdTxt.setText(CCCD);
        binding.tvPN.setText(phone);

        if (!photoUrl.isEmpty()) {
            Glide.with(this).load(photoUrl).into(binding.profileImage);
        }
    }

    private void showCustomInputDialog(String title, String currentValue, OnConfirmListener listener) {
        View view = getLayoutInflater().inflate(R.layout.dialog_input_field, null);
        EditText etInput = view.findViewById(R.id.etInput);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        tvTitle.setText(title);
        etInput.setText(currentValue);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String input = etInput.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show();
            } else {
                listener.onConfirm(input);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void saveCCCDToFirestoreAndPrefs(String CCCD) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        SharedPreferences.Editor editor = getSharedPreferences("UserInfo", MODE_PRIVATE).edit();
        editor.putString("CCCD", CCCD);
        editor.apply();

        FirebaseFirestore.getInstance().collection("Account")
                .document(user.getUid())
                .set(Map.of("CCCD", CCCD), SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Lưu CCCD thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        loadUserInfo();
    }

    private void savePhoneNumberToFirestoreAndPrefs(String phone) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        SharedPreferences.Editor editor = getSharedPreferences("UserInfo", MODE_PRIVATE).edit();
        editor.putString("phoneNumber", phone);
        editor.apply();

        FirebaseFirestore.getInstance().collection("Account")
                .document(user.getUid())
                .set(Map.of("PhoneNumber", phone), SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Lưu số điện thoại thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        loadUserInfo();
    }

    private interface OnConfirmListener {
        void onConfirm(String input);
    }

    private void setupTextAnimation() {
        Shader shader = new LinearGradient(0, 0, 0, binding.tvGreeting.getTextSize(),
                new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA},
                null, Shader.TileMode.CLAMP);
        binding.tvGreeting.getPaint().setShader(shader);

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1000);
        animator.setDuration(4000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            float translate = (float) animation.getAnimatedValue();
            Shader movingShader = new LinearGradient(
                    translate, 0, translate + binding.tvGreeting.getWidth(), binding.tvGreeting.getTextSize(),
                    new int[]{Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA},
                    null, Shader.TileMode.MIRROR);
            binding.tvGreeting.getPaint().setShader(movingShader);
            binding.tvGreeting.invalidate();
        });
        animator.start();
    }
}

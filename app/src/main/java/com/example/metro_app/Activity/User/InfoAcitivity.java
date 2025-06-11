package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    TextView PN;
    LinearLayout CCCDButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityInfoAcitivityBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        backbtn = findViewById(R.id.backBtn);
        btnLogout =findViewById(R.id.btnLogout);
        PN = findViewById(R.id.tvPN);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(InfoAcitivity.this, HomeActivity.class));
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InfoAcitivity.this, LoginActivity.class));
            }
        });
        CCCDButton=findViewById(R.id.CCCDButton);
        CCCDButton.setOnClickListener(v -> showEditCCCDDialog());
        PN.setOnClickListener(v -> {
            showEditPhoneNumberDialog();
        });
        loadUserInfo();

    }
    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String name = prefs.getString("name", "");
        String email = prefs.getString("email", "");
        String CCCD = prefs.getString("CCCD", "000000000000");
        String photoUrl = prefs.getString("photo", "");
        String PhoneNumber = prefs.getString("phoneNumber","000000000");
        Log.d("SDT", "loadUserInfo: "+ PhoneNumber);
        TextView tvName = findViewById(R.id.nameTxt);
        TextView tvName2 = findViewById(R.id.userName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvCCCD = findViewById(R.id.nationalIdTxt);
        ImageView imgPhoto = findViewById(R.id.profileImage);


        tvName.setText(name);
        tvName2.setText(name);
        tvEmail.setText(email);
        tvCCCD.setText(CCCD);
        PN.setText(PhoneNumber);

        if (!photoUrl.isEmpty()) {
            Glide.with(this).load(photoUrl).into(imgPhoto);
        }
    }
    private void showEditCCCDDialog() {
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String currentCCCD = prefs.getString("CCCD", "");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentCCCD);

        new AlertDialog.Builder(this)
                .setTitle("Nhập CCCD mới")
                .setView(input)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String newCCCD = input.getText().toString().trim();
                    if (newCCCD.isEmpty()) {
                        Toast.makeText(this, "CCCD không được để trống", Toast.LENGTH_SHORT).show();
                    } else {
                        saveCCCDToFirestoreAndPrefs(newCCCD);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void showEditPhoneNumberDialog() {
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String currentPN = prefs.getString("PhoneNumber", "");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentPN);

        new AlertDialog.Builder(this)
                .setTitle("Nhập Sdt mới")
                .setView(input)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String newPN = input.getText().toString().trim();
                    if (newPN.isEmpty()) {
                        Toast.makeText(this, "Sdt không được để trống", Toast.LENGTH_SHORT).show();
                    } else {
                        savePhoneNumberToFirestoreAndPrefs(newPN);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void saveCCCDToFirestoreAndPrefs(String CCCD) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("UserID", user.getUid());
        editor.putString("name", user.getDisplayName());
        editor.putString("email", user.getEmail());
        editor.putString("photo", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        editor.putString("CCCD", CCCD);
        editor.apply();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("CCCD", CCCD);

        db.collection("Account")
                .document(user.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Lưu CCCD thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    loadUserInfo();
    }
    private void savePhoneNumberToFirestoreAndPrefs(String phoneNumber) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("UserID", user.getUid());
        editor.putString("phoneNumber", phoneNumber);
        editor.putString("name", user.getDisplayName());
        editor.putString("email", user.getEmail());
        editor.putString("photo", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        editor.apply();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("PhoneNumber",phoneNumber );

        db.collection("Account")
                .document(user.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Lưu sdt thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        loadUserInfo();
    }




}
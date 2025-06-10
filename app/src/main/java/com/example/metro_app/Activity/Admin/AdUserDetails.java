package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.metro_app.Model.UserModel;
import com.example.metro_app.R;

public class AdUserDetails extends AppCompatActivity {
    private static final String TAG = "AdUserDetails";
    private ImageView editImageUser;
    private EditText editTextFullName, editTextEmail, editTextPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_edit);

        // Initialize views
        editImageUser = findViewById(R.id.editImageUser);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);

        // Get data from Intent
        Intent intent = getIntent();
        UserModel user = (UserModel) intent.getSerializableExtra("user");
        int position = intent.getIntExtra("position", -1);
        Log.d(TAG, "Received user: " + (user != null ? user.getName() : "null") + ", position: " + position);

        if (user != null) {
            editTextFullName.setText(user.getName());
            editTextEmail.setText(user.getEmail());

            if (user.getCCCD() != null && !user.getCCCD().isEmpty()) {
                editTextPhoneNumber.setText(user.getCCCD());
            } else {
                editTextPhoneNumber.setText("Chưa cập nhật");
            }

            String avatarUrl = null;
            try {
                avatarUrl = user.getClass().getMethod("getAvatarUrl") != null ? (String) user.getClass().getMethod("getAvatarUrl").invoke(user) : null;
            } catch (Exception e) {
                // Method does not exist or error, ignore
            }
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.userbtn) // fallback image
                        .error(R.drawable.userbtn)
                        .circleCrop()
                        .into(editImageUser);
            } else {
                editImageUser.setImageResource(R.drawable.userbtn);
            }
        } else {
            Log.e(TAG, "User data is null");
            Toast.makeText(this, "Lỗi: Không nhận được dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Button actions
        findViewById(R.id.button_cancel).setOnClickListener(v -> finish());
    }
}
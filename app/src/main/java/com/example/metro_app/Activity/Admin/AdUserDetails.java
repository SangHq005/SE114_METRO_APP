package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.Domain.UserModel;
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
        Log.d(TAG, "Received user: " + (user != null ? user.getFullName() : "null") + ", position: " + position);

        if (user != null) {
            editTextFullName.setText(user.getFullName());
            editTextEmail.setText(user.getEmail());
            editTextPhoneNumber.setText(user.getPhoneNumber());
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
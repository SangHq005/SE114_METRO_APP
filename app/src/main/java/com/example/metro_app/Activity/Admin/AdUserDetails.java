package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.metro_app.R;
public class AdUserDetails extends AppCompatActivity {
    private EditText editTextId, editTextFullName, editTextEmail, editTextPhoneNumber, editTextCCCD;
    private ImageView editImageUser;
    private TextView textViewHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_edit);

        // Initialize views
        editTextId = findViewById(R.id.editTextId);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextCCCD = findViewById(R.id.editTextCCCD);
        editImageUser = findViewById(R.id.editImageUser);

        // Example: Get user data from an Intent (could be passed from another activity)
        Intent intent = getIntent();
        String userId = intent.getStringExtra("user_id");
        String fullName = intent.getStringExtra("full_name");
        String email = intent.getStringExtra("email");
        String phoneNumber = intent.getStringExtra("phone_number");
        String cccd = intent.getStringExtra("cccd");
        String avatarUrl = intent.getStringExtra("avatar_url");

        // Set data to the views
        editTextId.setText(userId);
        editTextFullName.setText(fullName);
        editTextEmail.setText(email);
        editTextPhoneNumber.setText(phoneNumber);
        editTextCCCD.setText(cccd);

        // Load avatar image using Glide (or any image loading library you prefer)
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)  // URL of the avatar image
                    .into(editImageUser);
        } else {
            // Fallback if avatar URL is empty or null (use default avatar)
            editImageUser.setImageResource(R.drawable.userbtn);
        }

        // Optionally set the header text (e.g., "User Details")
        textViewHeader.setText("CHI TIẾT NGƯỜI DÙNG");
    }
}

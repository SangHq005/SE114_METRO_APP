package com.example.metro_app.Activity.Admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.R;

public class AdUserDetails extends AppCompatActivity {
    private ImageView editImageUser;
    private EditText editTextFullName, editTextEmail, editTextPhoneNumber, editTextCCCD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_edit);

        // Initialize views
        editImageUser = findViewById(R.id.editImageUser);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextCCCD = findViewById(R.id.editTextCCCD);

        // Button actions
        findViewById(R.id.button_cancel).setOnClickListener(v -> finish());

        findViewById(R.id.button_save).setOnClickListener(v -> {
            // Implement saving logic here
            finish();
        });
    }
}
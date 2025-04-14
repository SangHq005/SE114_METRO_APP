package com.example.metro_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText passwordEditText;
    private ImageView togglePasswordVisibility;
    private boolean isPasswordVisible = false;
    private TextView signInText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        passwordEditText = findViewById(R.id.password);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        signInText = findViewById(R.id.SignIn);

        // Xử lý ẩn/hiện mật khẩu
        togglePasswordVisibility.setOnClickListener(view -> {
            if (isPasswordVisible) {
                // Ẩn mật khẩu
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.eyeoff);
                isPasswordVisible = false;
            } else {
                // Hiện mật khẩu
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.eyeopen);
                isPasswordVisible = true;
            }
            // Đưa con trỏ về cuối
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // Chuyển sang LoginActivity khi nhấn "Sign In"
        signInText.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Tùy chọn: đóng RegisterActivity
        });
    }
}

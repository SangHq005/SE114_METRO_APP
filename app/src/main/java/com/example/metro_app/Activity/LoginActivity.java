package com.example.metro_app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.R;

public class LoginActivity extends AppCompatActivity {

    private EditText passwordEditText;
    private ImageView togglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ view
        passwordEditText = findViewById(R.id.password);
        togglePassword = findViewById(R.id.togglePasswordVisibility);
        TextView signUpText = findViewById(R.id.SignUp);

        // Xử lý toggle hiển thị mật khẩu
        togglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Ẩn mật khẩu
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePassword.setImageResource(R.drawable.eyeoff); // Icon ẩn
                } else {
                    // Hiện mật khẩu
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePassword.setImageResource(R.drawable.eyeopen); // Icon hiện
                }
                isPasswordVisible = !isPasswordVisible;
                // Giữ con trỏ ở cuối
                passwordEditText.setSelection(passwordEditText.getText().length());
            }
        });

        // Xử lý điều hướng sang RegisterActivity
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}

package com.example.metro_app.Activity.User;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.metro_app.R;
import com.example.metro_app.databinding.ActivityInfoAcitivityBinding;

public class InfoAcitivity extends AppCompatActivity {
    ActivityInfoAcitivityBinding binding;
    ImageView backbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityInfoAcitivityBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        backbtn = findViewById(R.id.backBtn);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(InfoAcitivity.this, "Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
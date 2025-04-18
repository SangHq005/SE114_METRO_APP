package com.example.metro_app.Activity.User;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.metro_app.Domain.NewsModel;
import com.example.metro_app.R;
import com.example.metro_app.ViewModel.MainViewModel;
import com.example.metro_app.databinding.ActivityDetailNewsBinding;

public class DetailNewsActivity extends AppCompatActivity {
    ActivityDetailNewsBinding binding;
    MainViewModel viewModel;

    NewsModel item;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new MainViewModel();
        item = (NewsModel) getIntent().getSerializableExtra("object");

        setVariable();

    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.header.setText(item.getHeader());
                binding.title.setText(item.getTitle());
                binding.date.setText(item.getDate());
                binding.description.setText(item.getDescription());
                binding.content.setText(item.getContent());

                Glide.with(DetailNewsActivity.this)
                        .load(item.getPic())
                        .into(binding.image);
            }
        });



    }
}
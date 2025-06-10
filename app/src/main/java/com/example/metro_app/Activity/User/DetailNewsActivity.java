package com.example.metro_app.Activity.User;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.metro_app.Domain.NewsModel;
import com.example.metro_app.Domain.PopularModel;
import com.example.metro_app.ViewModel.MainViewModel;
import com.example.metro_app.databinding.ActivityDetailNewsBinding;

public class DetailNewsActivity extends AppCompatActivity {
    ActivityDetailNewsBinding binding;
    MainViewModel viewModel;

    NewsModel newsItem;
    PopularModel popularItem;
    String itemType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new MainViewModel();

        // Nhận dữ liệu từ Intent
        itemType = getIntent().getStringExtra("type");

        if ("popular".equals(itemType)) {
            popularItem = (PopularModel) getIntent().getSerializableExtra("object");
        } else {
            newsItem = (NewsModel) getIntent().getSerializableExtra("object");
        }

        setVariable();
    }

    private void setVariable() {
        if ("popular".equals(itemType) && popularItem != null) {
            displayPopularItem();
        } else if (newsItem != null) {
            displayNewsItem();
        }

//        // Xử lý nút back
//        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void displayPopularItem() {
        binding.title.setText(popularItem.getTitle());
        binding.content.setText(popularItem.getContent()); // Sửa lại nếu dùng `getContent`
        binding.date.setText(popularItem.getDate());

        Glide.with(this)
                .load(popularItem.getPic())
                .into(binding.pic);
    }

    private void displayNewsItem() {
        binding.title.setText(newsItem.getTitle());
        binding.content.setText(newsItem.getDescription());
        binding.date.setText(newsItem.getDate());

        Glide.with(this)
                .load(newsItem.getPic())
                .into(binding.pic);
    }
}

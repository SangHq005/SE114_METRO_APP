package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.metro_app.Adapter.AllNewsAdapter;
import com.example.metro_app.Domain.NewsModel;
import com.example.metro_app.ViewModel.MainViewModel;
import com.example.metro_app.databinding.ActivityAllNewsBinding;

import java.util.ArrayList;

public class AllNewsActivity extends AppCompatActivity {
    ActivityAllNewsBinding binding;
    MainViewModel viewModel;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivityAllNewsBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        viewModel = new MainViewModel();
        initAllNews();

    }
    private void initAllNews() {
        binding.progressbarAllNews.setVisibility(View.VISIBLE);

        // Thiết lập LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        binding.recyclerViewAllNews.setLayoutManager(layoutManager);

        // Quan sát dữ liệu từ ViewModel
        viewModel.loadNews().observe(this, newsModels -> {
            if (newsModels != null && !newsModels.isEmpty()) {
                AllNewsAdapter adapter = new AllNewsAdapter((ArrayList<NewsModel>) newsModels);
                binding.recyclerViewAllNews.setAdapter(adapter);
            } else {
                System.out.println("Danh sách tin tức trống");
            }
            binding.progressbarAllNews.setVisibility(View.GONE);
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AllNewsActivity.this, HomeActivity.class));
            }
        });
    }
}










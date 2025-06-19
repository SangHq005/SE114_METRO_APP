package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.metro_app.Adapter.AllNewsAdapter;
import com.example.metro_app.Domain.NewsModel;
import com.example.metro_app.ViewModel.MainViewModel;
import com.example.metro_app.databinding.ActivityAllNewsBinding;

import java.util.ArrayList;
import java.util.List;

public class AllNewsActivity extends AppCompatActivity {
    ActivityAllNewsBinding binding;
    MainViewModel viewModel;
    private List<NewsModel> allNewsList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivityAllNewsBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        viewModel = new MainViewModel();

        initAllNews();
        setupSearchListener();

        // Nút quay lại
        binding.backBtn.setOnClickListener(v -> {
            startActivity(new Intent(AllNewsActivity.this, HomeActivity.class));
            finish();
        });
    }

    private void initAllNews() {
        binding.progressbarAllNews.setVisibility(View.VISIBLE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerViewAllNews.setLayoutManager(layoutManager);

        viewModel.loadNews().observe(this, newsModels -> {
            binding.progressbarAllNews.setVisibility(View.GONE);
            if (newsModels != null && !newsModels.isEmpty()) {
                allNewsList.clear();
                allNewsList.addAll(newsModels);

                AllNewsAdapter adapter = new AllNewsAdapter((ArrayList<NewsModel>) allNewsList);
                binding.recyclerViewAllNews.setAdapter(adapter);
            }
        });
    }

    private void setupSearchListener() {
        binding.editTextText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNews(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
            }
        });
    }

    private void filterNews(String keyword) {
        ArrayList<NewsModel> filteredList = new ArrayList<>();
        for (NewsModel model : allNewsList) {
            if (model.getTitle() != null && model.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                filteredList.add(model);
            }
        }

        AllNewsAdapter adapter = new AllNewsAdapter(filteredList);
        binding.recyclerViewAllNews.setAdapter(adapter);
    }
}
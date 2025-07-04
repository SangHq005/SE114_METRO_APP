package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.metro_app.Adapter.CategoryAdapter;
import com.example.metro_app.Adapter.NewsAdapter;
import com.example.metro_app.Adapter.PopularAdapter;
import com.example.metro_app.Domain.NewsModel;
import com.example.metro_app.Domain.PopularModel;
import com.example.metro_app.ViewModel.MainViewModel;
import com.example.metro_app.databinding.ActivityHomeBinding;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    ActivityHomeBinding binding;
    MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new MainViewModel();

        initCategory();
        initNews();
        initPopular();
    }

    private void initPopular() {
        // Thiết lập LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerViewPopular.setLayoutManager(layoutManager);

        // Quan sát dữ liệu từ ViewModel
        viewModel.loadPopular().observe(this, popularModels -> {
            if (popularModels != null && !popularModels.isEmpty()) {
                PopularAdapter adapter = new PopularAdapter((ArrayList<PopularModel>) popularModels);
                binding.recyclerViewPopular.setAdapter(adapter);
            }
        });
    }

    private void initNews() {
        // Thiết lập LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerViewNews.setLayoutManager(layoutManager);

        viewModel.loadNews().observe(this, newsModels -> {
            if (newsModels != null && !newsModels.isEmpty()) {
                NewsAdapter adapter = new NewsAdapter((ArrayList<NewsModel>) newsModels, this);
                binding.recyclerViewNews.setAdapter(adapter);
            }
        });
        binding.viewAllNewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AllNewsActivity.class));
            }
        });
    }

    private void initCategory() {
        GridLayoutManager layoutManager = new GridLayoutManager(HomeActivity.this, 4);
        binding.recyclerViewCategory.setLayoutManager(layoutManager);

        viewModel.loadCategory().observe(this, categoryModels -> {
            System.out.println("Số lượng category từ Firebase: " + categoryModels.size());

            ViewGroup.LayoutParams params = binding.recyclerViewCategory.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            binding.recyclerViewCategory.setLayoutParams(params);

            // Đảm bảo hiển thị đầy đủ các item
            binding.recyclerViewCategory.setHasFixedSize(false);
            binding.recyclerViewCategory.setNestedScrollingEnabled(true);

            // Tạo adapter với sự kiện click
            CategoryAdapter adapter = new CategoryAdapter(categoryModels, category -> {
                if ("Mua vé".equals(category.getName())) {
                    startActivity(new Intent(HomeActivity.this, MyTicketsActivity.class));
                }
                if ("Vé của tôi".equals(category.getName())) {
                    startActivity(new Intent(HomeActivity.this, YourTicketsActivity.class));
                }
                if ("Đổi mã".equals(category.getName())) {
                    startActivity(new Intent(HomeActivity.this, ChangeQRActivity.class));
                }
                if ("Bản đồ".equals(category.getName())) {
                    startActivity(new Intent(HomeActivity.this, FindPathActivity.class));
                }
                if ("Hành trình".equals(category.getName())) {
                    startActivity(new Intent(HomeActivity.this, JourneyActivity.class));
                }
                if ("Tài khoản".equals(category.getName())) {
                    startActivity(new Intent(HomeActivity.this, InfoAcitivity.class));
                }
                if ("Diễn đàn".equals(category.getName())) {
                    startActivity(new Intent(HomeActivity.this, ForumActivity.class));
                }
                if ("Lịch sử giao dịch".equals(category.getName())) {
                    startActivity(new Intent(HomeActivity.this, TransactionHistoryActivity.class));
                }
            });
            binding.recyclerViewCategory.setAdapter(adapter);
        });
    }
}
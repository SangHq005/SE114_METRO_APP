package com.example.metro_app.Activity.User;

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
        binding=ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new MainViewModel();
        initCategory();
        initNews();
        initPopular();

    }
    private void initPopular() {
        binding.progressBarPopular.setVisibility(View.VISIBLE);

        // Thiết lập LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        binding.recyclerViewPopular.setLayoutManager(layoutManager);

        // Quan sát dữ liệu từ ViewModel
        viewModel.loadPopular().observe(this, popularModels -> {
            if (popularModels != null && !popularModels.isEmpty()) {
                PopularAdapter adapter = new PopularAdapter((ArrayList<PopularModel>) popularModels);
                binding.recyclerViewPopular.setAdapter(adapter);
            } else {
                System.out.println("Danh sách tin tức trống");
            }
            binding.progressBarPopular.setVisibility(View.GONE);
        });
    }
private void initNews() {
    binding.progressBarNews.setVisibility(View.VISIBLE);

    // Thiết lập LayoutManager
    LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
    binding.recyclerViewNews.setLayoutManager(layoutManager);

    // Quan sát dữ liệu từ ViewModel
    viewModel.loadNews().observe(this, newsModels -> {
        if (newsModels != null && !newsModels.isEmpty()) {
            NewsAdapter adapter = new NewsAdapter((ArrayList<NewsModel>) newsModels);
            binding.recyclerViewNews.setAdapter(adapter);
        } else {
            System.out.println("Danh sách tin tức trống");
        }
        binding.progressBarNews.setVisibility(View.GONE);
    });
}

    private void initCategory() {
        binding.progressBarCategory.setVisibility(View.VISIBLE);

        // Giảm số cột xuống 2 để hiển thị rõ hơn
        GridLayoutManager layoutManager = new GridLayoutManager(HomeActivity.this, 4);
        binding.recyclerViewCategory.setLayoutManager(layoutManager);

        viewModel.loadCategory().observe(this, categoryModels -> {
            // In ra số lượng item để kiểm tra
            System.out.println("Số lượng category từ Firebase: " + categoryModels.size());

            // Đảm bảo RecyclerView có đủ không gian hiển thị
            ViewGroup.LayoutParams params = binding.recyclerViewCategory.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            binding.recyclerViewCategory.setLayoutParams(params);

            // Đảm bảo hiển thị đầy đủ các item
            binding.recyclerViewCategory.setHasFixedSize(false);
            binding.recyclerViewCategory.setNestedScrollingEnabled(true);

            // Tạo adapter và gán vào RecyclerView
            CategoryAdapter adapter = new CategoryAdapter(categoryModels);
            binding.recyclerViewCategory.setAdapter(adapter);
            binding.progressBarCategory.setVisibility(View.GONE);
        });

    }
}
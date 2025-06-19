package com.example.metro_app.Activity.User;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.metro_app.Domain.NewsModel;
import com.example.metro_app.Domain.PopularModel;
import com.example.metro_app.ViewModel.MainViewModel;
import com.example.metro_app.databinding.ActivityDetailNewsBinding;

import java.io.InputStream;
import java.net.URL;

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

        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void displayPopularItem() {
        binding.title.setText(popularItem.getTitle());
        binding.date.setText(popularItem.getDate());

        Html.ImageGetter imageGetter = (source) -> {
            Drawable drawable = null;
            try {
                InputStream is = (InputStream) new URL(source).getContent();
                drawable = Drawable.createFromStream(is, "src");
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return drawable;
        };

        Spanned htmlContent = Html.fromHtml(popularItem.getContent(), Html.FROM_HTML_MODE_COMPACT, imageGetter, null);
        binding.content.setText(htmlContent);

        // Load ảnh đại diện
        Glide.with(this)
                .load(popularItem.getPic())
                .into(binding.pic);
    }

    private void displayNewsItem() {
        binding.title.setText(newsItem.getTitle());
        binding.date.setText(newsItem.getDate());

        // Dùng Html.ImageGetter để load ảnh trong nội dung
        Html.ImageGetter imageGetter = (source) -> {
            Drawable drawable = null;
            try {
                InputStream is = (InputStream) new URL(source).getContent();
                drawable = Drawable.createFromStream(is, "src");
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return drawable;
        };

        Spanned htmlContent = Html.fromHtml(newsItem.getDescription(), Html.FROM_HTML_MODE_COMPACT, imageGetter, null);
        binding.content.setText(htmlContent);

        // Load ảnh đại diện
        Glide.with(this)
                .load(newsItem.getPic())
                .into(binding.pic);
    }
}
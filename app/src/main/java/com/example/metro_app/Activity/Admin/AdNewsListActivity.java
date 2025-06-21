package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.metro_app.Adapter.AdminNewsAdapter;
import com.example.metro_app.Domain.NewsModel;
import com.example.metro_app.databinding.ActivityAdminNewsListBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdNewsListActivity extends AppCompatActivity {
    private ActivityAdminNewsListBinding binding;
    private FirebaseFirestore db;
    private List<NewsModel> newsList = new ArrayList<>();
    private List<NewsModel> filteredNewsList = new ArrayList<>();
    private AdminNewsAdapter adapter;

    private static final int ADD_NEWS_REQUEST = 1;
    private static final int EDIT_NEWS_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminNewsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        setupSearchBar();
        loadNews();
        setupListeners();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerViewNewsList.setLayoutManager(layoutManager);
        adapter = new AdminNewsAdapter((ArrayList<NewsModel>) filteredNewsList);
        binding.recyclerViewNewsList.setAdapter(adapter);
    }

    private void setupSearchBar() {
        binding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNews(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterNews(String keyword) {
        filteredNewsList.clear();
        if (keyword.isEmpty()) {
            filteredNewsList.addAll(newsList);
        } else {
            for (NewsModel news : newsList) {
                if (news.getTitle() != null && news.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredNewsList.add(news);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadNews() {
        db.collection("news")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    newsList.clear();
                    filteredNewsList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        NewsModel news = new NewsModel();
                        news.setTitle(document.getString("title"));
                        news.setDate(document.getString("date"));
                        news.setPic(document.getString("pic"));
                        news.setDescription(document.getString("description"));
                        news.setUserid(document.getString("userid"));
                        news.setStatus(document.getString("status"));
                        news.setDocumentId(document.getId());
                        newsList.add(news);
                    }
                    filteredNewsList.addAll(newsList);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải tin tức: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupListeners() {
        binding.btnAddNewsItem.setOnClickListener(v -> {
            Intent intent = new Intent(AdNewsListActivity.this, AddNewsActivity.class);
            startActivityForResult(intent, ADD_NEWS_REQUEST);
        });

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            return true;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == ADD_NEWS_REQUEST || requestCode == EDIT_NEWS_REQUEST)) {
            loadNews(); // Reload danh sách tin tức
        }
    }
    @Override
    protected void onResume() {

        super.onResume();
        loadNews();
    }
}
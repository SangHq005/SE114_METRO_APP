package com.example.metro_app.Activity.User;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.metro_app.Adapter.PostAdapter;
import com.example.metro_app.Domain.PostModel;
import com.example.metro_app.R;
import com.example.metro_app.databinding.ActivityForumBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForumActivity extends AppCompatActivity {
    private ActivityForumBinding binding;
    private FirebaseFirestore db;
    private List<PostModel> postList;
    private PostAdapter postAdapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();

        // Lấy userId hiện tại từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        currentUserId = prefs.getString("UserID", null);

        // Load avatar từ SharedPreferences
        loadUserAvatar();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Load bài đăng từ Firestore
        loadPosts();

        // Thiết lập listener cho nút đăng bài
        setupListeners();
    }

    private void loadUserAvatar() {
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String avatarUrl = prefs.getString("photo", null);

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .into(binding.profileImage);
        } else {
            binding.profileImage.setImageResource(R.drawable.profile_placeholder);
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerViewPosts.setLayoutManager(layoutManager);
        postAdapter = new PostAdapter(postList, currentUserId, this::showEditDeleteDialog);
        binding.recyclerViewPosts.setAdapter(postAdapter);
    }

    private void loadPosts() {
        db.collection("forum")
                .orderBy("createAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        PostModel post = new PostModel();
                        post.setUserId(document.getString("userId"));
                        post.setDescription(document.getString("description"));
                        post.setCreateAt(document.getString("createAt"));
                        post.setPostId(document.getString("postId"));
                        postList.add(post);
                    }
                    postAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải bài đăng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupListeners() {
        binding.btnPost.setOnClickListener(v -> {
            String description = binding.editPost.getText().toString().trim();
            if (description.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung bài đăng", Toast.LENGTH_SHORT).show();
                return;
            }

            savePostToFirestore(description);
        });
    }

    private void savePostToFirestore(String description) {
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String userId = prefs.getString("UserID", null);

        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String createAt = new SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(new Date());

        Map<String, Object> post = new HashMap<>();
        post.put("userId", userId);
        post.put("description", description);
        post.put("createAt", createAt);

        db.collection("forum")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    String postId = documentReference.getId();
                    documentReference.update("postId", postId)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ForumActivity.this, "Đăng bài thành công", Toast.LENGTH_SHORT).show();
                                binding.editPost.setText("");
                                loadPosts(); // Reload danh sách bài đăng
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ForumActivity.this, "Cập nhật postId thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ForumActivity.this, "Đăng bài thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showEditDeleteDialog(PostModel post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa bài đăng");

        // Tạo EditText để chỉnh sửa description
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(post.getDescription());
        input.setMinLines(3);
        input.setPadding(16, 16, 16, 16);
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String newDescription = input.getText().toString().trim();
            if (newDescription.isEmpty()) {
                Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật description trên Firestore
            db.collection("forum").document(post.getPostId())
                    .update("description", newDescription)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Chỉnh sửa bài đăng thành công", Toast.LENGTH_SHORT).show();
                        loadPosts(); // Reload danh sách
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Chỉnh sửa bài đăng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
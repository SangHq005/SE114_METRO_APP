package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.example.metro_app.Adapter.PostAdapter;
import com.example.metro_app.Domain.PostModel;
import com.example.metro_app.R;
import com.example.metro_app.databinding.ActivityForumBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class ForumActivity extends AppCompatActivity {
    private ActivityForumBinding binding;
    private FirebaseFirestore db;
    private List<PostModel> postList;
    private PostAdapter postAdapter;
    private String currentUserId;

    private ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();

        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        currentUserId = prefs.getString("UserID", null);

        loadUserAvatar();
        setupRecyclerView();
        loadPosts();
        setupListeners();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForumActivity.this, HomeActivity.class));
            }
        });
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

        // SỬA LỖI TẠI ĐÂY: Thêm 'false' làm tham số cuối cùng
        postAdapter = new PostAdapter(postList, currentUserId, this::showEditDeleteDialog, false);

        binding.recyclerViewPosts.setAdapter(postAdapter);
    }

    // CẢI TIẾN: SỬ DỤNG SnapshotListener để cập nhật real-time
    private void loadPosts() {
        db.collection("forum")
                .orderBy("createAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Lỗi khi tải bài đăng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        List<PostModel> tempList = new ArrayList<>();
                        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : snapshots) {
                            PostModel post = doc.toObject(PostModel.class);
                            post.setPostId(doc.getId());
                            tempList.add(post);

                            // ✅ Đếm comment từ collection riêng "Comment"
                            Task<QuerySnapshot> task = db.collection("Comment")
                                    .whereEqualTo("postId", post.getPostId())
                                    .get();

                            tasks.add(task);
                        }

                        // Khi tất cả các task đếm comment hoàn thành
                        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                            for (int i = 0; i < results.size(); i++) {
                                QuerySnapshot commentSnapshot = (QuerySnapshot) results.get(i);
                                tempList.get(i).setCommentCount(commentSnapshot.size());
                            }

                            postList.clear();
                            postList.addAll(tempList);
                            postAdapter.notifyDataSetChanged();
                        });
                    }
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
        if (currentUserId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String createAt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        Map<String, Object> post = new HashMap<>();
        post.put("userId", currentUserId);
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
                                // Không cần gọi loadPosts() nữa vì SnapshotListener sẽ tự cập nhật
                            })
                            .addOnFailureListener(e -> Toast.makeText(ForumActivity.this, "Cập nhật postId thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(ForumActivity.this, "Đăng bài thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showEditDeleteDialog(PostModel post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa bài đăng");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(post.getDescription());
        input.setMinLines(3);
        input.setPadding(32, 32, 32, 32); // Tăng padding
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String newDescription = input.getText().toString().trim();
            if (newDescription.isEmpty()) {
                Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("forum").document(post.getPostId())
                    .update("description", newDescription)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Chỉnh sửa bài đăng thành công", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Chỉnh sửa bài đăng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
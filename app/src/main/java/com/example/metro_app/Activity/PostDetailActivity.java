package com.example.metro_app.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metro_app.Adapter.CommentAdapter;
import com.example.metro_app.Domain.PostModel;
import com.example.metro_app.Model.CommentModel;
import com.example.metro_app.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailActivity extends AppCompatActivity {

    private PostModel post;
    private FirebaseFirestore db;
    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private List<CommentModel> commentList = new ArrayList<>();

    private TextInputEditText etComment;
    private ImageButton btnSendComment, btnPostMenu;
    private CircleImageView imgCommentAvt;

    private TextView tvUserName, tvUserRole, tvPostTime, tvPostContent;
    private CircleImageView imgUserAvatar;

    private String currentUserId;
    private String avatarUrl;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = FirebaseFirestore.getInstance();

        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        imgCommentAvt = findViewById(R.id.postdetailUserAvt);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserRole = findViewById(R.id.tvUserRole);
        tvPostTime = findViewById(R.id.tvPostTime);
        tvPostContent = findViewById(R.id.tvPostContent);
        btnPostMenu = findViewById(R.id.btnPostMenu);
        imgUserAvatar = findViewById(R.id.imgUserAvatar);

        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        currentUserId = prefs.getString("UserID", "");
        avatarUrl = prefs.getString("photo", null);
        if (avatarUrl != null && !avatarUrl.isEmpty() && imgCommentAvt != null) {
            Glide.with(this).load(avatarUrl).into(imgCommentAvt);
        }

        post = (PostModel) getIntent().getSerializableExtra("post");
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        if (post != null) {
            displayPostInfo();
        }

        commentAdapter = new CommentAdapter(
                this,
                commentList,
                currentUserId,
                new CommentAdapter.OnCommentActionListener() {
                    @Override
                    public void onEdit(CommentModel comment) {
                        etComment.setText(comment.getComment());
                        etComment.requestFocus();

                        btnSendComment.setOnClickListener(v -> {
                            String newText = etComment.getText().toString().trim();
                            if (TextUtils.isEmpty(newText)) {
                                Toast.makeText(PostDetailActivity.this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            db.collection("Comment").document(comment.getCommentId())
                                    .update("comment", newText)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(PostDetailActivity.this, "Đã cập nhật bình luận", Toast.LENGTH_SHORT).show();
                                        etComment.setText("");
                                        btnSendComment.setOnClickListener(view -> addComment());
                                        loadComments();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(PostDetailActivity.this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show());
                        });
                    }

                    @Override
                    public void onDelete(CommentModel comment) {
                        db.collection("Comment").document(comment.getCommentId())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(PostDetailActivity.this, "Đã xoá bình luận", Toast.LENGTH_SHORT).show();
                                    loadComments();
                                })
                                .addOnFailureListener(e -> Toast.makeText(PostDetailActivity.this, "Lỗi khi xoá", Toast.LENGTH_SHORT).show());
                    }
                },
                isAdmin
        );

        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);

        loadComments();
        btnSendComment.setOnClickListener(v -> addComment());
    }

    private void displayPostInfo() {
        tvPostContent.setText(post.getDescription());
        tvPostTime.setText(post.getCreateAt());

        db.collection("Account").document(post.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("Name");
                        String role = documentSnapshot.getString("Role");
                        String photo = documentSnapshot.getString("avatarUrl");

                        tvUserName.setText(name != null ? name : "Ẩn danh");
                        tvUserRole.setText("Admin".equals(role) ? "Quản trị viên" : "Người dùng");

                        if (photo != null && !photo.isEmpty()) {
                            Glide.with(this).load(photo).into(imgUserAvatar);
                        } else {
                            imgUserAvatar.setImageResource(R.drawable.ic_person);
                        }
                    }
                });

        if (isAdmin || (post.getUserId() != null && post.getUserId().equals(currentUserId))) {
            btnPostMenu.setVisibility(View.VISIBLE);
            btnPostMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(this, btnPostMenu);
                if (post.getUserId() != null && post.getUserId().equals(currentUserId)) {
                    popupMenu.getMenu().add("Chỉnh sửa");
                }
                popupMenu.getMenu().add("Xóa");
                popupMenu.setOnMenuItemClickListener(item -> {
                    if ("Xóa".equals(item.getTitle())) {
                        db.collection("forum").document(post.getPostId()).delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Xóa bài đăng thành công", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    }
                    // Thêm logic cho "Chỉnh sửa" nếu cần
                    return true;
                });
                popupMenu.show();
            });
        } else {
            btnPostMenu.setVisibility(View.GONE);
        }
    }

    private void loadComments() {
        db.collection("Comment")
                .whereEqualTo("postId", post.getPostId())
                .orderBy("createAt")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) return;
                    commentList.clear();
                    if (snapshots != null) {
                        for (var doc : snapshots) {
                            CommentModel comment = doc.toObject(CommentModel.class);
                            comment.setCommentId(doc.getId());
                            commentList.add(comment);
                        }
                        commentAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void addComment() {
        String text = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Vui lòng nhập nội dung bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("comment", text);
        data.put("userId", currentUserId);
        data.put("postId", post.getPostId());
        data.put("createAt", com.google.firebase.Timestamp.now());

        db.collection("Comment")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    etComment.setText("");
                    Toast.makeText(this, "Đã gửi bình luận", Toast.LENGTH_SHORT).show();
                    loadComments();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi bình luận", Toast.LENGTH_SHORT).show());
    }
}
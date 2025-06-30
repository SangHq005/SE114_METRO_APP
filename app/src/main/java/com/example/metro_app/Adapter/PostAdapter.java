package com.example.metro_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metro_app.Activity.PostDetailActivity;
import com.example.metro_app.Domain.PostModel;
import com.example.metro_app.Model.CommentModel;
import com.example.metro_app.R;
import com.example.metro_app.databinding.ItemPostForumBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private final List<PostModel> postList;
    private final String currentUserId;
    private Context context;
    private final OnMenuClickListener menuClickListener;
    private final FirebaseFirestore db;
    private final boolean isAdmin;

    public interface OnMenuClickListener {
        void onMenuClick(PostModel post);
    }

    public PostAdapter(List<PostModel> postList, String currentUserId, OnMenuClickListener menuClickListener, boolean isAdmin) {
        this.postList = postList;
        this.currentUserId = currentUserId;
        this.menuClickListener = menuClickListener;
        this.db = FirebaseFirestore.getInstance();
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostForumBinding binding = ItemPostForumBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        context = parent.getContext();
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostModel post = postList.get(position);

        // Hiển thị nội dung bài đăng
        holder.binding.tvPostContent.setText(post.getDescription());
        holder.binding.tvPostTime.setText(post.getCreateAt());
        holder.binding.commentCountText.setText(post.getCommentCount() + " bình luận");


        // Load thông tin người đăng từ collection "Account"
        if (post.getUserId() != null) {
            db.collection("Account").document(post.getUserId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String avatarUrl = documentSnapshot.getString("avatarUrl");
                            String name = documentSnapshot.getString("Name");
                            String role = documentSnapshot.getString("Role");

                            // Load avatar
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(context)
                                        .load(avatarUrl)
                                        .placeholder(R.drawable.ic_person)
                                        .into(holder.binding.imgUserAvatar);
                            } else {
                                holder.binding.imgUserAvatar.setImageResource(R.drawable.ic_person);
                            }

                            // Hiển thị tên
                            holder.binding.tvUserName.setText(name != null ? name : "Không rõ");

                            // Hiển thị vai trò
                            if ("User".equals(role)) {
                                holder.binding.tvUserRole.setText("Người dùng");
                            } else if ("Admin".equals(role)) {
                                holder.binding.tvUserRole.setText("Quản trị viên");
                            } else {
                                holder.binding.tvUserRole.setText("Không rõ");
                            }
                        } else {
                            holder.binding.imgUserAvatar.setImageResource(R.drawable.ic_person);
                            holder.binding.tvUserName.setText("Không rõ");
                            holder.binding.tvUserRole.setText("Không rõ");
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.binding.imgUserAvatar.setImageResource(R.drawable.ic_person);
                        holder.binding.tvUserName.setText("Không rõ");
                        holder.binding.tvUserRole.setText("Không rõ");
                    });
        } else {
            holder.binding.imgUserAvatar.setImageResource(R.drawable.ic_person);
            holder.binding.tvUserName.setText("Không rõ");
            holder.binding.tvUserRole.setText("Không rõ");
        }

//        if (isAdmin || (post.getUserId() != null && post.getUserId().equals(currentUserId))) {
//            holder.binding.btnPostMenu.setVisibility(View.VISIBLE);
//            holder.binding.btnPostMenu.setOnClickListener(v -> {
//                PopupMenu popupMenu = new PopupMenu(context, holder.binding.btnPostMenu);
//                // Admin cũng có thể chỉnh sửa nếu bạn muốn
//                if (post.getUserId() != null && post.getUserId().equals(currentUserId)) {
//                    popupMenu.getMenu().add("Chỉnh sửa");
//                }
//                popupMenu.getMenu().add("Xóa"); // Admin luôn có nút xóa
//                popupMenu.setOnMenuItemClickListener(menuItem -> {
//                    if (menuItem.getTitle().equals("Chỉnh sửa")) {
//                        menuClickListener.onMenuClick(post);
//                    } else if (menuItem.getTitle().equals("Xóa")) {
//                        // Logic xóa giữ nguyên
//                        db.collection("forum").document(post.getPostId())
//                                .delete()
//                                .addOnSuccessListener(aVoid -> {
//                                    Toast.makeText(context, "Xóa bài đăng thành công", Toast.LENGTH_SHORT).show();
//                                    // Xóa khỏi list và cập nhật adapter
//                                })
//                                .addOnFailureListener(e -> {
//                                    Toast.makeText(context, "Xóa bài đăng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                });
//                    }
//                    return true;
//                });
//                popupMenu.show();
//            });
//        } else {
//            holder.binding.btnPostMenu.setVisibility(View.GONE);
//        }

        // Hiển thị btnPostMenu nếu userId khớp
//        if (post.getUserId() != null && post.getUserId().equals(currentUserId)) {
//            holder.binding.btnPostMenu.setVisibility(View.VISIBLE);
//            holder.binding.btnPostMenu.setOnClickListener(v -> {
//                PopupMenu popupMenu = new PopupMenu(context, holder.binding.btnPostMenu);
//                popupMenu.getMenu().add("Chỉnh sửa");
//                popupMenu.getMenu().add("Xóa");
//                popupMenu.setOnMenuItemClickListener(menuItem -> {
//                    if (menuItem.getTitle().equals("Chỉnh sửa")) {
//                        menuClickListener.onMenuClick(post); // Mở dialog chỉnh sửa
//                    } else if (menuItem.getTitle().equals("Xóa")) {
//                        // Xóa bài đăng trực tiếp
//                        db.collection("forum").document(post.getPostId())
//                                .delete()
//                                .addOnSuccessListener(aVoid -> {
//                                    Toast.makeText(context, "Xóa bài đăng thành công", Toast.LENGTH_SHORT).show();
//                                    postList.remove(position);
//                                    notifyItemRemoved(position);
//                                    notifyItemRangeChanged(position, postList.size());
//                                })
//                                .addOnFailureListener(e -> {
//                                    Toast.makeText(context, "Xóa bài đăng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                });
//                    }
//                    return true;
//                });
//                popupMenu.show();
//            });
//        } else {
//            holder.binding.btnPostMenu.setVisibility(View.GONE);
//        }
        if ((post.getUserId() != null && post.getUserId().equals(currentUserId)) || isAdmin) {
            holder.binding.btnPostMenu.setVisibility(View.VISIBLE);
            holder.binding.btnPostMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(context, holder.binding.btnPostMenu);

                // Nếu là người tạo bài viết => có quyền chỉnh sửa
                if (post.getUserId() != null && post.getUserId().equals(currentUserId)) {
                    popupMenu.getMenu().add("Chỉnh sửa");
                }

                // Admin và chính chủ đều có thể xoá
                popupMenu.getMenu().add("Xóa");

                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    String title = menuItem.getTitle().toString();
                    if (title.equals("Chỉnh sửa")) {
                        menuClickListener.onMenuClick(post); // Gọi lại Dialog chỉnh sửa
                    } else if (title.equals("Xóa")) {
                        db.collection("forum").document(post.getPostId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Xóa bài đăng thành công", Toast.LENGTH_SHORT).show();
                                    postList.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Xóa bài đăng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                    return true;
                });

                popupMenu.show();
            });
        } else {
            holder.binding.btnPostMenu.setVisibility(View.GONE);
        }

        // Placeholder cho nút bình luận
        holder.binding.layoutComment.setOnClickListener(v -> {
            // TODO: Thêm logic hiển thị bình luận nếu cần
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("post", post);
            intent.putExtra("isAdmin", isAdmin);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostForumBinding binding;

        public ViewHolder(ItemPostForumBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
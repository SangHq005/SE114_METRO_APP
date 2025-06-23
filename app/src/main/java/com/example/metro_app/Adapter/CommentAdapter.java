package com.example.metro_app.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metro_app.Model.CommentModel;
import com.example.metro_app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final List<CommentModel> commentList;
    private final String currentUserId;
    private final Context context;
    private final boolean isAdmin;

    public interface OnCommentActionListener {
        void onEdit(CommentModel comment);
        void onDelete(CommentModel comment);
    }

    private final OnCommentActionListener actionListener;

    public CommentAdapter(Context context, List<CommentModel> commentList, String currentUserId, OnCommentActionListener listener, boolean isAdmin) {
        this.context = context;
        this.commentList = commentList;
        this.currentUserId = currentUserId;
        this.actionListener = listener;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment_forum, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        CommentModel comment = commentList.get(position);

        holder.tvCommentContent.setText(comment.getComment());
        holder.tvCommentTime.setText(comment.getCreateAtInString());

        // Lấy thông tin người dùng từ Firestore
        FirebaseFirestore.getInstance()
                .collection("Account")
                .document(comment.getUserId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.getString("Name");
                        String role = snapshot.getString("Role");
                        String avatarUrl = snapshot.getString("avatarUrl");

                        holder.tvUserName.setText(name != null ? name : "Không rõ");
                        holder.tvUserRole.setText("Admin".equals(role) ? "Quản trị viên" : "Người dùng");

                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(context)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_person)
                                    .into(holder.imgUserAvatar);
                        } else {
                            holder.imgUserAvatar.setImageResource(R.drawable.ic_person);
                        }
                    } else {
                        holder.tvUserName.setText("Không rõ");
                        holder.tvUserRole.setText("Không rõ");
                        holder.imgUserAvatar.setImageResource(R.drawable.ic_person);
                    }
                })
                .addOnFailureListener(e -> {
                    holder.tvUserName.setText("Không rõ");
                    holder.tvUserRole.setText("Không rõ");
                    holder.imgUserAvatar.setImageResource(R.drawable.ic_person);
                });

        // Menu hiển thị nếu là bình luận của chính người dùng
        if (currentUserId.equals(comment.getUserId())) {
            holder.btnCommentMenu.setVisibility(View.VISIBLE);
            holder.btnCommentMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(context, holder.btnCommentMenu);
                popupMenu.getMenu().add("Chỉnh sửa");
                popupMenu.getMenu().add("Xóa");
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getTitle().equals("Chỉnh sửa")) {
                        actionListener.onEdit(comment);
                    } else if (menuItem.getTitle().equals("Xóa")) {
                        actionListener.onDelete(comment);
                    }
                    return true;
                });
                popupMenu.show();
            });
        } else {
            holder.btnCommentMenu.setVisibility(View.GONE);
        }

        if (isAdmin || currentUserId.equals(comment.getUserId())) {
            holder.btnCommentMenu.setVisibility(View.VISIBLE);
            holder.btnCommentMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(context, holder.btnCommentMenu);
                if (currentUserId.equals(comment.getUserId())) {
                    popupMenu.getMenu().add("Chỉnh sửa");
                }
                popupMenu.getMenu().add("Xóa"); // Admin luôn có nút xóa
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getTitle().equals("Chỉnh sửa")) {
                        actionListener.onEdit(comment);
                    } else if (menuItem.getTitle().equals("Xóa")) {
                        actionListener.onDelete(comment);
                    }
                    return true;
                });
                popupMenu.show();
            });
        } else {
            holder.btnCommentMenu.setVisibility(View.GONE);
        }

        holder.tvReplyComment.setOnClickListener(v -> {
            // Placeholder xử lý reply nếu muốn
        });
    }


    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgUserAvatar;
        TextView tvUserName, tvUserRole, tvCommentContent, tvCommentTime, tvReplyComment;
        ImageButton btnCommentMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUserAvatar = itemView.findViewById(R.id.imgCommentAvt);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvReplyComment = itemView.findViewById(R.id.tvReplyComment);
            btnCommentMenu = itemView.findViewById(R.id.btnCommentMenu);
        }
    }
}

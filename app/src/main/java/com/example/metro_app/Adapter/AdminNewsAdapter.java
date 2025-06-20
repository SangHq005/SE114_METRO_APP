package com.example.metro_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metro_app.Activity.Admin.AdEditNewsActivity;
import com.example.metro_app.Domain.NewsModel;
import com.example.metro_app.databinding.ViewholderNewsListItemBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminNewsAdapter extends RecyclerView.Adapter<AdminNewsAdapter.ViewHolder> {
    private final ArrayList<NewsModel> newsList;
    private Context context;
    private final FirebaseFirestore db;

    public AdminNewsAdapter(ArrayList<NewsModel> newsList) {
        this.newsList = newsList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderNewsListItemBinding binding = ViewholderNewsListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        context = parent.getContext();
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsModel item = newsList.get(position);

        // Lưu ID document từ Firestore
        String documentId = item.getDocumentId();

        // Hiển thị tiêu đề
        holder.binding.tvNewsTitle.setText(item.getTitle());

        // Hiển thị preview nội dung (description) dạng HTML
        String htmlContent = item.getDescription();
        if (htmlContent != null) {
            holder.binding.tvNewsContent.setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.binding.tvNewsContent.setText("");
        }

        // Hiển thị ngày đăng (chỉ lấy 10 ký tự đầu: dd/MM/yyyy)
        String date = item.getDate();
        if (date != null && date.length() >= 10) {
            holder.binding.tvDate.setText(date.substring(0, 10));
        } else {
            holder.binding.tvDate.setText(date != null ? date : "");
        }

        // Hiển thị trạng thái
        holder.binding.tvStatus.setText(item.getStatus());

        // Tải ảnh
        Glide.with(context).load(item.getPic()).into(holder.binding.ivNewsImage);

        // Lấy tên tác giả từ Firestore collection "Account"
        if (item.getUserid() != null) {
            db.collection("Account").document(item.getUserid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String authorName = documentSnapshot.getString("Name");
                            holder.binding.tvAuthor.setText(authorName != null ? authorName : "Không rõ");
                        } else {
                            holder.binding.tvAuthor.setText("Không rõ");
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.binding.tvAuthor.setText("Không rõ");
                    });
        } else {
            holder.binding.tvAuthor.setText("Không rõ");
        }

        // Xử lý nút menu
        holder.binding.btnMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.binding.btnMenu);
            popupMenu.getMenu().add("Chỉnh sửa");
            popupMenu.getMenu().add("Xóa");
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getTitle().equals("Chỉnh sửa")) {
                    // Chuyển sang AdEditNewsActivity
                    Intent intent = new Intent(context, AdEditNewsActivity.class);
                    intent.putExtra("news", item);
                    intent.putExtra("documentId", documentId);
                    ((AppCompatActivity) context).startActivityForResult(intent, 2); // Request code 2
                } else if (menuItem.getTitle().equals("Xóa")) {
                    // Xóa tin tức từ Firestore
                    if (documentId != null) {
                        db.collection("news").document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Xóa tin tức thành công", Toast.LENGTH_SHORT).show();
                                    newsList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, newsList.size());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Xóa tin tức thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context, "Không tìm thấy ID tin tức", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderNewsListItemBinding binding;

        public ViewHolder(ViewholderNewsListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
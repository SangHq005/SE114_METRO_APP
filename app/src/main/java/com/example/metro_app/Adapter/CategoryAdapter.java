package com.example.metro_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metro_app.Domain.CategoryModel;
import com.example.metro_app.databinding.ViewholderCategoryBinding;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.Viewholder> {
    final List<CategoryModel> categoryModelList;
    Context context;
    private final OnCategoryClickListener clickListener;

    // Interface cho sự kiện click
    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryModel category);
    }

    public CategoryAdapter(List<CategoryModel> categoryModelList, OnCategoryClickListener clickListener) {
        this.categoryModelList = categoryModelList;
        this.clickListener = clickListener;
    }

    public CategoryAdapter(List<CategoryModel> categoryModelList) {
        this(categoryModelList, null);
    }

    @NonNull
    @Override
    public CategoryAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderCategoryBinding binding = ViewholderCategoryBinding.inflate(LayoutInflater.from(context),
                parent, false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.Viewholder holder, int position) {
        CategoryModel list = categoryModelList.get(position);
        holder.binding.titleTxt.setText(list.getName());

        Glide.with(holder.itemView.getContext())
                .load(list.getImagePath())
                .into(holder.binding.ImagePath);

        // Thêm sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCategoryClick(list);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        final ViewholderCategoryBinding binding;

        public Viewholder(ViewholderCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
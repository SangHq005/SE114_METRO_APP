package com.example.metro_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metro_app.Activity.User.DetailNewsActivity;
import com.example.metro_app.Domain.NewsModel;
import com.example.metro_app.databinding.ViewholderAllnewsBinding;

import java.util.ArrayList;

public class AllNewsAdapter extends RecyclerView.Adapter<AllNewsAdapter.Viewholder> {
    ArrayList<NewsModel> newsModel;
    Context context;

    public AllNewsAdapter(ArrayList<NewsModel> newsModel) {
        this.newsModel = newsModel;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderAllnewsBinding binding = ViewholderAllnewsBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        context = parent.getContext();
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        NewsModel item = newsModel.get(position);
        holder.binding.title.setText(item.getTitle());
        holder.binding.description.setText(item.getDescription());
        holder.binding.date.setText(item.getDate());

        Glide.with(holder.itemView.getContext())
                .load(item.getPic())
                .into(holder.binding.image);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailNewsActivity.class);
            intent.putExtra("object", item);
            intent.putExtra("type", "news");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return newsModel.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        private final ViewholderAllnewsBinding binding;

        public Viewholder(ViewholderAllnewsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

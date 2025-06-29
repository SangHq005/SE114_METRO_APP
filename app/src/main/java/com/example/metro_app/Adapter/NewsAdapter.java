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
import com.example.metro_app.databinding.ViewholderNewsBinding;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private final ArrayList<NewsModel> newsList;
    private final Context context;

    public NewsAdapter(ArrayList<NewsModel> newsList, Context context) {
        this.newsList = newsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderNewsBinding binding = ViewholderNewsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsModel item = newsList.get(position);

        holder.binding.dateTxt.setText(item.getDate());
        holder.binding.titleTxt.setText(item.getTitle());
        Glide.with(context).load(item.getPic()).into(holder.binding.pic);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailNewsActivity.class);
            intent.putExtra("type", "news");
            intent.putExtra("object", item);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderNewsBinding binding;

        public ViewHolder(ViewholderNewsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
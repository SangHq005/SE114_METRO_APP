package com.example.metro_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metro_app.Activity.User.DetailNewsActivity;
import com.example.metro_app.Domain.NewsModel;
import com.example.metro_app.databinding.ViewholderNewsBinding;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.Viewholder>{
    ArrayList<NewsModel> newsModel;
    Context context;
    ViewholderNewsBinding binding;

    public NewsAdapter(ArrayList<NewsModel> newsModel) {

        this.newsModel = newsModel;
    }

    @NonNull
    @Override
    public NewsAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ViewholderNewsBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent,false);
        context=parent.getContext();
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        NewsModel list = newsModel.get(position);
        holder.binding.titleTxt.setText(list.getTitle());
        holder.binding.dateTxt.setText(list.getDate());


        Glide.with(holder.itemView.getContext())
                .load(list.getPic())
                .into(holder.binding.pic);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailNewsActivity.class);
                intent.putExtra("object", newsModel);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsModel.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        private final ViewholderNewsBinding binding;

        public Viewholder(ViewholderNewsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

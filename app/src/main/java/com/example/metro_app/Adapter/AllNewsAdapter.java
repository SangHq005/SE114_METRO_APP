package com.example.metro_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metro_app.Domain.NewsModel;
import com.example.metro_app.databinding.ViewholderAllnewsBinding;
import com.example.metro_app.databinding.ViewholderNewsBinding;

import java.util.ArrayList;

public class AllNewsAdapter extends RecyclerView.Adapter<AllNewsAdapter.Viewholder>{
    ArrayList<NewsModel> newsModel;
    Context context;
    ViewholderAllnewsBinding binding;

    public AllNewsAdapter(ArrayList<NewsModel> newsModel) {

        this.newsModel = newsModel;
    }

    @NonNull
    @Override
    public AllNewsAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ViewholderAllnewsBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent,false);
        context=parent.getContext();
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        NewsModel list = newsModel.get(position);
        holder.binding.title.setText(list.getTitle());
        holder.binding.description.setText(list.getDescription());
        holder.binding.date.setText(list.getDate());


        Glide.with(holder.itemView.getContext())
                .load(list.getPic())
                .into(holder.binding.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
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

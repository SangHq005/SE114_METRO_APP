//package com.example.metro_app.Adapter;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.example.metro_app.Domain.NewsModel;
//import com.example.metro_app.Domain.PopularModel;
//import com.example.metro_app.databinding.ViewholderNewsBinding;
//import com.example.metro_app.databinding.ViewholderPopularBinding;
//
//import java.util.ArrayList;
//
//public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.Viewholder>{
//    ArrayList<PopularModel> popularModel;
//    Context context;
//    ViewholderPopularBinding binding;
//
//    public PopularAdapter(ArrayList<PopularModel> popularModel) {
//
//        this.popularModel = popularModel;
//    }
//
//    @NonNull
//    @Override
//    public PopularAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        binding = ViewholderPopularBinding.inflate(LayoutInflater.from(parent.getContext()),
//                parent,false);
//        context=parent.getContext();
//        return new Viewholder(binding);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
//        PopularModel list = popularModel.get(position);
//        holder.binding.titleTxt.setText(list.getTitle());
//
//        Glide.with(holder.itemView.getContext())
//                .load(list.getPic())
//                .into(holder.binding.image);
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return popularModel.size();
//    }
//
//    public static class Viewholder extends RecyclerView.ViewHolder {
//        private final ViewholderPopularBinding binding;
//
//        public Viewholder(ViewholderPopularBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//        }
//    }
//}

package com.example.metro_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metro_app.Activity.User.DetailNewsActivity;
import com.example.metro_app.Domain.PopularModel;
import com.example.metro_app.databinding.ViewholderPopularBinding;

import java.util.ArrayList;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.Viewholder> {
    ArrayList<PopularModel> popularModel;
    Context context;

    public PopularAdapter(ArrayList<PopularModel> popularModel) {
        this.popularModel = popularModel;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderPopularBinding binding = ViewholderPopularBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        context = parent.getContext();
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        PopularModel item = popularModel.get(position);
        holder.binding.titleTxt.setText(item.getTitle());

        Glide.with(context)
                .load(item.getPic())
                .into(holder.binding.image);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailNewsActivity.class);
            intent.putExtra("type", "popular");
            intent.putExtra("object", item);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return popularModel.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        private final ViewholderPopularBinding binding;

        public Viewholder(ViewholderPopularBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

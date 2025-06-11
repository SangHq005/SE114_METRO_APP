package com.example.metro_app.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.Model.RawRoute;
import com.example.metro_app.R;

import java.util.List;

public class RawRouteAdapter extends RecyclerView.Adapter<RawRouteAdapter.ViewHolder> {

    public interface OnRouteClickListener {
        void onClick(RawRoute route);
    }

    private final List<RawRoute> routeList;
    private final OnRouteClickListener listener;

    public RawRouteAdapter(List<RawRoute> routeList, OnRouteClickListener listener) {
        this.routeList = routeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rawroute, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RawRoute route = routeList.get(position);
        holder.textView.setText(route.getRouteNo() + " - " + route.getRouteName());
        holder.itemView.setOnClickListener(v -> listener.onClick(route));
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.tvRouteItem);
        }
    }
}

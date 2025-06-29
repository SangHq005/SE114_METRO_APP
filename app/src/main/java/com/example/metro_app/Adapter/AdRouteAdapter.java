package com.example.metro_app.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.Domain.RouteModel;
import com.example.metro_app.R;

import java.util.List;
import java.util.Objects;

// Adapter for RecyclerView
public class AdRouteAdapter extends RecyclerView.Adapter<AdRouteAdapter.RouteViewHolder> {
    private List<RouteModel> filteredRouteList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public AdRouteAdapter(List<RouteModel> filteredRouteList, OnItemClickListener listener) {
        this.filteredRouteList = filteredRouteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        RouteModel route = filteredRouteList.get(position);
        holder.routeName.setText(holder.itemView.getContext().getString(
                R.string.route_name_format, route.getFromStation(), route.getToStation()));

        // Định dạng giá vé để hiển thị (tùy chọn)
        // Nếu bạn có một TextView cho giá vé, hãy thêm nó ở đây. Ví dụ:
        // TextView routePrice = holder.itemView.findViewById(R.id.tv_route_price);
        // if (route.getPrice() != null) {
        //     DecimalFormat formatter = new DecimalFormat("#,###");
        //     String formattedPrice = formatter.format(route.getPrice()) + " VND";
        //     routePrice.setText(formattedPrice);
        // }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        Log.d("RouteAdapter", "Item count: " + filteredRouteList.size());
        return filteredRouteList.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView routeName;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            routeName = itemView.findViewById(R.id.tv_route_name);
        }
    }

    public void updateList(List<RouteModel> newList) {
        Log.d("RouteAdapter", "Updating list with " + newList.size() + " items");
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new RouteDiffCallback(this.filteredRouteList, newList));
        this.filteredRouteList.clear();
        this.filteredRouteList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    static class RouteDiffCallback extends DiffUtil.Callback {
        private final List<RouteModel> oldList;
        private final List<RouteModel> newList;

        RouteDiffCallback(List<RouteModel> oldList, List<RouteModel> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            // So sánh dựa trên ID duy nhất của tuyến đường
            return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            RouteModel oldRoute = oldList.get(oldItemPosition);
            RouteModel newRoute = newList.get(newItemPosition);
            // So sánh nội dung của các đối tượng
            return oldRoute.getFromStation().equals(newRoute.getFromStation()) &&
                    oldRoute.getToStation().equals(newRoute.getToStation()) &&
                    Objects.equals(oldRoute.getPrice(), newRoute.getPrice());
        }
    }
}
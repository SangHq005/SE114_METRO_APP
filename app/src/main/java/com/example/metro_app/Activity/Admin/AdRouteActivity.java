package com.example.metro_app.Activity.Admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.Domain.RouteModel;
import com.example.metro_app.R;

import java.util.ArrayList;
import java.util.List;

// Adapter for RecyclerView
class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {
    private List<RouteModel> routeList;

    public RouteAdapter(List<RouteModel> routeList) {
        this.routeList = routeList;
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
        RouteModel route = routeList.get(position);
        holder.routeName.setText(route.getFromStation() + " - " + route.getToStation());
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView routeName;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            routeName = itemView.findViewById(R.id.tv_route_name);
        }
    }
}

public class AdRouteActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RouteAdapter routeAdapter;
    private List<RouteModel> routeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_route);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view_routes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample data
        routeList = new ArrayList<>();
        routeList.add(new RouteModel("Ga Bến Thành", "Ga Thảo Điền", "08:00", "08:30"));
        routeList.add(new RouteModel("Ga Công viên Văn Thánh", "Ga Rạch Chiếc", "10:00", "10:25"));
        routeList.add(new RouteModel("Ga Phước Long", "Ga Bình Thái", "11:00", "11:15"));
        routeList.add(new RouteModel("Ga Khu Công nghệ cao", "Ga Đại học Quốc gia", "12:00", "12:20"));
        routeList.add(new RouteModel("Ga Bến xe Suối Tiên", "Ga Nhà hát Thành phố", "13:00", "13:45"));
        routeList.add(new RouteModel("Ga Nhà hát Thành phố", "Ga Bến Thành", "14:00", "14:10"));
        routeList.add(new RouteModel("Ga Tân Cảng", "Ga Thảo Điền", "15:00", "15:15"));
        routeList.add(new RouteModel("Ga An Phú", "Ga Phước Long", "16:00", "16:25"));
        routeList.add(new RouteModel("Ga Bình Thái", "Ga Thủ Đức", "17:00", "17:15"));
        routeList.add(new RouteModel("Ga Đại học Quốc gia", "Ga Bến xe Suối Tiên", "18:00", "18:20"));

        // Set up adapter
        routeAdapter = new RouteAdapter(routeList);
        recyclerView.setAdapter(routeAdapter);
    }
}
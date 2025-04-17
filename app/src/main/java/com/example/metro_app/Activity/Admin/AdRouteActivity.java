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

import com.example.metro_app.R;
import com.example.metro_app.Domain.RouteModel;

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
        routeList.add(new RouteModel("Ga Bến Thành", "Ga Suối Tiên", "9:00", "10:00"));
        routeList.add(new RouteModel("Ga Bến Thành", "Ga Suối Tiên", "9:00", "10:00"));
        routeList.add(new RouteModel("Ga Bến Thành", "Ga Suối Tiên", "9:00", "10:00"));
        routeList.add(new RouteModel("Ga Bến Thành", "Ga Suối Tiên", "9:00", "10:00"));
        routeList.add(new RouteModel("Ga Bến Thành", "Ga Suối Tiên", "9:00", "10:00"));
        routeList.add(new RouteModel("Ga Bến Thành", "Ga Suối Tiên", "9:00", "10:00"));
        routeList.add(new RouteModel("Ga Bến Thành", "Ga Suối Tiên", "9:00", "10:00"));
        routeList.add(new RouteModel("Ga Bến Thành", "Ga Suối Tiên", "9:00", "10:00"));
        routeList.add(new RouteModel("Ga Bến Thành", "Ga Suối Tiên", "9:00", "10:00"));
        routeList.add(new RouteModel("Ga Bến Thành", "Ga Suối Tiên", "9:00", "10:00"));
        routeList.add(new RouteModel("Ga Bến Thành", "Ga Suối Tiên", "9:00", "10:00"));
        routeList.add(new RouteModel("Ga Bến Thành", "Ga Suối Tiên", "9:00", "10:00"));

        // Set up adapter
        routeAdapter = new RouteAdapter(routeList);
        recyclerView.setAdapter(routeAdapter);
    }
}
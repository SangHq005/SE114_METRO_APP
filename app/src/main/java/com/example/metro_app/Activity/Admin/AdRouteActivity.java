package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.Domain.RouteModel;
import com.example.metro_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

// Adapter for RecyclerView
class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {
    private List<RouteModel> filteredRouteList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public RouteAdapter(List<RouteModel> filteredRouteList, OnItemClickListener listener) {
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
        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
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
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new RouteDiffCallback(filteredRouteList, newList));
        filteredRouteList = new ArrayList<>(newList);
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
            return oldList.get(oldItemPosition) == newList.get(newItemPosition);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            RouteModel oldRoute = oldList.get(oldItemPosition);
            RouteModel newRoute = newList.get(newItemPosition);
            return oldRoute.getFromStation().equals(newRoute.getFromStation()) &&
                    oldRoute.getToStation().equals(newRoute.getToStation()) &&
                    oldRoute.getFromTime().equals(newRoute.getFromTime()) &&
                    oldRoute.getToTime().equals(newRoute.getToTime());
        }
    }
}

public class AdRouteActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RouteAdapter routeAdapter;
    private List<RouteModel> routeList;
   private List<RouteModel> filteredRouteList;

    private final ActivityResultLauncher<Intent> addRouteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    RouteModel route = (RouteModel) result.getData().getSerializableExtra("route");
                    routeList.add(route);
                    filterRoutes("");
                }
            });

    private final ActivityResultLauncher<Intent> editRouteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    RouteModel route = (RouteModel) result.getData().getSerializableExtra("route");
                    int position = result.getData().getIntExtra("position", -1);
                    if (position != -1) {
                        routeList.set(position, route);
                    }
                    filterRoutes("");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_route);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view_routes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize search bar
        EditText searchBar = findViewById(R.id.search_bar);

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

        // Initialize filtered list
        filteredRouteList = new ArrayList<>(routeList);

        // Set up adapter
        routeAdapter = new RouteAdapter(filteredRouteList, position -> {
            Intent intent = new Intent(AdRouteActivity.this, AdRouteDetails.class);
            intent.putExtra("route", filteredRouteList.get(position));
            intent.putExtra("position", routeList.indexOf(filteredRouteList.get(position)));
            editRouteLauncher.launch(intent);
        });
        recyclerView.setAdapter(routeAdapter);

        ImageButton addButton = findViewById(R.id.button_add_route);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdRouteActivity.this, AddRouteActivity.class);
            addRouteLauncher.launch(intent);
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRoutes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_ad_route);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_ad_home) {
                startActivity(new Intent(AdRouteActivity.this, AdHomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_ad_route) {
                return true;
            } else if (id == R.id.nav_ad_wallet) {
                startActivity(new Intent(AdRouteActivity.this, AdTicketActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_ad_userlist) {
                startActivity(new Intent(AdRouteActivity.this, AdUserActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void filterRoutes(String query) {
        filteredRouteList.clear();
        if (query.isEmpty()) {
            filteredRouteList.addAll(routeList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (RouteModel route : routeList) {
                if (route.getFromStation().toLowerCase().contains(lowerCaseQuery) ||
                        route.getToStation().toLowerCase().contains(lowerCaseQuery)) {
                    filteredRouteList.add(route);
                }
            }
        }
        routeAdapter.updateList(filteredRouteList);
    }


}
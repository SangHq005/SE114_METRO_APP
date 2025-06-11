package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

public class AdRouteActivity extends AppCompatActivity {
    private static final String TAG = "AdRouteActivity";
    private RecyclerView recyclerView;
    private RouteAdapter routeAdapter;
    private List<RouteModel> routeList;
    private List<RouteModel> filteredRouteList;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<Intent> addRouteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Tải lại toàn bộ danh sách để đảm bảo tính nhất quán
                    loadRoutesFromFirestore();
                }
            });

    private final ActivityResultLauncher<Intent> editRouteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Tải lại toàn bộ danh sách để đảm bảo tính nhất quán sau khi chỉnh sửa
                    loadRoutesFromFirestore();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_route);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view_routes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize search bar
        EditText searchBar = findViewById(R.id.search_bar);

        // Initialize lists
        routeList = new ArrayList<>();
        filteredRouteList = new ArrayList<>();

        // Set up adapter
        routeAdapter = new RouteAdapter(filteredRouteList, position -> {
            Intent intent = new Intent(AdRouteActivity.this, AdRouteDetails.class);
            // Lấy đúng đối tượng từ danh sách đã lọc
            RouteModel selectedRoute = filteredRouteList.get(position);
            intent.putExtra("route", selectedRoute);

            // Tìm vị trí của đối tượng trong danh sách gốc để cập nhật
            int originalPosition = routeList.indexOf(selectedRoute);
            intent.putExtra("position", originalPosition);

            editRouteLauncher.launch(intent);
        });
        recyclerView.setAdapter(routeAdapter);

        // Load routes from Firestore
        loadRoutesFromFirestore();

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

        // BottomNavigationView
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
            } else if (id == R.id.nav_ad_profile) {
                startActivity(new Intent(AdRouteActivity.this, AdProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadRoutesFromFirestore() {
        db.collection("TicketType")
                .whereEqualTo("Type", "Vé lượt")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        routeList.clear();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (var doc : querySnapshot.getDocuments()) {
                                try {
                                    String id = doc.getId(); // Get document ID
                                    String fromStation = doc.getString("StartStation");
                                    String toStation = doc.getString("EndStation");
                                    Double price = doc.getDouble("Price"); // Lấy giá vé dưới dạng Double

                                    if (id != null && fromStation != null && toStation != null && price != null) {
                                        RouteModel route = new RouteModel(id, fromStation, toStation, price);
                                        routeList.add(route);
                                    } else {
                                        Log.w(TAG, "Missing fields in document: " + doc.getId());
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing document: " + doc.getId(), e);
                                }
                            }
                            // Sau khi tải xong, cập nhật danh sách đã lọc
                            filterRoutes(((EditText)findViewById(R.id.search_bar)).getText().toString());
                        } else {
                            Toast.makeText(AdRouteActivity.this, "Không tìm thấy vé lượt.", Toast.LENGTH_SHORT).show();
                            routeList.clear();
                            filterRoutes("");
                        }
                    } else {
                        Log.e(TAG, "Error loading routes: ", task.getException());
                        Toast.makeText(AdRouteActivity.this, "Lỗi khi tải danh sách vé lượt: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void filterRoutes(String query) {
        List<RouteModel> newFilteredList = new ArrayList<>();
        if (query.isEmpty()) {
            newFilteredList.addAll(routeList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (RouteModel route : routeList) {
                if (route.getFromStation().toLowerCase().contains(lowerCaseQuery) ||
                        route.getToStation().toLowerCase().contains(lowerCaseQuery)) {
                    newFilteredList.add(route);
                }
            }
        }
        Log.d(TAG, "Filtering routes, new size: " + newFilteredList.size());
        routeAdapter.updateList(newFilteredList);
    }
}

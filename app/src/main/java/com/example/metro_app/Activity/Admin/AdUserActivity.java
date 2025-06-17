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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metro_app.utils.FireStoreHelper;
import com.example.metro_app.Model.UserModel;
import com.example.metro_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Adapter for RecyclerView
class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<UserModel> userList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(UserModel user);
    }

    public UserAdapter(List<UserModel> userList, OnItemClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        if (user == null) {
            Log.e("UserAdapter", "User at position " + position + " is null");
            holder.userName.setText("Unknown User");
            holder.userRole.setText("N/A");
            return;
        }

        // Set user name
        holder.userName.setText(user.getName() != null ? user.getName() : "Unknown");

        // Set user role (capitalize first letter for better look)
        String role = user.getRole() != null ? user.getRole() : "User";
        holder.userRole.setText(role.substring(0, 1).toUpperCase() + role.substring(1));
        String avatarUrl = null;
        try {
            avatarUrl = user.getClass().getMethod("getAvatarUrl") != null ? (String) user.getClass().getMethod("getAvatarUrl").invoke(user) : null;
        } catch (Exception e) {
            // Method does not exist or error, ignore
        }
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.userbtn) // fallback image
                    .error(R.drawable.userbtn)
                    .circleCrop()
                    .into(holder.userAvatar);
        } else {
            holder.userAvatar.setImageResource(R.drawable.userbtn);
        }
        // Set click listener
        holder.itemView.setOnClickListener(v -> listener.onItemClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView userRole; // Add TextView for role
        ImageView userAvatar;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.tv_user_name);
            userRole = itemView.findViewById(R.id.tv_user_role); // Initialize role TextView
            userAvatar = itemView.findViewById(R.id.ivUserAvatar);
            if (userName == null || userRole == null) {
                Log.e("UserAdapter", "A TextView is null, check item_user.xml IDs (tv_user_name, tv_user_role)");
            }
        }
    }

    public void updateList(List<UserModel> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new UserDiffCallback(this.userList, newList));
        this.userList.clear();
        this.userList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    // --- DiffUtil Callback for efficient updates ---
    static class UserDiffCallback extends DiffUtil.Callback {
        private final List<UserModel> oldList;
        private final List<UserModel> newList;

        UserDiffCallback(List<UserModel> oldList, List<UserModel> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }
        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            String oldUid = oldList.get(oldItemPosition).getUid();
            String newUid = newList.get(newItemPosition).getUid();
            return oldUid != null && oldUid.equals(newUid);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            // Assumes UserModel has a proper .equals() method for content comparison.
            // If not, you should compare fields individually.
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
}

public class AdUserActivity extends AppCompatActivity {
    private static final String TAG = "AdUserActivity";
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserModel> fullUserList; // To hold the complete list from Firestore
    private FireStoreHelper fireStoreHelper;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize lists and helpers
        fullUserList = new ArrayList<>();
        fireStoreHelper = new FireStoreHelper();

        // Setup UI components
        setupRecyclerView();
        setupSearch();
        setupBottomNavigation();

        // Fetch and display users
        fetchAndDisplayUsers();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up adapter with an empty list initially
        userAdapter = new UserAdapter(new ArrayList<>(), user -> {
            if (user == null) {
                Log.e(TAG, "Selected user is null");
                return;
            }
            Intent intent = new Intent(AdUserActivity.this, AdUserDetails.class);
            intent.putExtra("user", user);
            startActivity(intent); // Launch directly, no need for result
        });
        recyclerView.setAdapter(userAdapter);
    }

    private void fetchAndDisplayUsers() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Current user is not logged in.");
            Toast.makeText(this, "Lỗi: Không thể xác thực người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUserId = currentUser.getUid();

        fireStoreHelper.getAllUsers(new FireStoreHelper.Callback<List<UserModel>>() {
            @Override
            public void onSuccess(List<UserModel> result) {
                // Filter out the current user (the admin) from the list
                List<UserModel> filteredResult = result.stream()
                        .filter(user -> user.getUid() != null && !user.getUid().equals(currentUserId))
                        .collect(Collectors.toList());

                runOnUiThread(() -> {
                    fullUserList.clear();
                    fullUserList.addAll(filteredResult);
                    userAdapter.updateList(new ArrayList<>(fullUserList)); // Initial display
                    Log.d(TAG, "Successfully fetched and displayed " + fullUserList.size() + " users.");
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "onFailure: Cannot get User data", e);
                runOnUiThread(() -> Toast.makeText(AdUserActivity.this, "Không thể tải danh sách người dùng.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setupSearch() {
        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterUsers(String query) {
        List<UserModel> filteredList;
        String lowerCaseQuery = query.toLowerCase().trim();

        if (lowerCaseQuery.isEmpty()) {
            filteredList = new ArrayList<>(fullUserList);
        } else {
            filteredList = fullUserList.stream()
                    .filter(user -> (user.getName() != null && user.getName().toLowerCase().contains(lowerCaseQuery)) ||
                            (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseQuery)))
                    .collect(Collectors.toList());
        }
        userAdapter.updateList(filteredList);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_ad_userlist);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ad_home) {
                startActivity(new Intent(AdUserActivity.this, AdHomeActivity.class));
            } else if (id == R.id.nav_ad_route) {
                startActivity(new Intent(AdUserActivity.this, AdRouteActivity.class));
            } else if (id == R.id.nav_ad_wallet) {
                startActivity(new Intent(AdUserActivity.this, AdTicketActivity.class));
            } else if (id == R.id.nav_ad_profile) {
                startActivity(new Intent(AdUserActivity.this, AdProfileActivity.class));
            } else if (id == R.id.nav_ad_userlist) {
                return true; // Already on this screen
            }

            if (id != R.id.nav_ad_userlist) {
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }
}

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.Model.FireStoreHelper;
import com.example.metro_app.Model.UserModel;
import com.example.metro_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

// Adapter for RecyclerView
class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<UserModel> filteredUserList;
    private final OnItemClickListener listener;
    private final AppCompatActivity context;

    public interface OnItemClickListener {
        void onItemClick(UserModel user);
    }

    public UserAdapter(AppCompatActivity context, List<UserModel> filteredUserList, OnItemClickListener listener) {
        this.context = context;
        this.filteredUserList = filteredUserList;
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
        UserModel user = filteredUserList.get(position);
        if (user == null) {
            Log.e("UserAdapter", "User at position " + position + " is null");
            holder.userName.setText("Unknown User");
            return;
        }
        String fullName = user.getName() != null ? user.getName() : "Unknown";
        holder.userName.setText(context.getString(R.string.user_name_format, fullName));
        holder.itemView.setOnClickListener(v -> {
            Log.d("UserAdapter", "Item clicked at position: " + position + ", User: " + fullName);
            listener.onItemClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return filteredUserList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.tv_user_name);
            if (userName == null) {
                Log.e("UserAdapter", "tv_user_name is null, check item_user.xml");
            }
        }
    }

    public void updateList(List<UserModel> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new UserDiffCallback(filteredUserList, newList));
        filteredUserList = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    static class UserDiffCallback extends DiffUtil.Callback {
        private final List<UserModel> oldList;
        private final List<UserModel> newList;

        UserDiffCallback(List<UserModel> oldList, List<UserModel> newList) {
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
            String oldUid = oldList.get(oldItemPosition).getUid();
            String newUid = newList.get(newItemPosition).getUid();
            if (oldUid == null && newUid == null) return true;
            if (oldUid == null || newUid == null) return false;
            return oldUid.equals(newUid);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            UserModel oldUser = oldList.get(oldItemPosition);
            UserModel newUser = newList.get(newItemPosition);
            return (oldUser.getName() == null ? newUser.getName() == null : oldUser.getName().equals(newUser.getName())) &&
                    (oldUser.getEmail() == null ? newUser.getEmail() == null : oldUser.getEmail().equals(newUser.getEmail())) &&
                    (oldUser.getCCCD() == null ? newUser.getCCCD() == null : oldUser.getCCCD().equals(newUser.getCCCD())) &&
                    (oldUser.getRole() == null ? newUser.getRole() == null : oldUser.getRole().equals(newUser.getRole()));
        }
    }
}

public class AdUserActivity extends AppCompatActivity {
    private static final String TAG = "AdUserActivity";
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserModel> userList;
    private FireStoreHelper fireStoreHelper;

    private final ActivityResultLauncher<Intent> editUserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    UserModel user = (UserModel) result.getData().getSerializableExtra("user");
                    String userUid = result.getData().getStringExtra("user_uid");
                    if (user != null && userUid != null) {
                        int index = findUserIndexByUid(userUid);
                        if (index != -1) {
                            Log.d(TAG, "Updated user at index: " + index + ", User: " + user.getName());
                            userList.set(index, user);
                            filterUsers(""); // Refresh list
                        } else {
                            Log.e(TAG, "User UID not found in userList: " + userUid);
                            Toast.makeText(this, "Lỗi khi cập nhật người dùng.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Invalid user or user_uid: user=" + user + ", user_uid=" + userUid);
                        Toast.makeText(this, "Lỗi khi cập nhật người dùng.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "Result not OK or data null: resultCode=" + result.getResultCode());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view_users);
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView is null, check activity_admin_user.xml");
            Toast.makeText(this, "Lỗi giao diện: Không tìm thấy RecyclerView.", Toast.LENGTH_LONG).show();
            return;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize search bar
        EditText searchBar = findViewById(R.id.search_bar);
        if (searchBar == null) {
            Log.e(TAG, "Search bar is null, check activity_admin_user.xml");
            Toast.makeText(this, "Lỗi giao diện: Không tìm thấy thanh tìm kiếm.", Toast.LENGTH_LONG).show();
        }

        userList = new ArrayList<>();
        fireStoreHelper = new FireStoreHelper();

        // Set up adapter
        userAdapter = new UserAdapter(this, new ArrayList<>(), user -> {
            if (user == null) {
                Log.e(TAG, "Selected user is null");
                Toast.makeText(this, "Lỗi: Người dùng không tồn tại.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                Intent intent = new Intent(AdUserActivity.this, AdUserDetails.class);
                intent.putExtra("user", user);
                intent.putExtra("user_uid", user.getUid());
                Log.d(TAG, "Launching AdUserDetails for user: " + user.getName() + ", uid: " + user.getUid());
                editUserLauncher.launch(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error launching AdUserDetails: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi khi mở chi tiết người dùng: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        recyclerView.setAdapter(userAdapter);

        fireStoreHelper.getAllUsers(new FireStoreHelper.Callback<List<UserModel>>() {
            @Override
            public void onSuccess(List<UserModel> result) {
                runOnUiThread(() -> {
                    userList.clear();
                    userList.addAll(result);
                    userAdapter.updateList(new ArrayList<>(userList)); // Always pass a new list
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("Admin", "onFailure: Cannot get User data");
            }
        });

        // Search functionality
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterUsers(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // No action needed
                }
            });
        }

        //BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_ad_userlist);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_ad_home) {
                startActivity(new Intent(AdUserActivity.this, AdHomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_ad_route) {
                startActivity(new Intent(AdUserActivity.this, AdRouteActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_ad_wallet) {
                startActivity(new Intent(AdUserActivity.this, AdTicketActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_ad_userlist) {
                return true;
            }
            return false;
        });

    }

    private void filterUsers(String query) {
        List<UserModel> filtered = new ArrayList<>();
        if (query.isEmpty()) {
            filtered.addAll(userList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (UserModel user : userList) {
                if ((user.getName() != null && user.getName().toLowerCase().contains(lowerCaseQuery)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseQuery))) {
                    filtered.add(user);
                }
            }
        }
        userAdapter.updateList(filtered);
    }

    private int findUserIndexByUid(String uid) {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUid() != null && userList.get(i).getUid().equals(uid)) {
                return i;
            }
        }
        return -1;
    }
}
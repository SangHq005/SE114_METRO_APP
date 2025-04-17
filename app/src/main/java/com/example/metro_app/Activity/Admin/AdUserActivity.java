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

import com.example.metro_app.Domain.UserModel;
import com.example.metro_app.R;

import java.util.ArrayList;
import java.util.List;


// Adapter for RecyclerView
class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<UserModel> userList;

    public UserAdapter(List<UserModel> userList) {
        this.userList = userList;
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
        holder.userName.setText(user.getUsername());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.tv_user_name);
        }
    }
}

public class AdUserActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserModel> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample data
        userList = new ArrayList<>();
        userList.add(new UserModel("hathu"));
        userList.add(new UserModel("hathu"));
        userList.add(new UserModel("hathu"));
        userList.add(new UserModel("hathu"));
        userList.add(new UserModel("hathu"));
        userList.add(new UserModel("hathu"));
        userList.add(new UserModel("hathu"));
        userList.add(new UserModel("hathu"));
        userList.add(new UserModel("hathu"));
        userList.add(new UserModel("hathu"));
        userList.add(new UserModel("hathu"));


        // Set up adapter
        userAdapter = new UserAdapter(userList);
        recyclerView.setAdapter(userAdapter);
    }
}
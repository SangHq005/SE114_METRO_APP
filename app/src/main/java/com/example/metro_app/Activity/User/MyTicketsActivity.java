package com.example.metro_app.Activity.User;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.metro_app.Adapter.RouteListAdapter;
import com.example.metro_app.Adapter.TicketTypeAdapter;
import com.example.metro_app.Model.TicketType;
import com.example.metro_app.R;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MyTicketsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTickets, recyclerViewHSSV, recyclerViewRouteList;
    private TextView tvNoTicketsNoiBat, tvNoTicketsHSSV, tvNoTicketsRoute, nameTxt;
    private TicketTypeAdapter noiBatAdapter, hssvAdapter;
    private RouteListAdapter routeListAdapter;
    private List<TicketType> noiBatTickets, hssvTickets, routeTickets;
    private FirebaseFirestore db;
    private DecimalFormat decimalFormat;
    private ImageView imgAvt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_tickets);

        checkGooglePlayServices();

        // Lấy userId và name từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String userId = prefs.getString("UserID", null);
        String userName = prefs.getString("name", "Không có thông tin");
        Log.d("MyTicketsActivity", "Retrieved userId from SharedPreferences: " + userId);

        db = FirebaseFirestore.getInstance();

        decimalFormat = new DecimalFormat("#,###");
        decimalFormat.setGroupingSize(3);

        try {
            recyclerViewTickets = findViewById(R.id.recyclerViewTickets);
            recyclerViewHSSV = findViewById(R.id.recyclerViewHSSV);
            recyclerViewRouteList = findViewById(R.id.recyclerViewRouteList);
            tvNoTicketsNoiBat = findViewById(R.id.tv_no_tickets_noi_bat);
            tvNoTicketsHSSV = findViewById(R.id.tv_no_tickets_hssv);
            imgAvt = findViewById(R.id.img);
            nameTxt = findViewById(R.id.nameTxt);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("MyTicketsActivity", "Error initializing UI: " + e.getMessage());
            return;
        }

        // Hiển thị tên từ SharedPreferences
        nameTxt.setText(userName);
        String avatarUrl = null;
        try {
            avatarUrl = prefs.getString("photo",null);
        } catch (Exception e) {
            // Method does not exist or error, ignore
        }
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.userbtn) // fallback image
                    .error(R.drawable.userbtn)
                    .circleCrop()
                    .into(imgAvt);
        } else {
            imgAvt.setImageResource(R.drawable.userbtn);
        }

        recyclerViewTickets.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHSSV.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRouteList.setLayoutManager(new LinearLayoutManager(this));

        noiBatTickets = new ArrayList<>();
        hssvTickets = new ArrayList<>();
        routeTickets = new ArrayList<>();

        noiBatAdapter = new TicketTypeAdapter(noiBatTickets, ticket -> {
            fetchTicketDetailsAndStartActivity(ticket.getId());
        });
        hssvAdapter = new TicketTypeAdapter(hssvTickets, ticket -> {
            fetchTicketDetailsAndStartActivity(ticket.getId());
        });
        routeListAdapter = new RouteListAdapter(routeTickets, ticket -> {
            Intent intent = new Intent(MyTicketsActivity.this, DetailRouteActivity.class);
            intent.putExtra("ticket_id", ticket.getId());
            intent.putExtra("ticket_name", ticket.getName());
            intent.putExtra("start_station", ticket.getStartStation());
            intent.putExtra("end_station", ticket.getEndStation());
            intent.putExtra("ticket_price", ticket.getPrice());
            startActivity(intent);
        });

        recyclerViewTickets.setAdapter(noiBatAdapter);
        recyclerViewHSSV.setAdapter(hssvAdapter);
        recyclerViewRouteList.setAdapter(routeListAdapter);

        ImageView backBtn = findViewById(R.id.backBtn);
        if (backBtn != null) {
            backBtn.setClickable(true);
            backBtn.setFocusable(true);
            backBtn.bringToFront();
            backBtn.setOnClickListener(v -> {
                Log.d("MyTicketsActivity", "backBtn clicked");
                startActivity(new Intent(MyTicketsActivity.this, HomeActivity.class));
                finish();
            });
        } else {
            Log.e("MyTicketsActivity", "backBtn is null");
            Toast.makeText(this, "Không tìm thấy nút quay lại", Toast.LENGTH_LONG).show();
        }

        // Kiểm tra ToolbarconstraintLayout để debug
        View toolbar = findViewById(R.id.ToolbarconstraintLayout);
        if (toolbar != null) {
            toolbar.setOnClickListener(v -> Log.d("MyTicketsActivity", "ToolbarconstraintLayout clicked"));
        }

        loadTickets();
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        loadAnim(tvGreeting);
        loadAnim(nameTxt);

    }
    private void loadAnim(TextView textView){
        //hiệu ứng text
        Shader shader = new LinearGradient(
                0, 0, 0, textView.getTextSize(),
                new int[]{
                        Color.RED,
                        Color.BLUE,
                        Color.GREEN,
                        Color.MAGENTA
                },
                null,
                Shader.TileMode.CLAMP);

        textView.getPaint().setShader(shader);

        // Tạo hiệu ứng chạy màu bằng cách cập nhật shader liên tục
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1000);
        animator.setDuration(4000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            float translate = (float) animation.getAnimatedValue();
            Shader movingShader = new LinearGradient(
                    translate, 0, translate + textView.getWidth(), textView.getTextSize(),
                    new int[]{
                            Color.RED,
                            Color.BLUE,
                            Color.GREEN,
                            Color.MAGENTA
                    },
                    null,
                    Shader.TileMode.MIRROR);
            textView.getPaint().setShader(movingShader);
            textView.invalidate();
        });
        animator.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MyTicketsActivity", "onResume called");
        // Cập nhật tên từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String userName = prefs.getString("name", "Không có thông tin");
        nameTxt.setText(userName);
    }

    private void checkGooglePlayServices() {
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int resultCode = googleApi.isGooglePlayServicesAvailable(this);
    }

    private void fetchTicketDetailsAndStartActivity(String ticketId) {
        db.collection("TicketType").document(ticketId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String name = task.getResult().getString("Name");
                Object expirationObj = task.getResult().get("Active");
                Object autoActiveObj = task.getResult().get("AutoActive");

                long expiration = 0;
                long autoActive = 0;

                if (expirationObj instanceof Number) {
                    expiration = ((Number) expirationObj).longValue();
                } else {
                    Log.e("Firestore", "Expiration không phải kiểu Number cho ticketId: " + ticketId);
                    Toast.makeText(this, "Lỗi: Expiration không hợp lệ trong TicketType (ID: " + ticketId + ")", Toast.LENGTH_LONG).show();
                    return;
                }

                if (autoActiveObj instanceof Number) {
                    autoActive = ((Number) autoActiveObj).longValue();
                } else {
                    Log.e("Firestore", "AutoActive không phải kiểu Number cho ticketId: " + ticketId);
                    Toast.makeText(this, "Lỗi: AutoActive không hợp lệ trong TicketType (ID: " + ticketId + ")", Toast.LENGTH_LONG).show();
                    return;
                }

                Log.d("Firestore", "Fetched Expiration: " + expiration + ", AutoActive: " + autoActive + " for ticketId: " + ticketId);

                String price = "0 VND";
                Object priceObj = task.getResult().get("Price");
                if (priceObj != null) {
                    if (priceObj instanceof String) {
                        price = (String) priceObj;
                        if (!price.endsWith("VND")) {
                            try {
                                long priceValue = Long.parseLong(price);
                                price = decimalFormat.format(priceValue) + " VND";
                            } catch (NumberFormatException e) {
                            }
                        }
                    } else if (priceObj instanceof Long || priceObj instanceof Integer) {
                        long priceValue = ((Number) priceObj).longValue();
                        price = decimalFormat.format(priceValue) + " VND";
                    }
                }

                Intent intent = new Intent(MyTicketsActivity.this, ChooseTicketActivity.class);
                intent.putExtra("ticket_type_id", ticketId);
                intent.putExtra("ticket_name", name != null ? name : "Không có thông tin");
                intent.putExtra("ticket_active", String.valueOf(expiration));
                intent.putExtra("ticket_auto_active", String.valueOf(autoActive));
                intent.putExtra("ticket_price", price);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Lỗi tải dữ liệu vé: " + (task.getException() != null ? task.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadTickets() {
        if (tvNoTicketsNoiBat != null) {
            tvNoTicketsNoiBat.setVisibility(View.GONE);
        }
        if (tvNoTicketsHSSV != null) {
            tvNoTicketsHSSV.setVisibility(View.GONE);
        }
        if (tvNoTicketsRoute != null) {
            tvNoTicketsRoute.setVisibility(View.GONE);
        }

        db.collection("TicketType").get().addOnCompleteListener(task -> {
            try {
                if (task.isSuccessful()) {
                    noiBatTickets.clear();
                    hssvTickets.clear();
                    routeTickets.clear();

                    if (task.getResult().isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(MyTicketsActivity.this, "Không có dữ liệu trong TicketType", Toast.LENGTH_LONG).show());
                    } else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String name = document.getString("Name");
                            String type = document.getString("Type");
                            String startStation = document.getString("StartStation");
                            String endStation = document.getString("EndStation");
                            String status = document.getString("Status");
                            if (name == null) {
                                Object nameObj = document.get("Name");
                                if (nameObj != null) {
                                    name = String.valueOf(nameObj);
                                }
                            }
                            // Kiểm tra Status
                            if (status == null || !status.equals("Hoạt động")) {
                                Log.d("MyTicketsActivity", "Skipped ticket " + name + " due to Status: " + (status != null ? status : "null"));
                                continue; // Bỏ qua nếu Status không phải "Hoạt động"
                            }
                            String price = "0 VND";
                            Object priceObj = document.get("Price");
                            if (priceObj != null) {
                                if (priceObj instanceof String) {
                                    price = (String) priceObj;
                                    if (!price.endsWith("VND")) {
                                        try {
                                            long priceValue = Long.parseLong(price);
                                            price = decimalFormat.format(priceValue) + " VND";
                                        } catch (NumberFormatException e) {
                                        }
                                    }
                                } else if (priceObj instanceof Long || priceObj instanceof Integer) {
                                    long priceValue = ((Number) priceObj).longValue();
                                    price = decimalFormat.format(priceValue) + " VND";
                                }
                            }

                            if (name == null) {
                                continue;
                            }

                            TicketType ticket = new TicketType(id, name, price);
                            ticket.setType(type);
                            ticket.setStartStation(startStation);
                            ticket.setEndStation(endStation);

                            if (name.toLowerCase().contains("hssv")) {
                                hssvTickets.add(ticket);
                            } else if ("Vé dài hạn".equals(type)) {
                                noiBatTickets.add(ticket);
                            } else if ("Vé lượt".equals(type)) {
                                routeTickets.add(ticket);
                            }
                        }
                    }

                    if (noiBatAdapter != null) {
                        noiBatAdapter.notifyDataSetChanged();
                    }
                    if (hssvAdapter != null) {
                        hssvAdapter.notifyDataSetChanged();
                    }
                    if (routeListAdapter != null) {
                        routeListAdapter.notifyDataSetChanged();
                    }

                    if (tvNoTicketsNoiBat != null) {
                        tvNoTicketsNoiBat.setVisibility(noiBatTickets.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    if (tvNoTicketsHSSV != null) {
                        tvNoTicketsHSSV.setVisibility(hssvTickets.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    if (tvNoTicketsRoute != null) {
                        tvNoTicketsRoute.setVisibility(routeTickets.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MyTicketsActivity.this, "Lỗi tải dữ liệu: " + (task.getException() != null ? task.getException().getMessage() : "Không xác định"), Toast.LENGTH_LONG).show());
                    if (tvNoTicketsNoiBat != null) {
                        tvNoTicketsNoiBat.setVisibility(View.VISIBLE);
                    }
                    if (tvNoTicketsHSSV != null) {
                        tvNoTicketsHSSV.setVisibility(View.VISIBLE);
                    }
                    if (tvNoTicketsRoute != null) {
                        tvNoTicketsRoute.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MyTicketsActivity.this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }
}
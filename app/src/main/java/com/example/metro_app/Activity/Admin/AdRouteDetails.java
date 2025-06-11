package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText; // Import EditText
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.Domain.RouteModel;
import com.example.metro_app.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdRouteDetails extends AppCompatActivity {
    private static final String TAG = "AdRouteDetails";
    private FirebaseFirestore db;
    private List<String> stationNames;
    private final String[] statusOptions = {"Hoạt động", "Tạm dừng"};
    private EditText editTextPrice; // Thêm biến cho EditText giá vé

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_route_edit);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        Spinner spinnerFromStation = findViewById(R.id.spinner_type_fromStation);
        Spinner spinnerToStation = findViewById(R.id.spinner_type_toStation);
        Spinner spinnerStatus = findViewById(R.id.spinnerStatus);
        editTextPrice = findViewById(R.id.editTextPrice); // Khởi tạo EditText giá vé
        Button cancelButton = findViewById(R.id.button_cancel);
        Button saveButton = findViewById(R.id.button_save);

        // Initialize station list
        stationNames = new ArrayList<>();

        // Set up adapter for status spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Load stations from Firestore
        loadStationsFromFirestore(spinnerFromStation, spinnerToStation);

        // Get data from Intent
        Intent intent = getIntent();
        RouteModel route = (RouteModel) intent.getSerializableExtra("route");
        int position = intent.getIntExtra("position", -1);

        // Set initial values
        if (route != null) {
            // Load current route data including price
            db.collection("TicketType")
                    .document(route.getId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Load status
                            String status = documentSnapshot.getString("Status");
                            if (status != null) {
                                spinnerStatus.setSelection(getIndexOfStatus(status));
                            }

                            // Load price
                            Double price = documentSnapshot.getDouble("Price");
                            if (price != null) {
                                // Sử dụng String.format để tránh hiển thị ".0" cho số nguyên
                                editTextPrice.setText(String.format("%.0f", price));
                            }
                        }
                    });
            // Set station spinners after loading stations
            loadStationsFromFirestore(spinnerFromStation, spinnerToStation, route.getFromStation(), route.getToStation());
        }

        // Button actions
        cancelButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> {
            String fromStation = spinnerFromStation.getSelectedItem() != null ? spinnerFromStation.getSelectedItem().toString() : "";
            String toStation = spinnerToStation.getSelectedItem() != null ? spinnerToStation.getSelectedItem().toString() : "";
            String status = spinnerStatus.getSelectedItem().toString();
            String priceStr = editTextPrice.getText().toString();

            // Check for empty fields
            if (fromStation.isEmpty() || toStation.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(AdRouteDetails.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if fromStation and toStation are different
            if (fromStation.equals(toStation)) {
                Toast.makeText(AdRouteDetails.this, "Ga đi và ga đến không được giống nhau.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse the price
            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(AdRouteDetails.this, "Giá vé không hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update Firestore
            if (route != null) {
                String ticketName = "Vé lượt: " + fromStation + " - " + toStation;
                Map<String, Object> ticketData = new HashMap<>();
                ticketData.put("Name", ticketName);
                ticketData.put("StartStation", fromStation);
                ticketData.put("EndStation", toStation);
                ticketData.put("Status", status);
                ticketData.put("Price", price); // Thêm giá vé vào dữ liệu cập nhật

                db.collection("TicketType")
                        .document(route.getId())
                        .update(ticketData)
                        .addOnSuccessListener(aVoid -> {
                            // Create updated RouteModel with the new price
                            RouteModel updatedRoute = new RouteModel(route.getId(), fromStation, toStation, price);
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("route", updatedRoute);
                            resultIntent.putExtra("position", position);
                            setResult(RESULT_OK, resultIntent);
                            Toast.makeText(AdRouteDetails.this, "Tuyến đường đã được cập nhật!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AdRouteDetails.this, "Lỗi khi cập nhật vé: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });
    }

    private void loadStationsFromFirestore(Spinner spinnerFromStation, Spinner spinnerToStation) {
        loadStationsFromFirestore(spinnerFromStation, spinnerToStation, null, null);
    }

    private void loadStationsFromFirestore(Spinner spinnerFromStation, Spinner spinnerToStation, String selectedFrom, String selectedTo) {
        db.collection("stations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            stationNames.clear();
                            for (var doc : querySnapshot.getDocuments()) {
                                String stationName = doc.getString("Name");
                                if (stationName != null) {
                                    stationNames.add(stationName);
                                }
                            }
                            if (stationNames.isEmpty()) {
                                Toast.makeText(AdRouteDetails.this, "Không tìm thấy ga trong Firestore.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Set up adapters for spinners
                            ArrayAdapter<String> stationAdapter = new ArrayAdapter<>(AdRouteDetails.this,
                                    android.R.layout.simple_spinner_item, stationNames);
                            stationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerFromStation.setAdapter(stationAdapter);
                            spinnerToStation.setAdapter(stationAdapter);

                            // Set selected stations if provided
                            if (selectedFrom != null) {
                                spinnerFromStation.setSelection(getIndexOfStation(selectedFrom));
                            }
                            if (selectedTo != null) {
                                spinnerToStation.setSelection(getIndexOfStation(selectedTo));
                            }
                        } else {
                            Toast.makeText(AdRouteDetails.this, "Không tìm thấy ga trong Firestore.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AdRouteDetails.this, "Lỗi khi tải danh sách ga: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private int getIndexOfStation(String station) {
        for (int i = 0; i < stationNames.size(); i++) {
            if (stationNames.get(i).equals(station)) {
                return i;
            }
        }
        return 0;
    }

    private int getIndexOfStatus(String status) {
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equals(status)) {
                return i;
            }
        }
        return 0;
    }
}

package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.Model.TicketType;
import com.example.metro_app.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddRouteActivity extends AppCompatActivity {
    private static final String TAG = "AddRouteActivity";
    private FirebaseFirestore db;
    private List<String> stationNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_route);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        Spinner spinnerFromStation = findViewById(R.id.spinner_type_fromStation);
        Spinner spinnerToStation = findViewById(R.id.spinner_type_toStation);
        EditText editTextPrice = findViewById(R.id.editTextPrice);
        Button cancelButton = findViewById(R.id.cancel_button);
        Button saveButton = findViewById(R.id.save_button);

        // Load stations from Firestore
        stationNames = new ArrayList<>();
        loadStationsFromFirestore(spinnerFromStation, spinnerToStation);

        // Button actions
        cancelButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> {
            String fromStation = spinnerFromStation.getSelectedItem() != null ? spinnerFromStation.getSelectedItem().toString() : "";
            String toStation = spinnerToStation.getSelectedItem() != null ? spinnerToStation.getSelectedItem().toString() : "";
            String price = editTextPrice.getText().toString().trim();

            // Check for empty fields
            if (fromStation.isEmpty() || toStation.isEmpty() || price.isEmpty()) {
                Toast.makeText(AddRouteActivity.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if fromStation and toStation are different
            if (fromStation.equals(toStation)) {
                Toast.makeText(AddRouteActivity.this, "Ga đi và ga đến không được giống nhau.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if price is a valid positive number
            long priceValue;
            try {
                priceValue = Long.parseLong(price.replaceAll("[^0-9]", ""));
                if (priceValue <= 0) {
                    Toast.makeText(AddRouteActivity.this, "Giá vé phải là số dương.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(AddRouteActivity.this, "Giá vé không hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Format price for display
            DecimalFormat formatter = new DecimalFormat("#,###");
            String formattedPrice = formatter.format(priceValue) + " VND";

            // Generate unique ID
            String id = UUID.randomUUID().toString();

            // Create ticket name
            String ticketName = "Vé lượt: " + fromStation + " - " + toStation;

            // Create TicketType
            TicketType newTicket = new TicketType(id, fromStation, toStation, formattedPrice, ticketName);

            // Save to Firestore without Id field
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("Name", ticketName);
            ticketData.put("Price", priceValue); // Store as number
            ticketData.put("StartStation", fromStation);
            ticketData.put("EndStation", toStation);
            ticketData.put("Active", 0L); // Store as number
            ticketData.put("AutoActive", 30L); // Store as number
            ticketData.put("Status", "Hoạt động"); // Default status
            ticketData.put("Type", "Vé lượt");

            db.collection("TicketType")
                    .document(id)
                    .set(ticketData)
                    .addOnSuccessListener(aVoid -> {
                        // Return result to parent activity (if needed)
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("ticket", newTicket);
                        setResult(RESULT_OK, resultIntent);
                        Toast.makeText(AddRouteActivity.this, "Tuyến vé đã được lưu!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddRouteActivity.this, "Lỗi khi lưu vé: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    private void loadStationsFromFirestore(Spinner spinnerFromStation, Spinner spinnerToStation) {
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
                                Toast.makeText(AddRouteActivity.this, "Không tìm thấy ga trong Firestore.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Set up adapters for spinners
                            ArrayAdapter<String> stationAdapter = new ArrayAdapter<>(AddRouteActivity.this,
                                    android.R.layout.simple_spinner_item, stationNames);
                            stationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerFromStation.setAdapter(stationAdapter);
                            spinnerToStation.setAdapter(stationAdapter);
                        } else {
                            Toast.makeText(AddRouteActivity.this, "Không tìm thấy ga trong Firestore.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AddRouteActivity.this, "Lỗi khi tải danh sách ga: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
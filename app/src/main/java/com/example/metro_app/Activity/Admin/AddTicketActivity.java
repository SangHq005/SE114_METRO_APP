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

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddTicketActivity extends AppCompatActivity {
    private static final String TAG = "AddTicketActivity";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_ticket);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        EditText editTextName = findViewById(R.id.editTextName);
        EditText editTextPrice = findViewById(R.id.editTextPrice);
        EditText editTextActive = findViewById(R.id.editTextActive);
        EditText editTextAutoActive = findViewById(R.id.editTextAutoActive);
        Spinner spinnerStatus = findViewById(R.id.spinnerStatus);
        Button cancelButton = findViewById(R.id.cancel_button);
        Button saveButton = findViewById(R.id.save_button);

        // Set up adapter for status spinner
        String[] statusOptions = {"Hoạt động", "Tạm dừng"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Button actions
        cancelButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String price = editTextPrice.getText().toString().trim();
            String active = editTextActive.getText().toString().trim();
            String autoActive = editTextAutoActive.getText().toString().trim();
            String status = spinnerStatus.getSelectedItem().toString();

            // Check for empty fields
            if (name.isEmpty() || price.isEmpty() || active.isEmpty() || autoActive.isEmpty()) {
                Toast.makeText(AddTicketActivity.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if price is a valid positive number
            long priceValue;
            try {
                priceValue = Long.parseLong(price.replaceAll("[^0-9]", ""));
                if (priceValue <= 0) {
                    Toast.makeText(AddTicketActivity.this, "Giá vé phải là số dương.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(AddTicketActivity.this, "Giá vé không hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if active is a valid positive integer
            long activeDays;
            try {
                activeDays = Long.parseLong(active);
                if (activeDays <= 0) {
                    Toast.makeText(AddTicketActivity.this, "Số ngày kích hoạt phải là số dương.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(AddTicketActivity.this, "Số ngày kích hoạt không hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if autoActive is a valid non-negative integer
            long autoActiveDays;
            try {
                autoActiveDays = Long.parseLong(autoActive);
                if (autoActiveDays < 0) {
                    Toast.makeText(AddTicketActivity.this, "Số ngày tự động kích hoạt phải là số không âm.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(AddTicketActivity.this, "Số ngày tự động kích hoạt không hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Format price for display
            DecimalFormat formatter = new DecimalFormat("#,###");
            String formattedPrice = formatter.format(priceValue) + " VND";

            // Generate unique ID
            String id = UUID.randomUUID().toString();

            // Create TicketType
            TicketType newTicket = new TicketType(id, name, formattedPrice, active, autoActive, status);

            // Save to Firestore without Id field
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("Name", name);
            ticketData.put("Price", priceValue); // Store as number
            ticketData.put("Active", activeDays); // Store as number
            ticketData.put("AutoActive", autoActiveDays); // Store as number
            ticketData.put("Status", status);
            ticketData.put("Type", "Vé dài hạn"); // Add Type field

            db.collection("TicketType")
                    .document(id)
                    .set(ticketData)
                    .addOnSuccessListener(aVoid -> {
                        // Return result to AdTicketActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("ticket", newTicket);
                        setResult(RESULT_OK, resultIntent);
                        Toast.makeText(AddTicketActivity.this, "Loại vé đã được lưu!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddTicketActivity.this, "Lỗi khi lưu vé: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}
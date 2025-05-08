package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.Domain.TicketModel;
import com.example.metro_app.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdTicketDetails extends AppCompatActivity {
    private final String[] ticketTypes = {"Vé lượt đi", "Vé 1 ngày", "Vé 3 ngày", "Vé 30 ngày"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_ticket_edit);

        // Initialize views
        Spinner spinnerTypeTicket = findViewById(R.id.spinner_type_ticket);
        EditText editTextPrice = findViewById(R.id.editTextPrice);
        EditText editTextExpireDate = findViewById(R.id.editTextExpireDate);

        // Set up adapter for spinner
        ArrayAdapter<String> ticketTypeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, ticketTypes);
        ticketTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeTicket.setAdapter(ticketTypeAdapter);

        // Set up DatePickerDialog for expireDate
        editTextExpireDate.setFocusable(false); // Prevent manual input
        editTextExpireDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    AdTicketDetails.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format selected date as dd/MM/yyyy
                        String date = String.format(Locale.US, "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                        editTextExpireDate.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Get data from Intent
        Intent intent = getIntent();
        TicketModel ticket = (TicketModel) intent.getSerializableExtra("ticket");
        int position = intent.getIntExtra("position", -1);

        if (ticket != null) {
            spinnerTypeTicket.setSelection(getIndexOfTicketType(ticket.getTicketType()));
            editTextPrice.setText(ticket.getPrice());
            editTextExpireDate.setText(ticket.getExpireDate());
        }

        // Button actions
        findViewById(R.id.button_cancel).setOnClickListener(v -> finish());

        findViewById(R.id.button_save).setOnClickListener(v -> {
            String ticketType = spinnerTypeTicket.getSelectedItem().toString();
            String price = editTextPrice.getText().toString();
            String expireDate = editTextExpireDate.getText().toString();

            // Check for empty fields
            if (ticketType.isEmpty() || price.isEmpty() || expireDate.isEmpty()) {
                Toast.makeText(AdTicketDetails.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if price is a valid positive number
            try {
                double priceValue = Double.parseDouble(price.replaceAll("[^0-9.]", ""));
                if (priceValue <= 0) {
                    Toast.makeText(AdTicketDetails.this, "Giá vé phải là số dương.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(AdTicketDetails.this, "Giá vé không hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if expireDate is after today
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            try {
                Date expire = sdf.parse(expireDate);
                Date today = new Date();
                if (!expire.after(today)) {
                    Toast.makeText(AdTicketDetails.this, "Ngày hết hạn phải sau ngày hiện tại.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // All checks passed, save the ticket
                TicketModel updatedTicket = new TicketModel(ticketType, price, expireDate);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ticket", updatedTicket);
                resultIntent.putExtra("position", position);
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(AdTicketDetails.this, "Loại vé đã được cập nhật!", Toast.LENGTH_SHORT).show();
                finish();
            } catch (Exception e) {
                Toast.makeText(AdTicketDetails.this, "Định dạng ngày hết hạn không hợp lệ.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getIndexOfTicketType(String ticketType) {
        for (int i = 0; i < ticketTypes.length; i++) {
            if (ticketTypes[i].equals(ticketType)) {
                return i;
            }
        }
        return 0;
    }
}
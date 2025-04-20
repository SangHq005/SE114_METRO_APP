package com.example.metro_app.Activity.Admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.R;
public class AddTicketActivity extends AppCompatActivity {
    private Spinner spinnerTypeTicket, spinnerFromStation, spinnerToStation, spinnerStatus;
    private EditText editTextPrice, editTextExpireDate;
    private String selectedTicketType, selectedFromStation, selectedToStation, selectedStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_ticket);

        // Initialize views
        spinnerTypeTicket = findViewById(R.id.spinner_type_ticket);
        spinnerFromStation = findViewById(R.id.spinner_type_fromStation);
        spinnerToStation = findViewById(R.id.spinner_type_toStation);
        spinnerStatus = findViewById(R.id.spinner_status);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextExpireDate = findViewById(R.id.editTextExpireDate);

        // Set up the adapters for the spinners

        // Ticket Types
        ArrayAdapter<String> ticketTypeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Vé lượt đi", "Vé 1 ngày", "Vé 3 ngày", "Vé 30 ngày"});
        ticketTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeTicket.setAdapter(ticketTypeAdapter);

        // From Stations
        ArrayAdapter<String> fromStationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{
                "Ga Bến Thành", "Ga Nhà hát Thành phố", "Ga Ba Son", "Ga Công viên Văn Thánh", "Ga Tân Cảng",
                "Ga Thảo Điền", "Ga An Phú", "Ga Rạch Chiếc", "Ga Phước Long", "Ga Bình Thái", "Ga Thủ Đức",
                "Ga Khu Công nghệ cao", "Ga Đại học Quốc gia", "Ga Bến xe Suối Tiên"});
        fromStationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromStation.setAdapter(fromStationAdapter);

        // To Stations (same as from stations in this case)
        ArrayAdapter<String> toStationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{
                "Ga Bến Thành", "Ga Nhà hát Thành phố", "Ga Ba Son", "Ga Công viên Văn Thánh", "Ga Tân Cảng",
                "Ga Thảo Điền", "Ga An Phú", "Ga Rạch Chiếc", "Ga Phước Long", "Ga Bình Thái", "Ga Thủ Đức",
                "Ga Khu Công nghệ cao", "Ga Đại học Quốc gia", "Ga Bến xe Suối Tiên"});
        toStationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToStation.setAdapter(toStationAdapter);

        // Ticket Status
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Đang hoạt động", "Đang chờ"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Spinner item selected listeners
        spinnerTypeTicket.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedTicketType = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedTicketType = "";
            }
        });

        spinnerFromStation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedFromStation = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedFromStation = "";
            }
        });

        spinnerToStation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedToStation = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedToStation = "";
            }
        });

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedStatus = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedStatus = "";
            }
        });

        // Button actions (Cancel and Save)
        findViewById(R.id.cancel_button).setOnClickListener(v -> {
            finish(); // Close the activity
        });

        findViewById(R.id.save_button).setOnClickListener(v -> {
            // Example: Saving ticket data (You can replace this with actual database saving logic)
            String price = editTextPrice.getText().toString();
            String expireDate = editTextExpireDate.getText().toString();

            if (selectedTicketType.isEmpty() || selectedFromStation.isEmpty() || selectedToStation.isEmpty() || selectedStatus.isEmpty() ||
                    price.isEmpty() || expireDate.isEmpty()) {
                Toast.makeText(AddTicketActivity.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            } else {
                // Do your saving logic here
                Toast.makeText(AddTicketActivity.this, "Vé đã được lưu!", Toast.LENGTH_SHORT).show();
                // You can save the data to your database or perform other necessary actions
            }
        });
    }
}

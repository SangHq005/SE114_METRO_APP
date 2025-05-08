package com.example.metro_app.Activity.Admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.R;

public class AdRouteDetails extends AppCompatActivity {
    private Spinner spinnerFromStation, spinnerToStation;
    private EditText editTextFromTime, editTextToTime;
    private final String[] stations = {
            "Ga Bến Thành", "Ga Nhà hát Thành phố", "Ga Ba Son", "Ga Công viên Văn Thánh", "Ga Tân Cảng",
            "Ga Thảo Điền", "Ga An Phú", "Ga Rạch Chiếc", "Ga Phước Long", "Ga Bình Thái", "Ga Thủ Đức",
            "Ga Khu Công nghệ cao", "Ga Đại học Quốc gia", "Ga Bến xe Suối Tiên"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_route_edit);

        // Initialize views
        spinnerFromStation = findViewById(R.id.spinner_type_fromStation);
        spinnerToStation = findViewById(R.id.spinner_type_toStation);
        editTextFromTime = findViewById(R.id.editTextFromTime);
        editTextToTime = findViewById(R.id.editTextToTime);

        // Set up adapters for spinners
        ArrayAdapter<String> fromStationAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, stations);
        fromStationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromStation.setAdapter(fromStationAdapter);

        ArrayAdapter<String> toStationAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, stations);
        toStationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToStation.setAdapter(toStationAdapter);

        // Set sample data
        spinnerFromStation.setSelection(getIndexOfStation("Ga Bến Thành"));
        spinnerToStation.setSelection(getIndexOfStation("Ga Thảo Điền"));
        editTextFromTime.setText("08:00");
        editTextToTime.setText("08:30");

        // Button actions
        findViewById(R.id.button_cancel).setOnClickListener(v -> finish());

        findViewById(R.id.button_save).setOnClickListener(v -> {
            // Implement saving logic here
            finish();
        });
    }

    private int getIndexOfStation(String station) {
        for (int i = 0; i < stations.length; i++) {
            if (stations[i].equals(station)) {
                return i;
            }
        }
        return 0;
    }
}
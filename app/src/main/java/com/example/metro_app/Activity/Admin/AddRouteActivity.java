package com.example.metro_app.Activity.Admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.R;

public class AddRouteActivity extends AppCompatActivity {
    private Spinner spinnerFromStation, spinnerToStation;
    private EditText editTextFromTime, editTextToTime;
    private String selectedFromStation, selectedToStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_route);

        // Initialize views
        spinnerFromStation = findViewById(R.id.spinner_type_fromStation);
        spinnerToStation = findViewById(R.id.spinner_type_toStation);
        editTextFromTime = findViewById(R.id.editTextFromTime);
        editTextToTime = findViewById(R.id.editTextToTime);

        // Set up adapters for spinners
        // Station list
        String[] stations = {
                "Ga Bến Thành", "Ga Nhà hát Thành phố", "Ga Ba Son", "Ga Công viên Văn Thánh", "Ga Tân Cảng",
                "Ga Thảo Điền", "Ga An Phú", "Ga Rạch Chiếc", "Ga Phước Long", "Ga Bình Thái", "Ga Thủ Đức",
                "Ga Khu Công nghệ cao", "Ga Đại học Quốc gia", "Ga Bến xe Suối Tiên"
        };

        // From Station
        ArrayAdapter<String> fromStationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, stations);
        fromStationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromStation.setAdapter(fromStationAdapter);

        // To Station
        ArrayAdapter<String> toStationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, stations);
        toStationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToStation.setAdapter(toStationAdapter);

        // Spinner item selected listeners
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

        // Button actions
        findViewById(R.id.cancel_button).setOnClickListener(v -> finish());

        findViewById(R.id.save_button).setOnClickListener(v -> {
            String fromTime = editTextFromTime.getText().toString();
            String toTime = editTextToTime.getText().toString();

            if (selectedFromStation.isEmpty() || selectedToStation.isEmpty() ||
                    fromTime.isEmpty() || toTime.isEmpty()) {
                Toast.makeText(AddRouteActivity.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            } else {
                // Implement saving logic here (e.g., save to database)
                Toast.makeText(AddRouteActivity.this, "Tuyến đường đã được lưu!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
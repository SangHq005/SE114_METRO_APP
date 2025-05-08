package com.example.metro_app.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.Domain.RouteModel;
import com.example.metro_app.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddRouteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_route);

        // Initialize views
        Spinner spinnerFromStation = findViewById(R.id.spinner_type_fromStation);
        Spinner spinnerToStation = findViewById(R.id.spinner_type_toStation);
        EditText editTextStartTime = findViewById(R.id.editTextFromTime);
        EditText editTextEndTime = findViewById(R.id.editTextToTime);

        // Set up adapter for spinners
        String[] stations = {
                "Ga Bến Thành", "Ga Nhà hát Thành phố", "Ga Ba Son", "Ga Công viên Văn Thánh", "Ga Tân Cảng",
                "Ga Thảo Điền", "Ga An Phú", "Ga Rạch Chiếc", "Ga Phước Long", "Ga Bình Thái", "Ga Thủ Đức",
                "Ga Khu Công nghệ cao", "Ga Đại học Quốc gia", "Ga Bến xe Suối Tiên"
        };
        ArrayAdapter<String> stationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, stations);
        stationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromStation.setAdapter(stationAdapter);
        spinnerToStation.setAdapter(stationAdapter);

        // Set up TimePickerDialog for startTime
        editTextStartTime.setFocusable(false); // Prevent manual input
        editTextStartTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                    AddRouteActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        // Format selected time as HH:mm
                        String time = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                        editTextStartTime.setText(time);
                    }, hour, minute, true); // true for 24-hour format
            timePickerDialog.show();
        });

        // Set up TimePickerDialog for endTime
        editTextEndTime.setFocusable(false); // Prevent manual input
        editTextEndTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                    AddRouteActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        // Format selected time as HH:mm
                        String time = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                        editTextEndTime.setText(time);
                    }, hour, minute, true); // true for 24-hour format
            timePickerDialog.show();
        });

        // Button actions
        findViewById(R.id.cancel_button).setOnClickListener(v -> finish());

        findViewById(R.id.save_button).setOnClickListener(v -> {
            String fromStation = spinnerFromStation.getSelectedItem().toString();
            String toStation = spinnerToStation.getSelectedItem().toString();
            String startTime = editTextStartTime.getText().toString();
            String endTime = editTextEndTime.getText().toString();

            // Check for empty fields
            if (fromStation.isEmpty() || toStation.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(AddRouteActivity.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if fromStation and toStation are different
            if (fromStation.equals(toStation)) {
                Toast.makeText(AddRouteActivity.this, "Ga khởi hành và ga đến không được giống nhau.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if endTime is after startTime
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
            try {
                Date start = sdf.parse(startTime);
                Date end = sdf.parse(endTime);
                if (!end.after(start)) {
                    Toast.makeText(AddRouteActivity.this, "Giờ đến phải sau giờ khởi hành.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // All checks passed, save the route
                RouteModel newRoute = new RouteModel(fromStation, toStation, startTime, endTime);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("route", newRoute);
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(AddRouteActivity.this, "Tuyến đường đã được lưu!", Toast.LENGTH_SHORT).show();
                finish();
            } catch (Exception e) {
                Toast.makeText(AddRouteActivity.this, "Định dạng giờ không hợp lệ.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package com.example.metro_app.Activity.Admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.R;

public class AdTicketDetails extends AppCompatActivity {

    private EditText editTextId, editTextTicketType, editTextPrice, editTextExpireDate;
    private Spinner spinnerStatus;

    private final String[] statusOptions = {"Hết hạn", "Đang sử dụng", "Đang chờ"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_ticket_edit);

        // 1. Ánh xạ các EditText
        editTextId = findViewById(R.id.editTextId);
        editTextTicketType = findViewById(R.id.editTextTicketType);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextExpireDate = findViewById(R.id.editTextExpireDate);
        spinnerStatus = findViewById(R.id.spinner_status);

        // 2. Gán dữ liệu cho EditText
        editTextId.setText("01");
        editTextTicketType.setText("Vé 1 Ngày");
        editTextPrice.setText("6,000đ");
        editTextExpireDate.setText("05/05/2025");

        // 3. Gán dữ liệu cho Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // 4. Gán giá trị đã chọn sẵn cho Spinner (ví dụ: "Đang sử dụng")
        int selectedIndex = getIndexOfStatus("Đang sử dụng");
        spinnerStatus.setSelection(selectedIndex);
    }

    // Tìm index của trạng thái để chọn đúng item
    private int getIndexOfStatus(String status) {
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equals(status)) {
                return i;
            }
        }
        return 0; // fallback
    }
}

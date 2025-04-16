package com.example.metro_app.Activity.Admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.metro_app.R;

public class AdHomeActivity extends AppCompatActivity {

    private Spinner spinnerTime;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        // Spinner chọn thời gian
        spinnerTime = findViewById(R.id.spinner_time);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_filters, R.layout.spinner_items);
        spinnerTime.setAdapter(adapter);
        spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                Toast.makeText(AdHomeActivity.this, "Đã chọn: " + selected, Toast.LENGTH_SHORT).show();
                // TODO: Gọi API hoặc xử lý theo thời gian được chọn
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không chọn gì
            }
        });

        // Bottom Navigation xử lý sự kiện click
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_ad_route) {
                Toast.makeText(this, "Route", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_ad_wallet) {
                Toast.makeText(this, "Wallet", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_ad_home) {
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_ad_history) {
                Toast.makeText(this, "History", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_ad_profile) {
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });

    }
}

package com.example.metro_app.Activity.Admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.content.Intent;

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

        spinnerTime = findViewById(R.id.spinner_time);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_filters, R.layout.spinner_items);
        spinnerTime.setAdapter(adapter);
        spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ad_home) {
                return true;
            } else if (id == R.id.nav_ad_route) {
                startActivity(new Intent(AdHomeActivity.this, AdRouteActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_ad_wallet) {
                startActivity(new Intent(AdHomeActivity.this, AdTicketActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_ad_userlist) {
                startActivity(new Intent(AdHomeActivity.this, AdUserActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}

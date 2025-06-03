package com.example.metro_app.Activity.Admin;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.content.Intent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.Model.FireStoreHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.metro_app.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdHomeActivity extends AppCompatActivity {

    private Spinner spinnerTime;
    private BottomNavigationView bottomNavigationView;
    private TextView tvUser;
    private  TextView tvRevenue;
    private TextView tvTicket;
    private FireStoreHelper fireStoreHelper;
    private Handler handler = new Handler();
    private Runnable dataUpdater;
    private LineChart lineChart;
    private Button btnWeekly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        spinnerTime = findViewById(R.id.spinner_time);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_filters, R.layout.spinner_items);
        tvRevenue = findViewById(R.id.tvRevenue);
        tvUser = findViewById(R.id.tvUser);
        tvTicket = findViewById(R.id.tvTicket);
        lineChart = findViewById(R.id.lineChart);
        btnWeekly = findViewById(R.id.btnWeekly);
        fireStoreHelper =new FireStoreHelper();
        spinnerTime.setAdapter(adapter);
        spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        btnWeekly.setOnClickListener(v -> {
            // Get current week and year
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int week = cal.get(Calendar.WEEK_OF_YEAR);

            fireStoreHelper.sumByDayOfWeek(year, week, new FireStoreHelper.Callback<Map<Integer, Double>>() {
                @Override
                public void onSuccess(Map<Integer, Double> result) {
                    drawDayOfWeekChart(result);
                }
                @Override
                public void onFailure(Exception e) { }
            });
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
        btnWeekly.performClick();
    }
    private void updateRevenue(){
        fireStoreHelper.getSumOfTransaction(new FireStoreHelper.OnTransactionSumCallback() {
            @Override
            public void onCallback(double sum) {
                tvRevenue.setText(String.format("%.2f Đ",sum));
            }
        });
    }
    private void updateTicket(){
        fireStoreHelper.getTotalTiketSold(new FireStoreHelper.Callback<Long>() {
            @Override
            public void onSuccess(Long result) {
                tvTicket.setText(result + " Vé");
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }
    private void updateUserCount(){
        fireStoreHelper.getTotalUser(new FireStoreHelper.Callback<Long>() {
            @Override
            public void onSuccess(Long result) {
                tvUser.setText(result + "\nTài khoản");
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }
    private void drawDayOfWeekChart(Map<Integer, Double> dataMap) {
        // Day names: 1=Sunday, 2=Monday, ..., 7=Saturday
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        List<Entry> entries = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            float value = dataMap.containsKey(i) ? dataMap.get(i).floatValue() : 0f;
            entries.add(new Entry(i - 1, value));
        }

        LineDataSet dataSet = new LineDataSet(entries, "VND");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setCircleColor(Color.RED);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setLabelRotationAngle(0f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                if (i >= 0 && i < dayNames.length) {
                    return dayNames[i];
                }
                return "";
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(12f);

        // Calculate week range (Monday - Sunday)
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.WEEK_OF_YEAR, week);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM");
        String start = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 6);
        String end = sdf.format(cal.getTime());
        Description description = new Description();
        description.setText("Tuần: " + start + " - " + end + year
        );
        description.setTextColor(Color.WHITE);
        description.setTextSize(12f);
        lineChart.setDescription(description);

        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        dataUpdater = new Runnable() {
            @Override
            public void run() {
                updateRevenue();
                updateTicket();
                updateUserCount();
                handler.postDelayed(this, 5000);
            }
        };

        handler.post(dataUpdater); // Bắt đầu chạy lần đầu
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(dataUpdater);
    }

}

package com.example.metro_app.Activity.Admin;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.content.Intent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.utils.FireStoreHelper;
import com.example.metro_app.Model.TimeFilterType;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.metro_app.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AdHomeActivity extends AppCompatActivity {

    private Spinner spinnerTime;
    private BottomNavigationView bottomNavigationView;
    private TextView tvUser;
    private TextView tvRevenue;
    private TextView tvTicket;

    private TextView tvAccess;
    private FireStoreHelper fireStoreHelper;
    private Handler handler = new Handler();
    private Runnable dataUpdater;
    private LineChart lineChart;
    private Button btnWeekly,btnMonthly,btnYearly;
    private ImageView btnScanQR;
    private int FilterState = 0;

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
        btnMonthly = findViewById(R.id.btnMonthly);
        btnYearly = findViewById(R.id.btnYearly);

        btnScanQR = findViewById(R.id.btnScanQR);
        fireStoreHelper = new FireStoreHelper();
        spinnerTime.setAdapter(adapter);

        spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH) + 1;
                int year = cal.get(Calendar.YEAR);

                TimeFilterType filterType;
                switch (position) {
                    case 0:
                        filterType = TimeFilterType.TODAY;
                        break;
                    case 1:
                        filterType = TimeFilterType.THIS_WEEK;
                        break;
                    case 2:
                        filterType = TimeFilterType.THIS_MONTH;
                        break;
                    case 3:
                        filterType = TimeFilterType.ALL;
                        break;
                    default:
                        filterType = TimeFilterType.ALL;
                }

                fireStoreHelper.getSumOfTransaction(filterType, day, month, year, sum ->
                        tvRevenue.setText(String.format("%.2f đ", sum)));

                fireStoreHelper.getTotalTickets(filterType, day, month, year, new FireStoreHelper.Callback<Long>() {
                    @Override
                    public void onSuccess(Long result) {
                        tvTicket.setText(result.toString());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        tvTicket.setText("0");
                    }
                });

                fireStoreHelper.getTotalUsers(filterType, day, month, year, new FireStoreHelper.Callback<Long>() {
                    @Override
                    public void onSuccess(Long result) {
                        tvUser.setText(result.toString());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        tvUser.setText("0");
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnWeekly.setOnClickListener(v -> drawChartByFilter(TimeFilterType.THIS_WEEK));
        btnMonthly.setOnClickListener(v -> drawChartByFilter(TimeFilterType.THIS_MONTH));
        btnYearly.setOnClickListener(v -> drawChartByFilter(TimeFilterType.ALL));


        btnScanQR.setOnClickListener(v -> {
            startActivity(new Intent(AdHomeActivity.this, ScanQRActivity.class));
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
            } else if (id == R.id.nav_ad_profile) {
                startActivity(new Intent(AdHomeActivity.this, AdDashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        btnWeekly.performClick();
        btnWeekly.performClick();
    }

    private void drawChartByFilter(TimeFilterType type) {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        fireStoreHelper.sumByFilterForChart(type, day, month, year, new FireStoreHelper.Callback<Map<Integer, Double>>() {
            @Override
            public void onSuccess(Map<Integer, Double> result) {
                drawDynamicChart(type, result);
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }
    private void drawDynamicChart(TimeFilterType type, Map<Integer, Double> dataMap) {
        List<Entry> entries = new ArrayList<>();
        String[] labels;
        int maxIndex = 0;

        switch (type) {
            case THIS_WEEK:
                labels = new String[]{"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
                maxIndex = 7;
                for (int i = 1; i <= maxIndex; i++) {
                    float value = dataMap.getOrDefault(i, 0.0).floatValue();
                    entries.add(new Entry(i - 1, value));
                }
                break;

            case THIS_MONTH:
                Calendar cal = Calendar.getInstance();
                int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                labels = new String[daysInMonth];
                for (int i = 1; i <= daysInMonth; i++) {
                    labels[i - 1] = String.valueOf(i);
                    float value = dataMap.getOrDefault(i, 0.0).floatValue();
                    entries.add(new Entry(i - 1, value));
                }
                break;

            case ALL:
                labels = new String[]{"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
                for (int i = 1; i <= 12; i++) {
                    float value = dataMap.getOrDefault(i, 0.0).floatValue();
                    entries.add(new Entry(i - 1, value));
                }
                break;

            default:
                labels = new String[]{"1"};
                entries.add(new Entry(0, 0));
        }

        LineDataSet dataSet = new LineDataSet(entries, "VNĐ");
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
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                return i >= 0 && i < labels.length ? labels[i] : "";
            }
        });

        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setTextColor(Color.WHITE);
        lineChart.getLegend().setTextSize(12f);

        Description description = new Description();
        switch (type) {
            case THIS_WEEK:
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int week = cal.get(Calendar.WEEK_OF_YEAR);
                cal.set(Calendar.WEEK_OF_YEAR, week);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                String start = new SimpleDateFormat("dd/MM").format(cal.getTime());
                cal.add(Calendar.DAY_OF_MONTH, 6);
                String end = new SimpleDateFormat("dd/MM").format(cal.getTime());
                description.setText("Tuần: " + start + " - " + end + "/" + year);
                break;
            case THIS_MONTH:
                description.setText("Doanh thu theo ngày trong tháng");
                break;
            case ALL:
                description.setText("Doanh thu theo tháng trong năm");
                break;
        }

        description.setTextColor(Color.WHITE);
        description.setTextSize(12f);
        lineChart.setDescription(description);

        lineChart.animateX(1000);
        lineChart.invalidate();
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}

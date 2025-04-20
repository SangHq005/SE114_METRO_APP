package com.example.metro_app.Activity.User;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.R;

public class TransactionHistoryActivity extends AppCompatActivity {

    private ImageView calendarImg, closePopup;
    private LinearLayout calendarPopup;
    private CalendarView calendarView;
    private Button btnApply;
    private TextView filterTxt, resetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);  // Your layout file

        // Initialize views
        calendarImg = findViewById(R.id.calendarImg);
        calendarPopup = findViewById(R.id.calendarPopup);
        calendarView = findViewById(R.id.calendarView);
        btnApply = findViewById(R.id.btnApply);
        filterTxt = findViewById(R.id.filterTxt);
        closePopup = findViewById(R.id.closePopup);
        resetBtn = findViewById(R.id.resetBtn);

        // Handle calendar icon click
        calendarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle visibility of the calendar popup
                if (calendarPopup.getVisibility() == View.GONE) {
                    calendarPopup.setVisibility(View.VISIBLE);
                } else {
                    calendarPopup.setVisibility(View.GONE);
                }
            }
        });

        // Handle close popup button click
        closePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the calendar popup
                calendarPopup.setVisibility(View.GONE);
            }
        });

        // Handle apply button click
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected date from the calendar
                long selectedDate = calendarView.getDate();
                // You can format the date as per your requirement (e.g., using SimpleDateFormat)
                String formattedDate = formatDate(selectedDate); // Implement this method for date formatting

                // Set the formatted date to the filter text
                filterTxt.setText(formattedDate);

                // Hide the calendar popup after applying the filter
                calendarPopup.setVisibility(View.GONE);
            }
        });

        // Handle reset button click (optional, if you want to reset the filter to default)
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset the filter text to default
                filterTxt.setText("01-30/04/2025");
                // Close the popup
                calendarPopup.setVisibility(View.GONE);
            }
        });
    }

    // Method to format the selected date (you can customize this as needed)
    private String formatDate(long timestamp) {
        // Use SimpleDateFormat to format the date
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(new java.util.Date(timestamp));
    }
}

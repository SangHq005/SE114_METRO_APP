package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.metro_app.R;

public class ChooseTicketActivity extends AppCompatActivity {

    private String userUUID;
    private String ticketTypeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose_ticket);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("UUID")) {
                userUUID = intent.getStringExtra("UUID");
                System.out.println("Received UUID in ChooseTicketActivity: " + userUUID);
            }
            if (intent.hasExtra("ticket_type_id")) {
                ticketTypeId = intent.getStringExtra("ticket_type_id");
                System.out.println("Received ticket_type_id in ChooseTicketActivity: " + ticketTypeId);
            }
        }

        TextView typeTicketTxt = findViewById(R.id.typeTicketTxt);
        TextView loaiVeTxt = findViewById(R.id.loaiVeTxt);
        TextView hsdTxt = findViewById(R.id.hsdTxt);
        TextView luuYTxt = findViewById(R.id.luuYTxt);
        Button muaBtn = findViewById(R.id.muaBtn);

        String ticketName = getIntent().getStringExtra("ticket_name");
        String ticketPrice = getIntent().getStringExtra("ticket_price");
        String ticketActive = getIntent().getStringExtra("ticket_active");
        String ticketAutoActive = getIntent().getStringExtra("ticket_auto_active");


        typeTicketTxt.setText(ticketName != null ? ticketName : "Không có thông tin");
        loaiVeTxt.setText("Loại vé: " + (ticketName != null ? ticketName : "Không có thông tin"));
        hsdTxt.setText("HSD: " + (ticketActive != null ? ticketActive : "0") + " ngày kể từ ngày kích hoạt");
        luuYTxt.setText("Tự động kích hoạt sau " + (ticketAutoActive != null ? ticketAutoActive : "0") + " ngày kể từ ngày mua");

        muaBtn.setOnClickListener(v -> {
            Intent orderIntent = new Intent(ChooseTicketActivity.this, OrderInfoActivity.class);
            orderIntent.putExtra("ticket_type_id", ticketTypeId);
            orderIntent.putExtra("ticket_name", ticketName != null ? ticketName : "Không có thông tin");
            orderIntent.putExtra("ticket_price", ticketPrice != null ? ticketPrice : "0 VND");
            orderIntent.putExtra("ticket_expiration", ticketActive != null ? ticketActive : "0");
            orderIntent.putExtra("ticket_auto_active", ticketAutoActive != null ? ticketAutoActive : "0");
            orderIntent.putExtra("UUID", userUUID);


            startActivity(orderIntent);
        });
    }
}
package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.metro_app.R;

public class ChooseTicketActivity extends AppCompatActivity {

    private String userUUID; // Biến để lưu UUID
    private String ticketTypeId; // Biến để lưu ID của ticketType

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose_ticket);

        // Lấy UUID và ticketTypeId từ Intent
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

        // Khởi tạo views
        TextView typeTicketTxt = findViewById(R.id.typeTicketTxt);
        TextView loaiVeTxt = findViewById(R.id.loaiVeTxt);
        TextView hsdTxt = findViewById(R.id.hsdTxt);
        TextView luuYTxt = findViewById(R.id.luuYTxt);
        Button muaBtn = findViewById(R.id.muaBtn);

        // Lấy dữ liệu từ Intent để hiển thị
        String ticketName = getIntent().getStringExtra("ticket_name");
        String ticketPrice = getIntent().getStringExtra("ticket_price");
        String ticketExpiration = getIntent().getStringExtra("ticket_expiration");
        String ticketNote = getIntent().getStringExtra("ticket_note");

        // Hiển thị dữ liệu vào các TextView
        typeTicketTxt.setText(ticketName != null ? ticketName : "Không có thông tin");
        loaiVeTxt.setText("Loại vé: " + (ticketName != null ? ticketName : "Không có thông tin"));
        hsdTxt.setText("HSD: " + (ticketExpiration != null ? ticketExpiration : "Không có thông tin"));
        luuYTxt.setText("Lưu ý: " + (ticketNote != null ? ticketNote : "Không có thông tin"));

        // Xử lý sự kiện nhấn "Mua ngay"
        muaBtn.setOnClickListener(v -> {
            Intent orderIntent = new Intent(ChooseTicketActivity.this, OrderInfoActivity.class);
            orderIntent.putExtra("ticket_type_id", ticketTypeId); // Truyền ID của ticketType
            orderIntent.putExtra("ticket_name", ticketName != null ? ticketName : "Không có thông tin"); // Truyền name để hiển thị
            orderIntent.putExtra("ticket_price", ticketPrice != null ? ticketPrice : "0 VND");
            orderIntent.putExtra("ticket_expiration", ticketExpiration != null ? ticketExpiration : "Không có thông tin");
            orderIntent.putExtra("ticket_note", ticketNote != null ? ticketNote : "Không có thông tin");
            orderIntent.putExtra("UUID", userUUID);
            startActivity(orderIntent);
        });
    }
}
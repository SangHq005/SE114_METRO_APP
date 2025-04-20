package com.example.metro_app.Activity.User;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.Domain.PaymentMethodDialog;
import com.example.metro_app.R;

public class OrderInfoActivity extends AppCompatActivity implements PaymentMethodDialog.PaymentMethodListener {

    private TextView tvSelectedPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info);

        ImageView btnPaymentMethod = findViewById(R.id.rightChevronBtn);
        tvSelectedPayment = findViewById(R.id.paymentMethodTxt); // Thêm TextView này vào layout

        btnPaymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPaymentMethodDialog();
            }
        });
    }

    private void showPaymentMethodDialog() {
        PaymentMethodDialog dialog = new PaymentMethodDialog();
        dialog.setPaymentMethodListener(this);
        dialog.show(getSupportFragmentManager(), "PaymentMethodDialog");
    }

    @Override
    public void onPaymentMethodSelected(String method) {
        tvSelectedPayment.setText("Phương thức đã chọn: " + method);
    }
}
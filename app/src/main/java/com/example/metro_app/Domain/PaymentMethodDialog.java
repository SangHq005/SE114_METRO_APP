package com.example.metro_app.Domain;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.metro_app.R;

public class PaymentMethodDialog extends DialogFragment {

    private Context context;
    private RadioGroup radioGroupPayment;
    private Button btnConfirm;
    private PaymentMethodListener listener;

    public interface PaymentMethodListener {
        void onPaymentMethodSelected(String method);
    }

    // Constructor nhận Context
    public PaymentMethodDialog(@NonNull Context context) {
        this.context = context;
    }

    public void setPaymentMethodListener(PaymentMethodListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_payment_method, container, false);

        radioGroupPayment = view.findViewById(R.id.radio_group_payment);
        btnConfirm = view.findViewById(R.id.btn_confirm);

        // Khởi tạo các LinearLayout và RadioButton
        LinearLayout layoutWkb = (LinearLayout) radioGroupPayment.getChildAt(0);
        LinearLayout layoutInternational = (LinearLayout) radioGroupPayment.getChildAt(1);
        LinearLayout layoutMomo = (LinearLayout) radioGroupPayment.getChildAt(2);

        RadioButton radioWkb = view.findViewById(R.id.radio_wkb);
        RadioButton radioInternational = view.findViewById(R.id.radio_international);
        RadioButton radioMomo = view.findViewById(R.id.radio_momo);

        // Gán sự kiện onClick cho từng LinearLayout để chọn RadioButton
        layoutWkb.setOnClickListener(v -> {
            radioGroupPayment.check(R.id.radio_wkb);
        });

        layoutInternational.setOnClickListener(v -> {
            radioGroupPayment.check(R.id.radio_international);
        });

        layoutMomo.setOnClickListener(v -> {
            radioGroupPayment.check(R.id.radio_momo);
        });

        // Xử lý sự kiện nhấn nút "Xác nhận"
        btnConfirm.setOnClickListener(v -> {
            int selectedId = radioGroupPayment.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(context, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            String paymentMethod = "";
            if (selectedId == R.id.radio_wkb) {
                paymentMethod = "VN PAY";
            } else if (selectedId == R.id.radio_international) {
                paymentMethod = "Thẻ quốc tế";
            } else if (selectedId == R.id.radio_momo) {
                paymentMethod = "Ví MoMo";
            }

            if (listener != null) {
                listener.onPaymentMethodSelected(paymentMethod);
            }

            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}
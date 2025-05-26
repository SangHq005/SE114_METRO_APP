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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.metro_app.R;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodDialog extends DialogFragment {

    private Context context;
    private RadioGroup radioGroupPayment;
    private Button btnConfirm;
    private PaymentMethodListener listener;
    private List<PaymentMethod> paymentMethods;

    public interface PaymentMethodListener {
        void onPaymentMethodSelected(String method);
    }

    // Constructor nhận Context
    public PaymentMethodDialog(@NonNull Context context) {
        this.context = context;
        // Khởi tạo danh sách phương thức thanh toán
        paymentMethods = new ArrayList<>();
        paymentMethods.add(new PaymentMethod("VN PAY", "Thanh toán qua VNPay", R.drawable.logo_vnpay));
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

        // Tạo các RadioButton động dựa trên danh sách paymentMethods
        radioGroupPayment.removeAllViews();
        for (int i = 0; i < paymentMethods.size(); i++) {
            PaymentMethod method = paymentMethods.get(i);
            LinearLayout layout = new LinearLayout(context);
            layout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(0, 8, 0, 8);

            RadioButton radioButton = new RadioButton(context);
            radioButton.setId(View.generateViewId());
            radioButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            TextView textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            textView.setText(method.name);
            textView.setTextSize(16);
            textView.setPadding(8, 0, 0, 0);

            layout.addView(radioButton);
            layout.addView(textView);

            layout.setOnClickListener(v -> radioGroupPayment.check(radioButton.getId()));
            radioGroupPayment.addView(layout);
        }

        // Xử lý sự kiện nhấn nút "Xác nhận"
        btnConfirm.setOnClickListener(v -> {
            int selectedId = radioGroupPayment.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(context, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tìm phương thức được chọn dựa trên RadioButton được chọn
            String paymentMethod = null;
            for (int i = 0; i < radioGroupPayment.getChildCount(); i++) {
                LinearLayout layout = (LinearLayout) radioGroupPayment.getChildAt(i);
                RadioButton radioButton = (RadioButton) layout.getChildAt(0);
                if (radioButton.getId() == selectedId) {
                    paymentMethod = paymentMethods.get(i).name;
                    break;
                }
            }

            if (listener != null && paymentMethod != null) {
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
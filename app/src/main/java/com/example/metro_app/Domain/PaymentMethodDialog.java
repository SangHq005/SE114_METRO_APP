package com.example.metro_app.Domain;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.metro_app.R;

public class PaymentMethodDialog extends DialogFragment {

    private RadioButton radioVNPay;
    private LinearLayout vnpayLayout;
    private Button btnConfirm;

    private PaymentMethodListener listener;

    public interface PaymentMethodListener {
        void onPaymentMethodSelected(String methodName, int logoResId);
    }
    public void setPaymentMethodListener(PaymentMethodListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_payment_method, container, false);

        radioVNPay = view.findViewById(R.id.radio_vnpay);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        vnpayLayout = view.findViewById(R.id.vnpay_layout);

        // Bấm cả layout cũng chọn được radio
        vnpayLayout.setOnClickListener(v -> radioVNPay.setChecked(true));

        btnConfirm.setOnClickListener(v -> {
            if (!radioVNPay.isChecked()) {
                Toast.makeText(requireContext(), "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onPaymentMethodSelected("VN PAY", R.drawable.logo_vnpay);
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
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}

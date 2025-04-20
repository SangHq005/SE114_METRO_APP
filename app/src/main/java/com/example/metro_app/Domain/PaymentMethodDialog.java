package com.example.metro_app.Domain;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.metro_app.R;

public class PaymentMethodDialog extends DialogFragment {

    private RadioGroup radioGroupPayment;
    private Button btnConfirm;
    private PaymentMethodListener listener;

    public interface PaymentMethodListener {
        void onPaymentMethodSelected(String method);
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

        // Thiết lập phương thức mặc định (nếu cần)
        // radioGroupPayment.check(R.id.radio_wkb);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = radioGroupPayment.getCheckedRadioButtonId();

                if (selectedId == -1) {
                    Toast.makeText(getContext(), "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                    return;
                }

                RadioButton radioButton = radioGroupPayment.findViewById(selectedId);
                String paymentMethod = "";

                if (radioButton.getId() == R.id.radio_wkb) {
                    paymentMethod = "WKB";
                } else if (radioButton.getId() == R.id.radio_international) {
                    paymentMethod = "Thẻ quốc tế";
                } else if (radioButton.getId() == R.id.radio_momo) {
                    paymentMethod = "Ví MoMo";
                }

                if (listener != null) {
                    listener.onPaymentMethodSelected(paymentMethod);
                }

                dismiss();
            }
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
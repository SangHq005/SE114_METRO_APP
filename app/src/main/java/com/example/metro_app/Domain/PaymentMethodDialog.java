package com.example.metro_app.Domain;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodDialog extends DialogFragment {

    private static final String TAG = "PaymentMethodDialog";
    private Context context;
    private RadioGroup radioGroupPayment;
    private Button btnConfirm;
    private PaymentMethodListener listener;
    private List<PaymentMethod> paymentMethods;

    public interface PaymentMethodListener {
        void onPaymentMethodSelected(String method);
    }

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
            RadioButton radioButton = new RadioButton(context);
            radioButton.setId(View.generateViewId());
            radioButton.setText(method.name);
            radioButton.setTextSize(16);
            radioButton.setPadding(8, 8, 8, 8);
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
            ));

            // Gán sự kiện click cho RadioButton
            radioButton.setOnClickListener(v -> {
                radioButton.setChecked(true);
                Log.d(TAG, "RadioButton clicked for: " + method.name);
            });

            radioGroupPayment.addView(radioButton);
        }

        // Xử lý sự kiện nhấn nút "Xác nhận"
        btnConfirm.setOnClickListener(v -> {
            int selectedId = radioGroupPayment.getCheckedRadioButtonId();
            Log.d(TAG, "Selected RadioButton ID: " + selectedId);
            if (selectedId == -1) {
                Toast.makeText(context, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tìm phương thức được chọn
            String paymentMethod = null;
            for (int i = 0; i < radioGroupPayment.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) radioGroupPayment.getChildAt(i);
                if (radioButton.isChecked()) {
                    paymentMethod = paymentMethods.get(i).name;
                    break;
                }
            }

            if (paymentMethod == null) {
                Log.e(TAG, "Payment method is null despite selectedId: " + selectedId);
                Toast.makeText(context, "Lỗi: Không xác định được phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onPaymentMethodSelected(paymentMethod);
                Log.d(TAG, "Payment method selected and sent to listener: " + paymentMethod);
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
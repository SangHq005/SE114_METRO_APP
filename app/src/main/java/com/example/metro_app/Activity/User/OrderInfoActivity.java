package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.R;
import com.example.metro_app.Activity.User.MyTicketsActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vnpay.authentication.VNP_AuthenticationActivity;
import com.vnpay.authentication.VNP_SdkCompletedCallback;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class OrderInfoActivity extends AppCompatActivity {

    private DecimalFormat decimalFormat;
    private Button thanhToanBtn;
    private long ticketPrice;
    private String lastOrderId;
    private String lastTicketName;
    private boolean isPaymentProcessed = false; // Cờ để ngăn xử lý Intent lặp lại
    private static final String VNPAY_TMN_CODE = "Y5NZW2G4";
    private static final String VNPAY_HASH_SECRET = "0JJIFBP7MHJSFP2FM7GFSHHANH8Q7FUN";
    private static final String VNPAY_SCHEME = "metroapp";
    private static final String VNPAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info);

        decimalFormat = new DecimalFormat("#,###");
        decimalFormat.setGroupingSize(3);

        String ticketName = getIntent().getStringExtra("ticket_name");
        String ticketPriceStr = getIntent().getStringExtra("ticket_price");
        String ticketExpiration = getIntent().getStringExtra("ticket_expiration");
        String ticketNote = getIntent().getStringExtra("ticket_note");

        ticketPrice = 0;
        if (ticketPriceStr != null) {
            try {
                ticketPrice = Long.parseLong(ticketPriceStr.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                Log.e("OrderInfo", "Lỗi định dạng giá vé: ", e);
            }
        }

        TextView sanphamTxt = findViewById(R.id.sanphamTxt);
        TextView donGiaTxt = findViewById(R.id.donGiaTxt);
        TextView soLuongTxt = findViewById(R.id.soLuongTxt);
        TextView thanhTienTxt = findViewById(R.id.thanhTienTxt);
        TextView tongTienTxt = findViewById(R.id.tongTienTxt);
        TextView tongThanhTienTxt = findViewById(R.id.tongThanhTienTxt);
        TextView loaiVeTxt = findViewById(R.id.loaiVeTxt);
        TextView hsdTxt = findViewById(R.id.hsdTxt);
        TextView luuYTxt = findViewById(R.id.luuYTxt);
        thanhToanBtn = findViewById(R.id.thanhToanBtn);

        sanphamTxt.setText(ticketName != null ? ticketName : "Không có thông tin");
        donGiaTxt.setText(ticketPriceStr != null ? ticketPriceStr : "0 VND");
        int quantity = 1;
        soLuongTxt.setText(String.valueOf(quantity));
        long total = ticketPrice * quantity;
        String totalFormatted = decimalFormat.format(total) + " VND";
        thanhTienTxt.setText(totalFormatted);
        tongTienTxt.setText(totalFormatted);
        tongThanhTienTxt.setText(totalFormatted);

        loaiVeTxt.setText(ticketName != null ? ticketName : "Không có thông tin");
        hsdTxt.setText(ticketExpiration != null ? ticketExpiration : "Không có thông tin");
        luuYTxt.setText(ticketNote != null ? ticketNote : "Không có thông tin");

        thanhToanBtn.setOnClickListener(v -> initiateVNPayPayment(ticketPrice, ticketName));
    }

    private void initiateVNPayPayment(long amount, String ticketName) {
        try {
            String orderId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
            String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            this.lastOrderId = orderId;
            this.lastTicketName = ticketName;
            this.isPaymentProcessed = false; // Reset cờ khi bắt đầu thanh toán

            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", VNPAY_TMN_CODE);
            vnpParams.put("vnp_Amount", String.valueOf(amount * 100));
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", orderId);
            vnpParams.put("vnp_OrderInfo", "Thanh toan ve: " + ticketName);
            vnpParams.put("vnp_OrderType", "250000");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", "https://metro-app-3e121.web.app/vnpay_redirect.html");
            vnpParams.put("vnp_IpAddr", "127.0.0.1");
            vnpParams.put("vnp_CreateDate", createDate);

            StringBuilder query = new StringBuilder();
            for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
                if (query.length() > 0) query.append("&");
                query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            }

            String hashData = vnpParams.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> {
                        try {
                            return e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8.toString());
                        } catch (UnsupportedEncodingException ex) {
                            throw new RuntimeException(ex);
                        }
                    })
                    .collect(Collectors.joining("&"));
            String secureHash = HmacSHA512(hashData, VNPAY_HASH_SECRET);

            query.append("&vnp_SecureHash=").append(secureHash);

            String paymentUrl = VNPAY_URL + "?" + query.toString();
            Log.d("VNPay", "Payment URL: " + paymentUrl);

            Intent intent = new Intent(this, VNP_AuthenticationActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("url", paymentUrl);
            bundle.putString("scheme", VNPAY_SCHEME);
            bundle.putString("tmn_code", VNPAY_TMN_CODE);
            bundle.putBoolean("is_sandbox", true);
            intent.putExtras(bundle);

            VNP_AuthenticationActivity.setSdkCompletedCallback(new VNP_SdkCompletedCallback() {
                @Override
                public void sdkAction(String action) {
                    Log.d("VNPay", "Callback action received: " + action);
                    handleVNPayResult(action, lastOrderId, lastTicketName);
                }
            });

            Log.d("VNPay", "Starting VNP_AuthenticationActivity");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo thanh toán: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("VNPay", "Error initiating payment: ", e);
        }
    }

    private void handleVNPayResult(String action, String orderId, String ticketName) {
        Log.d("VNPay", "Handling action: " + action);
        if (isPaymentProcessed) {
            Log.d("VNPay", "Payment already processed, ignoring callback");
            return;
        }
        if (action.equals("SuccessBackAction")) {
            isPaymentProcessed = true;
            String transactionNo = UUID.randomUUID().toString().substring(0, 10);
            saveTransactionToFirestore(orderId, transactionNo, ticketName, ticketPrice);
            Toast.makeText(this, "Thanh toán thành công! Mã giao dịch: " + transactionNo, Toast.LENGTH_LONG).show();
            Intent successIntent = new Intent(this, HomeActivity.class);
            successIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(successIntent);
            finish();
        } else if (action.equals("FaildBackAction")) {
            Toast.makeText(this, "Thanh toán thất bại", Toast.LENGTH_LONG).show();
            finish();
        } else if (action.equals("WebBackAction") || action.equals("AppBackAction")) {
            Toast.makeText(this, "Thanh toán bị hủy", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Hành động không xác định: " + action, Toast.LENGTH_LONG).show();
            Log.e("VNPay", "Unknown action: " + action);
            finish();
        }
    }

    private void saveTransactionToFirestore(String orderId, String transactionNo, String ticketName, long amount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("orderId", orderId);
        transaction.put("transactionNo", transactionNo);
        transaction.put("ticketName", ticketName);
        transaction.put("amount", amount);
        transaction.put("status", "SUCCESS");
        transaction.put("timestamp", new Date());

        db.collection("Transactions")
                .document(orderId)
                .set(transaction)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Lưu giao dịch thành công: " + orderId);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi lưu giao dịch: ", e);
                    Toast.makeText(this, "Lỗi lưu giao dịch: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        Map<String, Object> ticket = new HashMap<>();
        ticket.put("ticketName", ticketName != null ? ticketName : "Không có thông tin");
        ticket.put("timestamp", new Date());

        db.collection("Ticket")
                .document(orderId)
                .set(ticket)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Lưu vé thành công: " + orderId);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi lưu vé: ", e);
                    Toast.makeText(this, "Lỗi lưu vé: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String HmacSHA512(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d("VNPay", "onNewIntent called with intent: " + (intent != null ? intent.toString() : "null"));
        if (isPaymentProcessed) {
            Log.d("VNPay", "Payment already processed, ignoring new intent");
            return;
        }
        if (intent != null) {
            Log.d("VNPay", "Intent action: " + intent.getAction());
            Log.d("VNPay", "Intent data: " + (intent.getData() != null ? intent.getData().toString() : "null"));
            if (intent.getData() != null) {
                Uri data = intent.getData();
                Log.d("VNPay", "Return URL: " + data.toString());
                String responseCode = data.getQueryParameter("vnp_ResponseCode");
                String transactionNo = data.getQueryParameter("vnp_TransactionNo");
                String orderId = data.getQueryParameter("vnp_TxnRef");
                Log.d("VNPay", "ResponseCode: " + responseCode + ", TransactionNo: " + transactionNo + ", OrderId: " + orderId);
                if (responseCode != null && transactionNo != null && orderId != null) {
                    isPaymentProcessed = true;
                    if ("00".equals(responseCode)) {
                        saveTransactionToFirestore(orderId, transactionNo, lastTicketName, ticketPrice);
                        Toast.makeText(this, "Thanh toán thành công! Mã giao dịch: " + transactionNo, Toast.LENGTH_LONG).show();
                        Intent successIntent = new Intent(this, MyTicketsActivity.class);
                        successIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(successIntent);
                        finish();
                    } else {
                        Toast.makeText(this, "Thanh toán thất bại, mã lỗi: " + responseCode, Toast.LENGTH_LONG).show();
                        Log.e("VNPay", "Payment failed with response code: " + responseCode);
                        finish();
                    }
                } else {
                    Log.e("VNPay", "Missing responseCode, transactionNo, or orderId in return URL");
                    Toast.makeText(this, "Lỗi xử lý kết quả thanh toán: Thiếu tham số", Toast.LENGTH_LONG).show();
                    finish();
                }
            } else {
                Log.e("VNPay", "No data in Intent");
                Toast.makeText(this, "Không nhận được dữ liệu thanh toán", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Log.e("VNPay", "Intent is null");
            Toast.makeText(this, "Không nhận được kết quả thanh toán", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}

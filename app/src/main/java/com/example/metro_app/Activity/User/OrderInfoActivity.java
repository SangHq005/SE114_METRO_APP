package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.Domain.PaymentMethodDialog;
import com.example.metro_app.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vnpay.authentication.VNP_AuthenticationActivity;
import com.vnpay.authentication.VNP_SdkCompletedCallback;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    private LinearLayout paymentMethodCard;
    private TextView paymentMethodTxt;
    private ImageView backBtn;
    private long ticketPrice;
    private String lastOrderId;
    private String lastTicketTypeId;
    private String userId;
    private String ticketExpiration;
    private String ticketAutoActive;
    private boolean isPaymentProcessed = false;
    private String selectedPaymentMethod = null;
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

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        userId = prefs.getString("UserID", null);
        Log.d("OrderInfo", "Retrieved userId from SharedPreferences: " + userId);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("ticket_type_id")) {
                lastTicketTypeId = intent.getStringExtra("ticket_type_id");
                Log.d("OrderInfo", "Received ticket_type_id in OrderInfoActivity: " + lastTicketTypeId);
            }
        }

        String ticketName = getIntent().getStringExtra("ticket_name");
        String ticketPriceStr = getIntent().getStringExtra("ticket_price");
        ticketExpiration = getIntent().getStringExtra("ticket_expiration");
        ticketAutoActive = getIntent().getStringExtra("ticket_auto_active");

        Log.d("OrderInfo", "Received ticket_expiration in onCreate: " + ticketExpiration);
        Log.d("OrderInfo", "Received ticket_auto_active in onCreate: " + ticketAutoActive);

        ticketPrice = 0;
        if (ticketPriceStr != null) {
            try {
                ticketPrice = Long.parseLong(ticketPriceStr.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                Log.e("OrderInfo", "Lỗi định dạng giá vé: ", e);
            }
        }

        // Ánh xạ view
        TextView sanphamTxt = findViewById(R.id.sanphamTxt);
        TextView donGiaTxt = findViewById(R.id.donGiaTxt);
        TextView soLuongTxt = findViewById(R.id.soLuongTxt);
        TextView thanhTienTxt = findViewById(R.id.thanhTienTxt);
        TextView tongTienTxt = findViewById(R.id.tongTienTxt);
        TextView loaiVeTxt = findViewById(R.id.loaiVeTxt);
        TextView hsdTxt = findViewById(R.id.hsdTxt);
        TextView luuYTxt = findViewById(R.id.luuYTxt);
        thanhToanBtn = findViewById(R.id.thanhToanBtn);
        paymentMethodCard = findViewById(R.id.paymentMethodCard);
        paymentMethodTxt = findViewById(R.id.paymentMethodTxt);
        backBtn = findViewById(R.id.backBtn);

        // Cập nhật giao diện
        sanphamTxt.setText(ticketName != null ? ticketName : "Không có thông tin");
        donGiaTxt.setText(ticketPriceStr != null ? ticketPriceStr : "0 VND");
        int quantity = 1;
        soLuongTxt.setText(String.valueOf(quantity));
        long total = ticketPrice * quantity;
        String totalFormatted = decimalFormat.format(total) + " VND";
        thanhTienTxt.setText(totalFormatted);
        tongTienTxt.setText(totalFormatted);

        loaiVeTxt.setText(ticketName != null ? ticketName : "Không có thông tin");
        hsdTxt.setText("HSD: " + (ticketExpiration != null ? ticketExpiration : "0") + " ngày kể từ ngày kích hoạt");
        luuYTxt.setText("Tự động kích hoạt sau " + (ticketAutoActive != null ? ticketAutoActive : "0") + " ngày kể từ ngày mua");

        // Sự kiện nhấn vào paymentMethodCard để mở PaymentMethodDialog
        paymentMethodCard.setOnClickListener(v -> {
            PaymentMethodDialog dialog = new PaymentMethodDialog(OrderInfoActivity.this);
            dialog.setPaymentMethodListener(method -> {
                selectedPaymentMethod = method;
                paymentMethodTxt.setText(method);
                Toast.makeText(OrderInfoActivity.this, "Đã chọn phương thức: " + method, Toast.LENGTH_SHORT).show();
            });
            dialog.show(getSupportFragmentManager(), "PaymentMethodDialog");
        });

        // Sự kiện nhấn nút Thanh toán
        thanhToanBtn.setOnClickListener(v -> {
            if (selectedPaymentMethod == null || !selectedPaymentMethod.equals("VN PAY")) {
                Toast.makeText(OrderInfoActivity.this, "Vui lòng chọn phương thức thanh toán VN PAY", Toast.LENGTH_SHORT).show();
                return;
            }
            initiateVNPayPayment(selectedPaymentMethod, ticketPrice);
        });

        // Sự kiện nhấn nút Back
        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(OrderInfoActivity.this, MyTicketsActivity.class));
            finish();
        });
    }

    private void initiateVNPayPayment(String ticketName, long amount) {
        try {
            String orderId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            this.lastOrderId = orderId;
            this.isPaymentProcessed = false;

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
                    handleVNPayResult(action, lastOrderId);
                }
            });

            Log.d("VNPay", "Starting VNP_AuthenticationActivity");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo thanh toán: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("VNPay", "Error initiating payment: ", e);
        }
    }

    private void handleVNPayResult(String action, String orderId) {
        Log.d("VNPay", "Handling action: " + action);
        if (isPaymentProcessed) {
            Log.d("VNPay", "Payment already processed, ignoring callback");
            return;
        }
        if (action.equals("SuccessBackAction")) {
            isPaymentProcessed = true;
            String transactionNo = UUID.randomUUID().toString().substring(0, 10);
            Toast.makeText(this, "Thanh toán thành công! Mã giao dịch: " + transactionNo, Toast.LENGTH_LONG).show();
            saveTransactionToFirestore(orderId, transactionNo, lastTicketTypeId, ticketPrice);
            Intent successIntent = new Intent(this, YourTicketsActivity.class);
            successIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(successIntent);
            finish();
            Log.d("OrderInfo", "Navigated to YourTicketsActivity after payment success");
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

    private void saveTransactionToFirestore(String orderId, String transactionNo, String ticketTypeId, long amount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        int expirationDays = 0;
        int autoActiveDays = 0;
        try {
            if (ticketExpiration == null || ticketExpiration.equals("null")) {
                throw new IllegalArgumentException("Expiration không được null (ticket_type_id: " + ticketTypeId + ")");
            }
            if (ticketAutoActive == null || ticketAutoActive.equals("null")) {
                throw new IllegalArgumentException("AutoActive không được null (ticket_type_id: " + ticketTypeId + ")");
            }
            expirationDays = Integer.parseInt(ticketExpiration);
            autoActiveDays = Integer.parseInt(ticketAutoActive);
            Log.d("Firestore", "expirationDays: " + expirationDays + ", autoActiveDays: " + autoActiveDays);

            if (expirationDays <= 0 || autoActiveDays < 0) {
                throw new IllegalArgumentException("ExpirationDays phải lớn hơn 0 và AutoActiveDays không được âm (ticket_type_id: " + ticketTypeId + ")");
            }
        } catch (NumberFormatException e) {
            Log.e("Firestore", "Lỗi định dạng expiration hoặc autoActive: ticket_expiration=" + ticketExpiration + ", ticket_auto_active=" + ticketAutoActive, e);
            Toast.makeText(this, "Lỗi: Dữ liệu Expiration hoặc AutoActive không hợp lệ", Toast.LENGTH_LONG).show();
            return;
        } catch (IllegalArgumentException e) {
            Log.e("Firestore", "Lỗi giá trị: " + e.getMessage());
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        Date currentTimestamp = new Date();
        Log.d("Firestore", "Current Timestamp: " + currentTimestamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTimestamp);
        calendar.add(Calendar.DAY_OF_MONTH, autoActiveDays);
        Date autoActiveDate = calendar.getTime();
        Log.d("Firestore", "AutoActiveDate: " + autoActiveDate);

        calendar.setTime(autoActiveDate);
        calendar.add(Calendar.DAY_OF_MONTH, expirationDays);
        Date expirationDate = calendar.getTime();
        Log.d("Firestore", "ExpirationDate: " + expirationDate);

        DocumentReference counterRef = db.collection("Metadata").document("ticketCounter");
        db.runTransaction(transaction -> {
            Long currentCode = transaction.get(counterRef).getLong("lastTicketCode");
            if (currentCode == null) {
                currentCode = 100000L;
                Map<String, Object> initialCounter = new HashMap<>();
                initialCounter.put("lastTicketCode", currentCode);
                transaction.set(counterRef, initialCounter);
            }

            String ticketCode = String.valueOf(currentCode + 1);

            Map<String, Object> counterUpdate = new HashMap<>();
            counterUpdate.put("lastTicketCode", currentCode + 1);
            transaction.set(counterRef, counterUpdate);

            Map<String, Object> transactionData = new HashMap<>();
            transactionData.put("orderId", orderId);
            transactionData.put("transactionNo", transactionNo);
            transactionData.put("ticketTypeId", ticketTypeId);
            transactionData.put("amount", amount);
            transactionData.put("status", "SUCCESS");
            transactionData.put("timestamp", currentTimestamp);

            Map<String, Object> ticket = new HashMap<>();
            ticket.put("ticketTypeId", ticketTypeId != null ? ticketTypeId : "unknown");
            ticket.put("ticketCode", ticketCode);
            ticket.put("timestamp", currentTimestamp);
            ticket.put("userId", userId != null ? userId : "unknown");
            ticket.put("Status", "Chưa kích hoạt");
            ticket.put("AutoActiveDate", autoActiveDate);
            ticket.put("ExpirationDate", expirationDate);

            transaction.set(db.collection("Transactions").document(orderId), transactionData);
            transaction.set(db.collection("Ticket").document(orderId), ticket);

            return ticketCode;
        }).addOnSuccessListener(ticketCode -> {
            Log.d("Firestore", "Lưu giao dịch và vé thành công: " + orderId + ", ticketCode: " + ticketCode);
            Intent successIntent = new Intent(this, YourTicketsActivity.class);
            successIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(successIntent);
            finish();
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Lỗi lưu giao dịch/vé: ", e);
            Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(this, "Thanh toán thành công! Mã giao dịch: " + transactionNo, Toast.LENGTH_LONG).show();
                        saveTransactionToFirestore(orderId, transactionNo, lastTicketTypeId, ticketPrice);
                        Intent successIntent = new Intent(this, YourTicketsActivity.class);
                        successIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(successIntent);
                        finish();
                        Log.d("OrderInfo", "Navigated to YourTicketsActivity after payment success");
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("OrderInfo", "onResume called");
        if (isPaymentProcessed) {
            Log.d("OrderInfo", "Payment processed, finishing OrderInfoActivity");
            finish();
        }
    }
}
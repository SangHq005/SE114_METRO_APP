package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    private final DecimalFormat decimalFormat = new DecimalFormat("#,###");
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
    private static final String PREFS_NAME = "OrderInfoPrefs";
    private static final String KEY_TICKET_TYPE_ID = "ticket_type_id";
    private static final String KEY_TICKET_EXPIRATION = "ticket_expiration";
    private static final String KEY_TICKET_AUTO_ACTIVE = "ticket_auto_active";
    private static final String KEY_TICKET_PRICE = "ticket_price"; // Thêm key cho ticketPrice

    // Interface callback để xử lý giá từ Firestore
    private interface OnPriceFetchedListener {
        void onPriceFetched(long price, String ticketName);
        void onPriceFetchFailed(String errorMessage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info);

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        userId = prefs.getString("UserID", null);
        Log.d("OrderInfo", "Retrieved userId from SharedPreferences: " + userId);

        // Lấy dữ liệu từ SharedPreferences hoặc Intent
        SharedPreferences orderPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("ticket_type_id")) {
                lastTicketTypeId = intent.getStringExtra("ticket_type_id");
                Log.d("OrderInfo", "Received ticket_type_id in OrderInfoActivity: " + lastTicketTypeId);
                orderPrefs.edit().putString(KEY_TICKET_TYPE_ID, lastTicketTypeId).apply();
            } else {
                lastTicketTypeId = orderPrefs.getString(KEY_TICKET_TYPE_ID, "unknown");
                Log.w("OrderInfo", "ticket_type_id not found in Intent, using saved value: " + lastTicketTypeId);
            }
            ticketExpiration = intent.getStringExtra("ticket_expiration");
            if (ticketExpiration == null) {
                ticketExpiration = orderPrefs.getString(KEY_TICKET_EXPIRATION, "0");
                Log.w("OrderInfo", "ticket_expiration is null, using saved value: " + ticketExpiration);
            } else {
                orderPrefs.edit().putString(KEY_TICKET_EXPIRATION, ticketExpiration).apply();
            }
            ticketAutoActive = intent.getStringExtra("ticket_auto_active");
            if (ticketAutoActive == null) {
                ticketAutoActive = orderPrefs.getString(KEY_TICKET_AUTO_ACTIVE, "0");
                Log.w("OrderInfo", "ticket_auto_active is null, using saved value: " + ticketAutoActive);
            } else {
                orderPrefs.edit().putString(KEY_TICKET_AUTO_ACTIVE, ticketAutoActive).apply();
            }
            String ticketPriceStr = intent.getStringExtra("ticket_price");
            if (ticketPriceStr != null) {
                try {
                    ticketPrice = Long.parseLong(ticketPriceStr.replaceAll("[^0-9]", ""));
                    orderPrefs.edit().putLong(KEY_TICKET_PRICE, ticketPrice).apply();
                    Log.d("OrderInfo", "Parsed and saved ticketPrice: " + ticketPrice);
                } catch (NumberFormatException e) {
                    Log.e("OrderInfo", "Lỗi định dạng giá vé: " + e.getMessage());
                    ticketPrice = orderPrefs.getLong(KEY_TICKET_PRICE, 0);
                    Toast.makeText(this, "Giá vé không hợp lệ, sử dụng giá lưu trữ: " + ticketPrice + " VND", Toast.LENGTH_LONG).show();
                }
            } else {
                ticketPrice = orderPrefs.getLong(KEY_TICKET_PRICE, 0);
                Log.w("OrderInfo", "ticketPriceStr is null, using saved ticketPrice: " + ticketPrice);
            }
        } else {
            Log.e("OrderInfo", "Intent is null in onCreate");
            lastTicketTypeId = orderPrefs.getString(KEY_TICKET_TYPE_ID, "unknown");
            ticketExpiration = orderPrefs.getString(KEY_TICKET_EXPIRATION, "0");
            ticketAutoActive = orderPrefs.getString(KEY_TICKET_AUTO_ACTIVE, "0");
            ticketPrice = orderPrefs.getLong(KEY_TICKET_PRICE, 0);
        }

        Log.d("OrderInfo", "Received ticket_expiration in onCreate: " + ticketExpiration);
        Log.d("OrderInfo", "Received ticket_auto_active in onCreate: " + ticketAutoActive);
        Log.d("OrderInfo", "Final ticketPrice in onCreate: " + ticketPrice);

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

        backBtn = findViewById(R.id.backBtn);
        LinearLayout selectedMethodLayout = findViewById(R.id.selectedMethodLayout);
        TextView selectedMethodTextView = findViewById(R.id.selectedMethodTextView);
        ImageView selectedLogoImageView = findViewById(R.id.selectedLogoImageView);
        TextView defaultMethodTextView = findViewById(R.id.defaultMethodTextView);

        // Cập nhật giao diện
        sanphamTxt.setText(intent.getStringExtra("ticket_name") != null ? intent.getStringExtra("ticket_name") : "Không có thông tin");
        donGiaTxt.setText(ticketPrice > 0 ? decimalFormat.format(ticketPrice) + " VND" : "0 VND");
        int quantity = 1;
        soLuongTxt.setText(String.valueOf(quantity));
        long total = ticketPrice * quantity;
        String totalFormatted = decimalFormat.format(total) + " VND";
        thanhTienTxt.setText(totalFormatted);
        tongTienTxt.setText(totalFormatted);

        loaiVeTxt.setText(intent.getStringExtra("ticket_name") != null ? intent.getStringExtra("ticket_name") : "Không có thông tin");
        hsdTxt.setText((ticketExpiration != null ? ticketExpiration : "0") + " ngày kể từ ngày kích hoạt");
        luuYTxt.setText("Tự động kích hoạt sau " + (ticketAutoActive != null ? ticketAutoActive : "0") + " ngày kể từ ngày mua");

        // Sự kiện nhấn vào paymentMethodCard để mở PaymentMethodDialog
        paymentMethodCard.setOnClickListener(v -> {
            PaymentMethodDialog dialog = new PaymentMethodDialog();
            dialog.setPaymentMethodListener((methodName, logoResId) -> {
                defaultMethodTextView.setVisibility(View.GONE);
                selectedMethodLayout.setVisibility(View.VISIBLE);
                selectedMethodTextView.setText(methodName);
                selectedLogoImageView.setImageResource(logoResId);
                Toast.makeText(OrderInfoActivity.this, "Đã chọn phương thức: " + methodName, Toast.LENGTH_SHORT).show();
                selectedPaymentMethod = methodName;
            });
            dialog.show(getSupportFragmentManager(), "PaymentMethodDialog");
        });

        // Sự kiện nhấn nút Thanh toán
        thanhToanBtn.setOnClickListener(v -> {
            if (selectedPaymentMethod == null || !selectedPaymentMethod.equals("VN PAY")) {
                Toast.makeText(OrderInfoActivity.this, "Vui lòng chọn phương thức thanh toán VN PAY", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ticketPrice <= 0) {
                Log.w("OrderInfo", "ticketPrice is invalid: " + ticketPrice + ", fetching from Firestore");
                fetchTicketPriceFromFirestore(lastTicketTypeId, new OnPriceFetchedListener() {
                    @Override
                    public void onPriceFetched(long price, String ticketName) {
                        ticketPrice = price;
                        orderPrefs.edit().putLong(KEY_TICKET_PRICE, ticketPrice).apply(); // Lưu lại giá mới
                        Log.d("OrderInfo", "Updated ticketPrice after fetch: " + ticketPrice);
                        if (ticketPrice <= 0) {
                            Toast.makeText(OrderInfoActivity.this, "Giá vé không hợp lệ sau khi fetch, vui lòng chọn lại", Toast.LENGTH_LONG).show();
                            return;
                        }
                        initiateVNPayPayment(ticketName, ticketPrice);
                    }

                    @Override
                    public void onPriceFetchFailed(String errorMessage) {
                        Log.e("OrderInfo", "Failed to fetch ticketPrice: " + errorMessage);
                        Toast.makeText(OrderInfoActivity.this, "Lỗi tải giá vé: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }
            initiateVNPayPayment(intent.getStringExtra("ticket_name"), ticketPrice);
        });

        // Sự kiện nhấn nút Back
        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(OrderInfoActivity.this, MyTicketsActivity.class));
            finish();
        });
    }

    private void fetchTicketPriceFromFirestore(String ticketTypeId, OnPriceFetchedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("TicketType").document(ticketTypeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object priceObj = documentSnapshot.get("Price");
                        String ticketName = documentSnapshot.getString("Name");
                        if (priceObj != null) {
                            if (priceObj instanceof Number) {
                                long price = ((Number) priceObj).longValue();
                                Log.d("OrderInfo", "Fetched ticketPrice from Firestore: " + price + " for ticketTypeId: " + ticketTypeId);
                                listener.onPriceFetched(price, ticketName != null ? ticketName : "Không có thông tin");
                            } else {
                                String error = "Price in Firestore is not a number for ticketTypeId: " + ticketTypeId;
                                Log.w("OrderInfo", error);
                                listener.onPriceFetchFailed(error);
                            }
                        } else {
                            String error = "Price is null in Firestore for ticketTypeId: " + ticketTypeId;
                            Log.w("OrderInfo", error);
                            listener.onPriceFetchFailed(error);
                        }
                    } else {
                        String error = "TicketType document not found for ticketTypeId: " + ticketTypeId;
                        Log.e("OrderInfo", error);
                        listener.onPriceFetchFailed(error);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OrderInfo", "Error fetching ticketPrice from Firestore: " + e.getMessage());
                    listener.onPriceFetchFailed(e.getMessage());
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
        isPaymentProcessed = true;
        String transactionNo = UUID.randomUUID().toString().substring(0, 10);

        if (action.equals("SuccessBackAction")) {
            Log.d("OrderInfo", "Payment successful, ticketPrice before saving: " + ticketPrice);
            Toast.makeText(this, "Thanh toán thành công! Mã giao dịch: " + transactionNo, Toast.LENGTH_LONG).show();
            saveTransactionToFirestore(orderId, transactionNo, lastTicketTypeId, ticketPrice, "SUCCESS");
            Intent successIntent = new Intent(this, YourTicketsActivity.class);
            successIntent.putExtra("UUID", userId);
            successIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(successIntent);
            finishAffinity(); // Đảm bảo đóng toàn bộ activity liên quan
            Log.d("OrderInfo", "Navigated to YourTicketsActivity after payment success");
        } else if (action.equals("FaildBackAction")) {
            Toast.makeText(this, "Thanh toán thất bại", Toast.LENGTH_LONG).show();
            saveTransactionToFirestore(orderId, transactionNo, lastTicketTypeId, ticketPrice, "FAILED");
            finish();
        } else if (action.equals("WebBackAction") || action.equals("AppBackAction")) {
            Toast.makeText(this, "Thanh toán bị hủy", Toast.LENGTH_LONG).show();
            saveTransactionToFirestore(orderId, transactionNo, lastTicketTypeId, ticketPrice, "FAILED");
            finish();
        } else {
            Toast.makeText(this, "Hành động không xác định: " + action, Toast.LENGTH_LONG).show();
            Log.e("VNPay", "Unknown action: " + action);
            saveTransactionToFirestore(orderId, transactionNo, lastTicketTypeId, ticketPrice, "FAILED");
            finish();
        }
    }

    private void saveTransactionToFirestore(String orderId, String transactionNo, String ticketTypeId, long amount, String status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        int expirationDays = 0;
        int autoActiveDays = 0;
        try {
            if (ticketExpiration == null || ticketExpiration.equals("null")) {
                Log.w("Firestore", "ticket_expiration is null or 'null', using default value 0");
                ticketExpiration = "0";
            }
            if (ticketAutoActive == null || ticketAutoActive.equals("null")) {
                Log.w("Firestore", "ticket_auto_active is null or 'null', using default value 0");
                ticketAutoActive = "0";
            }
            expirationDays = Integer.parseInt(ticketExpiration);
            autoActiveDays = Integer.parseInt(ticketAutoActive);
            Log.d("Firestore", "expirationDays: " + expirationDays + ", autoActiveDays: " + autoActiveDays);

            if (autoActiveDays < 0) {
                throw new IllegalArgumentException("AutoActiveDays không được âm (ticket_type_id: " + (ticketTypeId != null ? ticketTypeId : "unknown") + ")");
            }
        } catch (NumberFormatException e) {
            Log.e("Firestore", "Lỗi định dạng expiration hoặc autoActive: ticket_expiration=" + ticketExpiration + ", ticket_auto_active=" + ticketAutoActive, e);
            Toast.makeText(this, "Lỗi: Dữ liệu Expiration hoặc AutoActive không hợp lệ", Toast.LENGTH_LONG).show();
            return;
        } catch (IllegalArgumentException e) {
            Log.e("Firestore", "Lỗi giá trị: " + e.getMessage());
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            saveBasicTransaction(orderId, transactionNo, ticketTypeId, amount, status);
            return;
        }

        Date currentTimestamp = new Date();
        Log.d("Firestore", "Current Timestamp: " + currentTimestamp + ", amount: " + amount);

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
            Map<String, Object> transactionData = new HashMap<>();
            transactionData.put("orderId", orderId);
            transactionData.put("transactionNo", transactionNo);
            transactionData.put("ticketTypeId", ticketTypeId != null ? ticketTypeId : "unknown");
            transactionData.put("amount", amount); // Sử dụng amount trực tiếp
            transactionData.put("status", status);
            transactionData.put("timestamp", currentTimestamp);
            transactionData.put("userId", userId != null ? userId : "unknown");

            if (status.equals("SUCCESS")) {
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

                Map<String, Object> ticket = new HashMap<>();
                ticket.put("ticketTypeId", ticketTypeId != null ? ticketTypeId : "unknown");
                ticket.put("ticketCode", ticketCode);
                ticket.put("timestamp", currentTimestamp);
                ticket.put("userId", userId != null ? userId : "unknown");
                ticket.put("Status", "Chưa kích hoạt");
                ticket.put("AutoActiveDate", autoActiveDate);
                ticket.put("ExpirationDate", expirationDate);

                transaction.set(db.collection("Ticket").document(orderId), ticket);
            }

            transaction.set(db.collection("Transactions").document(orderId), transactionData);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "Lưu giao dịch thành công: " + orderId + ", amount: " + amount);
            if (status.equals("SUCCESS")) {
                Intent successIntent = new Intent(this, YourTicketsActivity.class);
                successIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(successIntent);
                finish();
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Lỗi lưu giao dịch: " + e.getMessage() + ", amount: " + amount);
            Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    // Phương thức lưu giao dịch cơ bản khi có lỗi
    private void saveBasicTransaction(String orderId, String transactionNo, String ticketTypeId, long amount, String status) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Date currentTimestamp = new Date();

        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("orderId", orderId);
        transactionData.put("transactionNo", transactionNo);
        transactionData.put("ticketTypeId", ticketTypeId != null ? ticketTypeId : "unknown");
        transactionData.put("amount", amount); // Sử dụng amount trực tiếp
        transactionData.put("status", status);
        transactionData.put("timestamp", currentTimestamp);
        transactionData.put("userId", userId != null ? userId : "unknown");

        db.collection("Transactions").document(orderId).set(transactionData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Lưu giao dịch cơ bản thành công: " + orderId + ", amount: " + amount))
                .addOnFailureListener(e -> Log.e("Firestore", "Lỗi lưu giao dịch cơ bản: " + e.getMessage() + ", amount: " + amount));
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
            Log.d("VNPay", "Payment already processed, finishing activity");
            finish();
            return;
        }
        if (intent != null && intent.getData() != null) {
            Log.d("VNPay", "Intent action: " + intent.getAction());
            Log.d("VNPay", "Intent data: " + intent.getData().toString());
            Uri data = intent.getData();
            Log.d("VNPay", "Return URL: " + data.toString());
            String responseCode = data.getQueryParameter("vnp_ResponseCode");
            String transactionNo = data.getQueryParameter("vnp_TransactionNo");
            String orderId = data.getQueryParameter("vnp_TxnRef");
            Log.d("VNPay", "ResponseCode: " + responseCode + ", TransactionNo: " + transactionNo + ", OrderId: " + orderId);
            if (responseCode != null && transactionNo != null && orderId != null) {
                isPaymentProcessed = true;
                if ("00".equals(responseCode)) {
                    Log.d("OrderInfo", "Payment successful in onNewIntent, ticketPrice: " + ticketPrice);
                    Toast.makeText(this, "Thanh toán thành công! Mã giao dịch: " + transactionNo, Toast.LENGTH_LONG).show();
                    saveTransactionToFirestore(orderId, transactionNo, lastTicketTypeId, ticketPrice, "SUCCESS");
                    Intent successIntent = new Intent(this, YourTicketsActivity.class);
                    successIntent.putExtra("UUID", userId);
                    successIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(successIntent);
                    finishAffinity();
                    Log.d("OrderInfo", "Navigated to YourTicketsActivity after payment success from onNewIntent");
                } else {
                    Toast.makeText(this, "Thanh toán thất bại, mã lỗi: " + responseCode, Toast.LENGTH_LONG).show();
                    Log.e("VNPay", "Payment failed with response code: " + responseCode);
                    saveTransactionToFirestore(orderId, transactionNo, lastTicketTypeId, ticketPrice, "FAILED");
                    finish();
                }
            } else {
                Log.e("VNPay", "Missing responseCode, transactionNo, or orderId in return URL");
                Toast.makeText(this, "Lỗi xử lý kết quả thanh toán: Thiếu tham số", Toast.LENGTH_LONG).show();
                saveTransactionToFirestore(lastOrderId, UUID.randomUUID().toString().substring(0, 10), lastTicketTypeId, ticketPrice, "FAILED");
                finish();
            }
        } else {
            Log.e("VNPay", "No data in Intent or Intent is null");
            Toast.makeText(this, "Không nhận được dữ liệu thanh toán", Toast.LENGTH_LONG).show();
            saveTransactionToFirestore(lastOrderId, UUID.randomUUID().toString().substring(0, 10), lastTicketTypeId, ticketPrice, "FAILED");
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
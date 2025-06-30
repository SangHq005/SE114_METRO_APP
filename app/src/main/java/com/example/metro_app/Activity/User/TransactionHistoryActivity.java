package com.example.metro_app.Activity.User;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.metro_app.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryActivity extends AppCompatActivity {

    private ImageView closePopup, backBtn;
    private LinearLayout calendarPopup;
    private CalendarView calendarView;
    private  Button btnApply;
    private TextView startDateTxt, endDateTxt, resetBtn, noTransactionsTxt;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private FirebaseFirestore db;
    private String userId;
    private boolean isSelectingStartDate = true; // Flag to track which date is being selected

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        userId = prefs.getString("UserID", null);
        Log.d("TransactionHistory", "userId: " + userId);

        // Initialize views
        backBtn = findViewById(R.id.backBtn);
        calendarPopup = findViewById(R.id.calendarPopup);
        calendarView = findViewById(R.id.calendarView);
        btnApply = findViewById(R.id.btnApply);
        startDateTxt = findViewById(R.id.startDateTxt);
        endDateTxt = findViewById(R.id.endDateTxt);
        resetBtn = findViewById(R.id.resetBtn);
        noTransactionsTxt = findViewById(R.id.noTransactionsTxt);
        recyclerView = findViewById(R.id.recyckerViewHistory);
        progressBar = findViewById(R.id.progressBarHistory);
        closePopup = findViewById(R.id.closePopup);

        // Check if critical views are null
        if (backBtn == null || calendarPopup == null || calendarView == null ||
                btnApply == null || startDateTxt == null || endDateTxt == null ||
                resetBtn == null || noTransactionsTxt == null || recyclerView == null ||
                progressBar == null || closePopup == null) {
            Log.e("TransactionHistory", "One or more views are null");
            Toast.makeText(this, "Lỗi giao diện, vui lòng thử lại", Toast.LENGTH_LONG).show();
            return;
        }

        // Set default dates
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        // End date: Current date (26/06/2025)
        endDateTxt.setText(sdf.format(calendar.getTime()));
        // Start date: First day of current month (01/06/2025)
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startDateTxt.setText(sdf.format(calendar.getTime()));

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactionList);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(transactionAdapter);

        // Check userId and load transactions
        if (userId == null) {
            Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            noTransactionsTxt.setVisibility(View.VISIBLE);
            noTransactionsTxt.setText("Không có thông tin người dùng");
            recyclerView.setVisibility(View.GONE);
            return;
        }

        // Load transactions based on default date range
        loadTransactionsByDate();

        // Handle back button click
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionHistoryActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Handle start date click
        startDateTxt.setOnClickListener(v -> {
            calendarPopup.setVisibility(View.VISIBLE);
            isSelectingStartDate = true;
        });

        // Handle end date click
        endDateTxt.setOnClickListener(v -> {
            calendarPopup.setVisibility(View.VISIBLE);
            isSelectingStartDate = false;
        });

        // Handle calendar date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);
            String formattedDate = dateFormat.format(selectedCalendar.getTime());

            if (isSelectingStartDate) {
                startDateTxt.setText(formattedDate);
            } else {
                endDateTxt.setText(formattedDate);
            }
        });

        // Handle close popup button click
        closePopup.setOnClickListener(v -> calendarPopup.setVisibility(View.GONE));

        // Handle apply button click
        btnApply.setOnClickListener(v -> {
            calendarPopup.setVisibility(View.GONE);
            loadTransactionsByDate();
        });

        // Handle reset button click
        resetBtn.setOnClickListener(v -> {
            // Reset to default dates
            calendar.setTime(new Date());
            endDateTxt.setText(sdf.format(calendar.getTime()));
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            startDateTxt.setText(sdf.format(calendar.getTime()));
            calendarPopup.setVisibility(View.GONE);
            noTransactionsTxt.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            loadTransactionsByDate();
        });
    }

    private void loadTransactionsByDate() {
        if (userId == null) {
            Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            noTransactionsTxt.setVisibility(View.VISIBLE);
            noTransactionsTxt.setText("Không có thông tin người dùng");
            recyclerView.setVisibility(View.GONE);
            return;
        }

        Log.d("TransactionHistory", "Starting to load transactions for userId: " + userId);
        progressBar.setVisibility(View.VISIBLE);
        noTransactionsTxt.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        List<Transaction> tempTransactionList = new ArrayList<>(); // Biến trung gian
        transactionAdapter.notifyDataSetChanged();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date startDate = sdf.parse(startDateTxt.getText().toString());
            Date endDate = sdf.parse(endDateTxt.getText().toString());

            // Set endDate to end of day
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            endDate = calendar.getTime();

            db.collection("Transactions")
                    .whereEqualTo("userId", userId)
                    .whereGreaterThanOrEqualTo("timestamp", startDate)
                    .whereLessThanOrEqualTo("timestamp", endDate)
                    .get()
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        Log.d("TransactionHistory", "Transaction query completed, success: " + task.isSuccessful());
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TransactionHistory", "Processing transaction: " + document.getId());
                                String ticketTypeId = document.getString("ticketTypeId");
                                Object amountObj = document.get("amount");
                                long amountValue;
                                if (amountObj != null) {
                                    if (amountObj instanceof Long) {
                                        amountValue = ((Long) amountObj).longValue();
                                    } else if (amountObj instanceof Integer) {
                                        amountValue = ((Integer) amountObj).longValue();
                                    } else if (amountObj instanceof Double) {
                                        amountValue = ((Double) amountObj).longValue();
                                    } else {
                                        amountValue = 0;
                                        Log.w("TransactionHistory", "Unsupported amount type for transaction with ticketTypeId: " + ticketTypeId + ", using default 0");
                                    }
                                } else {
                                    amountValue = 0;
                                }
                                String status = document.getString("status");
                                Date timestamp = document.getDate("timestamp");

                                if (ticketTypeId != null) {
                                    db.collection("TicketType").document(ticketTypeId)
                                            .get()
                                            .addOnSuccessListener(doc -> {
                                                String ticketTypeName = doc.getString("Name");
                                                Log.d("TransactionHistory", "Fetched ticketTypeName for ticketTypeId " + ticketTypeId + ": " + ticketTypeName + amountObj);
                                                if (ticketTypeName == null) {
                                                    Log.w("TransactionHistory", "Skipping transaction with null ticketTypeName for ticketTypeId: " + ticketTypeId);
                                                    return;
                                                }
                                                Transaction transaction = new Transaction(
                                                        ticketTypeName,
                                                        amountValue,
                                                        status,
                                                        timestamp
                                                );
                                                tempTransactionList.add(transaction);
                                                runOnUiThread(() -> {
                                                    transactionList.clear();
                                                    transactionList.addAll(tempTransactionList);
                                                    transactionAdapter.notifyDataSetChanged();
                                                    updateEmptyState();
                                                });
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("TransactionHistory", "Failed to fetch ticketType for ticketTypeId: " + ticketTypeId, e);
                                            });
                                } else {
                                    Log.w("TransactionHistory", "Skipping transaction with null ticketTypeId: " + document.getId());
                                }
                            }
                            runOnUiThread(() -> updateEmptyState());
                        } else {
                            Log.e("TransactionHistory", "Firestore query failed: ", task.getException());
                            Toast.makeText(this, "Lỗi tải giao dịch", Toast.LENGTH_LONG).show();
                            noTransactionsTxt.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    });
        } catch (Exception e) {
            Log.e("TransactionHistory", "Error parsing dates: ", e);
            progressBar.setVisibility(View.GONE);
            noTransactionsTxt.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void updateEmptyState() {
        if (transactionList.isEmpty()) {
            noTransactionsTxt.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noTransactionsTxt.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Transaction model class
    public static class Transaction {
        private String ticketTypeName;
        private long amount;
        private String status;
        private Date timestamp;

        public Transaction(String ticketTypeName, long amount, String status, Date timestamp) {
            this.ticketTypeName = ticketTypeName;
            this.amount = amount;
            this.status = status;
            this.timestamp = timestamp;
        }

        public String getTicketTypeName() {
            return ticketTypeName;
        }

        public long getAmount() {
            return amount;
        }

        public String getStatus() {
            return status;
        }

        public Date getTimestamp() {
            return timestamp;
        }
    }

    // RecyclerView Adapter
    private class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
        private final List<Transaction> transactions;

        public TransactionAdapter(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        @NonNull
        @Override
        public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.viewholder_ticket, parent, false);
            return new TransactionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
            Transaction transaction = transactions.get(position);

            holder.tvTitle.setText(transaction.getTicketTypeName() != null ? transaction.getTicketTypeName() : "N/A");

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            holder.tvDate.setText(transaction.getTimestamp() != null ? sdf.format(transaction.getTimestamp()) : "N/A");

            String statusText;
            if ("SUCCESS".equals(transaction.getStatus())) {
                statusText = "Thành công";
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.success_text));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_transaction_success);
            } else {
                statusText = "Thất bại";
                holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.failed_text));
                holder.tvStatus.setBackgroundResource(R.drawable.bg_transaction_failed);
            }
            holder.tvStatus.setText(statusText);

            String amountText = String.format(Locale.getDefault(), "%,d đ", transaction.getAmount());
            holder.tvAmount.setText(amountText);
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        class TransactionViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvStatus, tvAmount;

            public TransactionViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvAmount = itemView.findViewById(R.id.tvAmount);
            }
        }
    }
}
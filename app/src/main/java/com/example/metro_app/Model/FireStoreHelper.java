package com.example.metro_app.Model;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.metro_app.Domain.TicketModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FireStoreHelper {
    private FirebaseFirestore db;

    public FireStoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // Lấy danh sách toàn bộ người dùng
    public void getAllUsers(Callback<List<UserModel>> callback) {
        db.collection("Account")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserModel> userList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        UserModel user = doc.toObject(UserModel.class);
                        if (user != null) {
                            user.setUid(doc.getId());


                            // Thêm log thông tin user
                            Log.d("FIRESTORE_USER", "ID: " + user.getUid()
                                    + ", Name: " + user.getName()
                                    + ", Email: " + user.getEmail()
                                    + ", Role: " + user.getRole()
                                    + ", CCCD: " + user.getCCCD());

                            userList.add(user);
                        }
                    }
                    callback.onSuccess(userList);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_USER", "Error getting users", e);
                    callback.onFailure(e);
                });
    }


    public void getTicketsByUserId(String userId, @Nullable String statusFilter, Callback<List<TicketModel>> callback) {
        Query query = db.collection("Ticket").whereEqualTo("UserID", userId);
        if (statusFilter != null) {
            query = query.whereEqualTo("Status", statusFilter);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TicketModel> ticketList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        TicketModel ticket = doc.toObject(TicketModel.class);
                        ticket.setId(doc.getId()); // gán ID từ document
                        ticketList.add(ticket);
                    }
                    callback.onSuccess(ticketList);
                })
                .addOnFailureListener(callback::onFailure);
    }
    public void getSumOfTransactionFiltered(Integer day, Integer month, Integer year, OnTransactionSumCallback callback) {
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();

        if (year == null) {
            callback.onCallback(0.0);
            return;
        }

        // Thiết lập thời gian bắt đầu
        calStart.set(Calendar.YEAR, year);
        calStart.set(Calendar.MONTH, month != null ? month - 1 : 0);
        calStart.set(Calendar.DAY_OF_MONTH, day != null ? day : 1);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        // Thiết lập thời gian kết thúc
        if (day != null) {
            calEnd.setTime(calStart.getTime());
            calEnd.add(Calendar.DATE, 1); // +1 ngày
        } else if (month != null) {
            calEnd.setTime(calStart.getTime());
            calEnd.add(Calendar.MONTH, 1); // +1 tháng
        } else {
            calEnd.setTime(calStart.getTime());
            calEnd.add(Calendar.YEAR, 1); // +1 năm
        }

        Date startDate = calStart.getTime();
        Date endDate = calEnd.getTime();

        db.collection("Transactions")
                .whereGreaterThanOrEqualTo("timestamp", new Timestamp(startDate))
                .whereLessThan("timestamp", new Timestamp(endDate))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalAmount = 0.0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String status = doc.getString("status");
                        if ("SUCCESS".equals(status)) {
                            Number amount = doc.getDouble("amount");
                            if (amount != null) {
                                totalAmount += amount.doubleValue();
                            }
                        }
                    }
                    callback.onCallback(totalAmount);
                })
                .addOnFailureListener(e -> callback.onCallback(0.0));
    }

    public void getTotalTicketsFiltered(Integer day, Integer month, Integer year, Callback<Long> callback) {
        if (year == null) {
            callback.onFailure(new IllegalArgumentException("Year must be provided"));
            return;
        }

        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();

        calStart.set(Calendar.YEAR, year);
        calStart.set(Calendar.MONTH, month != null ? month - 1 : 0);
        calStart.set(Calendar.DAY_OF_MONTH, day != null ? day : 1);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        if (day != null) {
            calEnd.setTime(calStart.getTime());
            calEnd.add(Calendar.DATE, 1);
        } else if (month != null) {
            calEnd.setTime(calStart.getTime());
            calEnd.add(Calendar.MONTH, 1);
        } else {
            calEnd.setTime(calStart.getTime());
            calEnd.add(Calendar.YEAR, 1);
        }

        Date start = calStart.getTime();
        Date end = calEnd.getTime();

        db.collection("Ticket")
                .whereGreaterThanOrEqualTo("timestamp", new Timestamp(start))
                .whereLessThan("timestamp", new Timestamp(end))
                .get()
                .addOnSuccessListener(snapshots -> callback.onSuccess((long) snapshots.size()))
                .addOnFailureListener(callback::onFailure);
    }
    public void getTotalUsersFiltered(Integer day, Integer month, Integer year, Callback<Long> callback) {
        if (year == null) {
            callback.onFailure(new IllegalArgumentException("Year must be provided"));
            return;
        }

        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();

        calStart.set(Calendar.YEAR, year);
        calStart.set(Calendar.MONTH, month != null ? month - 1 : 0);
        calStart.set(Calendar.DAY_OF_MONTH, day != null ? day : 1);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        if (day != null) {
            calEnd.setTime(calStart.getTime());
            calEnd.add(Calendar.DATE, 1);
        } else if (month != null) {
            calEnd.setTime(calStart.getTime());
            calEnd.add(Calendar.MONTH, 1);
        } else {
            calEnd.setTime(calStart.getTime());
            calEnd.add(Calendar.YEAR, 1);
        }

        Date start = calStart.getTime();
        Date end = calEnd.getTime();

        db.collection("Account")
                .whereGreaterThanOrEqualTo("timestamp", new Timestamp(start))
                .whereLessThan("timestamp", new Timestamp(end))
                .get()
                .addOnSuccessListener(snapshots -> callback.onSuccess((long) snapshots.size()))
                .addOnFailureListener(callback::onFailure);
    }



    public void sumByDayOfWeek(int year, int weekOfYear, Callback<Map<Integer, Double>> callback) {
        Map<Integer, Double> sumByDay = new HashMap<>();
        db.collection("Transactions").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        if (timestamp == null) continue;
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(timestamp.toDate());
                        int y = cal.get(Calendar.YEAR);
                        int w = cal.get(Calendar.WEEK_OF_YEAR);
                        if (y == year && w == weekOfYear) {
                            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday, 2=Monday,...
                            Number amountNum = doc.getDouble("amount");
                            if (amountNum == null) continue;
                            double amount = amountNum.doubleValue();
                            sumByDay.put(dayOfWeek, sumByDay.getOrDefault(dayOfWeek, 0.0) + amount);
                        }
                    }
                    callback.onSuccess(sumByDay);
                })
                .addOnFailureListener(callback::onFailure);
    }




    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
    public interface OnTransactionSumCallback {
        void onCallback(double sum);
    }

}


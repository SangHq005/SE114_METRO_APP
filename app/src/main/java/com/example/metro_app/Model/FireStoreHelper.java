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
    public void getSumOfTransaction(OnTransactionSumCallback callback) {
        db.collection("Transactions").get().addOnSuccessListener(queryDocumentSnapshots -> {
            double totalAmount = 0.0;
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String status = document.getString("status");
                if ("SUCCESS".equals(status)) {
                    Number amount = document.getDouble("amount");
                    if (amount != null) {
                        totalAmount += amount.doubleValue();
                    }
                }
            }
            callback.onCallback(totalAmount);
        }).addOnFailureListener(e -> {
            callback.onCallback(0.0);
        });
    }
    public void getTotalTiketSold(Callback<Long> callback){

        AggregateQuery countQuery = db.collection("Ticket").count();

        countQuery.get(AggregateSource.SERVER).addOnSuccessListener(snapshot -> {
            callback.onSuccess(snapshot.getCount());
        });
    }
    public void getTotalUser(Callback<Long> callback){
        AggregateQuery countQuery = db.collection("Account").count();
        countQuery.get(AggregateSource.SERVER).addOnSuccessListener(snapshot -> {
            callback.onSuccess(snapshot.getCount());
        });
    }
    // ...existing code...

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


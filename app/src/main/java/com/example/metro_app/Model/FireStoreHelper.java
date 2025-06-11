package com.example.metro_app.Model;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.metro_app.Domain.TicketModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
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

    public void getUserById(String uid, Callback<UserModel> callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("User ID không được để trống"));
            return;
        }

        DocumentReference userRef = db.collection("Account").document(uid);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    UserModel user = document.toObject(UserModel.class);
                    if (user != null) {
                        user.setUid(document.getId());
                        Log.d(TAG, "Lấy thông tin người dùng thành công: " + user.getName());
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("Không thể chuyển đổi dữ liệu người dùng."));
                    }
                } else {
                    Log.d(TAG, "Không tìm thấy người dùng với UID: " + uid);
                    callback.onSuccess(null); // Không tìm thấy người dùng
                }
            } else {
                Log.e(TAG, "Lỗi khi lấy thông tin người dùng: ", task.getException());
                callback.onFailure(task.getException());
            }
        });
    }

    public void updateUserCccd(String uid, String newCccd, Callback<Void> callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("User ID không được để trống"));
            return;
        }

        DocumentReference userRef = db.collection("Account").document(uid);
        userRef.update("CCCD", newCccd)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cập nhật CCCD thành công cho UID: " + uid);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi cập nhật CCCD: ", e);
                    callback.onFailure(e);
                });
    }
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
    public void getSumOfTransaction(TimeFilterType type,
                                    @Nullable Integer day,
                                    @Nullable Integer month,
                                    @Nullable Integer year,
                                    OnTransactionSumCallback cb) {

        Date[] range = getStartEnd(type, day, month, year);

        Query q = db.collection("Transactions");
        if (range != null) {
            q = q.whereGreaterThanOrEqualTo("timestamp", new Timestamp(range[0]))
                    .whereLessThan("timestamp", new Timestamp(range[1]));
        }

        q.get().addOnSuccessListener(ss -> {
            double sum = 0;
            for (QueryDocumentSnapshot doc : ss) {
                if ("SUCCESS".equals(doc.getString("status"))) {
                    Double amt = doc.getDouble("amount");
                    if (amt != null) sum += amt;
                }
            }
            cb.onCallback(sum);
        }).addOnFailureListener(e -> cb.onCallback(0));
    }


    public void getTotalTickets(TimeFilterType type,
                                @Nullable Integer day,
                                @Nullable Integer month,
                                @Nullable Integer year,
                                Callback<Long> cb) {

        Date[] range = getStartEnd(type, day, month, year);

        Query q = db.collection("Ticket");
        if (range != null) {
            q = q.whereGreaterThanOrEqualTo("timestamp", new Timestamp(range[0]))
                    .whereLessThan("timestamp", new Timestamp(range[1]));
        }

        q.get()
                .addOnSuccessListener(snp -> cb.onSuccess((long) snp.size()))
                .addOnFailureListener(cb::onFailure);
    }

    public void getTotalUsers(TimeFilterType type,
                              @Nullable Integer day,
                              @Nullable Integer month,
                              @Nullable Integer year,
                              Callback<Long> cb) {

        Date[] range = getStartEnd(type, day, month, year);

        Query q = db.collection("Account");
        if (range != null) {
            q = q.whereGreaterThanOrEqualTo("firstTimeLogin", new Timestamp(range[0]))
                    .whereLessThan("firstTimeLogin", new Timestamp(range[1]));
        }

        q.get()
                .addOnSuccessListener(snp -> cb.onSuccess((long) snp.size()))
                .addOnFailureListener(cb::onFailure);
    }



    public void sumByFilterForChart(TimeFilterType type, Integer day, Integer month, Integer year, Callback<Map<Integer, Double>> callback) {
        db.collection("Transactions").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<Integer, Double> resultMap = new HashMap<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        if (timestamp == null || !"SUCCESS".equals(doc.getString("status"))) continue;

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(timestamp.toDate());
                        int tYear = cal.get(Calendar.YEAR);
                        int tMonth = cal.get(Calendar.MONTH) + 1;
                        int tDay = cal.get(Calendar.DAY_OF_MONTH);
                        int tWeek = cal.get(Calendar.WEEK_OF_YEAR);
                        int tDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=CN,...,7=Thứ 7

                        boolean isValid = false;

                        switch (type) {
                            case THIS_WEEK:
                                Calendar current = Calendar.getInstance();
                                if (tYear == current.get(Calendar.YEAR) && tWeek == current.get(Calendar.WEEK_OF_YEAR)) {
                                    isValid = true;
                                    // Dùng tDayOfWeek làm key
                                    resultMap.put(tDayOfWeek, resultMap.getOrDefault(tDayOfWeek, 0.0) + doc.getDouble("amount"));
                                }
                                break;

                            case THIS_MONTH:
                                if (tYear == year && tMonth == month) {
                                    isValid = true;
                                    resultMap.put(tDay, resultMap.getOrDefault(tDay, 0.0) + doc.getDouble("amount"));
                                }
                                break;

                            case ALL:
                                if (tYear == year) {
                                    isValid = true;
                                    resultMap.put(tMonth, resultMap.getOrDefault(tMonth, 0.0) + doc.getDouble("amount"));
                                }
                                break;
                        }
                    }

                    callback.onSuccess(resultMap);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void updateUserPhone(String uid, String newPhone, Callback<Void> callback) {
        FirebaseFirestore.getInstance()
                .collection("Account")
                .document(uid)
                .update("PhoneNumber", newPhone)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    private Date[] getStartEnd(TimeFilterType type,
                               @Nullable Integer day,
                               @Nullable Integer month,
                               @Nullable Integer year) {

        Calendar start = Calendar.getInstance();
        Calendar end   = Calendar.getInstance();

        switch (type) {
            case TODAY:
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);

                end.setTime(start.getTime());
                end.add(Calendar.DATE, 1);
                break;

            case THIS_WEEK:
                start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);

                end.setTime(start.getTime());
                end.add(Calendar.WEEK_OF_YEAR, 1);
                break;

            case THIS_MONTH:
                start.set(Calendar.DAY_OF_MONTH, 1);
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);

                end.setTime(start.getTime());
                end.add(Calendar.MONTH, 1);
                break;

            case CUSTOM_DATE:
                if (year == null) throw new IllegalArgumentException("Year is required for CUSTOM_DATE");
                start.set(Calendar.YEAR, year);
                start.set(Calendar.MONTH, month != null ? month - 1 : 0);
                start.set(Calendar.DAY_OF_MONTH, day != null ? day : 1);
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);

                end.setTime(start.getTime());
                if (day != null)       end.add(Calendar.DATE, 1);
                else if (month != null) end.add(Calendar.MONTH, 1);
                else                    end.add(Calendar.YEAR, 1);
                break;

            case ALL:
            default:
                return null; // báo hàm gọi biết là không cần whereGreater/Less
        }
        return new Date[]{ start.getTime(), end.getTime() };
    }




    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
    public interface OnTransactionSumCallback {
        void onCallback(double sum);
    }

}


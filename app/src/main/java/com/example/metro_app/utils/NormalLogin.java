package com.example.metro_app.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.metro_app.Model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NormalLogin {
    public interface AuthCallback {
        void onSuccess(UserModel user);
        void onFailure(String message);
    }

    public static void registerUser(String name, String email, String password, String phone, String cccd, AuthCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        callback.onFailure("Không thể lấy thông tin người dùng");
                        return;
                    }

                    String uid = firebaseUser.getUid();
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("Name", name);
                    userMap.put("Email", email);
                    userMap.put("PhoneNumber", phone);
                    userMap.put("CCCD", cccd);
                    userMap.put("Role", "User");

                    db.collection("Account").document(uid)
                            .set(userMap)
                            .addOnSuccessListener(aVoid -> {
                                UserModel user = new UserModel();
                                user.setUid(uid);
                                user.setName(name);
                                user.setEmail(email);
                                user.setPhoneNumber(phone);
                                user.setCCCD(cccd);
                                user.setRole("User");
                                callback.onSuccess(user);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("NormalLogin", "Lỗi Firestore khi lưu user", e);
                                callback.onFailure("Lỗi khi lưu người dùng");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("NormalLogin", "Lỗi tạo tài khoản", e);
                    callback.onFailure("Email đã tồn tại hoặc không hợp lệ");
                });
    }

    public static void loginUser(String email, String password, AuthCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        callback.onFailure("Không thể đăng nhập");
                        return;
                    }

                    String uid = firebaseUser.getUid();
                    db.collection("Account").document(uid)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (!documentSnapshot.exists()) {
                                    callback.onFailure("Tài khoản không tồn tại trong hệ thống");
                                    return;
                                }

                                UserModel user = documentSnapshot.toObject(UserModel.class);
                                user.setUid(uid);
                                callback.onSuccess(user);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("NormalLogin", "Lỗi khi lấy user từ Firestore", e);
                                callback.onFailure("Lỗi khi lấy thông tin người dùng");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("NormalLogin", "Đăng nhập thất bại", e);
                    callback.onFailure("Sai email hoặc mật khẩu");
                });
    }
}

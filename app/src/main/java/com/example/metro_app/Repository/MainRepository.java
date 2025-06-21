package com.example.metro_app.Repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.metro_app.Domain.CategoryModel;
import com.example.metro_app.Domain.NewsModel;
import com.example.metro_app.Domain.PopularModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainRepository {
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public LiveData<List<CategoryModel>> loadCategory() {
        final MutableLiveData<List<CategoryModel>> listdata = new MutableLiveData<>();

        DatabaseReference ref = firebaseDatabase.getReference("Category");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CategoryModel> lists = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    CategoryModel item = childSnapshot.getValue(CategoryModel.class);
                    if (item != null) {
                        lists.add(item);
                        Log.d("FirebaseData", "Category: " + item.getName());
                    }
                }
                listdata.setValue(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listdata.setValue(new ArrayList<>());
            }
        });
        return listdata;
    }

    public LiveData<List<NewsModel>> loadNews() {
        final MutableLiveData<List<NewsModel>> livedata = new MutableLiveData<>();

        firestore.collection("news")
                .whereEqualTo("status", "Đã xuất bản")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NewsModel> lists = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String date = document.getString("date");
                        String title = document.getString("title");
                        String pic = document.getString("pic");
                        String description = document.getString("description");
                        String userid = document.getString("userid");
                        String status = document.getString("status");
                        String documentId = document.getId();

                        NewsModel item = new NewsModel(date, description, pic, title, userid, status);
                        item.setDocumentId(documentId);
                        lists.add(item);
                        Log.d("Firebase", "News: " + lists.size());
                    }
                    livedata.setValue(lists);
                })
                .addOnFailureListener(e -> {
                    Log.e("MainRepository", "Error loading news: " + e.getMessage());
                    livedata.setValue(new ArrayList<>());
                });

        return livedata;
    }

    public LiveData<List<PopularModel>> loadPopular() {
        final MutableLiveData<List<PopularModel>> livedata = new MutableLiveData<>();

        DatabaseReference ref = firebaseDatabase.getReference("Popular");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PopularModel> lists = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    PopularModel item = childSnapshot.getValue(PopularModel.class);
                    if (item != null) {
                        lists.add(item);
                        Log.d("Firebase", "Popular: " + lists.size());
                    }
                }
                livedata.setValue(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                livedata.setValue(new ArrayList<>());
            }
        });
        return livedata;
    }
}
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

import java.util.ArrayList;
import java.util.List;

public class MainRepository {
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    public LiveData<List<CategoryModel>> loadCategory(){
        final MutableLiveData<List<CategoryModel>> listdata = new MutableLiveData<>();

        DatabaseReference ref = firebaseDatabase.getReference("Category");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CategoryModel> lists = new ArrayList<>();
                for(DataSnapshot childSnapshot:snapshot.getChildren()){
                    CategoryModel item = childSnapshot.getValue(CategoryModel.class);
                    if(item!=null){
                        lists.add(item);
                        Log.d("FirebaseData", "Category: " + item.getName()); // <-- thêm dòng này
                    }

                }
                listdata.setValue(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return listdata;
    }
    public LiveData<List<NewsModel>> loadNews(){
        final MutableLiveData<List<NewsModel>> livedata = new MutableLiveData<>();

        DatabaseReference ref = firebaseDatabase.getReference("News");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<NewsModel> lists = new ArrayList<>();
                for(DataSnapshot childSnapshot:snapshot.getChildren()){
                    NewsModel item = childSnapshot.getValue(NewsModel.class);
                    if(item!=null){
                        lists.add(item);
                        Log.d("Firebase", "Popular: " + lists.size()); // <-- thêm dòng này
                    }

                }
                livedata.setValue(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return livedata;
    }
    public LiveData<List<PopularModel>> loadPopular(){
        final MutableLiveData<List<PopularModel>> livedata = new MutableLiveData<>();

        DatabaseReference ref = firebaseDatabase.getReference("Popular");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PopularModel> lists = new ArrayList<>();
                for(DataSnapshot childSnapshot:snapshot.getChildren()){
                    PopularModel item = childSnapshot.getValue(PopularModel.class);
                    if(item!=null){
                        lists.add(item);
                        Log.d("Firebase", "Popular: " + lists.size()); // <-- thêm dòng này
                    }

                }
                livedata.setValue(lists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return livedata;
    }
}

package com.example.metro_app.ViewModel;

import androidx.lifecycle.LiveData;

import com.example.metro_app.Domain.CategoryModel;
import com.example.metro_app.Domain.NewsModel;
import com.example.metro_app.Domain.PopularModel;
import com.example.metro_app.Repository.MainRepository;

import java.util.List;

public class MainViewModel {
    private final MainRepository repository;

    public MainViewModel() {
        this.repository = new MainRepository();
    }
    public LiveData<List<CategoryModel>> loadCategory(){

        return repository.loadCategory();
    }
    public LiveData<List<NewsModel>> loadNews(){

        return repository.loadNews();
    }
    public LiveData<List<PopularModel>> loadPopular(){

        return repository.loadPopular();
    }

}

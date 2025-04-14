package com.example.metro_app.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.metro_app.Adapter.CategoryAdapter;
import com.example.metro_app.ViewModel.MainViewModel;
import com.example.metro_app.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {
    ActivityHomeBinding binding;
    MainViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new MainViewModel();
        initCategory();
        //initNews();

    }

//    private void initNews() {
//        DatabaseReference databaseReference = database.getReference("Popular");
//        binding.progressBarNews.setVisibility(View.VISIBLE);
//
//        ArrayList<NewsModel> newsModels = new ArrayList<>();
//
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists())
//                {
//                    for (DataSnapshot issue:snapshot.getChildren()){
//                        newsModels.add(issue.getValue(NewsModel.class));
//                    }
//                }
//                if(!newsModels.isEmpty()){
//                    binding.recyclerViewNews.setLayoutManager(new LinearLayoutManager(HomeActivity.this,LinearLayoutManager.HORIZONTAL,false));
//                    RecyclerView.Adapter<NewsAdapter.Viewholder> adapter = new NewsAdapter(newsModels);
//                    binding.recyclerViewNews.setAdapter(adapter);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void initCategory() {
        binding.progressBarCategory.setVisibility(View.VISIBLE);

        // Giảm số cột xuống 2 để hiển thị rõ hơn
        GridLayoutManager layoutManager = new GridLayoutManager(HomeActivity.this, 4);
        binding.recyclerViewCategory.setLayoutManager(layoutManager);

        viewModel.loadCategory().observe(this, categoryModels -> {
            // In ra số lượng item để kiểm tra
            System.out.println("Số lượng category từ Firebase: " + categoryModels.size());

            // Đảm bảo RecyclerView có đủ không gian hiển thị
            ViewGroup.LayoutParams params = binding.recyclerViewCategory.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            binding.recyclerViewCategory.setLayoutParams(params);

            // Đảm bảo hiển thị đầy đủ các item
            binding.recyclerViewCategory.setHasFixedSize(false);
            binding.recyclerViewCategory.setNestedScrollingEnabled(true);

            // Tạo adapter và gán vào RecyclerView
            CategoryAdapter adapter = new CategoryAdapter(categoryModels);
            binding.recyclerViewCategory.setAdapter(adapter);
            binding.progressBarCategory.setVisibility(View.GONE);
        });

    }
}
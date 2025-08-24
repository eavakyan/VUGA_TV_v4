package com.retry.vuga.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.adapters.HomeCatNameAdapter;
import com.retry.vuga.databinding.ActivityContentByDistributorBinding;
import com.retry.vuga.model.HomePage;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.ContentByDistributor;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ContentByDistributorActivity extends BaseActivity {
    
    private ActivityContentByDistributorBinding binding;
    private CompositeDisposable disposable;
    private HomeCatNameAdapter adapter;
    private String distributorName;
    private List<HomePage.GenreContents> genreContentsList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_content_by_distributor);
        
        // Get distributor name from intent
        distributorName = getIntent().getStringExtra("distributor_name");
        if (distributorName == null) {
            finish();
            return;
        }
        
        init();
        loadDistributorContent();
    }
    
    private void init() {
        disposable = new CompositeDisposable();
        
        binding.tvTitle.setText(distributorName);
        
        binding.btnBack.setOnClickListener(v -> finish());
        
        adapter = new HomeCatNameAdapter();
        binding.rvContent.setLayoutManager(new LinearLayoutManager(this));
        binding.rvContent.setAdapter(adapter);
        
        binding.loutLoader.setVisibility(View.VISIBLE);
    }
    
    private void loadDistributorContent() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("distributor", distributorName);
        
        disposable.add(RetrofitClient.getService().getContentByDistributor(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(() -> {
                    binding.loutLoader.setVisibility(View.GONE);
                })
                .subscribe((response, throwable) -> {
                    if (throwable != null) {
                        Log.e("ContentByDistributor", "API Error: " + throwable.getMessage(), throwable);
                        showNoData();
                        Toast.makeText(this, "Error loading content: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (response != null && response.getStatus()) {
                        if (response.getData() != null && !response.getData().isEmpty()) {
                            genreContentsList = response.getData();
                            adapter.updateItems(genreContentsList);
                            binding.tvNoData.setVisibility(View.GONE);
                            binding.rvContent.setVisibility(View.VISIBLE);
                            Log.d("ContentByDistributor", "Loaded " + genreContentsList.size() + " categories for " + distributorName);
                        } else {
                            Log.d("ContentByDistributor", "No content found for distributor: " + distributorName);
                            showNoData();
                        }
                    } else {
                        Log.e("ContentByDistributor", "API returned error status for distributor: " + distributorName);
                        showNoData();
                    }
                }));
    }
    
    private void showNoData() {
        binding.tvNoData.setVisibility(View.VISIBLE);
        binding.rvContent.setVisibility(View.GONE);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
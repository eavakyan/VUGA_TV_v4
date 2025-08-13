package com.retry.vuga.activities;

import android.os.Bundle;
import android.view.View;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import com.retry.vuga.R;
import com.retry.vuga.adapters.ContentGridAdapter;
import com.retry.vuga.databinding.ActivityWatchListBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class WatchListActivity extends BaseActivity {
    
    private ActivityWatchListBinding binding;
    private ContentGridAdapter adapter;
    private CompositeDisposable disposable = new CompositeDisposable();
    private List<ContentDetail.DataItem> watchlistItems = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_watch_list);
        
        initialization();
        loadWatchlist();
    }
    
    private void initialization() {
        // Set up toolbar
        binding.toolbar.setNavigationIcon(R.drawable.ic_back);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.toolbar.setTitle("My List");
        
        // Set up RecyclerView with grid layout
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        binding.rvWatchlist.setLayoutManager(layoutManager);
        
        adapter = new ContentGridAdapter();
        binding.rvWatchlist.setAdapter(adapter);
        
        // Set up empty state
        binding.tvEmptyMessage.setText("Your watchlist is empty");
    }
    
    private void loadWatchlist() {
        if (sessionManager.getUser() == null) {
            showEmptyState();
            return;
        }
        
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvWatchlist.setVisibility(View.GONE);
        binding.loutEmpty.setVisibility(View.GONE);
        
        Integer profileId = sessionManager.getUser().getLastActiveProfileId();
        
        disposable.add(RetrofitClient.getService()
                .getHomeData(sessionManager.getUser().getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(homePage -> {
                    binding.progressBar.setVisibility(View.GONE);
                    
                    if (homePage != null && homePage.getWatchlist() != null && !homePage.getWatchlist().isEmpty()) {
                        watchlistItems.clear();
                        watchlistItems.addAll(homePage.getWatchlist());
                        adapter.updateItems(watchlistItems);
                        binding.rvWatchlist.setVisibility(View.VISIBLE);
                        binding.loutEmpty.setVisibility(View.GONE);
                    } else {
                        showEmptyState();
                    }
                }, throwable -> {
                    binding.progressBar.setVisibility(View.GONE);
                    showEmptyState();
                }));
    }
    
    private void showEmptyState() {
        binding.rvWatchlist.setVisibility(View.GONE);
        binding.loutEmpty.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
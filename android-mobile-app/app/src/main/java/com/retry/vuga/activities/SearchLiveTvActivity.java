package com.retry.vuga.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.adapters.ContentGridAdapter;
import com.retry.vuga.databinding.ActivitySearchLiveTvBinding;
import com.retry.vuga.model.AllContent;
import com.retry.vuga.model.Language;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.CustomDialogBuilder;
import com.retry.vuga.utils.Global;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SearchLiveTvActivity extends BaseActivity {
    ActivitySearchLiveTvBinding binding;

    CompositeDisposable disposable;
    boolean isLoading = false;
    ContentGridAdapter contentGridAdapter;
    GridLayoutManager gridLayoutManager;
    boolean dataOver = false;
    String keyWord = "";
    HashMap<String, Object> hashMap = new HashMap<>();
    
    // Filter types
    private static final String FILTER_ALL = "all";
    private static final String FILTER_MOVIE = "movie";
    private static final String FILTER_TV_SHOWS = "series";
    private static final String FILTER_CAST = "cast";
    
    private String currentFilter = FILTER_ALL;
    private String searchType = "title"; // "title" or "cast"
    private int selectedLanguageId = 0; // 0 means all languages
    private String selectedLanguageName = "All Languages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_live_tv);

        initialization();
        setListeners();

    }

    private void searchContent() {


        binding.tvType.setVisibility(keyWord.isEmpty() ? View.VISIBLE : View.GONE);
        binding.centerLoader.setVisibility(View.GONE);
        binding.tvNoContent.setVisibility(View.GONE);

        if (dataOver || isLoading || keyWord.isEmpty()) {
            return;
        }
        isLoading = true;
        hashMap.clear();
        hashMap.put(Const.ApiKey.start, contentGridAdapter.getItemCount());
        hashMap.put(Const.ApiKey.limit, Const.PAGINATION_COUNT);
        hashMap.put(Const.ApiKey.keyword, keyWord);
        hashMap.put("search_type", searchType);
        
        // Add type filter only if not searching for cast and not "all"
        if (!searchType.equals("cast") && !currentFilter.equals(FILTER_ALL)) {
            if (currentFilter.equals(FILTER_MOVIE)) {
                hashMap.put(Const.ApiKey.type, 1); // Movie type = 1
            } else if (currentFilter.equals(FILTER_TV_SHOWS)) {
                hashMap.put(Const.ApiKey.type, 2); // Series type = 2
            }
        }
        
        // Add language filter if selected
        if (selectedLanguageId != 0) {
            hashMap.put(Const.ApiKey.language_id, selectedLanguageId);
        }

        disposable.clear();
        disposable.add(RetrofitClient.getService().searchContent(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {

                    if (contentGridAdapter.getItemCount() == 0) {
                        binding.centerLoader.setVisibility(View.VISIBLE);
                        binding.rv.setVisibility(View.GONE);

                    }
                    binding.tvNoContent.setVisibility(View.GONE);


                })
                .doOnTerminate(() -> {

                    binding.centerLoader.setVisibility(View.GONE);
                    isLoading = false;


                }).doOnError(throwable -> {

                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

                    isLoading = false;


                })
                .subscribe((searchResult, throwable) -> {

                    binding.centerLoader.setVisibility(View.GONE);

                    if (searchResult != null && searchResult.getStatus()) {

                        if (searchResult.getData().isEmpty()) {
                            if (contentGridAdapter.getItemCount() == 0) {
                                binding.tvNoContent.setVisibility(View.VISIBLE);

                            } else {
                                dataOver = true;
                            }
                        } else {


                            if (contentGridAdapter.getItemCount() == 0) {
                                contentGridAdapter.updateItems(searchResult.getData());

                            } else {
                                contentGridAdapter.loadMoreItems(searchResult.getData());

                            }
                            binding.rv.setVisibility(View.VISIBLE);


                        }


                    }

                }));

    }

    // LiveTV specific methods removed - content clicks are handled in adapter

    private void setListeners() {
        binding.centerLoader.setOnClickListener(v -> {

        });
        
        // Filter tab listeners
        binding.filterAll.setOnClickListener(v -> {
            selectFilter(FILTER_ALL);
        });
        
        binding.filterMovie.setOnClickListener(v -> {
            selectFilter(FILTER_MOVIE);
        });
        
        binding.filterTvShows.setOnClickListener(v -> {
            selectFilter(FILTER_TV_SHOWS);
        });
        
        binding.filterCast.setOnClickListener(v -> {
            selectFilter(FILTER_CAST);
        });

        binding.btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        
        // Language filter button click
        binding.btnLanguageFilter.setOnClickListener(v -> {
            showLanguageFilterMenu();
        });

        // Content item clicks are handled in the adapter itself


        binding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    Log.i("TAG", "onCreate: " + contentGridAdapter.getItemCount() + gridLayoutManager.findLastVisibleItemPosition());

                    // Only paginate if we have at least a full page of results and more data is available
                    if (contentGridAdapter.getItemCount() >= Const.PAGINATION_COUNT 
                            && contentGridAdapter.getItemCount() - 1 == gridLayoutManager.findLastVisibleItemPosition()
                            && !isLoading && !dataOver) {
                        searchContent();
                    }
                }

            }


        });

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                binding.etSearch.clearFocus();
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
                changeData();


                return true;
            }
            return false;
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                keyWord = s.toString();
                disposable.clear();
                isLoading = false;
                changeData();
            }
        });

    }

    private void changeData() {

        dataOver = false;
        contentGridAdapter.clear();
        searchContent();
    }

    private void initialization() {
        disposable = new CompositeDisposable();
        contentGridAdapter = new ContentGridAdapter();
        gridLayoutManager = new GridLayoutManager(this, 3); // 3 columns for grid

        binding.rv.setLayoutManager(gridLayoutManager);
        binding.rv.setAdapter(contentGridAdapter);
        binding.rv.setItemAnimator(null);


        binding.tvType.setVisibility(View.VISIBLE);
    }
    
    private void selectFilter(String filter) {
        currentFilter = filter;
        
        // Update search type based on filter
        if (filter.equals(FILTER_CAST)) {
            searchType = "cast";
        } else {
            searchType = "title";
        }
        
        // Update UI
        updateFilterUI();
        
        // Clear and search with new filter
        changeData();
    }
    
    private void updateFilterUI() {
        // Reset all backgrounds and text colors
        binding.filterAll.setBackgroundResource(R.drawable.filter_unselected_bg);
        binding.filterAll.setTextColor(getResources().getColor(R.color.text_color));
        
        binding.filterMovie.setBackgroundResource(R.drawable.filter_unselected_bg);
        binding.filterMovie.setTextColor(getResources().getColor(R.color.text_color));
        
        binding.filterTvShows.setBackgroundResource(R.drawable.filter_unselected_bg);
        binding.filterTvShows.setTextColor(getResources().getColor(R.color.text_color));
        
        binding.filterCast.setBackgroundResource(R.drawable.filter_unselected_bg);
        binding.filterCast.setTextColor(getResources().getColor(R.color.text_color));
        
        // Set selected filter background and text color
        switch(currentFilter) {
            case FILTER_ALL:
                binding.filterAll.setBackgroundResource(R.drawable.filter_selected_bg);
                binding.filterAll.setTextColor(getResources().getColor(R.color.white));
                break;
            case FILTER_MOVIE:
                binding.filterMovie.setBackgroundResource(R.drawable.filter_selected_bg);
                binding.filterMovie.setTextColor(getResources().getColor(R.color.white));
                break;
            case FILTER_TV_SHOWS:
                binding.filterTvShows.setBackgroundResource(R.drawable.filter_selected_bg);
                binding.filterTvShows.setTextColor(getResources().getColor(R.color.white));
                break;
            case FILTER_CAST:
                binding.filterCast.setBackgroundResource(R.drawable.filter_selected_bg);
                binding.filterCast.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }
    
    private void showLanguageFilterMenu() {
        PopupMenu popup = new PopupMenu(this, binding.btnLanguageFilter);
        
        // Add "All Languages" option
        popup.getMenu().add(0, 0, 0, "All Languages");
        
        // Add available languages from API or hardcoded list
        List<Language> languages = Global.getLanguages();
        for (int i = 0; i < languages.size(); i++) {
            Language lang = languages.get(i);
            // For now, using a simple mapping of language codes to IDs
            // In production, this should come from API
            int languageId = i + 1; // Simple ID mapping
            popup.getMenu().add(0, languageId, i + 1, lang.getEngName());
        }
        
        // Handle menu item clicks
        popup.setOnMenuItemClickListener(item -> {
            selectedLanguageId = item.getItemId();
            selectedLanguageName = item.getTitle().toString();
            
            // Update button tint to show if filter is active
            if (selectedLanguageId == 0) {
                binding.btnLanguageFilter.setColorFilter(getResources().getColor(R.color.text_color_light));
            } else {
                binding.btnLanguageFilter.setColorFilter(getResources().getColor(R.color.app_color));
            }
            
            // Clear and search with new language filter
            changeData();
            return true;
        });
        
        popup.show();
    }
}
package com.retry.vuga.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.activities.BaseActivity;
import com.retry.vuga.adapters.DiscoverAdapter;
import com.retry.vuga.adapters.DiscoverGenreListAdapter;
import com.retry.vuga.adapters.DiscoverLanguageListAdapter;
import com.retry.vuga.databinding.FragmentDiscoverBinding;
import com.retry.vuga.model.AppSetting;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.ViewModelFactory;
import com.retry.vuga.viewmodel.MainViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class DiscoverFragment extends BaseFragment {


    private static final int ALL = 0;
    private static final int MOVIES = 1;
    private static final int SERIES = 2;
    FragmentDiscoverBinding binding;
    CompositeDisposable disposable;


    DiscoverAdapter discoverAdapter;


    boolean isLoading = false;
    boolean dataOver = false;
    LinearLayoutManager linearLayoutManager;
    int selectedGenreId = 0, selectedLanguageId = 0, contentType = ALL;
    String keyWord = "";

    HashMap<String, Object> hashMap = new HashMap<>();
    MainViewModel mainViewModel;


    DiscoverGenreListAdapter genreListAdapter;
    DiscoverLanguageListAdapter languageListAdapter;

    List<AppSetting.GenreItem> genreList;
    List<AppSetting.LanguageItem> languageList;

    public DiscoverFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_discover, container, false);
        mainViewModel = new ViewModelProvider(requireActivity(), new ViewModelFactory(new MainViewModel()).createFor()).get(MainViewModel.class);


        initialization();
        setListeners();

        getSearchContent();


        return binding.getRoot();
    }


    private void getSearchContent() {

        if (dataOver || isLoading) {
            return;
        }

        isLoading = true;

        hashMap.clear();
        hashMap.put(Const.ApiKey.start, discoverAdapter.getItemCount());
        hashMap.put(Const.ApiKey.limit, Const.PAGINATION_COUNT);
        if (!keyWord.isEmpty()) {
            hashMap.put(Const.ApiKey.keyword, keyWord);
        }

        if (selectedLanguageId != 0) {
            hashMap.put(Const.ApiKey.language_id, selectedLanguageId);
        }
        if (selectedGenreId != 0) {
            hashMap.put(Const.ApiKey.genre_id, selectedGenreId);
        }
        if (contentType != ALL) {
            hashMap.put(Const.ApiKey.type, contentType);
        }

        disposable.clear();
        disposable.add(RetrofitClient.getService().searchContent(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {

                    if (discoverAdapter.getItemCount() == 0) {
                        binding.centerLoader.setVisibility(View.VISIBLE);
                        binding.rv.setVisibility(View.GONE);
                    }
                    binding.tvNoContent.setVisibility(View.GONE);

                })
                .doOnTerminate(() -> {

                    binding.centerLoader.setVisibility(View.GONE);

                    isLoading = false;
                }).doOnError(throwable -> {

                    Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    isLoading = false;
                })
                .subscribe((allContent, throwable) -> {


                    if (allContent != null && allContent.getStatus()) {

                        if (allContent.getData().isEmpty()) {
                            if (discoverAdapter.getItemCount() == 0) {
                                binding.tvNoContent.setVisibility(View.VISIBLE);

                            } else {
                                dataOver = true;
                            }
                        } else {


                            if (discoverAdapter.getItemCount() == 0) {
                                discoverAdapter.updateItems(allContent.getData());

                            } else {
                                discoverAdapter.loadMoreItems(allContent.getData());

                            }
                            binding.rv.setVisibility(View.VISIBLE);
                        }


                    } else {

                        Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

                    }

                }));


    }

    private void initialization() {

        disposable = new CompositeDisposable();
        discoverAdapter = new DiscoverAdapter();
        binding.rv.setAdapter(discoverAdapter);
        binding.rv.setItemAnimator(null);
        genreListAdapter = new DiscoverGenreListAdapter(selectedGenreId);
        languageListAdapter = new DiscoverLanguageListAdapter(selectedLanguageId);

        sessionManager.saveIntValue(Const.DataKey.GENRE_ID, 0);

        if (requireActivity() instanceof BaseActivity) {
            ((BaseActivity) requireActivity()).setBlur(binding.loutGenreBlur, binding.rootLout, 20f);
            ((BaseActivity) requireActivity()).setBlur(binding.loutLanguageBlur, binding.rootLout, 20f);

        }

        binding.loutGenreFilter.setVisibility(View.GONE);
        binding.loutLanguageFilter.setVisibility(View.GONE);

//   ------- setting GENRE-----------
        binding.rvGenere.setAdapter(genreListAdapter);
        binding.rvGenere.setItemAnimator(null);
        genreList = sessionManager.getAppSettings().getGenreItems();
        genreListAdapter.updateItems(genreList);
        if (genreList.isEmpty()) {
            binding.tvNoDataGenre.setVisibility(View.VISIBLE);
        }

        //   ------- setting LANGUAGE-----------

        binding.rvLanguage.setAdapter(languageListAdapter);
        binding.rvLanguage.setItemAnimator(null);
        languageList = sessionManager.getAppSettings().getLanguageItems();
        languageListAdapter.updateItems(languageList);
        if (languageList.isEmpty()) {
            binding.tvNoDataLanguage.setVisibility(View.VISIBLE);
        }

    }


    private void setListeners() {


        binding.btnClearSearch.setOnClickListener(v -> {

            binding.etSearch.setText("");
        });

        binding.loutLanguageBlur.setOnClickListener(v -> {

        });
        binding.loutGenreBlur.setOnClickListener(v -> {

        });

        languageListAdapter.onLanguageClick = model -> {
            selectedLanguageId = model.getId();
            binding.tvLanguageName.setText(model.getTitle());
            binding.loutLanguageFilter.setVisibility(selectedLanguageId == 0 ? View.GONE : View.VISIBLE);
            binding.setIsLanguage(selectedLanguageId != 0);
            changeData();
            binding.btnCloseLanguage.performClick();
        };

        binding.btnRemoveLanguageFilter.setOnClickListener(v -> {
            selectedLanguageId = 0;
            binding.loutLanguageFilter.setVisibility(View.GONE);
            binding.setIsLanguage(false);
            changeData();

        });

        binding.btnRemoveGenreFilter.setOnClickListener(v -> {
            selectedGenreId = 0;
            binding.loutGenreFilter.setVisibility(View.GONE);
            binding.setIsGenre(false);
            changeData();

        });

        genreListAdapter.onGenreClick = model -> {

            selectedGenreId = model.getId();
            binding.tvGenreName.setText(model.getTitle());
            binding.loutGenreFilter.setVisibility(selectedGenreId == 0 ? View.GONE : View.VISIBLE);
            binding.setIsGenre(selectedGenreId != 0);
            changeData();
            binding.btnCloseGenre.performClick();

        };

        binding.centerLoader.setOnClickListener(v -> {

        });

        binding.btnLanguage.setOnClickListener(v -> {

            binding.loutLanguageBlur.setVisibility(View.VISIBLE);
            mainViewModel.hideTopBar.setValue(true);
            languageListAdapter.updateSelected(selectedLanguageId);
            setSelectedLanguage();

        });

        binding.btnCloseLanguage.setOnClickListener(v -> {
            binding.loutLanguageBlur.setVisibility(View.GONE);
            mainViewModel.hideTopBar.setValue(false);

        });

        binding.btnCloseGenre.setOnClickListener(v -> {
            binding.loutGenreBlur.setVisibility(View.GONE);
            mainViewModel.hideTopBar.setValue(false);

        });

        binding.btnGENERE.setOnClickListener(v -> {

            binding.loutGenreBlur.setVisibility(View.VISIBLE);
            mainViewModel.hideTopBar.setValue(true);
            genreListAdapter.updateSelected(selectedGenreId);
            setSelectedGenre();


        });


        binding.tvAll.setOnClickListener(v -> {


            contentType = ALL;
            binding.setType(contentType);
            changeData();


        });


        binding.tvMovies.setOnClickListener(v -> {


            contentType = MOVIES;
            binding.setType(contentType);

            changeData();


        });

        binding.tvSeries.setOnClickListener(v -> {


            contentType = SERIES;
            binding.setType(contentType);
            changeData();


        });


        binding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (discoverAdapter.getItemCount() - 1 == linearLayoutManager.findLastVisibleItemPosition() && !isLoading) {
                        getSearchContent();

                    }
                }
            }


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

                if (keyWord.isEmpty()) {
                    binding.btnClearSearch.setVisibility(View.GONE);
                } else {
                    binding.btnClearSearch.setVisibility(View.VISIBLE);

                }
            }
        });

        mainViewModel.hideBottomSheet.observe(getViewLifecycleOwner(), isHide -> {
            if (isHide) {
                mainViewModel.hideBottomSheet.setValue(false);
                binding.btnCloseGenre.performClick();
                binding.btnCloseLanguage.performClick();
            }
        });
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.etSearch.clearFocus();
                InputMethodManager in = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
                dataOver = false;
                getSearchContent();
                return true;
            }
            return false;
        });


    }

    private void setSelectedGenre() {

        List<AppSetting.GenreItem> adapterList = genreListAdapter.getList();

        Optional<AppSetting.GenreItem> item = adapterList.stream().filter(genreItem -> genreItem.getId() == selectedGenreId).findFirst();
        if (item.isPresent()) {
            int pos = adapterList.indexOf(item.get());


            if (pos < adapterList.size()) {

                if (pos == 0) {
                    binding.rvGenere.scrollToPosition(pos);


                } else if (pos + 1 < adapterList.size()) { // to bring item in proper focus
                    binding.rvGenere.scrollToPosition(pos + 1);

                } else {

                    binding.rvGenere.scrollToPosition(pos);
                }
            }
        }
    }


    private void setSelectedLanguage() {

        List<AppSetting.LanguageItem> adapterList = languageListAdapter.getList();

        Optional<AppSetting.LanguageItem> item = adapterList.stream().filter(languageItem -> languageItem.getId() == selectedLanguageId).findFirst();
        if (item.isPresent()) {
            int pos = adapterList.indexOf(item.get());


            if (pos < adapterList.size()) {

                if (pos == 0) {
                    binding.rvLanguage.scrollToPosition(pos);


                } else if (pos + 1 < adapterList.size()) { // to bring item in proper focus
                    binding.rvLanguage.scrollToPosition(pos + 1);

                } else {

                    binding.rvLanguage.scrollToPosition(pos);
                }
            }
        }
    }

    private void changeData() {

        dataOver = false;
        discoverAdapter.clear();
        binding.rv.setAdapter(null);
        discoverAdapter = new DiscoverAdapter();
        binding.rv.setAdapter(discoverAdapter);
        getSearchContent();
    }


}
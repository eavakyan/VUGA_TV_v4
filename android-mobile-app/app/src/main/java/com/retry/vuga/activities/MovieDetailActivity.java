package com.retry.vuga.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.adapters.ContentDetailCastAdapter;
import com.retry.vuga.adapters.ContentDetailEpisodeAdapter;
import com.retry.vuga.adapters.ContentDetailGenreAdapter;
import com.retry.vuga.adapters.ContentDetailSeasonCountAdapter;
import com.retry.vuga.adapters.ContentDetailSourceAdapter;
import com.retry.vuga.adapters.HomeCatObjectAdapter;
import com.retry.vuga.databinding.ActivityMovieDetailBinding;
import com.retry.vuga.databinding.ItemContentSourceBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.Downloads;
import com.retry.vuga.model.history.MovieHistory;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.CustomDialogBuilder;
import com.retry.vuga.utils.Global;
import com.retry.vuga.utils.adds.MyRewardAds;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MovieDetailActivity extends BaseActivity {
    public static final int VIEW = 1;
    public static final int DOWNLOAD = 2;
    ActivityMovieDetailBinding binding;
    int contentId;
    CompositeDisposable disposable;
    ContentDetailCastAdapter castAdapter;
    ContentDetailGenreAdapter genreAdapter;

    HomeCatObjectAdapter moreLikeThisAdapter;
    boolean isAddedToWatchlist = false;
    HashMap<String, Object> dMap;

    List<ContentDetail.DataItem> moreList = new ArrayList<>();
    List<ContentDetail.SubtitlesItem> subTitlesList = new ArrayList<>();
    MyRewardAds myRewardAds;

    boolean rewardEarned = false;

    String trailerUrl;
    String titleName;

    ContentDetailSeasonCountAdapter seasonCountAdapter;
    ContentDetailEpisodeAdapter episodeAdapter;
    int seasonCount = 1;
    int episodeCount = 0;


    ContentDetailSourceAdapter contentSourceAdapter;

    @Override
    protected void onPause() {
        super.onPause();

    }


    ContentDetail.DataItem contentItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail);


        initialization();
        setListeners();


        contentId = getIntent().getIntExtra(Const.DataKey.CONTENT_ID, 0);

        if (contentId != 0) {
            getContentDetail();
        }

    }

    boolean isShareOpen = false;

    private void setListeners() {


        downloading_obj.observe(this, new Observer<Downloads>() {
            @Override
            public void onChanged(Downloads downloads) {
                Log.i("TAG", "internet onReceive: movie ");
                contentSourceAdapter.changeDownloadData(downloads);
            }
        });
        binding.loutSourcesBlur.setOnClickListener(v -> {

        });
        binding.blurViewPopup.setOnClickListener(v -> {

        });
        contentSourceAdapter.setOnItemClick(new ContentDetailSourceAdapter.OnItemClick() {
            @Override
            public void onPendingDownLoad() {
                binding.loutSourcesBlur.setVisibility(View.GONE);
                startActivity(new Intent(MovieDetailActivity.this, DownloadsActivity.class));

            }

            @Override
            public void onClick(ContentDetail.SourceItem model, ItemContentSourceBinding adapterBinding) {
//                AccessType :  1:free , 2:paid , 3:ad

                if (isNetworkConnected()) {
                    if (model.getAccess_type() == 1) {
                        increaseViews(model);

                        Intent intent = new Intent(MovieDetailActivity.this, PlayerNewActivity.class);
                        intent.putExtra(Const.DataKey.CONTENT_SOURCE, new Gson().toJson(model));
                        intent.putExtra(Const.DataKey.SUB_TITLES, new Gson().toJson(subTitlesList));
                        intent.putExtra(Const.DataKey.NAME, titleName);
                        intent.putExtra(Const.DataKey.THUMBNAIL, contentItem.getHorizontalPoster());
                        intent.putExtra(Const.DataKey.CONTENT_NAME, contentItem.getTitle());
                        intent.putExtra(Const.DataKey.CONTENT_ID, contentItem.getId());
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);

                    } else if (model.getAccess_type() == 2) {
//                        premium pop up
                        showPremiumPopup();


                    } else if (model.getAccess_type() == 3) {
//                      video ad pop up
                        showADDPopup(model, VIEW, adapterBinding);

                    }
                } else {
                    Toast.makeText(MovieDetailActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onDownloadClick(ContentDetail.SourceItem model, ItemContentSourceBinding adapterBinding) {

                if (isNetworkConnected()) {
                    if (model.getAccess_type() == 1) {
//                    start Downloading

                        increaseDownloads(model);
                        startBackgroundDownload(model, adapterBinding);
                    }

                    if (model.getAccess_type() == 2) {

                        showPremiumPopup();

                    }
                    if (model.getAccess_type() == 3) {

                        showADDPopup(model, DOWNLOAD, adapterBinding);
                    }
                } else {
                    Toast.makeText(MovieDetailActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onPauseClick(ContentDetail.SourceItem model) {
                if (isNetworkConnected()) {
                    if (downloadService != null && downloadService.getMyDownloader() != null) {
                        downloadService.getMyDownloader().pauseDownload(model);
                    }
                } else {
                    Toast.makeText(MovieDetailActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onResumeClick(ContentDetail.SourceItem model) {

                if (isNetworkConnected()) {
                    if (downloadService != null && downloadService.getMyDownloader() != null) {
                        downloadService.getMyDownloader().resumeDownload(model);
                    }
                } else {
                    Toast.makeText(MovieDetailActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

                }


            }
        });
        binding.btnCloseCast.setOnClickListener(v -> {
            if (binding.rvCast.getVisibility() == View.VISIBLE) {
                binding.btnCloseCast.animate().rotation(-90).setDuration(100);
                binding.rvCast.animate().translationY(-50).setDuration(300);
                new Handler().postDelayed(() -> {
                    binding.rvCast.setVisibility(View.GONE);
                }, 200);
            } else {
                binding.btnCloseCast.animate().rotation(90f).setDuration(200);
                binding.rvCast.setVisibility(View.VISIBLE);
                binding.rvCast.animate().translationY(0).setDuration(200);
            }
        });

        binding.btnCloseStoryLine.setOnClickListener(v -> {
            if (binding.tvDescription.getVisibility() == View.VISIBLE) {
                binding.tvDescription.setVisibility(View.GONE);
                binding.btnCloseStoryLine.animate().rotation(-90f).setDuration(100);
                binding.tvDescription.animate().translationY(-100).setDuration(300);


            } else {
                binding.tvDescription.setVisibility(View.VISIBLE);
                binding.btnCloseStoryLine.animate().rotation(90f).setDuration(200);
                binding.tvDescription.animate().translationY(0).setDuration(200);
            }
        });

        binding.btnShare.setOnClickListener(v -> {

            if (!isShareOpen && contentItem != null) {
                isShareOpen = true;
                binding.loutLoader.setVisibility(View.VISIBLE);

                increaseShare();

                String headLine = contentItem.getTitle();
                ContentMetadata contentMetadata = new ContentMetadata();

                BranchUniversalObject buo = new BranchUniversalObject()
                        .setCanonicalIdentifier("content/12345")
                        .setTitle(headLine)
                        .setContentImageUrl(Const.IMAGE_URL + contentItem.getHorizontalPoster())
                        .setContentDescription(getString(R.string.share_movie_title) + getResources().getString(R.string.app_name))
                        .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                        .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)

                        .setContentMetadata(contentMetadata.addCustomMetadata(Const.DataKey.CONTENT_ID, String.valueOf(contentId)));


                LinkProperties lp = new LinkProperties()
                        .setFeature("sharing")
                        .setCampaign("Content launch")
                        .setStage("User")
                        .addControlParameter("custom", "data")
                        .addControlParameter("custom_random", Long.toString(Calendar.getInstance().getTimeInMillis()));

                buo.generateShortUrl(this, lp, (url, error) -> {
                    isShareOpen = false;
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    String shareBody = url + getString(R.string.for_watching_amazing_content_like_this_install_app_now);
                    intent.setType("text/plain");
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share With");
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(intent, "Share With"));
                    binding.loutLoader.setVisibility(View.GONE);
                });

            }

        });

        binding.loutLoader.setOnClickListener(v -> {

        });
        binding.btnTrailer.setOnClickListener(v -> {
            if (trailerUrl == null || trailerUrl.isEmpty()) {
                return;
            }
            Intent intent = new Intent(this, PlayerNewActivity.class);
            intent.putExtra(Const.DataKey.TRAILER_URL, trailerUrl);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

        });


        binding.imgAddToWatchList.setOnClickListener(v -> {

            binding.loutLoader.setVisibility(View.VISIBLE);
            addRemoveWatchlist(contentId, !binding.getIsWatchlist(), new OnWatchList() {
                @Override
                public void onTerminate() {
                    binding.loutLoader.setVisibility(View.GONE);

                }

                @Override
                public void onError() {
                    binding.loutLoader.setVisibility(View.GONE);

                }

                @Override
                public void onSuccess() {
                    binding.loutLoader.setVisibility(View.GONE);
                    binding.setIsWatchlist(!binding.getIsWatchlist());
                }
            });

        });

        binding.btnCloseSource.setOnClickListener(v -> {
            binding.loutSourcesBlur.setVisibility(View.GONE);
        });

        binding.btnPlay.setOnClickListener(v -> {

            binding.rvSource.setAdapter(contentSourceAdapter);
            ArrayList<MovieHistory> movieHistories = sessionManager.getMovieHistories();
            ArrayList<ContentDetail.SourceItem> sourceItems = new ArrayList<>();
            boolean isAdded = false;
            for (int j = 0; j < movieHistories.size(); j++) {
                MovieHistory movieHistory = movieHistories.get(j);
                if (movieHistory != null && movieHistory.getSources() != null && movieHistory.getMovieId() != null && movieHistory.getMovieId() == contentId) {
                    for (int i = 0; i < contentItem.getContent_sources().size(); i++) {
                        isAdded = true;
                        ContentDetail.SourceItem contentSource = contentItem.getContent_sources().get(i);
                        for (int k = 0; k < movieHistory.getSources().size(); k++) {
                            ContentDetail.SourceItem sourceItem = movieHistory.getSources().get(k);
                            if (sourceItem.getId() == contentItem.getContent_sources().get(i).getId()) {
                                contentSource.playProgress = sourceItem.playProgress;
//                                break;
                            }
                        }
                        sourceItems.add(contentSource);
                    }
                }
            }
            if (!isAdded) {
                sourceItems = (ArrayList<ContentDetail.SourceItem>) contentItem.getContent_sources();
            }
            contentSourceAdapter.updateItems(sourceItems);
            binding.loutSourcesBlur.setVisibility(View.VISIBLE);


        });

        binding.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });


        seasonCountAdapter.setOnItemClick((model, position) -> {
            trailerUrl = contentItem.getSeasons().get(position).getTrailerUrl();
            episodeAdapter.updateItems(contentItem.getSeasons().get(position).getEpisodes());
            seasonCount = position + 1;
            dMap.put(Const.DataKey.SEASON_COUNT, seasonCount);

            Log.i("TAG", "setListeners: s : " + seasonCount);
        });

        episodeAdapter.setOnEpisodeClick((model, position) -> {
            titleName = model.getTitle();
            subTitlesList = model.getSubtitles();
            episodeCount = position + 1;
            dMap.put(Const.DataKey.EPISODE_COUNT, episodeCount);
            dMap.put(Const.DataKey.episode_name, model.getTitle());
            dMap.put(Const.DataKey.episode_image, model.getThumbnail());
            dMap.put(Const.DataKey.content_duration, model.getDuration());


            binding.rvSource.setAdapter(contentSourceAdapter);
            ArrayList<MovieHistory> movieHistories = sessionManager.getMovieHistories();
            ArrayList<ContentDetail.SourceItem> sourceItems = new ArrayList<>();
            boolean isAdded = false;
            for (int j = 0; j < movieHistories.size(); j++) {
                MovieHistory movieHistory = movieHistories.get(j);
                if (movieHistory != null && movieHistory.getSources() != null && movieHistory.getMovieId() != null && movieHistory.getMovieId() == contentId) {
                    for (int i = 0; i < model.getSources().size(); i++) {
                        isAdded = true;
                        ContentDetail.SourceItem contentSource = model.getSources().get(i);
                        for (int k = 0; k < movieHistory.getSources().size(); k++) {
                            ContentDetail.SourceItem sourceItem = movieHistory.getSources().get(k);
                            if (sourceItem.getId() == model.getSources().get(i).getId()) {
                                contentSource.playProgress = sourceItem.playProgress;
//                                break;
                            }
                        }
                        sourceItems.add(contentSource);
                    }
                }
            }
            if (!isAdded) {
                sourceItems = (ArrayList<ContentDetail.SourceItem>) model.getSources();
            }
            contentSourceAdapter.updateItems(sourceItems);
//            contentSourceAdapter.updateItems(model.getSources());
            binding.loutSourcesBlur.setVisibility(View.VISIBLE);


        });


    }

    private void showADDPopup(ContentDetail.SourceItem model, int type, ItemContentSourceBinding adapterBinding) {

        binding.blurViewPopup.setVisibility(View.VISIBLE);
        new CustomDialogBuilder(this).showUnlockDialog(new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {

                loadRewardedAdd(model, type);
            }

            @Override
            public void onDismiss() {
                binding.blurViewPopup.setVisibility(View.GONE);

            }
        });


    }

    private void loadRewardedAdd(ContentDetail.SourceItem model, int type) {
        myRewardAds.showAd();

        myRewardAds.setRewardAdListnear(new MyRewardAds.RewardAdListnear() {
            @Override
            public void onAdClosed() {

                Log.i("TAG", "add:closed ");
                if (rewardEarned) {


                    if (type == VIEW) {

                        increaseViews(model); //api

                        Intent intent = new Intent(MovieDetailActivity.this, PlayerNewActivity.class);
                        intent.putExtra(Const.DataKey.CONTENT_SOURCE, new Gson().toJson(model));
                        intent.putExtra(Const.DataKey.THUMBNAIL, contentItem.getHorizontalPoster());
                        Log.i("TAG", "onClick: " + subTitlesList.size());
                        intent.putExtra(Const.DataKey.NAME, contentItem.getTitle());
                        intent.putExtra(Const.DataKey.CONTENT_NAME, contentItem.getTitle());
                        intent.putExtra(Const.DataKey.CONTENT_ID, contentItem.getId());
                        intent.putExtra(Const.DataKey.SUB_TITLES, new Gson().toJson(subTitlesList));
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);


                    }
                    if (type == DOWNLOAD) {

                        contentSourceAdapter.changeItem(model);
//                        setBinding(adapterBinding, Global.DOWNLOAD_STATE.UNLOCKED);
//                        increaseDownloads(); //api
//                        startBackgroundDownload(model, adapterBinding);


                    }
                    rewardEarned = false;
                }

                myRewardAds = new MyRewardAds(MovieDetailActivity.this);
            }

            @Override
            public void onEarned() {
                rewardEarned = true;
                Log.i("TAG", "add:earned ");

            }
        });


    }

    private void showPremiumPopup() {
        binding.blurViewPopup.setVisibility(View.VISIBLE);

        new CustomDialogBuilder(this).showPremiumDialog(new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {

                startActivity(new Intent(MovieDetailActivity.this, ProActivity.class));

            }

            @Override
            public void onDismiss() {
                binding.blurViewPopup.setVisibility(View.GONE);


            }
        });


    }

    private void increaseShare() {
        disposable.clear();
        disposable.add(RetrofitClient.getService().increaseContentShare(contentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe((response, throwable) -> {
                    if (response != null) {
                        if (response.getStatus()) {
                            Log.i("TAG", "increaseDownloads: " + response.getMessage());
                        }
                    }

                }));
    }

    private void increaseViews(ContentDetail.SourceItem model) {
        Log.d("TAG", "increaseViews: " + model.getEpisodeId());
        disposable.clear();
        if (model.getEpisodeId() != 0) {
            disposable.add(RetrofitClient.getService().increaseEpisodeView(model.getEpisodeId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe((response, throwable) -> {
                    }));
        } else {
            disposable.add(RetrofitClient.getService().increaseContentView(contentId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe((response, throwable) -> {
                    }));
        }
    }

    private void increaseDownloads(ContentDetail.SourceItem model) {
        disposable.clear();
        if (model.getEpisodeId() != 0) {
            disposable.add(RetrofitClient.getService().increaseEpisodeDownload(model.getEpisodeId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe((response, throwable) -> {
                    }));
        } else {
            disposable.add(RetrofitClient.getService().increaseContentDownload(contentId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe((response, throwable) -> {
                    }));
        }

    }

    public String getPath() {
        String state = Environment.getExternalStorageState();
        File filesDir;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            filesDir = this.getExternalFilesDir(null);
        } else {
            // Load another directory, probably local memory
            filesDir = this.getFilesDir();
        }
        return filesDir.getPath();

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void startBackgroundDownload(ContentDetail.SourceItem model, ItemContentSourceBinding adapterBinding) {

        String url;
        if (model.getType() == 1 || model.getType() == 2) {
            Toast.makeText(this, getString(R.string.couldn_t_download), Toast.LENGTH_SHORT).show();
            return;
        } else if (model.getType() == 7) {
            if (model.getMediaItem() == null) {
                if (model.getSource() == null) {
                    return;
                }
                url = Const.IMAGE_URL + model.getSource();// old method
            } else {
                url = Const.IMAGE_URL + model.getMediaItem().getFile();//new method
            }
        } else {
            url = model.getSource();
        }


        String fileName;
        fileName = URLUtil.guessFileName(url, null, MimeTypeMap.getFileExtensionFromUrl(url));
        String path = getPath();


        Downloads downloadingObject = new Downloads(dMap);

        downloadingObject.setQuality(model.getQuality());
        downloadingObject.setSize(model.getSize());
        downloadingObject.setId(model.getId());
        downloadingObject.setPath(path);
        downloadingObject.setFileName(fileName);
        model.setContent_id(contentItem.getId());
        downloadingObject.setSourceItem(model);
        downloadingObject.setPlayProgress(0);
        downloadingObject.setUrl(url);


        File file = new File(path + "/" + fileName);
        Log.i("TAG", "startBackgroundDownload: " + file.getAbsolutePath());

        if (file.exists()) {

            downloadingObject.setDownloadStatus(Const.DownloadStatus.COMPLETED);
            sessionManager.addToDownloads(downloadingObject);
            Toast.makeText(MovieDetailActivity.this, getString(R.string.download_completed), Toast.LENGTH_SHORT).show();
            setBinding(adapterBinding, Const.DownloadStatus.COMPLETED);

        } else {

            downloadingObject.setDownloadStatus(Const.DownloadStatus.QUEUED);
            if (isNetworkConnected()) {
                if (sessionManager.getPendings().contains(downloadingObject)) {
                    // clicked second time
                    return;
                }

                if (downloadService != null && downloadService.getMyDownloader() != null) {

                    sessionManager.addToPending(downloadingObject);
                    downloadService.getMyDownloader().addToDownload(downloadingObject);


                }
            } else {
                Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

            }


        }


    }

//    private int getFileSizeMb(String url) {
//        final int[] size = {0};
//        Thread gfgThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//
//                    try {
//
//                        size[0] = getFileSize(url) / (1024 * 1024);
//
//
//                    } catch (MalformedURLException e) {
//                        Log.i("TAG", "startBackgroundDownload:MalformedURLException " + e.getMessage());
//                    } // Your network activity
//                    // code comes here
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        gfgThread.start();
//        try {
//            gfgThread.join();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        return size[0];
//    }

    //    private static int getFileSize(String string) throws MalformedURLException {
//
//
//        URL myURL = new URL(string);
//        URLConnection conn = null;
//        try {
//            conn = myURL.openConnection();
//            if (conn instanceof HttpURLConnection) {
//                ((HttpURLConnection) conn).setRequestMethod("HEAD");
//            }
//            conn.getInputStream();
//            return conn.getContentLength();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            if (conn instanceof HttpURLConnection) {
//                ((HttpURLConnection) conn).disconnect();
//            }
//        }
//    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        downloading_obj.removeObservers(this);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    private void setBinding(ItemContentSourceBinding adapterBinding, int downloadState) {

        adapterBinding.progress.setVisibility(View.GONE);
        adapterBinding.imgPlay.setVisibility(View.GONE);
        adapterBinding.imgPause.setVisibility(View.GONE);
        adapterBinding.imgDownload.setVisibility(View.GONE);
        adapterBinding.imgCheck.setVisibility(View.GONE);
        adapterBinding.imgPending.setVisibility(View.GONE);
        adapterBinding.imgLock.setVisibility(View.GONE);
        switch (downloadState) {
            case Const.DownloadStatus.COMPLETED:
                adapterBinding.imgCheck.setVisibility(View.VISIBLE);
                break;

        }

    }

    private void getContentDetail() {

        disposable.add(RetrofitClient.getService().getContentDetail(sessionManager.getUser().getId(), contentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {
                    binding.loutLoader.setVisibility(View.VISIBLE);

                })
                .doOnTerminate(() -> {

                    binding.loutLoader.setVisibility(View.GONE);


                }).doOnError(throwable -> {

                    binding.loutLoader.setVisibility(View.VISIBLE);


                })
                .subscribe((contentDetail, throwable) -> {


                    if (contentDetail != null) {

                        if (contentDetail.getStatus()) {

                            if (contentDetail.getData() != null) {
                                contentItem = contentDetail.getData();
                                binding.loutLoader.setVisibility(View.GONE);
                                setContentDetail();
                            }
                        } else {


                        }
                    }

                }));

    }

    private void setContentDetail() {
        dMap = new HashMap<>();


        dMap.put(Const.DataKey.NAME, contentItem.getTitle());
        dMap.put(Const.DataKey.CONTENT_TYPE, contentItem.getType());
        dMap.put(Const.DataKey.CONTENT_ID, contentItem.getId());
        dMap.put(Const.DataKey.IMAGE, contentItem.getHorizontalPoster());

        dMap.put(Const.DataKey.content_duration, contentItem.getDuration());

        if (contentItem.getType() == 2) {
            dMap.put(Const.DataKey.SEASON_COUNT, seasonCount);
            dMap.put(Const.DataKey.EPISODE_COUNT, episodeCount);

        }
        if (contentItem.getType() == 1) {

            trailerUrl = contentItem.getTrailerUrl();
        } else {
            trailerUrl = contentItem.getSeasons().isEmpty() ? "" : contentItem.getSeasons().get(0).getTrailerUrl();

        }

        if (contentItem.getType() == 1) {

            titleName = contentItem.getTitle();
        }
        binding.setContent(contentItem);

        if (contentItem.getGenreList().isEmpty()) {
            List<String> list = Global.getGenreListFromIds(contentItem.getGenreIds(), this);
            contentItem.setGenreList(list);
            setGenreAdapter(list);

        } else {
            setGenreAdapter(contentItem.getGenreList());


        }

        binding.setIsWatchlist(contentItem.getIs_watchlist());
        isAddedToWatchlist = contentItem.getIs_watchlist();


        if (contentItem.getCast().isEmpty()) {
            binding.loutCast.setVisibility(View.GONE);
        } else {
            binding.loutCast.setVisibility(View.VISIBLE);
            castAdapter.updateItems(contentItem.getCast());
        }


        for (int i = 0; i < contentItem.getMoreLikeThis().size(); i++) {
            if (contentId != contentItem.getMoreLikeThis().get(i).getId()) {
                moreList.add(contentItem.getMoreLikeThis().get(i));
            }
        }

        if (moreList.isEmpty() || contentItem.getType() == 2)
            binding.setIsMoreLikeThisVisible(false);
        else {
            binding.setIsMoreLikeThisVisible(true);
            moreLikeThisAdapter.updateItems(moreList);

        }

        if (contentItem.getType() == 2) {
            seasonCountAdapter.updateItems(contentItem.getSeasons());
            if (!contentItem.getSeasons().isEmpty()) {
                episodeAdapter.updateItems(contentItem.getSeasons().get(0).getEpisodes());
            }
        }


        Log.i("TAG", "setContentDetail: " + contentItem.getSubtitles().size());
        if (!contentItem.getSubtitles().isEmpty()) {
            subTitlesList = contentItem.getSubtitles();
            Log.i("TAG", "setContentDetail: " + subTitlesList.size());
        }

    }


    private void setGenreAdapter(List<String> list) {
        genreAdapter.updateItems(list);
    }


    private void initialization() {


        myRewardAds = new MyRewardAds(this);
        disposable = new CompositeDisposable();
        castAdapter = new ContentDetailCastAdapter();
        moreLikeThisAdapter = new HomeCatObjectAdapter();
        contentSourceAdapter = new ContentDetailSourceAdapter();
        seasonCountAdapter = new ContentDetailSeasonCountAdapter();
        episodeAdapter = new ContentDetailEpisodeAdapter();
        genreAdapter = new ContentDetailGenreAdapter();

        setBlur(binding.blurView, binding.rootLout, 15f);
        setBlur(binding.blurView2, binding.rootLout, 15f);
        setBlur(binding.loutSourcesBlur, binding.rootLout, 20f);
        setBlur(binding.blurViewPopup, binding.rootLout, 10f);

        binding.rvCast.setAdapter(castAdapter);
        binding.rvMoreLikeThis.setAdapter(moreLikeThisAdapter);
        binding.rvSeason.setAdapter(seasonCountAdapter);
        binding.rvEpisodes.setAdapter(episodeAdapter);
        binding.rvGenere.setAdapter(genreAdapter);

        binding.rvCast.setItemAnimator(null);
        binding.rvMoreLikeThis.setItemAnimator(null);
        binding.rvSeason.setItemAnimator(null);
        binding.rvEpisodes.setItemAnimator(null);
        binding.rvSource.setItemAnimator(null);
        binding.rvEpisodes.setItemAnimator(null);


        //................. fot chips..........................
        FlexboxLayoutManager flayoutManager = new FlexboxLayoutManager(this);
        flayoutManager.setFlexDirection(FlexDirection.ROW);
        flayoutManager.setJustifyContent(JustifyContent.CENTER);
        flayoutManager.setAlignItems(AlignItems.CENTER);
        binding.rvGenere.setLayoutManager(flayoutManager);
//....................................................

    }


}

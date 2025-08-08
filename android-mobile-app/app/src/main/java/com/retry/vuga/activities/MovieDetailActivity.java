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
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import androidx.databinding.DataBindingUtil;
import com.bumptech.glide.Glide;
import androidx.lifecycle.Observer;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.mediarouter.app.MediaRouteChooserDialog;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.common.images.WebImage;
import android.net.Uri;

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
import com.retry.vuga.model.Profile;
import com.retry.vuga.model.history.MovieHistory;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.CustomDialogBuilder;
import com.retry.vuga.utils.Global;
import com.retry.vuga.utils.TrailerUtils;
import com.retry.vuga.utils.UniversalCastButton;
import com.retry.vuga.utils.UniversalCastManager;
import com.retry.vuga.utils.adds.MyRewardAds;
import com.retry.vuga.dialogs.DownloadProgressDialog;
import com.retry.vuga.dialogs.RatingDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import android.os.Handler;

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
    DownloadProgressDialog downloadProgressDialog;
    ContentDetail.SourceItem currentDownloadingSource;

    @Override
    protected void onPause() {
        super.onPause();
        
        // Pause trailer video if playing
        if (binding != null) {
            if (binding.videoTrailer != null && binding.videoTrailer.isPlaying()) {
                binding.videoTrailer.pause();
                binding.btnPlayPauseTrailer.setImageResource(R.drawable.ic_play);
                binding.btnPlayPauseTrailer.setVisibility(View.VISIBLE);
                binding.btnPlayPauseTrailer.setAlpha(1f);
            }
            
            // Pause YouTube video if playing
            if (binding.webviewYoutubeTrailer != null && binding.webviewYoutubeTrailer.getVisibility() == View.VISIBLE) {
                binding.webviewYoutubeTrailer.onPause();
                binding.webviewYoutubeTrailer.pauseTimers();
            }
        }
    }


    ContentDetail.DataItem contentItem = null;

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh content detail to get latest watchlist state
        if (contentId != 0 && contentItem != null) {
            getContentDetail();
        }
        
        // Resume YouTube video if it was playing
        if (binding != null && binding.webviewYoutubeTrailer != null && 
            binding.webviewYoutubeTrailer.getVisibility() == View.VISIBLE) {
            binding.webviewYoutubeTrailer.onResume();
            binding.webviewYoutubeTrailer.resumeTimers();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail);


        initialization();
        setListeners();
        
        // Ensure loader is hidden initially
        binding.loutLoader.setVisibility(View.GONE);


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
                
                // Update download progress dialog if it's showing
                if (downloadProgressDialog != null && downloadProgressDialog.isShowing() && 
                    currentDownloadingSource != null && downloads.getId() == currentDownloadingSource.getId()) {
                    
                    downloadProgressDialog.setDownloadState(downloads.getDownloadStatus());
                    
                    if (downloads.getDownloadStatus() == Const.DownloadStatus.PROGRESSING) {
                        downloadProgressDialog.updateProgress(downloads.getProgress());
                        
                        // Calculate downloaded size - use source size as it's constant
                        long totalSize;
                        if (currentDownloadingSource != null && currentDownloadingSource.getSize() != null) {
                            try {
                                totalSize = Long.parseLong(currentDownloadingSource.getSize()) * 1024L * 1024L; // Convert MB to bytes
                            } catch (Exception e) {
                                totalSize = 500L * 1024L * 1024L; // Default to 500MB
                            }
                        } else {
                            try {
                                totalSize = Long.parseLong(downloads.getSize()) * 1024L * 1024L; // Convert MB to bytes
                            } catch (Exception e) {
                                totalSize = 500L * 1024L * 1024L; // Default to 500MB
                            }
                        }
                        long downloadedSize = (long)(totalSize * downloads.getProgress() / 100.0);
                        downloadProgressDialog.updateDownloadSize(downloadedSize, totalSize);
                    } else if (downloads.getDownloadStatus() == Const.DownloadStatus.COMPLETED) {
                        // Auto dismiss after 2 seconds when completed
                        new Handler().postDelayed(() -> {
                            if (downloadProgressDialog != null && downloadProgressDialog.isShowing()) {
                                downloadProgressDialog.dismiss();
                            }
                        }, 2000);
                    }
                }
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

        // Setup UniversalCastButton
        binding.btnCast.setOnCastDeviceSelectedListener(new UniversalCastButton.OnCastDeviceSelectedListener() {
            @Override
            public void onDeviceSelected(UniversalCastManager.CastDevice device) {
                Log.d("MovieDetail", "Cast device selected: " + device.name + " (" + device.type + ")");
                if (contentItem != null) {
                    // Handle casting based on device type
                    if (device.type == UniversalCastManager.DeviceType.GOOGLE_CAST) {
                        // For Google Cast, use existing loadMediaToCast method
                        CastContext castContext = CastContext.getSharedInstance(MovieDetailActivity.this);
                        CastSession castSession = castContext.getSessionManager().getCurrentCastSession();
                        if (castSession != null && castSession.isConnected()) {
                            loadMediaToCast(castSession);
                        }
                    } else if (device.type == UniversalCastManager.DeviceType.DLNA) {
                        // For DLNA, cast the video
                        castToDlna(device);
                    }
                }
            }
            
            @Override
            public void onDeviceDisconnected() {
                Log.d("MovieDetail", "Cast device disconnected");
                stopCasting();
            }
        });
        
        binding.btnShare.setOnClickListener(v -> {
            Log.d("MovieDetail", "Share button clicked!");
            
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

        Log.d("Watchlist", "Setting up watchlist click listener, view is: " + binding.imgAddToWatchList);
        
        // Ensure the watchlist button is clickable and on top
        binding.imgAddToWatchList.setClickable(true);
        binding.imgAddToWatchList.setEnabled(true);
        binding.imgAddToWatchList.bringToFront();
        
        // Add touch listener to verify touches are being received
        binding.imgAddToWatchList.setOnTouchListener((v, event) -> {
            Log.d("Watchlist", "Touch event: " + event.getAction());
            return false; // Return false to let the click listener handle it
        });
        
        binding.imgAddToWatchList.setOnClickListener(v -> {
            Log.d("Watchlist", "Watchlist icon clicked!");
            
            // Ensure we're not blocked by loader
            if (binding.loutLoader.getVisibility() == View.VISIBLE) {
                Log.e("Watchlist", "Loader is blocking clicks!");
                binding.loutLoader.setVisibility(View.GONE);
            }
            
            binding.loutLoader.setVisibility(View.VISIBLE);
            boolean newWatchlistState = !isAddedToWatchlist;
            Log.d("Watchlist", "Current state: " + isAddedToWatchlist + ", New state: " + newWatchlistState);
            addRemoveWatchlist(MovieDetailActivity.this, contentId, newWatchlistState, new OnWatchList() {
                @Override
                public void onTerminate() {
                    binding.loutLoader.setVisibility(View.GONE);

                }

                @Override
                public void onError() {
                    binding.loutLoader.setVisibility(View.GONE);
                    Log.e("Watchlist", "Error updating watchlist");
                }

                @Override
                public void onSuccess() {
                    binding.loutLoader.setVisibility(View.GONE);
                    isAddedToWatchlist = newWatchlistState;
                    // Update the content item's watchlist state as well
                    if (contentItem != null) {
                        contentItem.setIs_watchlist(newWatchlistState);
                    }
                    binding.setIsWatchlist(newWatchlistState);
                    binding.executePendingBindings();
                    
                    // Manually update the icon as a fallback
                    binding.imgAddToWatchList.setImageResource(
                        newWatchlistState ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_not
                    );
                    
                    // Force the watchlist button to be clickable again
                    binding.imgAddToWatchList.setClickable(true);
                    binding.imgAddToWatchList.setEnabled(true);
                    
                    Log.d("Watchlist", "Update successful - new state: " + newWatchlistState);
                }
            });

        });

        binding.btnCloseSource.setOnClickListener(v -> {
            binding.loutSourcesBlur.setVisibility(View.GONE);
        });
        
        binding.btnDownload.setOnClickListener(v -> {
            // Handle download button click
            if (contentItem == null || contentItem.getContent_sources() == null || contentItem.getContent_sources().isEmpty()) {
                Toast.makeText(MovieDetailActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check age restrictions first
            if (!checkAgeRestrictions()) {
                return;
            }
            
            // Get the first downloadable source
            ContentDetail.SourceItem downloadableSource = null;
            for (ContentDetail.SourceItem source : contentItem.getContent_sources()) {
                if (source.getIs_download() == 1) {
                    downloadableSource = source;
                    break;
                }
            }
            
            if (downloadableSource == null) {
                Toast.makeText(MovieDetailActivity.this, getString(R.string.no_downloadable_source), Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check if already downloaded
            List<Downloads> downloads = sessionManager.getDownloads();
            for (Downloads download : downloads) {
                if (download.getSourceItem() != null && download.getSourceItem().getId() == downloadableSource.getId()) {
                    Toast.makeText(MovieDetailActivity.this, getString(R.string.already_downloaded), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Check access type and start download
            if (isNetworkConnected()) {
                if (downloadableSource.getAccess_type() == 1) {
                    // Free content - start download directly
                    increaseDownloads(downloadableSource);
                    currentDownloadingSource = downloadableSource;
                    showDownloadProgressDialog(downloadableSource);
                } else if (downloadableSource.getAccess_type() == 2) {
                    // Premium content
                    showPremiumPopup();
                } else if (downloadableSource.getAccess_type() == 3) {
                    // Ad-locked content
                    showADDPopup(downloadableSource, DOWNLOAD, null);
                }
            } else {
                Toast.makeText(MovieDetailActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnPlay.setOnClickListener(v -> {
            // Direct play without source selection
            if (contentItem == null || contentItem.getContent_sources() == null || contentItem.getContent_sources().isEmpty()) {
                Toast.makeText(MovieDetailActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the first (and only) source
            ContentDetail.SourceItem sourceToPlay = contentItem.getContent_sources().get(0);
            
            // Check play progress from history
            ArrayList<MovieHistory> movieHistories = sessionManager.getMovieHistories();
            for (MovieHistory movieHistory : movieHistories) {
                if (movieHistory != null && movieHistory.getSources() != null && 
                    movieHistory.getMovieId() != null && movieHistory.getMovieId() == contentId) {
                    for (ContentDetail.SourceItem historySource : movieHistory.getSources()) {
                        if (historySource.getId() == sourceToPlay.getId()) {
                            sourceToPlay.playProgress = historySource.playProgress;
                            break;
                        }
                    }
                }
            }

            // Check access type and play directly
            if (isNetworkConnected()) {
                if (sourceToPlay.getAccess_type() == 1) {
                    increaseViews(sourceToPlay);

                    Intent intent = new Intent(MovieDetailActivity.this, PlayerNewActivity.class);
                    intent.putExtra(Const.DataKey.CONTENT_SOURCE, new Gson().toJson(sourceToPlay));
                    intent.putExtra(Const.DataKey.SUB_TITLES, new Gson().toJson(subTitlesList));
                    intent.putExtra(Const.DataKey.NAME, titleName);
                    intent.putExtra(Const.DataKey.THUMBNAIL, contentItem.getHorizontalPoster());
                    intent.putExtra(Const.DataKey.CONTENT_NAME, contentItem.getTitle());
                    intent.putExtra(Const.DataKey.CONTENT_ID, contentItem.getId());
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);

                } else if (sourceToPlay.getAccess_type() == 2) {
                    // Premium pop up
                    showPremiumPopup();

                } else if (sourceToPlay.getAccess_type() == 3) {
                    // Video ad pop up
                    showADDPopup(sourceToPlay, VIEW, null);
                }
            } else {
                Toast.makeText(MovieDetailActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
        
        // Add click listener for rating
        binding.layoutRating.setOnClickListener(v -> {
            if (contentItem != null) {
                showRatingDialog();
            }
        });
        
        // Debug check for overlays
        Log.d("Watchlist", "Initial visibility check:");
        Log.d("Watchlist", "Blur view visibility: " + binding.blurView.getVisibility());
        Log.d("Watchlist", "Sources blur visibility: " + binding.loutSourcesBlur.getVisibility());
        Log.d("Watchlist", "Loader visibility: " + binding.loutLoader.getVisibility());
        Log.d("Watchlist", "Watchlist button visibility: " + binding.imgAddToWatchList.getVisibility());


        seasonCountAdapter.setOnItemClick((model, position) -> {
            trailerUrl = contentItem.getSeasons().get(position).getTrailerUrl();
            episodeAdapter.updateItems(contentItem.getSeasons().get(position).getEpisodes());
            seasonCount = position + 1;
            dMap.put(Const.DataKey.SEASON_COUNT, seasonCount);

            Log.i("TAG", "setListeners: s : " + seasonCount);
        });

        episodeAdapter.setOnEpisodeRatingClick(episode -> {
            showEpisodeRatingDialog(episode);
        });
        
        episodeAdapter.setOnEpisodeClick((model, position) -> {
            // Check age restrictions first
            if (!checkAgeRestrictions()) {
                return;
            }
            
            titleName = model.getTitle();
            subTitlesList = model.getSubtitles();
            episodeCount = position + 1;
            dMap.put(Const.DataKey.EPISODE_COUNT, episodeCount);
            dMap.put(Const.DataKey.episode_name, model.getTitle());
            dMap.put(Const.DataKey.episode_image, model.getThumbnail());
            dMap.put(Const.DataKey.content_duration, model.getDuration());

            // Direct play without source selection
            if (model.getSources() == null || model.getSources().isEmpty()) {
                Toast.makeText(MovieDetailActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the first (and only) source
            ContentDetail.SourceItem sourceToPlay = model.getSources().get(0);
            
            // Check play progress from history
            ArrayList<MovieHistory> movieHistories = sessionManager.getMovieHistories();
            for (MovieHistory movieHistory : movieHistories) {
                if (movieHistory != null && movieHistory.getSources() != null && 
                    movieHistory.getMovieId() != null && movieHistory.getMovieId() == contentId) {
                    for (ContentDetail.SourceItem historySource : movieHistory.getSources()) {
                        if (historySource.getId() == sourceToPlay.getId()) {
                            sourceToPlay.playProgress = historySource.playProgress;
                            break;
                        }
                    }
                }
            }

            // Check access type and play directly
            if (isNetworkConnected()) {
                if (sourceToPlay.getAccess_type() == 1) {
                    increaseViews(sourceToPlay);

                    Intent intent = new Intent(MovieDetailActivity.this, PlayerNewActivity.class);
                    intent.putExtra(Const.DataKey.CONTENT_SOURCE, new Gson().toJson(sourceToPlay));
                    intent.putExtra(Const.DataKey.SUB_TITLES, new Gson().toJson(subTitlesList));
                    intent.putExtra(Const.DataKey.NAME, titleName);
                    intent.putExtra(Const.DataKey.THUMBNAIL, contentItem.getHorizontalPoster());
                    intent.putExtra(Const.DataKey.CONTENT_NAME, contentItem.getTitle());
                    intent.putExtra(Const.DataKey.CONTENT_ID, contentItem.getId());
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);

                } else if (sourceToPlay.getAccess_type() == 2) {
                    // Premium pop up
                    showPremiumPopup();

                } else if (sourceToPlay.getAccess_type() == 3) {
                    // Video ad pop up
                    showADDPopup(sourceToPlay, VIEW, null);
                }
            } else {
                Toast.makeText(MovieDetailActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            }
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
    
    private void showDownloadProgressDialog(ContentDetail.SourceItem sourceItem) {
        downloadProgressDialog = new DownloadProgressDialog(this, contentItem.getTitle(), contentItem.getHorizontalPoster());
        
        // Set initial file size from source
        if (sourceItem != null && sourceItem.getSize() != null) {
            try {
                long totalSize = Long.parseLong(sourceItem.getSize()) * 1024L * 1024L; // Convert MB to bytes
                downloadProgressDialog.updateDownloadSize(0, totalSize);
            } catch (Exception e) {
                // Default to 0 / 500MB if parsing fails
                downloadProgressDialog.updateDownloadSize(0, 500L * 1024L * 1024L);
            }
        }
        
        downloadProgressDialog.setOnDownloadActionListener(new DownloadProgressDialog.OnDownloadActionListener() {
            @Override
            public void onBackgroundDownload() {
                // Continue download in background
                Toast.makeText(MovieDetailActivity.this, "Download continues in background", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onCancelDownload() {
                // Cancel the download
                if (downloadService != null && downloadService.getMyDownloader() != null) {
                    downloadService.getMyDownloader().cancelDownload();
                }
                // Remove from pending downloads
                List<Downloads> pendings = sessionManager.getPendings();
                for (Downloads d : pendings) {
                    if (d.getId() == sourceItem.getId()) {
                        sessionManager.removeFileFromPending(d);
                        break;
                    }
                }
                
                Toast.makeText(MovieDetailActivity.this, "Download cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        
        downloadProgressDialog.show();
        downloadProgressDialog.setDownloadState(Const.DownloadStatus.START);
        
        // Start the actual download
        ItemContentSourceBinding dummyBinding = ItemContentSourceBinding.inflate(getLayoutInflater());
        startBackgroundDownload(sourceItem, dummyBinding);
    }
    
    private boolean hasEnoughStorage(long requiredBytes) {
        try {
            File path = new File(getPath());
            long availableBytes = path.getUsableSpace();
            long totalBytes = path.getTotalSpace();
            
            // Industry best practice: Allow downloads up to 10% of total storage or 90% of available storage
            long maxAllowedBytes = Math.min(totalBytes / 10, (long)(availableBytes * 0.9));
            
            // Check if required space is available
            if (requiredBytes > maxAllowedBytes) {
                return false;
            }
            
            // Also check if at least 500MB will remain after download
            long minRemainingSpace = 500L * 1024L * 1024L; // 500MB
            return (availableBytes - requiredBytes) > minRemainingSpace;
        } catch (Exception e) {
            Log.e("Storage", "Error checking storage: " + e.getMessage());
            return true; // Allow download if we can't check
        }
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
            // Check storage before downloading
            long estimatedSize;
            try {
                int sizeInMB = Integer.parseInt(model.getSize());
                estimatedSize = sizeInMB > 0 ? sizeInMB * 1024L * 1024L : 500L * 1024L * 1024L;
            } catch (Exception e) {
                estimatedSize = 500L * 1024L * 1024L; // Default to 500MB if parsing fails
            }
            
            if (!hasEnoughStorage(estimatedSize)) {
                Toast.makeText(MovieDetailActivity.this, getString(R.string.insufficient_storage), Toast.LENGTH_SHORT).show();
                return;
            }

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
        if (downloadProgressDialog != null && downloadProgressDialog.isShowing()) {
            downloadProgressDialog.dismiss();
        }
        
        // Clean up WebView
        if (binding != null && binding.webviewYoutubeTrailer != null) {
            binding.webviewYoutubeTrailer.loadUrl("about:blank");
            binding.webviewYoutubeTrailer.clearHistory();
            binding.webviewYoutubeTrailer.destroy();
        }
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
        Integer profileId = sessionManager.getUser().getLastActiveProfileId();
        
        disposable.add(RetrofitClient.getService().getContentDetail(sessionManager.getUser().getId(), contentId, profileId)
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

            trailerUrl = TrailerUtils.getEffectiveTrailerUrl(contentItem);
        } else {
            trailerUrl = contentItem.getSeasons().isEmpty() ? "" : contentItem.getSeasons().get(0).getTrailerUrl();

        }

        if (contentItem.getType() == 1) {

            titleName = contentItem.getTitle();
        }
        binding.setContent(contentItem);
        
        // Setup trailer video player if trailer exists
        setupTrailerPlayer();

        if (contentItem.getGenreList().isEmpty()) {
            List<String> list = Global.getGenreListFromIds(contentItem.getGenreIds(), this);
            contentItem.setGenreList(list);
            setGenreAdapter(list);

        } else {
            setGenreAdapter(contentItem.getGenreList());


        }

        binding.setIsWatchlist(contentItem.getIs_watchlist());
        isAddedToWatchlist = contentItem.getIs_watchlist();
        Log.d("Watchlist", "Initial watchlist state for content " + contentId + ": " + isAddedToWatchlist);
        
        // Manually set initial icon state as well
        binding.imgAddToWatchList.setImageResource(
            isAddedToWatchlist ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_not
        );


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

        if (moreList.isEmpty())
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

        // Set content sources for movie type
        if (contentItem.getType() == 1 && contentItem.getContent_sources() != null && !contentItem.getContent_sources().isEmpty()) {
            contentSourceAdapter.updateItems(contentItem.getContent_sources());
        }

        // Check if any source is downloadable and show/hide download button accordingly
        boolean hasDownloadableSource = false;
        
        // For movies, check content sources directly
        if (contentItem.getType() == 1 && contentItem.getContent_sources() != null) {
            for (ContentDetail.SourceItem source : contentItem.getContent_sources()) {
                if (source.getIs_download() == 1) {
                    hasDownloadableSource = true;
                    break;
                }
            }
        }
        
        // For series (type == 2), download button should remain hidden
        // Series downloads are handled at episode level, not content level
        
        binding.btnDownload.setVisibility(hasDownloadableSource ? View.VISIBLE : View.GONE);

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
        setBlur(binding.loutSourcesBlur, binding.rootLout, 20f);
        setBlur(binding.blurViewPopup, binding.rootLout, 10f);
        
        // Initialize Cast session management
        initializeCastSessionManagement();
        
        // Make sure blur views don't intercept clicks
        binding.blurView.setClickable(false);
        binding.blurView.setFocusable(false);

        binding.rvCast.setAdapter(castAdapter);
        binding.rvMoreLikeThis.setAdapter(moreLikeThisAdapter);
        binding.rvSeason.setAdapter(seasonCountAdapter);
        binding.rvEpisodes.setAdapter(episodeAdapter);
        binding.rvGenere.setAdapter(genreAdapter);
        binding.rvSource.setAdapter(contentSourceAdapter);

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
    
    private void showCastDialog() {
        Log.d("Cast", "showCastDialog called");
        try {
            CastContext castContext = CastContext.getSharedInstance(this);
            Log.d("Cast", "CastContext obtained");
            CastSession castSession = castContext.getSessionManager().getCurrentCastSession();
            
            if (castSession != null && castSession.isConnected()) {
                Log.d("Cast", "Already connected to cast device");
                // Already connected to a cast device, load the media
                loadMediaToCast(castSession);
            } else {
                Log.d("Cast", "Not connected, showing cast dialog");
                // Create and show MediaRouteChooserDialog
                MediaRouteChooserDialog dialog = new MediaRouteChooserDialog(this);
                dialog.setRouteSelector(CastContext.getSharedInstance(this).getMergedSelector());
                dialog.show();
                Log.d("Cast", "MediaRouteChooserDialog shown");
                
                // Listen for cast connection
                castContext.getSessionManager().addSessionManagerListener(
                    new SessionManagerListener<CastSession>() {
                        @Override
                        public void onSessionStarted(CastSession session, String sessionId) {
                            loadMediaToCast(session);
                            castContext.getSessionManager().removeSessionManagerListener(this, CastSession.class);
                        }
                        
                        @Override
                        public void onSessionResumed(CastSession session, boolean wasSuspended) {}
                        
                        @Override
                        public void onSessionEnded(CastSession session, int error) {}
                        
                        @Override
                        public void onSessionSuspended(CastSession session, int reason) {}
                        
                        @Override
                        public void onSessionStarting(CastSession session) {}
                        
                        @Override
                        public void onSessionStartFailed(CastSession session, int error) {
                            Toast.makeText(MovieDetailActivity.this, "Failed to connect to cast device", Toast.LENGTH_SHORT).show();
                        }
                        
                        @Override
                        public void onSessionEnding(CastSession session) {}
                        
                        @Override
                        public void onSessionResuming(CastSession session, String sessionId) {}
                        
                        @Override
                        public void onSessionResumeFailed(CastSession session, int error) {}
                    }, CastSession.class);
            }
        } catch (Exception e) {
            Log.e("Cast", "Error showing cast dialog: " + e.getMessage());
            Toast.makeText(this, "Cast is not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadMediaToCast(CastSession castSession) {
        if (contentItem == null || contentItem.getContent_sources() == null || contentItem.getContent_sources().isEmpty()) {
            Toast.makeText(this, "No media source available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get the first available source
        ContentDetail.SourceItem source = contentItem.getContent_sources().get(0);
        
        // Build media metadata
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, contentItem.getTitle());
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, contentItem.getDescription());
        
        if (contentItem.getHorizontalPoster() != null) {
            movieMetadata.addImage(new WebImage(Uri.parse(Const.BASE + contentItem.getHorizontalPoster())));
        }
        
        // Build media info
        MediaInfo mediaInfo = new MediaInfo.Builder(source.getSource())
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("video/mp4")
                .setMetadata(movieMetadata)
                .build();
        
        // Load media to cast device
        MediaLoadRequestData loadRequest = new MediaLoadRequestData.Builder()
                .setMediaInfo(mediaInfo)
                .setAutoplay(true)
                .build();
                
        castSession.getRemoteMediaClient().load(loadRequest);
        Toast.makeText(this, "Casting to TV...", Toast.LENGTH_SHORT).show();
    }
    
    private void castToDlna(UniversalCastManager.CastDevice device) {
        Log.d("MovieDetail", "Casting to DLNA device: " + device.name);
        
        // Get the cast manager from the button
        UniversalCastManager castManager = binding.btnCast.getCastManager();
        
        if (contentItem != null && contentItem.getContent_sources() != null && !contentItem.getContent_sources().isEmpty()) {
            // Get the first available source
            ContentDetail.SourceItem source = contentItem.getContent_sources().get(0);
            String videoUrl = source.getSource();
            
            // Ensure video URL is absolute
            if (!videoUrl.startsWith("http://") && !videoUrl.startsWith("https://")) {
                // Check source type
                if (source.getType() == 7 && source.getMediaItem() != null) {
                    videoUrl = Const.IMAGE_URL + source.getMediaItem().getFile();
                } else {
                    videoUrl = Const.IMAGE_URL + source.getSource();
                }
            }
            
            String title = contentItem.getTitle();
            Log.d("MovieDetail", "Video URL for DLNA: " + videoUrl);
            
            // Show progress
            Toast.makeText(this, "Connecting to " + device.name + "...", Toast.LENGTH_SHORT).show();
            
            // Connect to the DLNA device and immediately cast
            castManager.connectToDlnaDevice(device);
            
            // Cast the media immediately after connection
            // The castManager will handle the connection state internally
            String imageUrl = contentItem.getHorizontalPoster() != null ? 
                Const.BASE + contentItem.getHorizontalPoster() : "";
            
            // Create final copies for lambda
            final String finalVideoUrl = videoUrl;
            final String finalTitle = title;
            
            // Small delay to ensure connection is established
            new Handler().postDelayed(() -> {
                castManager.castMedia(finalVideoUrl, finalTitle, contentItem.getDescription(), imageUrl);
            }, 500);
        } else {
            Toast.makeText(this, "No video source available", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void stopCasting() {
        // Get the cast manager from the button
        UniversalCastManager castManager = binding.btnCast.getCastManager();
        castManager.stopCasting();
        updateCastState(false, null);
    }
    
    private void initializeCastSessionManagement() {
        try {
            CastContext castContext = CastContext.getSharedInstance(this);
            castContext.getSessionManager().addSessionManagerListener(new SessionManagerListener<CastSession>() {
                @Override
                public void onSessionStarted(CastSession session, String sessionId) {
                    Log.d("Cast", "Session started");
                    updateCastState(true, session);
                    if (contentItem != null) {
                        loadMediaToCast(session);
                    }
                }
                
                @Override
                public void onSessionResumed(CastSession session, boolean wasSuspended) {
                    Log.d("Cast", "Session resumed");
                    updateCastState(true, session);
                }
                
                @Override
                public void onSessionEnded(CastSession session, int error) {
                    Log.d("Cast", "Session ended");
                    updateCastState(false, null);
                }
                
                @Override
                public void onSessionSuspended(CastSession session, int reason) {
                    Log.d("Cast", "Session suspended");
                }
                
                @Override
                public void onSessionStarting(CastSession session) {
                    Log.d("Cast", "Session starting");
                    showCastConnecting();
                }
                
                @Override
                public void onSessionStartFailed(CastSession session, int error) {
                    Log.d("Cast", "Session start failed");
                    Toast.makeText(MovieDetailActivity.this, "Failed to connect to cast device", Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onSessionEnding(CastSession session) {
                    Log.d("Cast", "Session ending");
                }
                
                @Override
                public void onSessionResumeFailed(CastSession session, int error) {
                    Log.d("Cast", "Session resume failed");
                }
                
                @Override
                public void onSessionResuming(CastSession session, String sessionId) {}
            }, CastSession.class);
            
            // Check if already casting
            CastSession currentSession = castContext.getSessionManager().getCurrentCastSession();
            if (currentSession != null && currentSession.isConnected()) {
                updateCastState(true, currentSession);
            }
        } catch (Exception e) {
            Log.e("Cast", "Error initializing cast: " + e.getMessage());
        }
    }
    
    private void updateCastState(boolean isCasting, CastSession session) {
        runOnUiThread(() -> {
            if (isCasting) {
                // UniversalCastButton handles its own icon updates
                // binding.btnCast.setImageResource(R.drawable.ic_cast_connected);
                // binding.btnCast.setColorFilter(getResources().getColor(R.color.app_color));
                
                // Show mini controller
                View miniController = findViewById(R.id.cast_mini_controller);
                if (miniController != null) {
                    miniController.setVisibility(View.VISIBLE);
                    
                    // Update mini controller UI
                    if (contentItem != null) {
                        ImageView thumbnail = miniController.findViewById(R.id.cast_thumbnail);
                        TextView title = miniController.findViewById(R.id.cast_title);
                        TextView status = miniController.findViewById(R.id.cast_status);
                        ImageView playPause = miniController.findViewById(R.id.cast_play_pause);
                        ImageView stop = miniController.findViewById(R.id.cast_stop);
                        ProgressBar progress = miniController.findViewById(R.id.cast_progress);
                        
                        // Load thumbnail
                        if (thumbnail != null && contentItem.getHorizontalPoster() != null) {
                            Glide.with(this)
                                .load(Const.IMAGE_URL + contentItem.getHorizontalPoster())
                                .into(thumbnail);
                        }
                        
                        // Set title
                        if (title != null) {
                            title.setText(contentItem.getTitle());
                        }
                        
                        // Set status
                        if (status != null && session != null) {
                            String deviceName = session.getCastDevice() != null ? 
                                session.getCastDevice().getFriendlyName() : "Cast Device";
                            status.setText("Casting to " + deviceName);
                        }
                        
                        // Set up play/pause button
                        if (playPause != null && session != null) {
                            RemoteMediaClient remoteMediaClient = session.getRemoteMediaClient();
                            if (remoteMediaClient != null) {
                                // Update play/pause icon based on player state
                                remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
                                    @Override
                                    public void onStatusUpdated() {
                                        int playerState = remoteMediaClient.getPlayerState();
                                        if (playerState == MediaStatus.PLAYER_STATE_PLAYING) {
                                            playPause.setImageResource(R.drawable.ic_pause);
                                        } else {
                                            playPause.setImageResource(R.drawable.ic_play);
                                        }
                                        
                                        // Update progress
                                        if (progress != null) {
                                            long position = remoteMediaClient.getApproximateStreamPosition();
                                            MediaInfo mediaInfo = remoteMediaClient.getMediaInfo();
                                            if (mediaInfo != null) {
                                                long duration = mediaInfo.getStreamDuration();
                                                if (duration > 0) {
                                                    progress.setMax((int) duration);
                                                    progress.setProgress((int) position);
                                                }
                                            }
                                        }
                                    }
                                });
                                
                                playPause.setOnClickListener(v -> {
                                    if (remoteMediaClient.isPlaying()) {
                                        remoteMediaClient.pause();
                                    } else {
                                        remoteMediaClient.play();
                                    }
                                });
                            }
                        }
                        
                        // Set up stop button
                        if (stop != null) {
                            stop.setOnClickListener(v -> {
                                CastContext.getSharedInstance(this).getSessionManager().endCurrentSession(true);
                            });
                        }
                    }
                }
                
                // Show "Now casting" toast
                Toast.makeText(this, "Connected to cast device", Toast.LENGTH_SHORT).show();
            } else {
                // UniversalCastButton handles its own icon updates
                // binding.btnCast.setImageResource(R.drawable.ic_cast);
                // binding.btnCast.clearColorFilter();
                
                // Hide mini controller
                View miniController = findViewById(R.id.cast_mini_controller);
                if (miniController != null) {
                    miniController.setVisibility(View.GONE);
                }
            }
        });
    }
    
    private void showCastConnecting() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Connecting to cast device...", Toast.LENGTH_SHORT).show();
        });
    }
    
    private boolean checkAgeRestrictions() {
        if (contentItem == null) {
            return true;
        }
        
        try {
            // Get current profile from user
            if (sessionManager.getUser() == null || sessionManager.getUser().getLastActiveProfileId() == null) {
                return true; // No profile restriction if no user/profile
            }
            
            // For now, we'll implement a basic check
            // In a full implementation, you would need to fetch the profile details
            // including age and isKidsProfile from the API
            Profile currentProfile = getCurrentProfile();
            if (currentProfile != null && !contentItem.isAppropriateFor(currentProfile)) {
                showAgeRestrictionDialog();
                return false;
            }
            
            return true;
        } catch (Exception e) {
            // If there's any error checking age restrictions, allow access
            Log.e("AgeRestriction", "Error checking age restrictions: " + e.getMessage());
            return true;
        }
    }
    
    private Profile getCurrentProfile() {
        // This is a simplified version - in a full implementation,
        // you would fetch the current profile from the API or local storage
        // For now, we'll return null to allow all content
        // TODO: Implement proper profile fetching
        return null;
    }
    
    private void showAgeRestrictionDialog() {
        String message;
        String ageRatingCode = contentItem.getAgeRatingCode();
        
        if (!"NR".equals(ageRatingCode)) {
            message = "This content is rated " + ageRatingCode + " and may not be appropriate for all ages. " +
                    "Please check your profile age settings.";
        } else {
            message = "This content may not be appropriate for all ages. " +
                    "Please check your profile age settings.";
        }
        
        new android.app.AlertDialog.Builder(this)
                .setTitle("Age Restriction")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNegativeButton("Age Settings", (dialog, which) -> {
                    // Navigate to age settings - for now just show a message
                    Toast.makeText(this, "Age settings feature coming soon", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
    
    private void showRatingDialog() {
        RatingDialog dialog = new RatingDialog(this, contentItem.getTitle());
        dialog.setOnRatingSubmitListener(rating -> {
            submitRating(rating);
        });
        dialog.show();
    }
    
    private void submitRating(int rating) {
        // Check if user is logged in
        if (sessionManager.getUser() == null) {
            Toast.makeText(this, "Please login to rate content", Toast.LENGTH_SHORT).show();
            return;
        }
        
        binding.loutLoader.setVisibility(View.VISIBLE);
        
        HashMap<String, Object> params = new HashMap<>();
        params.put("app_user_id", sessionManager.getUser().getId());
        params.put("content_id", contentId);
        params.put("rating", rating);
        
        // Add profile_id if available
        if (sessionManager.getUser().getLastActiveProfileId() != null) {
            params.put("profile_id", sessionManager.getUser().getLastActiveProfileId());
        }
        
        disposable.add(RetrofitClient.getService()
                .rateContent(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    binding.loutLoader.setVisibility(View.GONE);
                    if (response != null && response.getStatus()) {
                        Toast.makeText(this, "Rating submitted successfully", Toast.LENGTH_SHORT).show();
                        // Refresh content details to get updated ratings
                        getContentDetail();
                    } else {
                        Toast.makeText(this, response != null ? response.getMessage() : "Failed to submit rating", Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    binding.loutLoader.setVisibility(View.GONE);
                    Toast.makeText(this, "Error submitting rating", Toast.LENGTH_SHORT).show();
                    Log.e("Rating", "Error submitting rating", throwable);
                }));
    }
    
    private void showEpisodeRatingDialog(ContentDetail.SeasonItem.EpisodesItem episode) {
        RatingDialog dialog = new RatingDialog(this, episode.getTitle());
        dialog.setOnRatingSubmitListener(rating -> {
            submitEpisodeRating(episode, rating);
        });
        dialog.show();
    }
    
    private void submitEpisodeRating(ContentDetail.SeasonItem.EpisodesItem episode, int rating) {
        // Check if user is logged in
        if (sessionManager.getUser() == null) {
            Toast.makeText(this, "Please login to rate episode", Toast.LENGTH_SHORT).show();
            return;
        }
        
        binding.loutLoader.setVisibility(View.VISIBLE);
        
        HashMap<String, Object> params = new HashMap<>();
        params.put("app_user_id", sessionManager.getUser().getId());
        params.put("episode_id", episode.getId());
        params.put("rating", rating);
        
        // Add profile_id if available
        if (sessionManager.getUser().getLastActiveProfileId() != null) {
            params.put("profile_id", sessionManager.getUser().getLastActiveProfileId());
        }
        
        disposable.add(RetrofitClient.getService()
                .rateEpisode(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    binding.loutLoader.setVisibility(View.GONE);
                    if (response != null && response.getStatus()) {
                        Toast.makeText(this, "Episode rating submitted successfully", Toast.LENGTH_SHORT).show();
                        // Refresh content details to get updated ratings
                        getContentDetail();
                    } else {
                        Toast.makeText(this, response != null ? response.getMessage() : "Failed to submit rating", Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    binding.loutLoader.setVisibility(View.GONE);
                    Toast.makeText(this, "Error submitting episode rating", Toast.LENGTH_SHORT).show();
                    Log.e("Rating", "Error submitting episode rating", throwable);
                }));
    }
    
    private void setupTrailerPlayer() {
        if (contentItem == null) {
            Log.d("Trailer", "Content item is null, showing poster");
            showPosterOnly();
            return;
        }
        
        String trailerUrl = TrailerUtils.getEffectiveTrailerUrl(contentItem);
        Log.d("Trailer", "Effective Trailer URL: " + trailerUrl);
        
        if (trailerUrl == null || trailerUrl.isEmpty() || trailerUrl.equals("null")) {
            Log.d("Trailer", "No trailer URL available, showing poster");
            showPosterOnly();
            return;
        }
        
        Log.d("Trailer", "Setting up trailer player for URL: " + trailerUrl);
        
        // Hide poster when we have a trailer
        binding.imgPosterBackground.setVisibility(View.GONE);
        
        // Check if it's a YouTube URL
        if (isYouTubeUrl(trailerUrl)) {
            Log.d("Trailer", "Detected YouTube URL, using WebView");
            setupYouTubePlayer(trailerUrl);
        } else {
            Log.d("Trailer", "Detected CDN URL, using VideoView");
            setupCdnVideoPlayer(trailerUrl);
        }
    }
    
    private void showPosterOnly() {
        binding.videoTrailer.setVisibility(View.GONE);
        binding.webviewYoutubeTrailer.setVisibility(View.GONE);
        binding.btnPlayPauseTrailer.setVisibility(View.GONE);
        binding.imgPosterBackground.setVisibility(View.VISIBLE);
    }
    
    private boolean isYouTubeUrl(String url) {
        return url != null && (
            url.contains("youtube.com") || 
            url.contains("youtu.be") ||
            url.contains("youtube-nocookie.com")
        );
    }
    
    private String extractYouTubeVideoId(String url) {
        String videoId = null;
        
        // Handle different YouTube URL formats
        if (url.contains("youtube.com/watch?v=")) {
            // Standard YouTube URL
            int start = url.indexOf("v=") + 2;
            int end = url.indexOf("&", start);
            if (end == -1) end = url.length();
            videoId = url.substring(start, end);
        } else if (url.contains("youtu.be/")) {
            // Shortened YouTube URL
            int start = url.lastIndexOf("/") + 1;
            int end = url.indexOf("?", start);
            if (end == -1) end = url.length();
            videoId = url.substring(start, end);
        } else if (url.contains("youtube.com/embed/")) {
            // Embedded YouTube URL
            int start = url.indexOf("embed/") + 6;
            int end = url.indexOf("?", start);
            if (end == -1) end = url.length();
            videoId = url.substring(start, end);
        }
        
        return videoId;
    }
    
    private void setupYouTubePlayer(String youtubeUrl) {
        binding.videoTrailer.setVisibility(View.GONE);
        binding.btnPlayPauseTrailer.setVisibility(View.GONE);
        binding.webviewYoutubeTrailer.setVisibility(View.VISIBLE);
        
        // Extract video ID
        String videoId = extractYouTubeVideoId(youtubeUrl);
        if (videoId == null) {
            Log.e("Trailer", "Could not extract YouTube video ID from: " + youtubeUrl);
            // Fall back to poster
            binding.webviewYoutubeTrailer.setVisibility(View.GONE);
            binding.imgPosterBackground.setVisibility(View.VISIBLE);
            return;
        }
        
        // Configure WebView
        WebSettings webSettings = binding.webviewYoutubeTrailer.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        
        // Set WebView clients
        binding.webviewYoutubeTrailer.setWebChromeClient(new WebChromeClient());
        binding.webviewYoutubeTrailer.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        
        // Build YouTube embed HTML
        String embedHtml = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">" +
            "<style>" +
            "body { margin: 0; padding: 0; background: black; }" +
            "iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<iframe " +
            "src=\"https://www.youtube.com/embed/" + videoId + "?autoplay=0&loop=1&playlist=" + videoId + "&rel=0&showinfo=0&modestbranding=1\" " +
            "frameborder=\"0\" " +
            "allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" " +
            "allowfullscreen>" +
            "</iframe>" +
            "</body>" +
            "</html>";
        
        // Load the YouTube player
        binding.webviewYoutubeTrailer.loadData(embedHtml, "text/html", "UTF-8");
    }
    
    private void setupCdnVideoPlayer(String trailerUrl) {
        Log.d("Trailer", "Setting up CDN video player with URL: " + trailerUrl);
        
        // Build the full URL if it's not already absolute
        String fullTrailerUrl = trailerUrl;
        if (!trailerUrl.startsWith("http://") && !trailerUrl.startsWith("https://")) {
            fullTrailerUrl = Const.IMAGE_URL + trailerUrl;
            Log.d("Trailer", "Built full URL: " + fullTrailerUrl);
        }
        
        // Validate URL before attempting to play
        try {
            Uri.parse(fullTrailerUrl);
        } catch (Exception e) {
            Log.e("Trailer", "Invalid URL format: " + fullTrailerUrl, e);
            showPosterOnly();
            return;
        }
        
        binding.videoTrailer.setVisibility(View.VISIBLE);
        binding.btnPlayPauseTrailer.setVisibility(View.VISIBLE);
        binding.webviewYoutubeTrailer.setVisibility(View.GONE);
        
        // Set up the video view
        binding.videoTrailer.setVideoURI(Uri.parse(fullTrailerUrl));
        
        // Handle video prepared
        binding.videoTrailer.setOnPreparedListener(mp -> {
            Log.d("Trailer", "Video prepared successfully");
            // Video is ready but don't start playing automatically
            mp.setLooping(true); // Loop the trailer
        });
        
        // Handle video errors - silently fall back to poster
        final String finalFullTrailerUrl = fullTrailerUrl;
        binding.videoTrailer.setOnErrorListener((mp, what, extra) -> {
            Log.e("Trailer", "Error playing trailer - what: " + what + ", extra: " + extra + ", URL: " + finalFullTrailerUrl);
            // Silently fall back to poster without showing error message
            showPosterOnly();
            return true;
        });
        
        // Set up play/pause button
        binding.btnPlayPauseTrailer.setOnClickListener(v -> {
            if (binding.videoTrailer.isPlaying()) {
                binding.videoTrailer.pause();
                binding.btnPlayPauseTrailer.setImageResource(R.drawable.ic_play);
            } else {
                binding.videoTrailer.start();
                binding.btnPlayPauseTrailer.setImageResource(R.drawable.ic_pause);
                
                // Hide the play button after 3 seconds when playing
                new Handler().postDelayed(() -> {
                    if (binding.videoTrailer.isPlaying()) {
                        binding.btnPlayPauseTrailer.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction(() -> binding.btnPlayPauseTrailer.setVisibility(View.GONE));
                    }
                }, 3000);
            }
        });
        
        // Show play button again when video is tapped
        binding.videoTrailer.setOnClickListener(v -> {
            if (binding.videoTrailer.isPlaying()) {
                binding.btnPlayPauseTrailer.setVisibility(View.VISIBLE);
                binding.btnPlayPauseTrailer.setAlpha(0f);
                binding.btnPlayPauseTrailer.animate()
                    .alpha(1f)
                    .setDuration(300);
                
                // Hide again after 3 seconds
                new Handler().postDelayed(() -> {
                    if (binding.videoTrailer.isPlaying()) {
                        binding.btnPlayPauseTrailer.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction(() -> binding.btnPlayPauseTrailer.setVisibility(View.GONE));
                    }
                }, 3000);
            }
        });
    }


}

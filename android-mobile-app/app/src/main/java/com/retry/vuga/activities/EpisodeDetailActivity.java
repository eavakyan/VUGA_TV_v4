package com.retry.vuga.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.adapters.MoreEpisodesAdapter;
import com.retry.vuga.databinding.ActivityEpisodeDetailBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.EpisodeWatchlistResponse;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.dialogs.RatingDialog;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.OnSwipeTouchListeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class EpisodeDetailActivity extends BaseActivity {
    
    private ActivityEpisodeDetailBinding binding;
    private ContentDetail.SeasonItem.EpisodesItem episodeItem;
    private ContentDetail.DataItem contentDetails;
    private int contentId;
    private int seasonId;
    private int episodeNumber;
    private String contentTitle;
    private String contentThumbnail;
    private List<ContentDetail.SubtitlesItem> subTitlesList = new ArrayList<>();
    private MoreEpisodesAdapter moreEpisodesAdapter;
    private CompositeDisposable disposable = new CompositeDisposable();
    private boolean isInWatchlist = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_episode_detail);
        
        initialization();
        initListeners();
        loadMoreEpisodes();
        checkEpisodeWatchlistStatus();
    }
    
    private void initialization() {
        // Get episode data from intent
        String episodeJson = getIntent().getStringExtra("EPISODE_DATA");
        if (episodeJson != null) {
            episodeItem = new Gson().fromJson(episodeJson, ContentDetail.SeasonItem.EpisodesItem.class);
            binding.setEpisode(episodeItem);
        }
        
        // Get additional data
        contentId = getIntent().getIntExtra(Const.DataKey.CONTENT_ID, 0);
        seasonId = getIntent().getIntExtra("SEASON_ID", 0);
        episodeNumber = getIntent().getIntExtra("EPISODE_NUMBER", 0);
        contentTitle = getIntent().getStringExtra(Const.DataKey.CONTENT_NAME);
        contentThumbnail = getIntent().getStringExtra(Const.DataKey.THUMBNAIL);
        
        // Get content details if passed
        String contentJson = getIntent().getStringExtra("CONTENT_DATA");
        if (contentJson != null) {
            contentDetails = new Gson().fromJson(contentJson, ContentDetail.DataItem.class);
        }
        
        // Get subtitles if available
        String subtitlesJson = getIntent().getStringExtra(Const.DataKey.SUB_TITLES);
        if (subtitlesJson != null) {
            ContentDetail.SubtitlesItem[] subtitlesArray = new Gson().fromJson(subtitlesJson, ContentDetail.SubtitlesItem[].class);
            if (subtitlesArray != null) {
                for (ContentDetail.SubtitlesItem subtitle : subtitlesArray) {
                    subTitlesList.add(subtitle);
                }
            }
        }
        
        // Setup UI elements
        setupUI();
        
        // Check if episode has download option
        if (episodeItem != null && episodeItem.getSources() != null) {
            boolean hasDownloadableSource = false;
            for (ContentDetail.SourceItem source : episodeItem.getSources()) {
                if (source.getIs_download() == 1) {
                    hasDownloadableSource = true;
                    break;
                }
            }
            binding.btnDownload.setVisibility(hasDownloadableSource ? View.VISIBLE : View.GONE);
        }
        
        // Setup RecyclerView for more episodes
        moreEpisodesAdapter = new MoreEpisodesAdapter();
        binding.rvMoreEpisodes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvMoreEpisodes.setAdapter(moreEpisodesAdapter);
    }
    
    private void setupUI() {
        // Set show name
        if (contentTitle != null) {
            binding.tvShowName.setText(contentTitle);
        }
        
        // Set episode title with S#E# format
        if (episodeItem != null) {
            String fullTitle = "";
            if (seasonId > 0 && episodeNumber > 0) {
                fullTitle = "S" + seasonId + "E" + episodeNumber + ": ";
            } else if (episodeItem.getNumber() > 0) {
                fullTitle = "Episode " + episodeItem.getNumber() + ": ";
            }
            fullTitle += episodeItem.getTitle();
            binding.tvEpisodeTitle.setText(fullTitle);
            
            // Set episode source title if available
            if (episodeItem.getSources() != null && !episodeItem.getSources().isEmpty()) {
                ContentDetail.SourceItem firstSource = episodeItem.getSources().get(0);
                if (firstSource.getTitle() != null && !firstSource.getTitle().isEmpty()) {
                    binding.tvShowName.setText(firstSource.getTitle());
                }
            }
            
            // Set episode duration
            String formattedDuration = episodeItem.getFormattedDuration();
            if (formattedDuration != null && !formattedDuration.isEmpty()) {
                binding.tvDuration.setText(formattedDuration);
                binding.tvDuration.setVisibility(View.VISIBLE);
            } else {
                // Try to get duration from raw value
                String duration = episodeItem.getDuration();
                if (duration != null && !duration.isEmpty()) {
                    try {
                        // Duration is already in minutes from database
                        int totalMinutes = Integer.parseInt(duration);
                        
                        if (totalMinutes < 60) {
                            binding.tvDuration.setText(totalMinutes + " min");
                        } else {
                            int hours = totalMinutes / 60;
                            int minutes = totalMinutes % 60;
                            if (minutes == 0) {
                                binding.tvDuration.setText(hours + " hr");
                            } else {
                                binding.tvDuration.setText(hours + " hr " + minutes + " min");
                            }
                        }
                        binding.tvDuration.setVisibility(View.VISIBLE);
                    } catch (NumberFormatException e) {
                        binding.tvDuration.setVisibility(View.GONE);
                    }
                } else {
                    binding.tvDuration.setVisibility(View.GONE);
                }
            }
            
            // Hide release date for episodes (not available in model)
            binding.tvReleaseDate.setVisibility(View.GONE);
        }
    }
    
    private void initListeners() {
        // Back button with swipe down support
        binding.btnBack.setOnClickListener(v -> onBackPressed());
        
        // Enable swipe down to close - only on the video/thumbnail area
        View.OnTouchListener swipeListener = new OnSwipeTouchListeners(this) {
            @Override
            public void onSwipeDown() {
                Log.d("EpisodeDetail", "Swipe down detected - closing activity");
                finish();
                overridePendingTransition(0, android.R.anim.fade_out);
            }
        };
        
        };
        
        // Find the parent container of the video/thumbnail area (first 300dp)
        // Since the parent RelativeLayout doesn't have an ID, we need to get it through its children
        if (binding.imgEpisodeThumbnail.getParent() instanceof View) {
            View videoContainer = (View) binding.imgEpisodeThumbnail.getParent();
            videoContainer.setOnTouchListener(swipeListener);
        }
        
        // Also set on the individual elements as fallback
        binding.imgEpisodeThumbnail.setOnTouchListener(swipeListener);
        binding.btnPlayEpisode.setOnTouchListener(swipeListener);
        
        // Play button click (bottom bar)
        binding.btnPlay.setOnClickListener(v -> playEpisode());
        
        // Play button overlay click - handle both click and swipe
        binding.btnPlayEpisode.setOnClickListener(v -> playEpisode());
        
        // Override the swipe listener for play button to handle both click and swipe
        binding.btnPlayEpisode.setOnTouchListener(new OnSwipeTouchListeners(this) {
            @Override
            public void onSwipeDown() {
                finish();
                overridePendingTransition(0, android.R.anim.fade_out);
            }
            
            @Override
            public void onSingleTouch() {
                playEpisode();
            }
        });
        
        // Download button click
        binding.btnDownload.setOnClickListener(v -> {
            if (episodeItem != null && episodeItem.getSources() != null && !episodeItem.getSources().isEmpty()) {
                Toast.makeText(this, "Download feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Rate button
        binding.btnRate.setOnClickListener(v -> showRatingDialog());
        
        // Watchlist button
        binding.btnWatchlist.setOnClickListener(v -> toggleWatchlist());
        
        // Share button
        binding.btnShare.setOnClickListener(v -> shareEpisode());
        
        // Cast button
        binding.btnCast.setOnClickListener(v -> {
            Toast.makeText(this, "Cast feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        // AirPlay button (hide on Android, iOS only feature)
        binding.btnAirplay.setVisibility(View.GONE);
        
        // Handle more episodes item click
        moreEpisodesAdapter.setOnItemClickListener((episode, position) -> {
            // Navigate to the clicked episode
            Intent intent = new Intent(this, EpisodeDetailActivity.class);
            intent.putExtra("EPISODE_DATA", new Gson().toJson(episode));
            intent.putExtra(Const.DataKey.CONTENT_ID, contentId);
            intent.putExtra(Const.DataKey.CONTENT_NAME, contentTitle);
            intent.putExtra(Const.DataKey.THUMBNAIL, contentThumbnail);
            intent.putExtra("SEASON_ID", seasonId);
            intent.putExtra("EPISODE_NUMBER", episode.getNumber());
            
            if (subTitlesList != null && !subTitlesList.isEmpty()) {
                intent.putExtra(Const.DataKey.SUB_TITLES, new Gson().toJson(subTitlesList));
            }
            
            if (contentDetails != null) {
                intent.putExtra("CONTENT_DATA", new Gson().toJson(contentDetails));
            }
            
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }
    
    private void showRatingDialog() {
        RatingDialog dialog = new RatingDialog(this, episodeItem.getTitle());
        dialog.setOnRatingSubmitListener(rating -> {
            submitEpisodeRating(rating);
        });
        dialog.show();
    }
    
    private void submitEpisodeRating(int rating) {
        UserRegistration.Data user = sessionManager.getUser();
        if (user == null) {
            Toast.makeText(this, "Please login to rate episode", Toast.LENGTH_SHORT).show();
            return;
        }
        
        HashMap<String, Object> params = new HashMap<>();
        params.put("app_user_id", user.getId());
        params.put("episode_id", episodeItem.getId());
        params.put("rating", rating);
        
        if (user.getLastActiveProfileId() != null) {
            params.put("profile_id", user.getLastActiveProfileId());
        }
        
        disposable.add(RetrofitClient.getService()
                .rateEpisode(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response != null && response.getStatus()) {
                        Toast.makeText(this, "Rating submitted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to submit rating", Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    Toast.makeText(this, "Error submitting rating", Toast.LENGTH_SHORT).show();
                    Log.e("EpisodeDetail", "Error submitting rating", throwable);
                }));
    }
    
    private void checkEpisodeWatchlistStatus() {
        if (episodeItem == null) return;
        
        UserRegistration.Data user = sessionManager.getUser();
        if (user == null) return;
        
        HashMap<String, Object> params = new HashMap<>();
        params.put("app_user_id", user.getId());
        params.put("episode_id", episodeItem.getId());
        
        if (user.getLastActiveProfileId() != null) {
            params.put("profile_id", user.getLastActiveProfileId());
        }
        
        disposable.add(RetrofitClient.getService()
                .checkEpisodeWatchlist(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response != null && response.getStatus()) {
                        // Check the actual watchlist status
                        isInWatchlist = response.isInWatchlist();
                        updateWatchlistIcon();
                    }
                }, throwable -> {
                    Log.e("EpisodeDetail", "Error checking watchlist status", throwable);
                }));
    }
    
    private void toggleWatchlist() {
        UserRegistration.Data user = sessionManager.getUser();
        if (user == null) {
            Toast.makeText(this, "Please login to add to watchlist", Toast.LENGTH_SHORT).show();
            return;
        }
        
        HashMap<String, Object> params = new HashMap<>();
        params.put("app_user_id", user.getId());
        params.put("episode_id", episodeItem.getId());
        
        if (user.getLastActiveProfileId() != null) {
            params.put("profile_id", user.getLastActiveProfileId());
        }
        
        // Optimistically update UI
        isInWatchlist = !isInWatchlist;
        updateWatchlistIcon();
        
        disposable.add(RetrofitClient.getService()
                .toggleEpisodeWatchlist(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response != null && response.getStatus()) {
                        Toast.makeText(this, isInWatchlist ? "Added to watchlist" : "Removed from watchlist", Toast.LENGTH_SHORT).show();
                    } else {
                        // Revert on failure
                        isInWatchlist = !isInWatchlist;
                        updateWatchlistIcon();
                        Toast.makeText(this, "Failed to update watchlist", Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    // Revert on error
                    isInWatchlist = !isInWatchlist;
                    updateWatchlistIcon();
                    Toast.makeText(this, "Error updating watchlist", Toast.LENGTH_SHORT).show();
                    Log.e("EpisodeDetail", "Error toggling watchlist", throwable);
                }));
    }
    
    private void updateWatchlistIcon() {
        binding.ivWatchlist.setImageResource(isInWatchlist ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_not);
    }
    
    private void shareEpisode() {
        String shareText = "Check out this episode: " + episodeItem.getTitle();
        if (contentTitle != null) {
            shareText = "Check out " + episodeItem.getTitle() + " from " + contentTitle;
        }
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Episode"));
    }
    
    private void loadMoreEpisodes() {
        if (contentDetails == null || contentDetails.getSeasons() == null) {
            // Try to get episodes from the current season
            binding.tvMoreEpisodesTitle.setVisibility(View.GONE);
            binding.rvMoreEpisodes.setVisibility(View.GONE);
            return;
        }
        
        // Find current season's episodes
        List<ContentDetail.SeasonItem.EpisodesItem> moreEpisodes = new ArrayList<>();
        List<ContentDetail.SeasonItem.EpisodesItem> unseenEpisodes = new ArrayList<>();
        
        for (ContentDetail.SeasonItem season : contentDetails.getSeasons()) {
            if (season.getId() == seasonId || (seasonId == 0 && season.getEpisodes() != null)) {
                // Add all episodes from the same season
                for (ContentDetail.SeasonItem.EpisodesItem episode : season.getEpisodes()) {
                    if (episode.getId() != episodeItem.getId()) {
                        moreEpisodes.add(episode);
                        
                        // Check if episode has been watched (you can enhance this with actual watch history)
                        // For now, we'll show episodes that come after the current episode number as "unseen"
                        if (episode.getNumber() > episodeItem.getNumber()) {
                            unseenEpisodes.add(episode);
                        }
                    }
                }
                break;
            }
        }
        
        // Prioritize showing unseen episodes, otherwise show all other episodes from the season
        List<ContentDetail.SeasonItem.EpisodesItem> episodesToShow = unseenEpisodes.isEmpty() ? moreEpisodes : unseenEpisodes;
        
        if (episodesToShow.isEmpty()) {
            binding.tvMoreEpisodesTitle.setVisibility(View.GONE);
            binding.rvMoreEpisodes.setVisibility(View.GONE);
        } else {
            // Limit to show max 10 episodes in the horizontal list
            if (episodesToShow.size() > 10) {
                episodesToShow = episodesToShow.subList(0, 10);
            }
            
            moreEpisodesAdapter.setEpisodes(episodesToShow);
            binding.tvMoreEpisodesTitle.setText(unseenEpisodes.isEmpty() ? "More Episodes" : "Episodes You Haven't Seen");
            binding.tvMoreEpisodesTitle.setVisibility(View.VISIBLE);
            binding.rvMoreEpisodes.setVisibility(View.VISIBLE);
        }
    }
    
    private void playEpisode() {
        if (episodeItem == null || episodeItem.getSources() == null || episodeItem.getSources().isEmpty()) {
            Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get the first source (or you can show source selection if multiple)
        ContentDetail.SourceItem sourceToPlay = episodeItem.getSources().get(0);
        
        // Check access type and play
        if (isNetworkConnected()) {
            if (sourceToPlay.getAccess_type() == 1) {
                // Free content - play directly
                Intent intent = new Intent(EpisodeDetailActivity.this, PlayerNewActivity.class);
                intent.putExtra(Const.DataKey.CONTENT_SOURCE, new Gson().toJson(sourceToPlay));
                intent.putExtra(Const.DataKey.SUB_TITLES, new Gson().toJson(subTitlesList));
                intent.putExtra(Const.DataKey.NAME, episodeItem.getTitle());
                intent.putExtra(Const.DataKey.THUMBNAIL, episodeItem.getThumbnail());
                intent.putExtra(Const.DataKey.CONTENT_NAME, contentTitle);
                intent.putExtra(Const.DataKey.CONTENT_ID, contentId);
                intent.putExtra(Const.DataKey.RELEASE_YEAR, 0);
                intent.putExtra(Const.DataKey.DURATION, episodeItem.getDuration());
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                
            } else if (sourceToPlay.getAccess_type() == 2) {
                // Premium content
                Toast.makeText(this, "Premium subscription required", Toast.LENGTH_SHORT).show();
                
            } else if (sourceToPlay.getAccess_type() == 3) {
                // Ad-supported content
                Toast.makeText(this, "Watch ad to play", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
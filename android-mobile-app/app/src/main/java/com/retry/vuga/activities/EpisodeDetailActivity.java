package com.retry.vuga.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.databinding.ActivityEpisodeDetailBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.utils.Const;

import java.util.ArrayList;
import java.util.List;

public class EpisodeDetailActivity extends BaseActivity {
    
    private ActivityEpisodeDetailBinding binding;
    private ContentDetail.SeasonItem.EpisodesItem episodeItem;
    private int contentId;
    private String contentTitle;
    private String contentThumbnail;
    private List<ContentDetail.SubtitlesItem> subTitlesList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_episode_detail);
        
        initialization();
        initListeners();
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
        contentTitle = getIntent().getStringExtra(Const.DataKey.CONTENT_NAME);
        contentThumbnail = getIntent().getStringExtra(Const.DataKey.THUMBNAIL);
        
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
    }
    
    private void initListeners() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
        
        // Play button click
        binding.btnPlay.setOnClickListener(v -> playEpisode());
        
        // Play button overlay click
        binding.btnPlayEpisode.setOnClickListener(v -> playEpisode());
        
        // Download button click
        binding.btnDownload.setOnClickListener(v -> {
            if (episodeItem != null && episodeItem.getSources() != null && !episodeItem.getSources().isEmpty()) {
                // Handle download - you can implement this similar to movie download
                Toast.makeText(this, "Download feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void playEpisode() {
        if (episodeItem == null || episodeItem.getSources() == null || episodeItem.getSources().isEmpty()) {
            Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get the first source (or you can show source selection if multiple)
        ContentDetail.SourceItem sourceToPlay = episodeItem.getSources().get(0);
        
        // Check play progress from history
        // This would need to be adapted to your actual history tracking system
        // For now, we'll skip history checking for episodes
        
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
                intent.putExtra(Const.DataKey.RELEASE_YEAR, 0); // Episodes don't have release year
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
}
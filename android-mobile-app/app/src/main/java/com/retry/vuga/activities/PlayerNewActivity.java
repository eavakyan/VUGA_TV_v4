package com.retry.vuga.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.retry.vuga.R;
import com.retry.vuga.adapters.SubtitleLanguagesAdapter;
import com.retry.vuga.databinding.ActivityPlayerNewBinding;
import com.retry.vuga.model.AppSetting;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.Downloads;
import com.retry.vuga.model.LiveTv;
import com.retry.vuga.model.LiveTvChannel;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.CustomDialogBuilder;
import com.retry.vuga.utils.OnSwipeTouchListeners;
import com.retry.vuga.utils.SessionManager;
import com.retry.vuga.utils.adds.MyInterstitialAds;
import com.retry.vuga.utils.subtitle.SubtitleDisplay;
import com.retry.vuga.utils.subtitle.SubtitleParser;
import com.retry.vuga.utils.UniversalCastButton;
import com.retry.vuga.utils.UniversalCastManager;
import com.retry.vuga.utils.VideoCacheManager;
import com.retry.vuga.viewmodel.PlayerViewModel;

import org.jetbrains.annotations.NotNull;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerNewActivity extends BaseActivity {
    private static final String TAG = "PlayerNewActivity";
    ActivityPlayerNewBinding binding;
    SubtitleLanguagesAdapter subtitleLanguagesAdapter;
    String trailerUrl;
    String contentSource;
    String subTitles;
    String download;
    String liveTv;
    String videoPath;

    SessionManager sessionManager;
    ContentDetail.SourceItem modelSource;
    LiveTv.CategoryItem.TvChannelItem modelChannel;
    LiveTvChannel liveTvChannel;
    Downloads modelDownload;

    List<ContentDetail.SubtitlesItem> subTitlesList = new ArrayList<>();
    List<String> subtitleLanguageList = new ArrayList<>();


    AudioManager audioManager;
    int subtitlePosition = 0;
    boolean start;
    boolean left;
    boolean right;
    boolean swipe_move;


    int device_width, device_height;
    private int maxGestureLength = 0;
    Runnable showRunnable = () -> setControllerVisibility(View.GONE);
    
    // Runnable to periodically save playback progress
    Runnable progressSaveRunnable = new Runnable() {
        @Override
        public void run() {
            saveCurrentProgress();
            // Schedule next save in 10 seconds
            handler.postDelayed(this, 10000);
        }
    };
    Handler handler = new Handler();

    LibVLC libvlc;
    ExoPlayer simpleExoPlayer;
    boolean isSetProgress = false;

    private PlayerViewModel viewModel;
    private View fullscreenView;
    
    // Universal Cast support
    private UniversalCastButton castButton;
    private UniversalCastManager castManager;
    private UniversalCastManager.CastDevice selectedCastDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_player_new);
        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        initView();
        initListeners();
        download = getIntent().getStringExtra(Const.DataKey.DOWNLOADS);


        if (download == null && sessionManager.getAppSettings().getSettings().getIsCustomAnd() == 1 && sessionManager.getCustomAds() != null && !sessionManager.getCustomAds().getData().isEmpty()) {
            binding.customAdsView.setOnAdsClose(() -> {
                binding.customAdsView.removeAllViews();
                binding.customAdsView.setVisibility(View.GONE);
                initPlayer();
            });
        } else {
            binding.customAdsView.onDetachedFromWindow();
            binding.customAdsView.removeAllViews();
            binding.customAdsView.setVisibility(View.GONE);
            initPlayer();
        }
        binding.setViewModel(viewModel);
    }

    Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            if (playbackState == Player.STATE_BUFFERING) {
                binding.loader.setVisibility(View.VISIBLE);
            } else {
                binding.loader.setVisibility(View.GONE);
            }
            viewModel.isLoading.set(false);
            if (playbackState == Player.STATE_READY && !isSetProgress) {
                if (modelSource != null) {
                    simpleExoPlayer.seekTo((modelSource.playProgress * simpleExoPlayer.getDuration()) / 100);
                } else if (modelDownload != null) {
                    simpleExoPlayer.seekTo((modelDownload.getPlayProgress() * simpleExoPlayer.getDuration()) / 100);
                }
                isSetProgress = true;
            }
            Player.Listener.super.onPlaybackStateChanged(playbackState);
        }
        
        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            if (isPlaying) {
                // Start periodic progress saving when playing
                handler.removeCallbacks(progressSaveRunnable);
                handler.postDelayed(progressSaveRunnable, 10000);
            } else {
                // Stop periodic saves when not playing
                handler.removeCallbacks(progressSaveRunnable);
                // Save current progress when stopping
                saveCurrentProgress();
            }
            Player.Listener.super.onIsPlayingChanged(isPlaying);
        }
    };

    public static String convertMillisecondsToHMS(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        // Format hours, minutes, and seconds into hh:mm:ss
        return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds);
    }


    private int widthScreen = 0;

    private void initPlayer() {


        trailerUrl = getIntent().getStringExtra(Const.DataKey.TRAILER_URL);
        contentSource = getIntent().getStringExtra(Const.DataKey.CONTENT_SOURCE);
        subTitles = getIntent().getStringExtra(Const.DataKey.SUB_TITLES);
        download = getIntent().getStringExtra(Const.DataKey.DOWNLOADS);
        liveTv = getIntent().getStringExtra(Const.DataKey.LIVE_TV_MODEL);


        if (liveTv != null) {
            // Try to parse as new LiveTvChannel model first
            try {
                liveTvChannel = new Gson().fromJson(liveTv, LiveTvChannel.class);
                binding.tvTitle.setText(liveTvChannel.getTitle());
                videoPath = liveTvChannel.getStreamUrl();
            } catch (Exception e) {
                // Fallback to old model if parsing fails
                modelChannel = new Gson().fromJson(liveTv, LiveTv.CategoryItem.TvChannelItem.class);
                binding.tvTitle.setText(modelChannel.getChannelTitle());
                videoPath = modelChannel.getStreamUrl();
            }
            
            if (videoPath != null && !videoPath.isEmpty()) {
                // Always use ExoPlayer which supports both HLS and DASH
                playByExoPlayer();
            }
        }
        if (download != null) {

            modelDownload = new Gson().fromJson(download, Downloads.class);

            binding.tvTitle.setText(modelDownload.getType() == 1 ? modelDownload.getTitle() : modelDownload.getEpisodeName());

            File file = new File(modelDownload.getPath() + "/" + modelDownload.getFileName());
            if (file.exists()) {
                videoPath = file.getAbsolutePath();
                playByExoPlayer();
//                playByVLCPlayer();
            } else
                Toast.makeText(this, getString(R.string.file_does_not_exist), Toast.LENGTH_SHORT).show();


        }

        if (subTitles != null) {

            subTitlesList = new Gson().fromJson(subTitles, new TypeToken<List<ContentDetail.SubtitlesItem>>() {
            }.getType());

            if (!subTitlesList.isEmpty()) {

                for (int i = 0; i < subTitlesList.size(); i++) {

                    int finalI = i;
                    Optional<AppSetting.LanguageItem> obj = sessionManager.getAppSettings().getLanguageItems().stream().filter(languageItem -> languageItem.getId() == subTitlesList.get(finalI).getLanguage_id()).findFirst();
                    if (obj.isPresent()) {
                        AppSetting.LanguageItem languageItem = obj.get();
                        subtitleLanguageList.add(languageItem.getTitle());
                    } else {
                        subtitleLanguageList.add("Unknown");

                    }

                }

            }
            if (!subTitlesList.isEmpty()) {
                binding.btnSubtitle.setVisibility(View.VISIBLE);
            } else {
                binding.btnSubtitle.setVisibility(View.GONE);
            }


        }

        if (contentSource != null) {

            modelSource = new Gson().fromJson(contentSource, ContentDetail.SourceItem.class);
            String title = getIntent().getStringExtra(Const.DataKey.NAME);
            if (title != null) {
                binding.tvTitle.setText(title);
            }

            if (modelSource != null) {

                videoPath = modelSource.getSource();


                switch (modelSource.getType()) {
                    case 1:
                        playYoutube();
                        break;
                    case 2://m3u8
                        playByExoPlayer();
                        break;
                    case 3://mov
                    case 4://mp4
                    case 5://mkv
                    case 6://webm
//                        playByExoPlayer();
                        playByVLCPlayer();
                        break;
                    case 7:
                        if (modelSource.getMediaItem() == null) {
                            if (modelSource.getSource() != null) { // for old data test purpose
                                videoPath = Const.IMAGE_URL + modelSource.getSource();
//                                playByExoPlayer();
                                playByVLCPlayer();
                            }
                            return;
                        }
                        videoPath = Const.IMAGE_URL + modelSource.getMediaItem().getFile();
//                        playByExoPlayer();
                        playByVLCPlayer();
                        break;
                    case 8:
                        playEmbedUrl();
                        break;
                }

            }
        }


        if (trailerUrl != null) {

            playTrailer();

        }
    }

    private void playEmbedUrl() {
        binding.btnBack.setVisibility(View.VISIBLE);
        binding.exoPlayerView.setVisibility(View.GONE);
        binding.webView.setVisibility(View.VISIBLE);
        binding.vclLout.setVisibility(View.GONE);
        binding.rtlLoader.setVisibility(View.GONE);
        binding.tvSubtitle.setVisibility(View.GONE);
        binding.loader.setVisibility(View.GONE);
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setSupportMultipleWindows(true);
        binding.webView.getSettings().setDomStorageEnabled(true);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webView.setWebViewClient(new WebViewClient());
        binding.webView.getSettings().setAllowFileAccess(false);
        binding.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        Pattern pattern = Pattern.compile("/d/([a-zA-Z0-9_-]+)");
        Matcher matcher = pattern.matcher(modelSource.getSource());

        if (matcher.find()) {
            String fileId = matcher.group(1); // Extracted file ID
            System.out.println("File ID: " + fileId);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            String fram = "<html>" +
                    "<head><style>body { margin: 0; padding: 0; }</style></head>" +
                    "<body>" +
                    "<iframe id='videoFrame' width='100%' height='300' " +
                    "src='" + "https://drive.google.com/file/d/" + fileId + "/preview" +
                    "' frameborder='0' allowfullscreen></iframe>" +
                    "<script>" +
                    "function resizeIframe() {" +
                    "    var iframe = document.getElementById('videoFrame');" +
                    "    iframe.style.width = window.innerWidth + 'px';" +
                    "    iframe.style.height = window.innerHeight + 'px';" +
                    "}" +
                    "window.onload = resizeIframe;" +
                    "window.onresize = resizeIframe;" +
                    "</script>" +
                    "</body></html>";
            binding.webView.loadData(fram, "text/html", "utf-8");
        } else {
            binding.webView.loadUrl(modelSource.getSource());
            System.out.println("File ID not found.");
        }

    }

    //https://drive.google.com/file/d/1wULqpQIzMa0f75i5-xrjpOeOV-dk8s3o/view
    String getIdFromUrl() {
        String url = "https://drive.google.com/file/d/1wULqpQIzMa0f75i5-xrjpOeOV-dk8s3o/view";
        return "";
    }

    private void playByExoPlayer() {

        binding.btnBack.setVisibility(View.VISIBLE);
        binding.exoPlayerView.setVisibility(View.VISIBLE);
        binding.vclLout.setVisibility(View.GONE);
        binding.rtlLoader.setVisibility(View.GONE);
        binding.loader.setVisibility(View.VISIBLE);
        // Ensure custom ads view is hidden
        binding.customAdsView.setVisibility(View.GONE);
        binding.customAdsView.removeAllViews();
        ((TextView) binding.exoPlayerView.findViewById(R.id.tv_rewind)).setText(String.valueOf(Const.PLAYER_SEC));
        ((TextView) binding.exoPlayerView.findViewById(R.id.tv_forward)).setText(String.valueOf(Const.PLAYER_SEC));
        binding.exoPlayerView.findViewById(R.id.btn_rewind).setOnClickListener(view -> simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() - Const.PLAYER_SEC * 1000));
        binding.exoPlayerView.findViewById(R.id.btn_forward).setOnClickListener(view -> simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() + Const.PLAYER_SEC * 1000));
        binding.exoPlayerView.findViewById(R.id.exo_pause_btn).setOnClickListener(view -> {
            simpleExoPlayer.pause();
            binding.exoPlayerView.findViewById(R.id.exo_play_btn).setVisibility(View.VISIBLE);
            binding.exoPlayerView.findViewById(R.id.exo_pause_btn).setVisibility(View.GONE);
        });
        binding.exoPlayerView.findViewById(R.id.exo_play_btn).setOnClickListener(view -> {
            simpleExoPlayer.play();
            binding.exoPlayerView.findViewById(R.id.exo_play_btn).setVisibility(View.GONE);
            binding.exoPlayerView.findViewById(R.id.exo_pause_btn).setVisibility(View.VISIBLE);
        });
        // Initialize video cache manager
        VideoCacheManager cacheManager = VideoCacheManager.getInstance(this);
        
        // Create ExoPlayer with adaptive load control based on network
        simpleExoPlayer = new ExoPlayer.Builder(this)
                .setLoadControl(cacheManager.getAdaptiveLoadControl())
                .build();
        binding.exoPlayerView.setPlayer(simpleExoPlayer);
        binding.exoPlayerView.setKeepScreenOn(true);

        // Create media source with caching support
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(videoPath)
                .build();
        
        MediaSource mediaSource;
        String lowerCasePath = videoPath.toLowerCase();
        
        if (lowerCasePath.contains(".m3u8")) {
            // HLS stream with caching
            mediaSource = new HlsMediaSource.Factory(cacheManager.getCacheDataSourceFactory())
                    .createMediaSource(mediaItem);
        } else if (lowerCasePath.contains(".mpd")) {
            // DASH stream with caching
            mediaSource = new DashMediaSource.Factory(
                    new DefaultDashChunkSource.Factory(cacheManager.getCacheDataSourceFactory()),
                    cacheManager.getCacheDataSourceFactory())
                    .createMediaSource(mediaItem);
        } else {
            // Progressive media (MP4, etc.) with caching
            mediaSource = new ProgressiveMediaSource.Factory(cacheManager.getCacheDataSourceFactory())
                    .createMediaSource(mediaItem);
        }

        simpleExoPlayer.setMediaSource(mediaSource);
        simpleExoPlayer.prepare();
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.addListener(playerListener);


    }

    private void playByVLCPlayer() {
        binding.vclLout.setVisibility(View.VISIBLE);
        setControllerVisibility(View.VISIBLE);
        // Ensure custom ads view is hidden
        binding.customAdsView.setVisibility(View.GONE);
        binding.customAdsView.removeAllViews();

        binding.vclLout.setHovered(true);
        binding.vclLout.setActivated(true);


        final ArrayList<String> args = new ArrayList<>();
//        args.add("-mkv");
        libvlc = new LibVLC(this, args);
        viewModel.mMediaPlayer = new MediaPlayer(libvlc);


        viewModel.mMediaPlayer.attachViews(binding.vclLout, null, false, false);
        viewModel.mMediaPlayer.setVideoScale(MediaPlayer.ScaleType.SURFACE_FILL);
        final Media media = new Media(libvlc, Uri.parse(Uri.decode(videoPath)));

        viewModel.mMediaPlayer.setMedia(media);
        viewModel.mMediaPlayer.setVideoScale(MediaPlayer.ScaleType.SURFACE_BEST_FIT);

        media.release();
        viewModel.mMediaPlayer.play();
        viewModel.onPlay();


//viewModel.initPLayer();
        Log.i("TAG", "playByVLCPlayer: ");

        String lengthString = convertMillisecondsToHMS(viewModel.mMediaPlayer.getLength());
        String currentString = convertMillisecondsToHMS((long) viewModel.mMediaPlayer.getPosition());

        binding.tvDuration.setText(lengthString);
        binding.tvPosition.setText(currentString);
        binding.tvRewind.setText(String.valueOf(Const.PLAYER_SEC));
        binding.tvForward.setText(String.valueOf(Const.PLAYER_SEC));


        binding.exoPlayBtn.setOnClickListener(v -> {
            if (viewModel.isPlay.get()) {
                viewModel.onPause();
            } else {
                viewModel.onPlay();
            }
        });

        binding.btnForward.setOnClickListener(v -> {
            if (viewModel.mMediaPlayer != null) {
                viewModel.removeCallback.setValue(true);
                viewModel.mMediaPlayer.setTime(viewModel.mMediaPlayer.getTime() + (Const.PLAYER_SEC * 1000));
                viewModel.removeCallback.setValue(false);
            }
        });
        binding.btnRewind.setOnClickListener(v -> {
            if (viewModel.mMediaPlayer != null) {
                viewModel.removeCallback.setValue(true);
                viewModel.mMediaPlayer.setTime(viewModel.mMediaPlayer.getTime() - (Const.PLAYER_SEC * 1000));
                viewModel.removeCallback.setValue(false);
            }
        });

        binding.vclLout.setOnHoverListener((v, event) -> {
            Log.i("TAG", "playByVLCPlayer: false ");
            return true;
        });

        binding.fullscreenBtn.setOnClickListener(v -> {
            viewModel.removeCallback.setValue(true);

            if (viewModel.mMediaPlayer.getVideoScale() == MediaPlayer.ScaleType.SURFACE_BEST_FIT) {
                viewModel.mMediaPlayer.setVideoScale(MediaPlayer.ScaleType.SURFACE_FIT_SCREEN);

            } else {
                viewModel.mMediaPlayer.setVideoScale(MediaPlayer.ScaleType.SURFACE_BEST_FIT);

            }
            viewModel.removeCallback.setValue(false);

        });


    }


    @Override
    protected void onPause() {
        super.onPause();
        // Cancel periodic saves when pausing
        handler.removeCallbacks(progressSaveRunnable);
        
        if (simpleExoPlayer != null) {
            simpleExoPlayer.pause();
        } else {
            viewModel.onPause();
        }
        
        // Save progress when pausing
        saveCurrentProgress();
        
        binding.customAdsView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (simpleExoPlayer != null) {
            simpleExoPlayer.play();
        } else {
            viewModel.onPlay();
        }
        binding.customAdsView.onResume();
    }

    private void saveCurrentProgress() {
        int currentProgress = 0;
        
        // Get current progress based on player type
        if (simpleExoPlayer != null && simpleExoPlayer.getDuration() > 0) {
            currentProgress = (int) (simpleExoPlayer.getCurrentPosition() * 100 / simpleExoPlayer.getDuration());
        } else if (viewModel.mMediaPlayer != null && viewModel.mMediaPlayer.getLength() > 0) {
            currentProgress = (int) (viewModel.mMediaPlayer.getTime() * 100 / viewModel.mMediaPlayer.getLength());
        }
        
        // Save progress to local storage
        if (currentProgress > 0) {
            viewModel.progress.set(currentProgress);
            if (modelDownload != null) {
                sessionManager.editDownloads(modelDownload, currentProgress);
            } else if (modelSource != null) {
                modelSource.setContent_id(getIntent().getIntExtra(Const.DataKey.CONTENT_ID, -1));
                Integer releaseYear = getIntent().hasExtra(Const.DataKey.RELEASE_YEAR) ? 
                    getIntent().getIntExtra(Const.DataKey.RELEASE_YEAR, 0) : null;
                String duration = getIntent().getStringExtra(Const.DataKey.DURATION);
                sessionManager.updateMovieHistory(modelSource, currentProgress, 
                    getIntent().getStringExtra(Const.DataKey.CONTENT_NAME), 
                    getIntent().getStringExtra(Const.DataKey.THUMBNAIL),
                    releaseYear,
                    duration);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the periodic progress save
        handler.removeCallbacks(progressSaveRunnable);
        
        // Save final progress
        saveCurrentProgress();
        
        if (simpleExoPlayer != null) {
            simpleExoPlayer.removeListener(playerListener);
            simpleExoPlayer.release();
        } else {
            viewModel.release();
        }
        
        // Clean up cast resources
        if (castManager != null) {
            castManager.cleanup();
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initListeners() {
        MyInterstitialAds myInterstitialAds = new MyInterstitialAds(this);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                myInterstitialAds.showAds();
                finish();
            }
        });
        viewModel.isBack.observe(this, isBack -> {
            if (isBack) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        viewModel.isAvailable.observe(this, isAvailable -> {
            if (isAvailable && !isSetProgress) {
                if (modelSource != null) {
                    viewModel.mMediaPlayer.setTime((modelSource.playProgress * viewModel.mMediaPlayer.getLength()) / 100);
                } else if (modelDownload != null) {
                    viewModel.mMediaPlayer.setTime((modelDownload.getPlayProgress() * viewModel.mMediaPlayer.getLength()) / 100);
                }
                isSetProgress = true;
                
                // Start periodic progress saving for VLC player
                if (viewModel.mMediaPlayer != null && viewModel.mMediaPlayer.isPlaying()) {
                    handler.removeCallbacks(progressSaveRunnable);
                    handler.postDelayed(progressSaveRunnable, 10000);
                }
            }
        });
        viewModel.removeCallback.observe(this, isRemoved -> {
            if (isRemoved) {
                handler.removeCallbacks(showRunnable);
            } else {
                handler.postDelayed(showRunnable, 2000);
            }
        });
        binding.vclLout.setOnTouchListener(new OnSwipeTouchListeners(this) {
            final int streamMaxVolume = audioManager.getStreamMaxVolume(3);

            @Override
            public void onScrollTouch(MotionEvent motionEvent, @NonNull MotionEvent motionEvent2, float f, float f2) {
                float progress;
                int i;
                double d;
                if (motionEvent.getX() > ((float) (PlayerNewActivity.this.widthScreen / 2))) {
                    binding.swipeLout.volPogressContainer.setVisibility(View.VISIBLE);
                    PlayerNewActivity.this.binding.swipeLout.volProgress.incrementProgressBy((int) f2);
                    progress = ((float) PlayerNewActivity.this.binding.swipeLout.volProgress.getProgress()) / ((float) PlayerNewActivity.this.maxGestureLength);
                    int i2 = (int) (((float) streamMaxVolume) * progress);
                    audioManager.setStreamVolume(3, i2, 0);
                    if (i2 != audioManager.getStreamVolume(3)) {
                        audioManager.setStreamVolume(3, i2, AudioManager.FLAG_SHOW_UI);
                    }

                    if (progress <= 0.0f) {
                        i = R.drawable.ic_mute;
                    } else {
                        //                        i = d < 0.25d ? R.drawable.ic_volume_mute_white_72dp : d < 0.75d ? R.drawable.ic_volume_down_white_72dp : R.drawable.ic_volume_up_white_72dp;
                        i = R.drawable.ic_unmute;
                    }
                    PlayerNewActivity.this.binding.swipeLout.volIcon.setImageResource(i);
                } else if (motionEvent.getX() < ((float) (PlayerNewActivity.this.widthScreen / 2))) {
                    binding.swipeLout.brightPogressContainer.setVisibility(View.VISIBLE);
                    binding.swipeLout.brightProgress.incrementProgressBy((int) f2);
                    progress = ((float) binding.swipeLout.brightProgress.getProgress()) / ((float) maxGestureLength);
                    WindowManager.LayoutParams attributes = getWindow().getAttributes();
                    attributes.screenBrightness = progress;
                    getWindow().setAttributes(attributes);

                    d = progress;
                    i = d < 0.25d ? R.drawable.ic_brightness_low : d < 0.75d ? R.drawable.ic_brightness_medium : R.drawable.ic_brightness_full;
                    binding.swipeLout.brightIcon.setImageResource(i);

                }
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {


                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        start = true;
//                        binding.exoController.setVisibility(View.GONE);
                        if (event.getX() < ((float) device_width / 2)) {
                            left = true;
                            right = false;
                        } else if (event.getX() > ((float) device_width / 2)) {
                            left = false;
                            right = true;
                        }

                        break;

                    case MotionEvent.ACTION_MOVE:
                        swipe_move = true;
                        break;

                    case MotionEvent.ACTION_UP:
                        swipe_move = false;
                        start = false;
                        binding.swipeLout.volPogressContainer.setVisibility(View.GONE);
                        binding.swipeLout.brightPogressContainer.setVisibility(View.GONE);

                        break;
                }

                return super.onTouch(v, event);
            }

            @Override
            public void onDoubleTouch() {
                super.onDoubleTouch();
            }


            @Override
            public void onSingleTouch() {
                super.onSingleTouch();
                viewModel.removeCallback.setValue(true);
                if (binding.exoController.getVisibility() == View.GONE) {
                    setControllerVisibility(View.VISIBLE);
                    viewModel.removeCallback.setValue(false);
                } else {
                    setControllerVisibility(View.GONE);
                }
            }

        });
        binding.vclLout.addOnLayoutChangeListener((view, i, i2, i3, i4, i5, i6, i7, i8) -> {
            maxGestureLength = (int) (((float) Math.min(i3 - i, i4 - i2)) * 0.75f);
            binding.swipeLout.volProgress.setMax(maxGestureLength);
            binding.swipeLout.brightProgress.setMax(maxGestureLength);
            binding.swipeLout.volProgress.setProgress((int) (((float) binding.swipeLout.volProgress.getMax()) * (((float) audioManager.getStreamVolume(3)) / ((float) audioManager.getStreamMaxVolume(3)))));
            DisplayMetrics displayMetrics = new DisplayMetrics();

            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            widthScreen = displayMetrics.widthPixels;
        });

        binding.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        binding.btnSubtitle.setOnClickListener(v -> {
            viewModel.onPause();

            new CustomDialogBuilder(this).showSubtitleDialog(subtitlePosition, subtitleLanguageList, position -> {
                subtitlePosition = position;
                long time = viewModel.mMediaPlayer.getTime();
                viewModel.mMediaPlayer.setEventListener(null);
                viewModel.isAdded = false;
                viewModel.isLoading.set(true);
                viewModel.onPause();
                if (position == 0) {
                    final Media media = new Media(libvlc, Uri.parse(Uri.decode(videoPath)));
                    viewModel.mMediaPlayer.setMedia(media);
                    viewModel.mMediaPlayer.setVideoScale(MediaPlayer.ScaleType.SURFACE_BEST_FIT);
                    media.release();
                    viewModel.mMediaPlayer.play();
                    viewModel.onPlay();
                    binding.tvSubtitle.setText("");
                    binding.tvSubtitle.setVisibility(View.GONE);
                    viewModel.setSubtitle(null);
                    viewModel.mMediaPlayer.setTime(time);
                    return;
                }

                final Media media = new Media(libvlc, Uri.parse(Uri.decode(videoPath)));
                viewModel.mMediaPlayer.setMedia(media);
//                media.addSlave(new Media.Slave(Media.Slave.Type.Subtitle, 4, Const.IMAGE_URL + subTitlesList.get(position - 1).getSubtitleFile()));
                media.parseAsync();

                String fileName = URLUtil.guessFileName(Const.IMAGE_URL + subTitlesList.get(position - 1).getSubtitleFile(), null, MimeTypeMap.getFileExtensionFromUrl(Const.IMAGE_URL + subTitlesList.get(position - 1).getSubtitleFile()));
                PRDownloader.download(Const.IMAGE_URL + subTitlesList.get(position - 1).getSubtitleFile(), getExternalFilesDir(null).getAbsolutePath(), fileName)
                        .build()
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                List<SubtitleParser.Subtitle> subtitles = new ArrayList<>();
                                try {
                                    subtitles = SubtitleParser.parseSRT(getExternalFilesDir(null).getAbsolutePath() + "/" + fileName);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                SubtitleDisplay subtitleDisplay = new SubtitleDisplay(binding.tvSubtitle, subtitles, System.currentTimeMillis());
                                subtitleDisplay.updateSubtitle((int) viewModel.mMediaPlayer.getTime());
                                viewModel.setSubtitle(subtitleDisplay);
//                viewModel.mMediaPlayer.setVideoScale(MediaPlayer.ScaleType.SURFACE_BEST_FIT);
                                viewModel.mMediaPlayer.play();

                                viewModel.onPlay();
                                viewModel.mMediaPlayer.setTime(time);
                                media.release();
                            }

                            @Override
                            public void onError(Error error) {

                            }
                        });


                Log.d("TAG", "onItemClick: ");
            });


        });

    }

    private void setControllerVisibility(int visibility) {
        binding.exoController.setVisibility(visibility);
        binding.btnBack.setVisibility(visibility);
        binding.tvTitle.setVisibility(visibility);
    }

    private void initView() {


        noStatusBar();
        sessionManager = new SessionManager(this);
        subtitleLanguagesAdapter = new SubtitleLanguagesAdapter();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        binding.youtubePlayerView.setVisibility(View.GONE);
        binding.exoPlayerView.setVisibility(View.GONE);
        binding.btnBack.setVisibility(View.GONE);
//        binding.btnMute.setVisibility(View.GONE);
        binding.btnSubtitle.setVisibility(View.GONE);
        sessionManager.saveIntValue(Const.DataKey.SUBTITLE_POSITION, 0);
        
        // Initialize Universal Cast functionality
        initializeCastSupport();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        device_width = displayMetrics.widthPixels;
        device_height = displayMetrics.heightPixels;


    }


    private void playYoutube() {

        playByYoutubePlayer();
    }

    private void playByYoutubePlayer() {

        setControllerVisibility(View.GONE);
        binding.vclLout.setVisibility(View.GONE);
        binding.btnSubtitle.setVisibility(View.GONE);
//        binding.imgLogo.setVisibility(View.GONE);
//        binding.btnMute.setVisibility(View.GONE);
//        binding.swipeLout.
        if (videoPath.toLowerCase(Locale.ROOT).contains("https://www.youtube.com/watch?v=")) {
            videoPath = videoPath.replace("https://www.youtube.com/watch?v=", "");

        }
        if (videoPath.toLowerCase(Locale.ROOT).contains("https://youtu.be/")) {
            videoPath = videoPath.replace("https://youtu.be/", "");
        }
        String[] s = videoPath.split("t=");
        videoPath = s[0];
        String[] s1 = videoPath.split("si=");
        videoPath = s1[0];

        videoPath = videoPath.replace("&", "");
        videoPath = videoPath.replace("?", "");
        binding.youtubePlayerView.setVisibility(View.VISIBLE);
        Log.i("TAG", "onReady: " + videoPath);

        binding.youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NotNull YouTubePlayer youTubePlayer) {
                super.onReady(youTubePlayer);
                Log.i("TAG", "onReady: " + videoPath);
                youTubePlayer.loadVideo(videoPath, 0);

            }

            @Override
            public void onError(@NotNull YouTubePlayer youTubePlayer, @NotNull PlayerConstants.PlayerError error) {
                super.onError(youTubePlayer, error);
                Log.i("TAG", "onError: " + error);
                Toast.makeText(PlayerNewActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }

        });


    }

    private void playTrailer() {
        videoPath = trailerUrl;
        playByYoutubePlayer();
    }

    private void initializeCastSupport() {
        castButton = binding.btnCast;
        castManager = castButton.getCastManager();
        
        // DLNA Test Button
        // DLNA test button removed from production
        
        // Set up cast device selection listener
        castButton.setOnCastDeviceSelectedListener(new UniversalCastButton.OnCastDeviceSelectedListener() {
            @Override
            public void onDeviceSelected(UniversalCastManager.CastDevice device) {
                selectedCastDevice = device;
                Log.d(TAG, "Cast device selected: " + device.name + " (" + device.type + ")");
                
                // If we have a video playing, start casting
                if (videoPath != null && !videoPath.isEmpty()) {
                    String title = binding.tvTitle.getText().toString();
                    String subtitle = null;
                    String imageUrl = null;
                    
                    // Get content details for better metadata
                    if (modelSource != null && contentSource != null) {
                        try {
                            ContentDetail.SourceItem source = new Gson().fromJson(contentSource, ContentDetail.SourceItem.class);
                            // Add more metadata if available
                        } catch (Exception e) {
                            Log.w(TAG, "Could not parse content source for metadata", e);
                        }
                    }
                    
                    Toast.makeText(PlayerNewActivity.this, "Casting to " + device.name, Toast.LENGTH_SHORT).show();
                    castManager.castMedia(videoPath, title, subtitle, imageUrl);
                }
            }
            
            @Override
            public void onDeviceDisconnected() {
                selectedCastDevice = null;
                Log.d(TAG, "Cast device disconnected");
                Toast.makeText(PlayerNewActivity.this, "Disconnected from cast device", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void noStatusBar() {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }
}
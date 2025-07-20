package com.retry.vuga.utils.custom_view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.databinding.DataBindingUtil;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemCustomAdsBinding;
import com.retry.vuga.databinding.ItemCustomAdsImageBinding;
import com.retry.vuga.model.ads.CustomAds;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.Global;
import com.retry.vuga.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Jeel Khokhariya
 * on 15/07/22.
 */
public class CustomAdsView extends LinearLayoutCompat {

    Handler handler = new Handler();
    Handler skipHandler = new Handler();
    Handler imageSkipHandler = new Handler();
    long currentVideoSkipMilli = 1000;
    long totalVideoSkipMillisecond = 1000;
    long totalImageSkipMilli = 1000;
    long currentImageSkipMilli = 0;
    ItemCustomAdsBinding itemCustomAdsBinding;
    ItemCustomAdsImageBinding itemCustomAdsImageBinding;
    ExoPlayer exoPlayer;
    private OnAdsClose onAdsClose;

    public CustomAdsView(@NonNull Context context) {
        super(context);
        initAds();
    }

    private void callApiForIncreaseAdView(Long id) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(RetrofitClient.getService().increaseAdMetric(id, "view")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .subscribe((customAds, throwable) -> {
                })
        );
    }

    public void initAds() {
        SessionManager sessionManager = new SessionManager(getContext());
        if (sessionManager.getCustomAds() == null || sessionManager.getCustomAds().getData().isEmpty()) {
            onDetachedFromWindow();
            return;
        }
        CustomAds.DataItem customAds = sessionManager.getCustomAds().getData().get(new Random().nextInt(sessionManager.getCustomAds().getData().size()));
        boolean isVideoAds = new Random().nextBoolean();
        currentVideoSkipMilli = sessionManager.getAppSettings().getSettings().getVideoSkipTime() * 1000L;
        totalVideoSkipMillisecond = sessionManager.getAppSettings().getSettings().getVideoSkipTime() * 1000L;
        List<CustomAds.Sources> videolist = new ArrayList<>();
        List<CustomAds.Sources> imageList = new ArrayList<>();
        int ran = new Random().nextInt(customAds.getSources().size());

        CustomAds.Sources sources = customAds.getSources().get(ran);

        if (sources.getType() == 1) {
            itemCustomAdsBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.item_custom_ads, null, false);
            itemCustomAdsBinding.setModel(customAds);
            itemCustomAdsBinding.setVideoData(sources);
            Glide.with(getContext())
                    .load(Const.IMAGE_URL + customAds.getBrandLogo())
                    .into(itemCustomAdsBinding.icThumb);
            exoPlayer = new ExoPlayer.Builder(getContext()).build();
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(Const.IMAGE_URL + sources.getContent()));
            Log.i("TAG", "initAds: ");
            itemCustomAdsBinding.tvViewMore.setOnClickListener(view -> {
                if (itemCustomAdsBinding.tvDescription.getVisibility() == View.GONE) {
                    itemCustomAdsBinding.tvViewMore.setText(R.string.view_less);
                    itemCustomAdsBinding.tvDescription.setVisibility(View.VISIBLE);
                } else {
                    itemCustomAdsBinding.tvViewMore.setText(R.string.view_more);
                    itemCustomAdsBinding.tvDescription.setVisibility(View.GONE);
                }
            });
            itemCustomAdsBinding.btnOpenLink.setOnClickListener(view -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(customAds.getAndroidLink()));
                getContext().startActivity(i);
                callApiForIncreaseAdClick(customAds.getId());
            });
            itemCustomAdsBinding.exoPlayerView.setPlayer(exoPlayer);
            itemCustomAdsBinding.exoPlayerView.setKeepScreenOn(true);
            handler.postDelayed(runnable, 1000);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    Log.d("TAG", "onPlaybackStateChanged: " + playbackState);
                    if (playbackState == Player.STATE_BUFFERING) {

                        handler.removeCallbacks(videoSkipRunnable);
                    } else if (playbackState == Player.STATE_READY) {
                        handler.postDelayed(videoSkipRunnable, 1000);
                    } else if (playbackState == Player.STATE_ENDED) {
                        itemCustomAdsBinding.tvSkip.performClick();
                    }

                }
            });
            exoPlayer.setPlayWhenReady(true);
            addView(itemCustomAdsBinding.getRoot(), new LinearLayoutCompat.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        } else {

            itemCustomAdsImageBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.item_custom_ads_image, null, false);
            itemCustomAdsImageBinding.setModel(customAds);
            itemCustomAdsImageBinding.setVideoData(sources);
            Glide.with(getContext())
                    .load(Const.IMAGE_URL + sources.getContent())
                    .into(itemCustomAdsImageBinding.ivAdImage);
            Glide.with(getContext())
                    .load(Const.IMAGE_URL + customAds.getBrandLogo())
                    .into(itemCustomAdsImageBinding.icThumb);
            totalImageSkipMilli = sources.getShow_time() * 1000;
            imageSkipHandler.postDelayed(imageSkipRunnable, 1000);
            itemCustomAdsImageBinding.btnLink.setOnClickListener(view -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                callApiForIncreaseAdClick(customAds.getId());
                i.setData(Uri.parse(customAds.getAndroidLink()));
                getContext().startActivity(i);
            });
            addView(itemCustomAdsImageBinding.getRoot(), new LinearLayoutCompat.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        callApiForIncreaseAdView(customAds.getId());
    }    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int progress = Math.toIntExact((exoPlayer.getCurrentPosition() * 100) / exoPlayer.getDuration());
            itemCustomAdsBinding.progressBar.setProgress(progress, true);
            if (exoPlayer.getDuration() > 0)
                itemCustomAdsBinding.tvTime.setText(Global.convertSecondsToHMmSs(exoPlayer.getDuration() - exoPlayer.getCurrentPosition()));
            handler.postDelayed(runnable, 0);
        }
    };

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (itemCustomAdsImageBinding != null) {
            imageSkipHandler.removeCallbacks(imageSkipRunnable);
        }
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
            skipHandler.removeCallbacks(videoSkipRunnable);
            handler.removeCallbacks(runnable);

        }
    }

    private void callApiForIncreaseAdClick(Long id) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(RetrofitClient.getService().increaseAdMetric(id, "click")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .subscribe((customAds, throwable) -> {
                })
        );
    }



    Runnable videoSkipRunnable = new Runnable() {
        @Override
        public void run() {
            currentVideoSkipMilli -= 1000;
            Log.d("TAG", "run: " + exoPlayer.getCurrentPosition());
            if (currentVideoSkipMilli > 0) {
                if (exoPlayer.getPlaybackState() == Player.STATE_BUFFERING) {
                    skipHandler.removeCallbacks(videoSkipRunnable);
                } else {
                    skipHandler.postDelayed(videoSkipRunnable, 1000);
                }
                itemCustomAdsBinding.tvSkip.setText("Ad Skip : " + Global.convertSecondsToHMmSs(currentVideoSkipMilli));
            } else {
                itemCustomAdsBinding.tvSkip.setText("Skip");
                itemCustomAdsBinding.tvSkip.setOnClickListener(view -> {
                    if (onAdsClose != null) {
                        onAdsClose.close();
                        onDetachedFromWindow();
                    } else {
                        Log.i("TAG", "run:onAdsClose null ");
                    }
                });
                skipHandler.removeCallbacks(videoSkipRunnable);
            }

        }
    };

    public void setOnAdsClose(OnAdsClose onAdsClose) {
        this.onAdsClose = onAdsClose;
    }

    Runnable imageSkipRunnable = new Runnable() {
        @Override
        public void run() {
            currentImageSkipMilli += 1000;
            int progress = Math.toIntExact((currentImageSkipMilli * 100) / totalImageSkipMilli);
            itemCustomAdsImageBinding.progressBar.setProgress(progress, true);
            if (progress >= 100) {
                imageSkipHandler.removeCallbacks(imageSkipRunnable);
                new Handler().postDelayed(() -> {
                    if (onAdsClose != null) {
                        onAdsClose.close();
                    } else {

                    }

                }, 1000);
                return;
            }
            imageSkipHandler.postDelayed(imageSkipRunnable, 1000);
        }
    };

    public void onResume() {
        if (itemCustomAdsImageBinding != null) {
            imageSkipHandler.postDelayed(imageSkipRunnable, 0);
        }
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            skipHandler.postDelayed(videoSkipRunnable, 0);
            handler.postDelayed(runnable, 0);
        }
    }

    public CustomAdsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAds();
    }

    public CustomAdsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAds();
    }

    public void onPause() {
        if (itemCustomAdsImageBinding != null) {
            imageSkipHandler.removeCallbacks(imageSkipRunnable);
        }
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            skipHandler.removeCallbacks(videoSkipRunnable);
            handler.removeCallbacks(runnable);
        }
    }


    public interface OnAdsClose {
        void close();
    }


}

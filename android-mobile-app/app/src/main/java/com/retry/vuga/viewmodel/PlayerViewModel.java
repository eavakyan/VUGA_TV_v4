package com.retry.vuga.viewmodel;

import static com.retry.vuga.activities.PlayerNewActivity.convertMillisecondsToHMS;

import android.os.Handler;
import android.widget.SeekBar;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.MutableLiveData;

import com.retry.vuga.utils.subtitle.SubtitleDisplay;

import org.videolan.libvlc.MediaPlayer;


public class PlayerViewModel extends BaseViewModel {
    public ObservableField<String> remainTime = new ObservableField<>();
    public ObservableField<String> totalTime = new ObservableField<>();
    public ObservableInt progress = new ObservableInt();
    public ObservableBoolean isPlay = new ObservableBoolean();
    public ObservableBoolean isLoading = new ObservableBoolean(true);
    public ObservableBoolean isShowController = new ObservableBoolean(true);
    public MutableLiveData<Boolean> isBack = new MutableLiveData<>();
    public MutableLiveData<Boolean> isAvailable = new MutableLiveData<>();
    public MutableLiveData<Boolean> removeCallback = new MutableLiveData<>();
    public MediaPlayer mMediaPlayer;
    public boolean isAdded = false;
    Handler playerHandler = new Handler();
    private SubtitleDisplay subtitleDisplay;

    public void onSeek() {

    }

    public void onPlay() {

        if (mMediaPlayer != null) {
            removeCallback.setValue(false);
            mMediaPlayer.play();
            playerHandler.postDelayed(runnable, 0);
            isPlay.set(true);
            isShowController.set(true);
        }
    }

    public void setSubtitle(SubtitleDisplay subtitleDisplay) {
        this.subtitleDisplay = subtitleDisplay;
    }    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isAdded) {
                isAdded = true;
                mMediaPlayer.setEventListener(event -> {

                    isLoading.set(event.getBuffering() >= 1);

                    if (!isLoading.get() && mMediaPlayer.getLength() > 0 && remainTime.get() != null && totalTime.get() != null) {
                        isBack.setValue(remainTime != null && remainTime.get() != null && totalTime != null && totalTime.get() != null && remainTime.get().equals(totalTime.get()));
                    }
                });
            }
            if (!isLoading.get() && mMediaPlayer.getTime() > 0) {
                isAvailable.setValue(true);
                remainTime.set(convertMillisecondsToHMS(mMediaPlayer.getTime()));
                totalTime.set(convertMillisecondsToHMS(mMediaPlayer.getLength()));
                if (mMediaPlayer.getLength() > 0) {
                    progress.set((int) (mMediaPlayer.getTime() * 100 / mMediaPlayer.getLength()));
                    if (subtitleDisplay != null) {
                        subtitleDisplay.updateSubtitle((int) mMediaPlayer.getTime());
                    }
                }
            }
            playerHandler.postDelayed(runnable, 1000);
        }
    };


    public SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (mMediaPlayer != null) {
                remainTime.set(convertMillisecondsToHMS(i * mMediaPlayer.getLength() / 100));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            onPause();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mMediaPlayer != null) {
                mMediaPlayer.setTime(seekBar.getProgress() * mMediaPlayer.getLength() / 100);
                new Handler().postDelayed(() -> onPlay(), 2000);
            }
        }
    };

    public void onPause() {
        if (mMediaPlayer != null) {
            removeCallback.setValue(true);
            mMediaPlayer.pause();
            playerHandler.removeCallbacks(runnable);
            isPlay.set(false);
            isShowController.set(false);
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.detachViews();
            mMediaPlayer.release();
        }
    }



}

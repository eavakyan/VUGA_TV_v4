package com.retry.vuga.utils.subtitle;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import org.videolan.libvlc.MediaPlayer;

import java.util.List;

public class SubtitleDisplay {

    private List<SubtitleParser.Subtitle> subtitles;
    private TextView subtitleTextView;
    private Handler handler = new Handler();
    private long videoStartTime;
    private MediaPlayer mMediaPlayer;

    public SubtitleDisplay(TextView subtitleTextView, List<SubtitleParser.Subtitle> subtitles, long videoStartTime) {
        this.subtitleTextView = subtitleTextView;
        this.subtitles = subtitles;
        this.videoStartTime = videoStartTime;
    }


    public void updateSubtitle(int currentPosition) {
        if (subtitles != null && !subtitles.isEmpty()) {

            for (SubtitleParser.Subtitle subtitle : subtitles) {
                if (currentPosition >= subtitle.getStartTime() && currentPosition <= subtitle.getEndTime()) {
                    subtitleTextView.setVisibility(View.VISIBLE);
                    subtitleTextView.setText(subtitle.getText());
                    return;
                }
            }
            subtitleTextView.setVisibility(View.GONE); // Clear subtitle if no match found
        }
    }


}

package com.retry.vuga.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.retry.vuga.R;
import com.retry.vuga.model.Downloads;
import com.retry.vuga.utils.Const;

public class DownloadProgressDialog extends Dialog {

    private ImageFilterView imgThumbnail;
    private TextView tvTitle;
    private TextView tvStatus;
    private TextView tvProgressPercent;
    private TextView tvDownloadSize;
    private ProgressBar progressBar;
    private TextView btnBackground;
    private TextView btnCancel;
    
    private String title;
    private String thumbnailUrl;
    private OnDownloadActionListener listener;
    private boolean isCancelable = false;
    
    public interface OnDownloadActionListener {
        void onBackgroundDownload();
        void onCancelDownload();
    }
    
    public DownloadProgressDialog(@NonNull Context context) {
        super(context);
    }
    
    public DownloadProgressDialog(@NonNull Context context, String title, String thumbnailUrl) {
        super(context);
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup dialog window
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_download_progress);
        
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }
        
        // Initialize views
        imgThumbnail = findViewById(R.id.img_thumbnail);
        tvTitle = findViewById(R.id.tv_title);
        tvStatus = findViewById(R.id.tv_status);
        tvProgressPercent = findViewById(R.id.tv_progress_percent);
        tvDownloadSize = findViewById(R.id.tv_download_size);
        progressBar = findViewById(R.id.progress_bar);
        btnBackground = findViewById(R.id.btn_background);
        btnCancel = findViewById(R.id.btn_cancel);
        
        // Set initial values
        if (title != null) {
            tvTitle.setText(title);
        }
        
        if (thumbnailUrl != null) {
            Glide.with(getContext())
                    .load(Const.IMAGE_URL + thumbnailUrl)
                    .placeholder(R.drawable.logo)
                    .into(imgThumbnail);
        }
        
        // Set click listeners
        btnBackground.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBackgroundDownload();
            }
            dismiss();
        });
        
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelDownload();
            }
            dismiss();
        });
        
        // Make dialog non-cancelable by default
        setCancelable(isCancelable);
        setCanceledOnTouchOutside(false);
    }
    
    public void setOnDownloadActionListener(OnDownloadActionListener listener) {
        this.listener = listener;
    }
    
    public void updateProgress(int progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
        if (tvProgressPercent != null) {
            tvProgressPercent.setText(progress + "%");
        }
    }
    
    public void updateDownloadSize(long currentBytes, long totalBytes) {
        if (tvDownloadSize != null) {
            String current = formatBytes(currentBytes);
            String total = formatBytes(totalBytes);
            tvDownloadSize.setText(current + " / " + total);
        }
    }
    
    public void updateStatus(String status) {
        if (tvStatus != null) {
            tvStatus.setText(status);
        }
    }
    
    public void setDownloadState(int state) {
        switch (state) {
            case Const.DownloadStatus.QUEUED:
                updateStatus("Waiting in queue...");
                btnCancel.setEnabled(true);
                break;
            case Const.DownloadStatus.START:
                updateStatus("Starting download...");
                btnCancel.setEnabled(true);
                break;
            case Const.DownloadStatus.PROGRESSING:
                updateStatus("Downloading...");
                btnCancel.setEnabled(true);
                break;
            case Const.DownloadStatus.PAUSED:
                updateStatus("Download paused");
                btnCancel.setText("Resume");
                break;
            case Const.DownloadStatus.COMPLETED:
                updateStatus("Download completed!");
                progressBar.setProgress(100);
                tvProgressPercent.setText("100%");
                btnCancel.setVisibility(View.GONE);
                btnBackground.setText("Done");
                btnBackground.setOnClickListener(v -> dismiss());
                break;
        }
    }
    
    public void setAllowCancelable(boolean cancelable) {
        this.isCancelable = cancelable;
        if (isShowing()) {
            setCancelable(cancelable);
        }
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
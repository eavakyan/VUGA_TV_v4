package com.retry.vuga.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.adapters.DownloadSeriesAdapter;
import com.retry.vuga.databinding.ActivityDownloadesBinding;
import com.retry.vuga.model.Downloads;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.CustomDialogBuilder;
import com.retry.vuga.utils.SessionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DownloadsSeriesActivity extends BaseActivity {
    ActivityDownloadesBinding binding;

    List<Downloads> mainList = new ArrayList<>();
    List<Downloads> sortedList = new ArrayList<>();
    SessionManager sessionManager;
    DownloadSeriesAdapter downloadAdapter;
    NotificationManager notificationManager;
    boolean isItPauseOrResume = false;
    int content_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_downloades);


        initView();
        initListeners();


    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void initListeners() {


        downloading_obj.observe(this, new Observer<Downloads>() {
            @Override
            public void onChanged(Downloads downloads) {

                if (isItPauseOrResume) {
                    isItPauseOrResume = false;
                    changeData();
                    downloadAdapter.changeDownloadData(downloads);

                } else {

                    downloadAdapter.changeDownloadData(downloads);
                }

            }
        });

        binding.blurView.setOnClickListener(v -> {

        });
        binding.loutLoader.setOnClickListener(v -> {

        });


        downloadAdapter.setOnClick(new DownloadSeriesAdapter.OnClick() {
            @Override
            public void onClick(Downloads model) {

                goToWatchDownload(model);

            }

            @Override
            public void onMenuClick(Downloads model) {

                showWatchDialogue(model);


            }

            @Override
            public void onQueuedClick(Downloads model) {
                showDeleteDialogue(model);
            }

            @Override
            public void onInsideClick(Downloads model) {
            }

            @Override
            public void onDownloadClick(Downloads model) {
                showResumeDialogue(model);
            }

            @Override
            public void onProgressClick(Downloads model) {
                showPauseDialogue(model);
            }
        });


        binding.btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });


        binding.deleteAllBtn.setOnClickListener(v -> {
            binding.blurView.setVisibility(View.VISIBLE);
            new CustomDialogBuilder(DownloadsSeriesActivity.this).showSimplePopup(false, getString(R.string.delete_from_downloads_all), new CustomDialogBuilder.OnDismissListener() {
                @Override
                public void onPositiveDismiss() {
                    deleteAll();


                }

                @Override
                public void onDismiss() {
                    binding.blurView.setVisibility(View.GONE);


                }
            });
        });

    }

    private void goToWatchDownload(Downloads model) {

        File file = new File(model.getPath() + "/" + model.getFileName());

        if (file.exists()) {
            Intent intent = new Intent(DownloadsSeriesActivity.this, PlayerNewActivity.class);
            intent.putExtra(Const.DataKey.DOWNLOADS, new Gson().toJson(model));
            startActivity(intent);
        } else {
            Toast.makeText(DownloadsSeriesActivity.this, getString(R.string.file_does_not_exist), Toast.LENGTH_SHORT).show();

        }
    }

    private void showRemoveAlert(Downloads model) {

        binding.blurView.setVisibility(View.VISIBLE);

        new CustomDialogBuilder(this).showSimplePopup(false, getString(R.string.delete_from_downloads), new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {
                removeFromDownloads(model);

            }

            @Override
            public void onDismiss() {
                binding.blurView.setVisibility(View.GONE);

            }
        });
    }

    private void showWatchDialogue(Downloads model) {
        binding.blurView.setVisibility(View.VISIBLE);

        new CustomDialogBuilder(this).showWatchDownloadDialog(model.getTitle(), new CustomDialogBuilder.OnDownloadDismissListener() {
            @Override
            public void onTopDismiss() {
                goToWatchDownload(model);
            }

            @Override
            public void onDelete() {
                showRemoveAlert(model);
            }

            @Override
            public void onDismiss() {
                binding.blurView.setVisibility(View.GONE);

            }
        });
    }

    private void showPauseDialogue(Downloads model) {
        binding.blurView.setVisibility(View.VISIBLE);

        new CustomDialogBuilder(this).showPauseDownloadDialog(model.getTitle(), new CustomDialogBuilder.OnDownloadDismissListener() {
            @Override
            public void onTopDismiss() {
                pauseDownload(model);
            }

            @Override
            public void onDelete() {
                showRemoveAlert(model);
            }

            @Override
            public void onDismiss() {
                binding.blurView.setVisibility(View.GONE);

            }
        });
    }

    private void pauseDownload(Downloads model) {

        if (isNetworkConnected()) {
            if (downloadService != null && downloadService.getMyDownloader() != null) {
                isItPauseOrResume = true;
                downloadService.getMyDownloader().pauseDownload(model);
            }
        } else {
            Toast.makeText(DownloadsSeriesActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

        }
    }

    private void showResumeDialogue(Downloads model) {
        binding.blurView.setVisibility(View.VISIBLE);

        new CustomDialogBuilder(this).showResumeDownloadDialog(model.getTitle(), new CustomDialogBuilder.OnDownloadDismissListener() {
            @Override
            public void onTopDismiss() {
                isItPauseOrResume = true;
                resumeDownload(model);

            }

            @Override
            public void onDelete() {
                showRemoveAlert(model);
            }

            @Override
            public void onDismiss() {
                binding.blurView.setVisibility(View.GONE);

            }
        });
    }

    private void resumeDownload(Downloads model) {

        if (isNetworkConnected()) {
            if (downloadService != null && downloadService.getMyDownloader() != null) {
                downloadService.getMyDownloader().resumeDownload(model);
                changeData();
            }
        } else {
            Toast.makeText(DownloadsSeriesActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

        }
    }

    private void showDeleteDialogue(Downloads model) {
        binding.blurView.setVisibility(View.VISIBLE);

        new CustomDialogBuilder(this).showDeleteDownloadDialog(model.getTitle(), new CustomDialogBuilder.OnDownloadDismissListener() {
            @Override
            public void onTopDismiss() {

            }

            @Override
            public void onDelete() {
                showRemoveAlert(model);
            }

            @Override
            public void onDismiss() {
                binding.blurView.setVisibility(View.GONE);

            }
        });
    }

    private void deleteAll() {

        binding.loutLoader.setVisibility(View.VISIBLE);
        if (downloadService != null
                && downloadService.getMyDownloader() != null
                && downloadService.getMyDownloader().anotherIsDownloading
        ) {
            downloadService.getMyDownloader().cancelDownload();

        }

        List<Downloads> downloadsList = sessionManager.getDownloads().stream().filter(downloads -> downloads.getContentId() == content_id).collect(Collectors.toList());
        for (int i = 0; i < downloadsList.size(); i++) {
            sessionManager.removeFileFromDownloads(downloadsList.get(i));
            notificationManager.cancel(downloadsList.get(i).getId());
        }
        if (isNetworkConnected()) {
            List<Downloads> pendingList = sessionManager.getPendings().stream().filter(downloads -> downloads.getContentId() == content_id).collect(Collectors.toList());

            for (int i = 0; i < pendingList.size(); i++) {
                sessionManager.removeFileFromPending(pendingList.get(i));
                notificationManager.cancel(pendingList.get(i).getId());

            }
        }
        binding.loutLoader.setVisibility(View.GONE);

        changeData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        changeData();
    }

    private void changeData() {
        List<Downloads> pendingList = sessionManager.getPendings().stream().filter(downloads -> downloads.getContentId() == content_id).collect(Collectors.toList());
        List<Downloads> downloadsList = sessionManager.getDownloads().stream().filter(downloads -> downloads.getContentId() == content_id).collect(Collectors.toList());
        List<Downloads> downloadsListWithoutFile = new ArrayList<>();

        mainList.clear();

        for (int i = 0; i < downloadsList.size(); i++) {

            File file = new File(downloadsList.get(i).getPath() + "/" + downloadsList.get(i).getFileName());
            if (file.exists()) {
                mainList.add(downloadsList.get(i));
            } else {
                downloadsListWithoutFile.add(downloadsList.get(i));
            }
        }

        for (int i = 0; i < downloadsListWithoutFile.size(); i++) {
            sessionManager.removeFileFromDownloads(downloadsListWithoutFile.get(i));

        }

//        mainList.addAll(downloadsList);
        if (isNetworkConnected()) {
            mainList.addAll(pendingList);
        }

        List<Integer> seasonIds = new ArrayList<>();


        for (int i = 0; i < mainList.size(); i++) {
            if (!seasonIds.contains(mainList.get(i).getSeasonCount())) {
                seasonIds.add(mainList.get(i).getSeasonCount());
            }
        }

        sortedList.clear();
        for (int i = 0; i < seasonIds.size(); i++) {

            int finalI = i;
            List<Downloads> listByIds = mainList.stream().filter(downloads -> downloads.getSeasonCount() == seasonIds.get(finalI)).collect(Collectors.toList());
            if (!listByIds.isEmpty()) {

                Downloads downloads = new Downloads(seasonIds.get(finalI), true);
                sortedList.addAll(listByIds);
                sortedList.add(downloads);


            }
        }

        downloadAdapter.updateItems(sortedList);
        if (sortedList.isEmpty()) {
            binding.tvNoDownloads.setVisibility(View.VISIBLE);
            binding.deleteAllBtn.setVisibility(View.GONE);
        } else {
            binding.tvNoDownloads.setVisibility(View.GONE);
            binding.deleteAllBtn.setVisibility(View.VISIBLE);
        }

    }

    private void initView() {
        sessionManager = new SessionManager(this);
        downloadAdapter = new DownloadSeriesAdapter();


        binding.rvDownloads.setAdapter(downloadAdapter);
        ((SimpleItemAnimator) binding.rvDownloads.getItemAnimator()).setSupportsChangeAnimations(false);

        content_id = getIntent().getIntExtra(Const.DataKey.CONTENT_ID, 0);
        String title = getIntent().getStringExtra(Const.DataKey.NAME);
        if (title != null) {
            binding.tvTitle.setText(title);
        }


        setBlur(binding.blurView, binding.rootLout, 10f);
        notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloading_obj.removeObservers(this);


    }


    private void removeFromDownloads(Downloads model) {

        Optional<Downloads> pendingObj = sessionManager.getPendings().stream().filter(downloads1 -> downloads1.getId() == model.getId()).findFirst();
        Optional<Downloads> downObj = sessionManager.getDownloads().stream().filter(downloads1 -> downloads1.getId() == model.getId()).findFirst();


        if (pendingObj.isPresent()) {
            if (downloadService != null
                    && downloadService.getMyDownloader() != null
                    && downloadService.getMyDownloader().anotherIsDownloading
                    && downloadService.getMyDownloader().currentDownloadObject != null
                    && downloadService.getMyDownloader().currentDownloadObject.getId() == model.getId()) {
                downloadService.getMyDownloader().cancelDownload();


                sessionManager.removeFileFromPending(pendingObj.get());

            } else {
                sessionManager.removeObjectFromPending(pendingObj.get());

            }

        } else if (downObj.isPresent()) {
            sessionManager.removeFileFromDownloads(downObj.get());

        }

        changeData();


        notificationManager.cancel(model.getId());


    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
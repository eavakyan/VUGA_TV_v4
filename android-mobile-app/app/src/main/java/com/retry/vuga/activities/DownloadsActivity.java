package com.retry.vuga.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.adapters.DownloadAdapter;
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

public class DownloadsActivity extends BaseActivity {
    ActivityDownloadesBinding binding;

    List<Downloads> mainList = new ArrayList<>();
    List<Downloads> sortedList = new ArrayList<>();
    DownloadAdapter pendingAdapter;
    NotificationManager notificationManager;
    boolean isItPauseOrResume = false;


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

    @Override
    protected void onResume() {
        super.onResume();

        changeData();
    }

    private void initListeners() {


        downloading_obj.observe(this, new Observer<Downloads>() {
            @Override
            public void onChanged(Downloads downloads) {

                if (isItPauseOrResume) {
                    isItPauseOrResume = false;
                    changeData();
                    pendingAdapter.changeDownloadData(downloads);

                } else {

                    pendingAdapter.changeDownloadData(downloads);
                }

            }
        });

        binding.blurView.setOnClickListener(v -> {

        });
        binding.loutLoader.setOnClickListener(v -> {

        });


        pendingAdapter.setOnClick(new DownloadAdapter.OnClick() {
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

                Intent intent = new Intent(DownloadsActivity.this, DownloadsSeriesActivity.class);
                intent.putExtra(Const.DataKey.CONTENT_ID, model.getContentId());
                intent.putExtra(Const.DataKey.NAME, model.getTitle());
                startActivity(intent);
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
            new CustomDialogBuilder(DownloadsActivity.this).showSimplePopup(false, getString(R.string.delete_from_downloads_all), new CustomDialogBuilder.OnDismissListener() {
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
            Intent intent = new Intent(DownloadsActivity.this, PlayerNewActivity.class);
            intent.putExtra(Const.DataKey.DOWNLOADS, new Gson().toJson(model));
            startActivity(intent);
        } else {
            Toast.makeText(DownloadsActivity.this, getString(R.string.file_does_not_exist), Toast.LENGTH_SHORT).show();

        }
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
            Toast.makeText(DownloadsActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

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
            Toast.makeText(DownloadsActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();

        }
    }

    private void showDeleteDialogue(Downloads model) {
        binding.blurView.setVisibility(View.VISIBLE);

        new CustomDialogBuilder(this).showDeleteDownloadDialog(model.getTitle(), new CustomDialogBuilder.OnDownloadDismissListener() {
            @Override
            public void onTopDismiss() {
                //no byn for thi here

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


    private void deleteAll() {

        binding.loutLoader.setVisibility(View.VISIBLE);
        if (downloadService != null
                && downloadService.getMyDownloader() != null
                && downloadService.getMyDownloader().anotherIsDownloading
        ) {
            downloadService.getMyDownloader().cancelDownload();

        }

        List<Downloads> downloadsList = sessionManager.getDownloads();
        for (int i = 0; i < downloadsList.size(); i++) {
            sessionManager.removeFileFromDownloads(downloadsList.get(i));
            notificationManager.cancel(downloadsList.get(i).getId());
        }
        if (isNetworkConnected()) {
            List<Downloads> pendingList = sessionManager.getPendings();

            for (int i = 0; i < pendingList.size(); i++) {
                sessionManager.removeFileFromPending(pendingList.get(i));
                notificationManager.cancel(pendingList.get(i).getId());

            }
        }
        binding.loutLoader.setVisibility(View.GONE);

        changeData();

    }


    private void changeData() {
        List<Downloads> pendingList = sessionManager.getPendings();
        List<Downloads> downloadsList = sessionManager.getDownloads();
        List<Downloads> downloadsListWithoutFile = new ArrayList<>();
        mainList.clear();

        Log.i("TAG", "changeData: " + downloadsList.size());
        for (int i = 0; i < downloadsList.size(); i++) {

            File file = new File(downloadsList.get(i).getPath() + "/" + downloadsList.get(i).getFileName());
            if (file.exists()) {
                mainList.add(downloadsList.get(i));
            } else {
                downloadsListWithoutFile.add(downloadsList.get(i));
            }
        }

        Log.i("TAG", "changeData: " + mainList.size());
        Log.i("TAG", "changeData: " + downloadsListWithoutFile.size());


        for (int i = 0; i < downloadsListWithoutFile.size(); i++) {
            sessionManager.removeFileFromDownloads(downloadsListWithoutFile.get(i));

        }


//        mainList.addAll(downloadsList);
        if (isNetworkConnected()) {
            mainList.addAll(pendingList);
        }

        List<Integer> ids = new ArrayList<>();


        for (int i = 0; i < mainList.size(); i++) {
            if (!ids.contains(mainList.get(i).getContentId())) {
                ids.add(mainList.get(i).getContentId());
            }
        }

        sortedList.clear();
        for (int i = 0; i < ids.size(); i++) {

            int finalI = i;
            List<Downloads> listByIds = mainList.stream().filter(downloads -> downloads.getContentId() == ids.get(finalI)).collect(Collectors.toList());
            if (!listByIds.isEmpty()) {
                if (listByIds.get(0).getType() == 2) {
                    // first object ni anadar akhu list save karavyu 6
                    Downloads downloads = listByIds.get(0);
                    List<Downloads> episodeList = listByIds;
                    downloads.setEpisodeList(episodeList);
                    listByIds.set(0, downloads);
                    sortedList.add(listByIds.get(0));
                } else {
                    // here client will always add only one souce for one content ,
                    // but in testing if there are multiple so we are showing them sepratly
                    sortedList.addAll(listByIds);
                }
            }
        }


        pendingAdapter.updateItems(sortedList);
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
        pendingAdapter = new DownloadAdapter();


        binding.rvDownloads.setAdapter(pendingAdapter);
        ((SimpleItemAnimator) binding.rvDownloads.getItemAnimator()).setSupportsChangeAnimations(false);




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

        int position = sortedList.indexOf(model);
        sortedList.remove(model);
        pendingAdapter.notifyItemRemoved(position);
        pendingAdapter.notifyItemRangeChanged(position, sortedList.size());
        if (sortedList.isEmpty()) {
            pendingAdapter.updateItems(sortedList);
            binding.tvNoDownloads.setVisibility(View.VISIBLE);
            binding.deleteAllBtn.setVisibility(View.GONE);
        }


        notificationManager.cancel(model.getId());



    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
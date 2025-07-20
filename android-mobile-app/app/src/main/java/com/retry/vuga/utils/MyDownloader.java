package com.retry.vuga.utils;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.activities.DownloadsActivity;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.Downloads;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;


public class MyDownloader {

    private final SessionManager sessionManager;
    private final DownloadService downloadService;
    Context context;
    public Downloads currentDownloadObject;
    public boolean anotherIsDownloading = false;
    int downloadID;
    int downloadingProgress;
    NotificationManager notificationManager;
    NotificationCompat.Builder notificationBuilder;


    public void addToDownload(Downloads model) {

        startDownload(model);

    }


    public MyDownloader(DownloadService downloadService, Context context) {
        this.downloadService = downloadService;
        this.context = context;
        sessionManager = new SessionManager(context);
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build();
        PRDownloader.initialize(context, config);

    }

    public void pauseDownload(Downloads model) {
        if (downloadID != 0 && currentDownloadObject != null && currentDownloadObject.getId() == model.getId() && anotherIsDownloading) {
            Log.i("TAG", "myDown pauseDownload: ");
            PRDownloader.pause(downloadID);

        }
    }

    public void resumeDownload(Downloads model) {

        if (downloadID != 0 && currentDownloadObject != null && currentDownloadObject.getId() == model.getId()) {
            PRDownloader.resume(downloadID);
            Log.i("TAG", "myDown resumeDownload: ");
        } else {
            Log.i("TAG", "myDown startAgainDownload: ");
            Downloads down_obj = null;

            for (int i = 0; i < sessionManager.getPendings().size(); i++) {
                if (sessionManager.getPendings().get(i).getId() == model.getId()) {
                    down_obj = sessionManager.getPendings().get(i);
                    break;
                }
            }

            if (down_obj == null) {
                return;
            }
            startDownload(down_obj);
        }

    }

    public void pauseDownload(ContentDetail.SourceItem model) {
        if (downloadID != 0 && currentDownloadObject != null && currentDownloadObject.getId() == model.getId() && anotherIsDownloading) {
            Log.i("TAG", "myDown pauseDownload: ");
            PRDownloader.pause(downloadID);
        }
    }

    public void cancelDownload() {
        if (downloadID != 0) {
            PRDownloader.cancel(downloadID);

        }
    }

    public void resumeDownload(ContentDetail.SourceItem model) {


        if (downloadID != 0 && currentDownloadObject != null && currentDownloadObject.getId() == model.getId()) {
            PRDownloader.resume(downloadID);
            Log.i("TAG", "myDown resumeDownload: ");
        } else {
            Downloads down_obj = null;
            Log.i("TAG", "myDown startAgainDownload: ");
            for (int i = 0; i < sessionManager.getPendings().size(); i++) {
                if (sessionManager.getPendings().get(i).getId() == model.getId()) {
                    down_obj = sessionManager.getPendings().get(i);
                    break;
                }
            }
            if (down_obj == null) {
                return;
            }
            startDownload(down_obj);
        }

    }

    private void startDownload(Downloads model) {
        if (anotherIsDownloading || currentDownloadObject != null || downloadID != 0) {
            //becoz only anotherIsDownloading can be false if downloading is paused
            sendBrodcast(model, Const.DownloadStatus.QUEUED);
            return;
        }


        long availableMb = context.getApplicationContext().getExternalFilesDir(null).getUsableSpace() / (1024 * 1024);
        Log.i("TAG", "startDownload: " + availableMb);

        if (availableMb < 400) {
            Toast.makeText(downloadService, context.getString(R.string.not_enough_storage_space), Toast.LENGTH_SHORT).show();
            downloadID = 0;
            anotherIsDownloading = false;
            currentDownloadObject = null;
            downloadingProgress = 0;
            sessionManager.removeObjectFromPending(model);
            sendBrodcast(model, Const.DownloadStatus.ERROR);
            return;
        }

        anotherIsDownloading = true;
        downloadID = PRDownloader.download(model.getUrl(), model.getPath(), model.getFileName()).build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        sessionManager.saveBooleanValue(Const.DataKey.IS_DOWNLOAD_PAUSED, false);
                        currentDownloadObject = model;
                        Log.i("TAG", "myDown :start loading  " + model.getId());
                        Log.i("TAG", "myDown :start loading  " + downloadingProgress);
                        sessionManager.changePendingStatus(model, Const.DownloadStatus.START, downloadingProgress == 0 ? model.getProgress() : downloadingProgress);
                        sendBrodcast(model, Const.DownloadStatus.START);
                        sendStartNotification(downloadingProgress == 0 ? model.getProgress() : downloadingProgress, currentDownloadObject);
                        anotherIsDownloading = true;


                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                        Log.i("TAG", "myDown onPausedd: ");
                        sessionManager.saveBooleanValue(Const.DataKey.IS_DOWNLOAD_PAUSED, true);

                        sessionManager.changePendingStatus(model, Const.DownloadStatus.PAUSED, downloadingProgress);
                        sendBrodcast(model, Const.DownloadStatus.PAUSED);

                        currentDownloadObject = null;
                        downloadID = 0;
                        anotherIsDownloading = false;
                        downloadingProgress = 0;


                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        Log.i("TAG", "onCancel: ");
                        sessionManager.removeObjectFromPending(model);
                        sendBrodcast(model, Const.DownloadStatus.ERROR);
                        if (notificationManager != null) {
                            notificationManager.cancel(currentDownloadObject.getId());
                        }
                        currentDownloadObject = null;
                        downloadID = 0;
                        anotherIsDownloading = false;
                        downloadingProgress = 0;

                        checkForPending();


                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        long p = progress.currentBytes * 100 / progress.totalBytes;

                        int newP = (int) p;
                        Log.i("TAG", "onReceive: sent is 5 " + newP);


                        if (newP != downloadingProgress && newP % 5 == 0) {
                            Log.i("TAG", "onReceive: sent is 5 " + downloadingProgress);
                            downloadingProgress = newP;
                            sessionManager.changePendingStatus(model, Const.DownloadStatus.PROGRESSING, downloadingProgress);
                            sendBrodcast(model, Const.DownloadStatus.PROGRESSING);
                            sendProgressNoti(downloadingProgress, currentDownloadObject);

                        }


                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        Log.i("TAG", "myDown :onDownloadComplete  " + model.getId());


                        sessionManager.removeObjectFromPending(model);

                        model.setDownloadStatus(Const.DownloadStatus.COMPLETED);
                        sessionManager.addToDownloads(model);
                        sendBrodcast(model, Const.DownloadStatus.COMPLETED);
                        sendProgressNoti(100, currentDownloadObject);

                        downloadID = 0;
                        anotherIsDownloading = false;
                        currentDownloadObject = null;
                        downloadingProgress = 0;


                        checkForPending();
                    }

                    @Override
                    public void onError(Error error) {
                        Log.i("TAG", "myDown onError: " + model.getId());
                        if (notificationManager != null && currentDownloadObject != null) {
                            notificationManager.cancel(currentDownloadObject.getId());
                        }
                        if (error.isConnectionError()) {
                            Toast.makeText(context, context.getString(R.string.no_connection), Toast.LENGTH_LONG).show();
                            sessionManager.saveBooleanValue(Const.DataKey.IS_DOWNLOAD_PAUSED, true);
                            sessionManager.changePendingStatus(model, Const.DownloadStatus.PAUSED, downloadingProgress);
                            Log.i("TAG", "internet onReceive: service ");
                            sendBrodcast(model, Const.DownloadStatus.ERROR);

                        } else {
                            sessionManager.removeObjectFromPending(model);
                            sendBrodcast(model, Const.DownloadStatus.ERROR);

                            checkForPending();
                            Toast.makeText(context, context.getString(R.string.couldn_t_download), Toast.LENGTH_SHORT).show();
                        }
                        downloadID = 0;
                        anotherIsDownloading = false;
                        currentDownloadObject = null;
                        downloadingProgress = 0;


                    }


                });


    }

    private void sendProgressNoti(int progress, Downloads currentObject) {
        if (!sessionManager.getBooleanValue(Const.DataKey.NOTIFICATION)) {
            return;
        }


        if (currentObject != null && notificationBuilder != null && notificationManager != null) {

            if (progress == 100) {
                notificationBuilder.setProgress(0, 0, false).build();
                notificationBuilder.setSmallIcon(R.drawable.ic_checkbox);
                notificationBuilder.setAutoCancel(true);
                notificationBuilder.setOngoing(false);


            } else {
                notificationBuilder.setProgress(100, progress, false).build();

            }
            setBigDetails(progress);

            notificationManager.notify(currentObject.getId(), notificationBuilder.build());

        }

    }

    private void sendStartNotification(int progress, Downloads currentObject) {
        if (!sessionManager.getBooleanValue(Const.DataKey.NOTIFICATION)) {
            return;
        }
        if (currentObject == null) {
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bigPicture = null;

                try {
                    URL url = new URL(Const.IMAGE_URL + currentObject.getContentImage());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();

                    Bitmap picture = BitmapFactory.decodeStream(inputStream);
                    bigPicture = Bitmap.createScaledBitmap(picture, 100, 60, true);


                } catch (IOException e) {
                    e.printStackTrace();
                }


                Intent resultIntent = new Intent(context, DownloadsActivity.class);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntentWithParentStack(resultIntent);
// Get the PendingIntent containing the entire back stack.
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(0,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


                notificationBuilder = new NotificationCompat.Builder(context, Global.DOWNLOAD_NOTI_CHANNEL_ID)
                        .setContentTitle(currentObject.getTitle())
                        .setSubText(ContextCompat.getString(context, R.string.downloads))
                        .setProgress(100, progress, false)
                        .setSound(null)
                        .setOngoing(true)
                        .setColor(ContextCompat.getColor(context, R.color.app_color))
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setSmallIcon(R.drawable.ic_download)
                        .setContentIntent(resultPendingIntent);

                setBigDetails(progress);

                if (bigPicture != null) {
                    notificationBuilder.setLargeIcon(bigPicture);

                }


                if (notificationManager != null) {

                    notificationManager.notify(currentObject.getId(), notificationBuilder.build());
                }


            }
        });


        thread.start();


    }

    private void setBigDetails(int progress) {

        if (progress == 100) {
            notificationBuilder.setProgress(0, 0, false).build();
            String detail = "";
            if (currentDownloadObject.getType() == 2) {
                detail = currentDownloadObject.getEpisodeName() + "\n" +
                        "S " + Global.getFormattedText(currentDownloadObject.getSeasonCount()) + "  "
                        + "E " + Global.getFormattedText(currentDownloadObject.getEpisodeCount()) + "\n";

            }

            detail = detail + ContextCompat.getString(context, R.string.download_completed);

            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(detail)
            );
        } else {
            String detail = "";
            if (currentDownloadObject != null && currentDownloadObject.getType() == 2) {
                detail = currentDownloadObject.getEpisodeName() + "\n"
                        + "S " + Global.getFormattedText(currentDownloadObject.getSeasonCount()) + "  "
                        + "E " + Global.getFormattedText(currentDownloadObject.getEpisodeCount()) + "\n"
                ;
            }

            detail = detail + progress + "% " + ContextCompat.getString(context, R.string.downloaded);

            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(detail)
            );
        }
    }

    private void sendBrodcast(Downloads model, int status) {
        Intent intent = new Intent(Const.DataKey.DOWNLOAD_BROAD_CAST);
        Downloads downloads = new Downloads(downloadingProgress, model.getId());
        downloads.setContentId(model.getContentId());
        downloads.setType(model.getType());
        downloads.setDownloadStatus(status);
        intent.putExtra(Const.DataKey.DOWNLOAD_OBJ, new Gson().toJson(downloads));
        context.sendBroadcast(intent);

    }


    public void checkForPending() {
        Log.i("TAG", "myDown checkForPending: ");
        List<Downloads> penList = sessionManager.getPendings();
        if (!penList.isEmpty()) {
            Optional<Downloads> item = penList.stream().filter(downloadItem -> downloadItem.getDownloadStatus() == Const.DownloadStatus.PAUSED).findFirst();

            if (item.isPresent()) {
                startDownload(item.get());

            } else {
                startDownload(penList.get(0));

            }
            Log.i("TAG", "myDown checkForPending: yes 6 " + sessionManager.getPendings().get(0));

        }

    }


}

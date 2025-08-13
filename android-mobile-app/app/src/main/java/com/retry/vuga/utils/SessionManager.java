package com.retry.vuga.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.retry.vuga.model.AllSubscriptionData;
import com.retry.vuga.model.AppSetting;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.Downloads;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.model.ads.CustomAds;
import com.retry.vuga.model.history.MovieHistory;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SessionManager {
    private SharedPreferences pref;
    private Context context;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(Const.PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }


    public void saveStringValue(String key, String i) {
        editor.putString(key, i);
        editor.apply();
    }

    public void saveIntValue(String key, int i) {
        editor.putInt(key, i);
        editor.apply();
    }


    public void saveUser(UserRegistration.Data data) {

        editor.putString(Const.DataKey.USER, new Gson().toJson(data));
        editor.apply();

    }

    public String getLanguage() {
        return pref.getString(Const.DataKey.LANGUAGE, "en");
    }

    public void saveLanguage(String id) {
        editor.putString(Const.DataKey.LANGUAGE, id);
        editor.apply();
    }

    public UserRegistration.Data getUser() {

        String user = pref.getString(Const.DataKey.USER, null);
        if (user != null) {
            return new Gson().fromJson(user, UserRegistration.Data.class);
        }
        return null;
    }

    public void saveFireBaseToken(String token) {
        editor.putString(Const.DataKey.TOKEN, token);
        editor.apply();
    }

    public int getIntValue(String key) {
        return pref.getInt(key, -1);
    }

    public String getStringValue(String key) {
        return pref.getString(key, "");
    }

    public String getFireBaseToken() {
        String token = pref.getString(Const.DataKey.TOKEN, "");
        if (!token.isEmpty()) {
            return token;
        }
        return null;
    }


    public void saveBooleanValue(String key, boolean b) {

        editor.putBoolean(key, b);
        editor.apply();
    }

    public Boolean getBooleanValue(String key) {

        return pref.getBoolean(key, false);

    }

    public AppSetting getAppSettings() {
        String settings = pref.getString(Const.DataKey.APP_SETTINGS, null);
        if (settings != null) {
            return new Gson().fromJson(settings, AppSetting.class);
        }

        return null;
    }

    public void saveSubscriptionData(AllSubscriptionData allSubscriptionData) {

        editor.putString(Const.DataKey.SUBSCRIPTION_DATA, new Gson().toJson(allSubscriptionData));
        editor.apply();
    }


    public AllSubscriptionData getSubscriptionData() {
        String sub = pref.getString(Const.DataKey.SUBSCRIPTION_DATA, null);
        if (sub != null) {
            return new Gson().fromJson(sub, AllSubscriptionData.class);
        }

        return null;
    }

    public void saveSettingData(AppSetting appSetting) {

        editor.putString(Const.DataKey.APP_SETTINGS, new Gson().toJson(appSetting));
        editor.apply();
    }


    public void clear() {
        editor.clear();
        editor.apply();
    }

    public void saveBranchData(String s) {
        editor.putString(Const.DataKey.BRANCH_DATA, s);
        editor.apply();
    }

    public String getBranchData() {
        return pref.getString(Const.DataKey.BRANCH_DATA, null);
    }

    public void removeBranchData() {
        editor.remove(Const.DataKey.BRANCH_DATA);
        editor.apply();
    }

    public void removeObjectFromDownloads(Downloads model) {
        List<Downloads> list = getDownloads();
        if (!list.isEmpty()) {
            Log.i("TAG", "removeFromDownloads: " + list.size());
            list.remove(model);
            editor.putString(Const.DataKey.DOWNLOADS, new Gson().toJson(list));
            editor.apply();


        }
    }

    public void changePendingStatus(Downloads model, int status, int... progress) {
        List<Downloads> list = getPendings();
        if (!list.isEmpty()) {

            int pos = -1;
            for (int i = 0; i < list.size(); i++) {
                if (Objects.equals(model.getId(), list.get(i).getId())) {
                    pos = i;
                    break;
                }
            }
            if (pos == -1) {
                return;
            }
            Downloads d = list.get(pos);
            d.setDownloadStatus(status);
            if (progress.length > 0) {
                d.setProgress(progress[0]);

            }
            list.set(pos, d);
            editor.putString(Const.DataKey.pending_list, new Gson().toJson(list));
            editor.apply();


        }
    }

    public void removeObjectFromPending(Downloads model) {
        List<Downloads> list = getPendings();
        if (!list.isEmpty()) {

            int pos = -1;
            for (int i = 0; i < list.size(); i++) {
                if (Objects.equals(model.getId(), list.get(i).getId())) {
                    pos = i;
                    break;
                }
            }
            if (pos == -1) {
                return;
            }
            list.remove(pos);
            editor.putString(Const.DataKey.pending_list, new Gson().toJson(list));
            editor.apply();


        }
    }

    public void removeFileFromDownloads(Downloads model) {
        List<Downloads> list = getDownloads();
        if (!list.isEmpty()) {

            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId() == model.getId()) {
                    File file = new File(model.getPath() + "/" + model.getFileName());
                    if (file.exists()) {
                        Log.i("TAG", "removeFileFromDownloads:file found " + model.getFileName());
                        file.delete();
                    } else {
                        Log.i("TAG", "removeFileFromDownloads:file not found " + model.getFileName());

                    }
                    list.remove(i);

                    editor.putString(Const.DataKey.DOWNLOADS, new Gson().toJson(list));
                    editor.apply();
                }

            }


        }
    }

    public void removeFileFromPending(Downloads model) {
        List<Downloads> list = getPendings();
        if (!list.isEmpty()) {

            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId() == model.getId()) {
                    File file = new File(model.getPath() + "/" + model.getFileName());
                    if (file.exists()) {
                        Log.i("TAG", "removeFileFromPending:file found " + model.getFileName());
                        file.delete();
                    } else {
                        Log.i("TAG", "removeFileFromPending:file not found " + model.getFileName());

                    }
                    list.remove(i);

                    editor.putString(Const.DataKey.pending_list, new Gson().toJson(list));
                    editor.apply();
                }

            }


        }
    }

    public void addToDownloads(Downloads model) {
        List<Downloads> downloads = getDownloads();
        downloads.add(model);
        editor.putString(Const.DataKey.DOWNLOADS, new Gson().toJson(downloads));
        editor.apply();

    }

    public void editDownloads(Downloads download, int progress) {
        List<Downloads> downloads = getDownloads();
        for (int i = 0; i < downloads.size(); i++) {
            if (downloads.get(i).getId() == download.getId()) {
                downloads.get(i).setPlayProgress(progress);
                updateMovieHistory(download.getSourceItem(), progress, download.getTitle(), download.getContentImage());
                break;
            }
        }
        editor.putString(Const.DataKey.DOWNLOADS, new Gson().toJson(downloads));
        editor.apply();
    }

    public void addToPending(Downloads model) {
        List<Downloads> downloads = getPendings();
        downloads.add(model);
        editor.putString(Const.DataKey.pending_list, new Gson().toJson(downloads));
        editor.apply();

    }

    public List<Downloads> getDownloads() {

        String d = pref.getString(Const.DataKey.DOWNLOADS, null);
        if (d != null) {

            return new Gson().fromJson(d, new TypeToken<List<Downloads>>() {

            }.getType());
        } else return new ArrayList<>();
    }

    public List<Downloads> getPendings() {

        String d = pref.getString(Const.DataKey.pending_list, null);
        if (d != null) {

            return new Gson().fromJson(d, new TypeToken<List<Downloads>>() {

            }.getType());
        } else return new ArrayList<>();
    }

    public void saveCustomAds(CustomAds customAds) {
        if (customAds != null && customAds.getStatus() && customAds.getData() != null) {
            saveStringValue(Const.DataKey.CUSTOM_ADS, new Gson().toJson(customAds));
        }
    }

    public CustomAds getCustomAds() {
        String customAdsStr = getStringValue(Const.DataKey.CUSTOM_ADS);
        if (customAdsStr != null && !customAdsStr.isEmpty()) {
            return new Gson().fromJson(customAdsStr, CustomAds.class);
        }
        return null;
    }

    public void updateMovieHistory(ContentDetail.SourceItem modelSource, int progress, String title, String thumbnail) {
        updateMovieHistory(modelSource, progress, title, thumbnail, null, null);
    }
    
    public void updateMovieHistory(ContentDetail.SourceItem modelSource, int progress, String title, String thumbnail, Integer releaseYear, String duration) {
        if (modelSource == null) return;
        ArrayList<MovieHistory> histories = getMovieHistories();
        ArrayList<ContentDetail.SourceItem> sourceItems = new ArrayList<>();
        boolean isAddedContent = false;
        histories.sort(Comparator.comparing(MovieHistory::getId));
        MovieHistory movieHistory = null;
        for (int i = 0; i < histories.size(); i++) {
            movieHistory = histories.get(i);
            if (movieHistory != null) {
                isAddedContent = modelSource.getContent_id() == (movieHistory.getMovieId() != null ? movieHistory.getMovieId() : -1);
                if (isAddedContent) {
                    if (movieHistory.getSources() != null) {
                        sourceItems = movieHistory.getSources();
                    } else {
                        sourceItems = new ArrayList<>();
                    }
                }
            }
            if (isAddedContent) {
                histories.remove(i);
                break;
            }
        }

        for (int i = 0; i < sourceItems.size(); i++) {
            if (sourceItems.get(i).getId() == modelSource.getId()) {
                sourceItems.remove(i);
                break;
            }
        }

//        }
        modelSource.setPlayProgress(progress);
        modelSource.time = System.currentTimeMillis();
        sourceItems.add(modelSource);
        //!histories.isEmpty() && histories.get(histories.size() - 1) != null ? histories.get(histories.size() - 1).getId() + 1 : 0
        movieHistory = new MovieHistory(1, modelSource.getContent_id(), title, thumbnail, System.currentTimeMillis(), sourceItems, releaseYear, duration);
//        movieHistory.setTime(System.currentTimeMillis());
        histories.add(movieHistory);
        if (histories.size() > Const.HISTORY_COUNT) {
            histories.remove(0);
        }
        saveStringValue(Const.DataKey.MOVIE_HISTORY, new Gson().toJson(histories));
    }

    public void updateMovieHistory(int contentId, int id, int progress, String title, String thumbnail) {
        updateMovieHistory(contentId, id, progress, title, thumbnail, null, null);
    }
    
    public void updateMovieHistory(int contentId, int id, int progress, String title, String thumbnail, Integer releaseYear, String duration) {
        ArrayList<MovieHistory> histories = getMovieHistories();
        ArrayList<ContentDetail.SourceItem> sourceItems = new ArrayList<>();
        boolean isAddedContent = false;
        histories.sort(Comparator.comparing(MovieHistory::getId));
        MovieHistory movieHistory = null;
        for (int i = 0; i < histories.size(); i++) {
            movieHistory = histories.get(i);
            if (movieHistory != null) {
                isAddedContent = contentId == (movieHistory.getMovieId() != null ? movieHistory.getMovieId() : -1);
                if (isAddedContent) {
                    if (movieHistory.getSources() != null) {
                        sourceItems = movieHistory.getSources();
                    } else {
                        sourceItems = new ArrayList<>();
                    }
                }
            }
            if (isAddedContent) {
                histories.remove(i);
                break;
            }
        }
        ContentDetail.SourceItem modelSource = null;

        for (int i = 0; i < sourceItems.size(); i++) {
            if (sourceItems.get(i).getId() == id) {
                modelSource = sourceItems.get(i);
                break;
            }
        }
        if (modelSource == null) return;
//        }
        modelSource.setPlayProgress(progress);
        modelSource.time = System.currentTimeMillis();
        sourceItems.add(modelSource);
        //!histories.isEmpty() && histories.get(histories.size() - 1) != null ? histories.get(histories.size() - 1).getId() + 1 : 0
        movieHistory = new MovieHistory(1, modelSource.getContent_id(), title, thumbnail, System.currentTimeMillis(), sourceItems, releaseYear, duration);
//        movieHistory.setTime(System.currentTimeMillis());
        histories.add(movieHistory);
        if (histories.size() > Const.HISTORY_COUNT) {
            histories.remove(0);
        }
        saveStringValue(Const.DataKey.MOVIE_HISTORY, new Gson().toJson(histories));
    }

    public void deleteMovieFromHistory(int movieId) {
        ArrayList<MovieHistory> histories = getMovieHistories();
        for (int i = 0; i < histories.size(); i++) {
            MovieHistory movieHistory = histories.get(i);
            if (movieHistory != null && movieHistory.getMovieId() != null && movieHistory.getMovieId() == movieId) {
                histories.remove(movieHistory);
                break;
            }
        }
        saveStringValue(Const.DataKey.MOVIE_HISTORY, new Gson().toJson(histories));
    }

    public ArrayList<MovieHistory> getMovieHistories() {
        String history = getStringValue(Const.DataKey.MOVIE_HISTORY);
        if (history != null && !history.isEmpty()) {
            return new Gson().fromJson(history, new TypeToken<ArrayList<MovieHistory>>() {
            }.getType());
        }
        return new ArrayList<>();
    }
}

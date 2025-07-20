package com.retry.vuga.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.retry.vuga.model.AppSetting;
import com.retry.vuga.model.Language;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
public class Global {

    public static final String DOWNLOAD_NOTI_CHANNEL_ID = "downloads_notification";
//    public static CustomerInfo customerInfo = null;


    public static String getFormattedText(int count) {

        if (count < 10) {
            String cNew = "0" + count;
            return cNew;
        }
        return String.valueOf(count);
    }

    public static String prettyCount(Number number) {
        try {
            char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
            long numValue = number.longValue();
            int value = (int) Math.floor(Math.log10(numValue));
            int base = value / 3;
            if (value >= 3 && base < suffix.length) {
                double value2 = numValue / Math.pow(10.0, (base * 3));
                if (Double.toString(value2).contains(".")) {
                    String[] parts = Double.toString(value2).split("\\.");
                    String num = parts[parts.length - 1];
                    if (num.contains("0")) {
                        return new DecimalFormat("#0").format(value2) + suffix[base];
                    } else {
                        return new DecimalFormat("#0.0").format(value2) + suffix[base];
                    }
                } else {
                    return new DecimalFormat("#0").format(value2) + suffix[base];
                }
            } else {
                return new DecimalFormat("#,##0").format(numValue);
            }
        } catch (Exception e) {
            return number.toString();
        }
    }

    public static void createNotificationChannels(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {

                NotificationChannel channel = new NotificationChannel(Global.DOWNLOAD_NOTI_CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_LOW);
                channel.setDescription("Receives downloading info");
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                ((NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public static String convertSecondsToHMmSs(Long seconds) {
        long s = TimeUnit.MILLISECONDS.toSeconds(seconds) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(seconds)
        );
        long m = TimeUnit.MILLISECONDS.toMinutes(seconds) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(seconds)
        );
        long h = TimeUnit.MILLISECONDS.toHours(seconds);
        if (h >= 1) {
            return String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s);
        } else {
            return String.format(Locale.ENGLISH, "%02d:%02d", m, s);
        }
    }

    public static String listOfStringToString(List<String> list) {
        return TextUtils.join(", ", list);
    }

    public static String getGenreStringFromIds(String ids, Context context) {

        List<String> nameList = new ArrayList<>();
        SessionManager sessionManager = new SessionManager(context);
        List<AppSetting.GenreItem> genrelist = sessionManager.getAppSettings().getGenreItems();
        List<String> list = Global.convertStringToList(ids);

        for (int i = 0; i < list.size(); i++) {

            int finalI = i;
            Optional<AppSetting.GenreItem> item = genrelist.stream().filter(genreItem -> genreItem.getId() == Integer.parseInt(list.get(finalI))).findFirst();
            if (item.isPresent()) {
                nameList.add(item.get().getTitle());
            }

        }

        return listOfStringToString(nameList);


    }

    public static List<String> getGenreListFromIds(String ids, Context context) {

        List<String> nameList = new ArrayList<>();
        SessionManager sessionManager = new SessionManager(context);
        List<AppSetting.GenreItem> genrelist = sessionManager.getAppSettings().getGenreItems();
        List<String> list = Global.convertStringToList(ids);

        for (int i = 0; i < list.size(); i++) {

            int finalI = i;
            Optional<AppSetting.GenreItem> item = genrelist.stream().filter(genreItem -> genreItem.getId() == Integer.parseInt(list.get(finalI))).findFirst();
            if (item.isPresent()) {
                nameList.add(item.get().getTitle());
            }

        }

        return nameList;


    }

    public static String listOfIntegerToString(List<String> list) {
        return TextUtils.join(",", list);
    }

    public static List<String> convertStringToList(String s) {
        if (s.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> stringList = new ArrayList<>(Arrays.asList(s.split(",")));


        return stringList;
    }

    public static List<Language> getLanguages() {
        List<Language> list = new ArrayList<>();

        list.add(new Language("Arabic", "العربية", "ar"));
        list.add(new Language("Chinese(Simplified)", "简体中文", "zh"));
        list.add(new Language("English", "English", "en"));
        list.add(new Language("Danish", "Dansk", "da"));
        list.add(new Language("Dutch", "Nederlands", "nl"));
        list.add(new Language("French", "Français", "fr"));
        list.add(new Language("German", "Deutsch", "de"));
        list.add(new Language("Greek", "Ελληνικά", "el"));
        list.add(new Language("Hindi", "हिंदी", "hi"));
        list.add(new Language("Indonesian", "Bahasa Indonesia", "in"));
        list.add(new Language("Italian", "Italiano", "it"));
        list.add(new Language("Japanese", "日本語", "ja"));
        list.add(new Language("Korean", "한국어", "ko"));
        list.add(new Language("Norwegian", "norsk", "nb"));
        list.add(new Language("Polish", "Polski", "pl"));
        list.add(new Language("Portuguese", "Português", "pt"));
        list.add(new Language("Russian", "Русский", "ru"));
        list.add(new Language("Spanish", "Español", "es"));
        list.add(new Language("Swedish", "Svenska", "sv"));
        list.add(new Language("Thai", "ภาษาไทย", "th"));
        list.add(new Language("Turkish", "Türkçe", "tr"));
        list.add(new Language("Vietnamese", "Tiếng Việt", "vi"));

        return list;
    }


}


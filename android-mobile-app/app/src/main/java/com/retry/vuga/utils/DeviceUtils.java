package com.retry.vuga.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DeviceUtils {
    
    /**
     * Check if the device is a tablet
     * @param context The application context
     * @return true if the device is a tablet, false otherwise
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
    
    /**
     * Check if the device is in landscape orientation
     * @param context The application context
     * @return true if the device is in landscape, false otherwise
     */
    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
    
    /**
     * Check if the device is a tablet in landscape mode
     * @param context The application context
     * @return true if the device is a tablet in landscape, false otherwise
     */
    public static boolean isTabletLandscape(Context context) {
        return isTablet(context) && isLandscape(context);
    }
    
    /**
     * Get the screen width in pixels
     * @param context The application context
     * @return screen width in pixels
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
    
    /**
     * Get the screen height in pixels
     * @param context The application context
     * @return screen height in pixels
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
    
    /**
     * Get the screen density
     * @param context The application context
     * @return screen density
     */
    public static float getScreenDensity(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.density;
    }
    
    /**
     * Convert dp to pixels
     * @param context The application context
     * @param dp The dp value to convert
     * @return pixel value
     */
    public static int dpToPx(Context context, float dp) {
        return (int) (dp * getScreenDensity(context));
    }
    
    /**
     * Convert pixels to dp
     * @param context The application context
     * @param px The pixel value to convert
     * @return dp value
     */
    public static float pxToDp(Context context, int px) {
        return px / getScreenDensity(context);
    }
} 
package com.retry.vuga.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.retry.vuga.R;

import java.io.File;


public class GlideLoader {
    private Context mContext;
    private CircularProgressDrawable circularProgressDrawable;

    public GlideLoader(Context context) {


        this.mContext = context;
        circularProgressDrawable = new CircularProgressDrawable(mContext);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(20f);
        circularProgressDrawable.setColorSchemeColors(
                getColor(R.color.app_color),
                getColor(R.color.app_color_50)
        );
        circularProgressDrawable.start();

    }

    void loadWithCircleCrop(String imageUrl, ImageView imageView) {
        if (mContext != null && imageView != null) {
            Glide.with(mContext).load(imageUrl).apply(
                    new RequestOptions().circleCrop().error(
                            R.color.edit_text_bg_color
                    ).priority(Priority.HIGH)
            ).into(imageView);
        }
    }

    public void loadImage(String imageUrl, ImageView imageView) {
        Log.i("TAG", "loadImage: " + imageUrl);
        if (mContext != null && imageView != null) {
            Glide.with(mContext).load(imageUrl)
//                    .placeholder(circularProgressDrawable)
                    .apply(
                    new RequestOptions().error(
                            R.color.view_bg_color
                    ).priority(Priority.HIGH)
            ).into(imageView);
        }
    }

    void loadNotificationImage(Drawable drawable, ImageView imageView) {
        if (imageView != null && drawable != null) {
            imageView.setImageDrawable(drawable);
        }

    }

    void loadMediaImage(String imageUrl, ImageView imageView) {
        if (mContext != null && imageView != null) {

            Glide.with(mContext).load(new File(imageUrl)).apply(
                    new RequestOptions()
                            .placeholder(circularProgressDrawable).error(
                                    R.color.transparent
                            ).priority(Priority.HIGH)
            ).into(imageView);
        }
    }

    void loadMediaRoundImage(String imageUrl, ImageView imageView) {
        if (mContext != null && imageView != null) {
            Glide.with(mContext).load(new File(imageUrl)).apply(
                    new RequestOptions().circleCrop().placeholder(circularProgressDrawable).error(
                            R.color.transparent
                    ).priority(Priority.HIGH)
            ).into(imageView);
        }
    }

    void loadMediaRoundBitmap(Bitmap bitmap, ImageView imageView) {
        if (mContext != null && imageView != null) {
            Glide.with(mContext).load(bitmap).apply(
                    new RequestOptions().circleCrop().placeholder(circularProgressDrawable).error(
                            R.color.transparent
                    ).priority(Priority.HIGH)
            ).into(imageView);
        }
    }


    public void loadRoundDrawable(Drawable imageUrl, ImageView imageView) {
        if (mContext != null && imageView != null) {
            Glide.with(mContext).load(imageUrl).apply(
                    new RequestOptions().circleCrop().placeholder(circularProgressDrawable).error(
                            R.color.transparent
                    ).priority(Priority.HIGH)
            ).into(imageView);
        }
    }

    private int getColor(int color) {
        return ContextCompat.getColor(mContext, color);
    }

    // Static helper methods for adapters
    public static void load(Context context, String imageUrl, ImageView imageView, int placeholder) {
        if (context != null && imageView != null) {
            Glide.with(context)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(placeholder)
                            .error(placeholder)
                            .priority(Priority.HIGH))
                    .into(imageView);
        }
    }

    public static void loadIntoImageView(Context context, String imageUrl, ImageView imageView, int placeholder) {
        load(context, imageUrl, imageView, placeholder);
    }
}

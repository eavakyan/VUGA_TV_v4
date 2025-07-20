package com.retry.vuga.utils;

import android.text.TextUtils;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;


public class BindingAdapters {
    @BindingAdapter({"round_corner_image", "type"})
    public static void loadImage(ImageView imageFilterView, String url, int type) {
        Glide.with(imageFilterView)
                .load(Const.IMAGE_URL + url)
                .into(imageFilterView);
    }

    @BindingAdapter({"image_url"})
    public static void loadImage(ImageView view, String image_url) {
        if (image_url != null) {
            if (!TextUtils.isEmpty(image_url)) {
                String url = Const.IMAGE_URL + image_url;
                new GlideLoader(view.getContext()).loadImage(url, view);
            }
        }

    }


}

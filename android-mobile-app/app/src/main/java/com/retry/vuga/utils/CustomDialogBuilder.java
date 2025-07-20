package com.retry.vuga.utils;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.Window;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;

import com.retry.vuga.R;
import com.retry.vuga.adapters.SubtitleLanguagesAdapter;
import com.retry.vuga.databinding.ItemPopUpSimpleBinding;
import com.retry.vuga.databinding.ItemPopupDeleteAccountBinding;
import com.retry.vuga.databinding.ItemPopupDeleteDownloadBinding;
import com.retry.vuga.databinding.ItemPopupLockedBinding;
import com.retry.vuga.databinding.ItemPopupLogOutBinding;
import com.retry.vuga.databinding.ItemPopupPauseDownloadBinding;
import com.retry.vuga.databinding.ItemPopupPremiumBinding;
import com.retry.vuga.databinding.ItemPopupResumeDownloadBinding;
import com.retry.vuga.databinding.ItemPopupSubtitleBinding;
import com.retry.vuga.databinding.ItemPopupWatchNowBinding;

import java.util.List;


public class CustomDialogBuilder {
    private Context mContext;
    private Dialog mBuilder = null;

    public CustomDialogBuilder(Context context) {
        this.mContext = context;
        if (mContext != null) {
            mBuilder = new Dialog(mContext);
            mBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mBuilder.setCancelable(false);
            mBuilder.setCanceledOnTouchOutside(false);

            if (mBuilder.getWindow() != null) {
                mBuilder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }


    public void showLogoutDialog(OnDismissListener onDismissListener) {

        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(false);
        ItemPopupLogOutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_popup_log_out, null, false);
        mBuilder.setContentView(binding.getRoot());


        binding.btnCancel.setOnClickListener(v -> {
            mBuilder.dismiss();

        });
        binding.btnLogOut.setOnClickListener(v -> {
            mBuilder.dismiss();

            onDismissListener.onPositiveDismiss();
        });
        mBuilder.setOnDismissListener(dialog -> onDismissListener.onDismiss());

        mBuilder.show();


    }

    public void showPauseDownloadDialog(String title, OnDownloadDismissListener onDismissListener) {

        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(false);
        ItemPopupPauseDownloadBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_popup_pause_download, null, false);
        mBuilder.setContentView(binding.getRoot());
        binding.tvContentName.setText(title);

        binding.btnCancel.setOnClickListener(v -> {
            mBuilder.dismiss();
        });
        binding.btnPause.setOnClickListener(v -> {
            mBuilder.dismiss();
            onDismissListener.onTopDismiss();
        });

        binding.btnDelete.setOnClickListener(v -> {
            mBuilder.dismiss();
            onDismissListener.onDelete();
        });

        mBuilder.setOnDismissListener(dialog -> onDismissListener.onDismiss());

        mBuilder.show();


    }

    public void showDeleteDownloadDialog(String title, OnDownloadDismissListener onDismissListener) {

        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(false);
        ItemPopupDeleteDownloadBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_popup_delete_download, null, false);
        mBuilder.setContentView(binding.getRoot());
        binding.tvContentName.setText(title);

        binding.btnCancel.setOnClickListener(v -> {
            mBuilder.dismiss();

        });

        binding.btnDelete.setOnClickListener(v -> {
            mBuilder.dismiss();
            onDismissListener.onDelete();
        });

        mBuilder.setOnDismissListener(dialog -> onDismissListener.onDismiss());

        mBuilder.show();


    }

    public void showResumeDownloadDialog(String title, OnDownloadDismissListener onDismissListener) {

        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(false);
        ItemPopupResumeDownloadBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_popup_resume_download, null, false);
        mBuilder.setContentView(binding.getRoot());
        binding.tvContentName.setText(title);

        binding.btnCancel.setOnClickListener(v -> {
            mBuilder.dismiss();

        });
        binding.btnResume.setOnClickListener(v -> {
            mBuilder.dismiss();
            onDismissListener.onTopDismiss();
        });

        binding.btnDelete.setOnClickListener(v -> {
            mBuilder.dismiss();
            onDismissListener.onDelete();
        });

        mBuilder.setOnDismissListener(dialog -> onDismissListener.onDismiss());

        mBuilder.show();


    }

    public void showWatchDownloadDialog(String title, OnDownloadDismissListener onDismissListener) {

        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(false);
        ItemPopupWatchNowBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_popup_watch_now, null, false);
        mBuilder.setContentView(binding.getRoot());
        binding.tvContentName.setText(title);

        binding.btnCancel.setOnClickListener(v -> {
            mBuilder.dismiss();

        });
        binding.btnWatchNow.setOnClickListener(v -> {
            mBuilder.dismiss();
            onDismissListener.onTopDismiss();
        });

        binding.btnDelete.setOnClickListener(v -> {
            mBuilder.dismiss();
            onDismissListener.onDelete();
        });

        mBuilder.setOnDismissListener(dialog -> onDismissListener.onDismiss());
        mBuilder.show();


    }

    public void showDeleteDialog(OnDismissListener onDismissListener) {

        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(false);
        ItemPopupDeleteAccountBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_popup_delete_account, null, false);
        mBuilder.setContentView(binding.getRoot());


        binding.btnCancel.setOnClickListener(v -> {
            mBuilder.dismiss();

        });
        binding.btnDelete.setOnClickListener(v -> {
            mBuilder.dismiss();

            onDismissListener.onPositiveDismiss();
        });
        mBuilder.setOnDismissListener(dialog -> onDismissListener.onDismiss());

        mBuilder.show();


    }

    public void showPremiumDialog(OnDismissListener onDismissListener) {

        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(false);
        ItemPopupPremiumBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_popup_premium, null, false);

        mBuilder.setContentView(binding.getRoot());


        binding.btnCancel.setOnClickListener(v -> {
            mBuilder.dismiss();

        });
        binding.btnSubscribe.setOnClickListener(v -> {
            mBuilder.dismiss();

            onDismissListener.onPositiveDismiss();
        });

        mBuilder.setOnDismissListener(dialog -> onDismissListener.onDismiss());

        mBuilder.show();


    }

    public void showUnlockDialog(OnDismissListener onDismissListener) {

        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(false);
        ItemPopupLockedBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_popup_locked, null, false);
        mBuilder.setContentView(binding.getRoot());


        binding.btnCancel.setOnClickListener(v -> {
            mBuilder.dismiss();

        });
        binding.btnWatch.setOnClickListener(v -> {
            mBuilder.dismiss();

            onDismissListener.onPositiveDismiss();
        });
        mBuilder.setOnDismissListener(dialog -> onDismissListener.onDismiss());
        mBuilder.show();


    }

    public void showSubtitleDialog(int subtitlePosition, List<String> list, OnSubtitleDismiss onDismissListener) {

        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(true);
        ItemPopupSubtitleBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_popup_subtitle, null, false);
        mBuilder.setContentView(binding.getRoot());

        SubtitleLanguagesAdapter subtitleLanguagesAdapter = new SubtitleLanguagesAdapter();
        binding.rvLanguage.setAdapter(subtitleLanguagesAdapter);
        if (subtitlePosition < list.size() + 1) {
            binding.rvLanguage.scrollToPosition(subtitlePosition);
        }
        subtitleLanguagesAdapter.updateItems(list);

        subtitleLanguagesAdapter.onLanguageClick = new SubtitleLanguagesAdapter.OnLanguageClick() {
            @Override
            public void onClick(int position) {
                onDismissListener.onItemClick(position);
                mBuilder.dismiss();


            }
        };
        mBuilder.show();


    }

    public void showSimplePopup(boolean is_watchlist, String title, OnDismissListener onDismissListener) {

        if (mContext == null)
            return;

        mBuilder.setCancelable(true);
        mBuilder.setCanceledOnTouchOutside(false);
        ItemPopUpSimpleBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_pop_up_simple, null, false);
        mBuilder.setContentView(binding.getRoot());

        binding.title.setText(title);
        if (is_watchlist) {
            binding.img.setImageDrawable(AppCompatResources.getDrawable(mContext, R.drawable.ic_bookmark_not));
        }

        binding.btnCancel.setOnClickListener(v -> {
            mBuilder.dismiss();

        });
        binding.btnYes.setOnClickListener(v -> {
            mBuilder.dismiss();
            onDismissListener.onPositiveDismiss();
        });


        mBuilder.setOnDismissListener(dialog -> onDismissListener.onDismiss());

        mBuilder.show();


    }


    public void dismiss() {
        if (mBuilder != null) {
            mBuilder.dismiss();
        }
    }

    public interface OnSubtitleDismiss {
        void onItemClick(int position);


    }
    public interface OnDismissListener {
        void onPositiveDismiss();

        void onDismiss();

    }

    public interface OnDownloadDismissListener {
        void onTopDismiss();

        void onDelete();

        void onDismiss();



    }

}
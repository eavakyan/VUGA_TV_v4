package com.retry.vuga.bottomSheets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.retry.vuga.R;
import com.retry.vuga.databinding.BottomsheetWebBinding;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;

public class WebBottomSheet extends BottomSheetDialogFragment {
    BottomsheetWebBinding binding;
    int type;
    BottomSheetBehavior bottomSheetBehavior;
    View v;
    SessionManager sessionManager;

    public WebBottomSheet(int i) {
        this.type = i;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                setupFullHeight(bottomSheetDialog);
            }
        });
        return dialog;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v = LayoutInflater.from(getActivity()).inflate(R.layout.bottomsheet_web, container, false);
        sessionManager = new SessionManager(getActivity());


        binding = DataBindingUtil.bind(v);

        binding.getRoot().setNestedScrollingEnabled(true);

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.setBackgroundColor(requireActivity().getColor(R.color.app_black));

        if (type == 1) {

            binding.tvHeading.setText(getActivity().getString(R.string.privacy_policy));
            binding.webView.loadUrl(Const.PRIVACY_URL);

        } else if (type == 2) {

            binding.tvHeading.setText(getActivity().getString(R.string.terms_of_use));
            binding.webView.loadUrl(Const.TERMS_URL);

        }


        binding.btnClose.setOnClickListener(view -> {
            dismiss();
        });
        return binding.getRoot();
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = getWindowHeight();
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}

package com.retry.vuga.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.retry.vuga.R;

public class RatingDialog extends Dialog {

    private OnRatingSubmitListener listener;
    private String title;
    private String contentTitle;
    private int selectedRating = 0;
    
    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvRatingText;
    private TextView btnCancel;
    private TextView btnSubmit;
    
    private ImageView[] stars = new ImageView[5];
    private String[] ratingTexts = {
        "",
        "Poor",
        "Fair", 
        "Good",
        "Very Good",
        "Excellent"
    };

    public interface OnRatingSubmitListener {
        void onRatingSubmitted(int rating);
    }

    public RatingDialog(@NonNull Context context, String contentTitle) {
        super(context);
        this.contentTitle = contentTitle;
    }

    public void setOnRatingSubmitListener(OnRatingSubmitListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_rating);
        
        // Make dialog background transparent
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        initViews();
        setupStarClickListeners();
        setupButtonClickListeners();
        
        // Set title
        if (contentTitle != null && !contentTitle.isEmpty()) {
            tvTitle.setText("Rate \"" + contentTitle + "\"");
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvRatingText = findViewById(R.id.tv_rating_text);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSubmit = findViewById(R.id.btn_submit);
        
        stars[0] = findViewById(R.id.star_1);
        stars[1] = findViewById(R.id.star_2);
        stars[2] = findViewById(R.id.star_3);
        stars[3] = findViewById(R.id.star_4);
        stars[4] = findViewById(R.id.star_5);
    }

    private void setupStarClickListeners() {
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> selectRating(rating));
        }
    }

    private void selectRating(int rating) {
        selectedRating = rating;
        
        // Update star colors
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setColorFilter(getContext().getResources().getColor(R.color.app_color, null), PorterDuff.Mode.SRC_IN);
            } else {
                stars[i].setColorFilter(getContext().getResources().getColor(R.color.text_color_light, null), PorterDuff.Mode.SRC_IN);
            }
        }
        
        // Show rating text
        tvRatingText.setText(ratingTexts[rating]);
        tvRatingText.setVisibility(View.VISIBLE);
        
        // Show submit button
        btnSubmit.setVisibility(View.VISIBLE);
    }

    private void setupButtonClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSubmit.setOnClickListener(v -> {
            if (listener != null && selectedRating > 0) {
                // Convert 1-5 rating to 0-10 scale (multiply by 2)
                listener.onRatingSubmitted(selectedRating * 2);
            }
            dismiss();
        });
    }
}
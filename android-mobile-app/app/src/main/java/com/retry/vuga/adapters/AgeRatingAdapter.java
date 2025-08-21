package com.retry.vuga.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemAgeRatingBinding;
import com.retry.vuga.model.AgeRating;

import java.util.List;

public class AgeRatingAdapter extends RecyclerView.Adapter<AgeRatingAdapter.ViewHolder> {
    private List<AgeRating> ageRatings;
    private int selectedRatingId;
    private OnRatingClickListener listener;

    public interface OnRatingClickListener {
        void onRatingClick(AgeRating rating);
    }

    public AgeRatingAdapter(List<AgeRating> ageRatings, int selectedRatingId, OnRatingClickListener listener) {
        this.ageRatings = ageRatings;
        this.selectedRatingId = selectedRatingId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAgeRatingBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_age_rating,
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AgeRating rating = ageRatings.get(position);
        holder.bind(rating);
    }

    @Override
    public int getItemCount() {
        return ageRatings != null ? ageRatings.size() : 0;
    }

    public void setSelectedRatingId(int selectedRatingId) {
        int oldPosition = getPositionForRatingId(this.selectedRatingId);
        int newPosition = getPositionForRatingId(selectedRatingId);
        
        this.selectedRatingId = selectedRatingId;
        
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition);
        }
        if (newPosition != -1) {
            notifyItemChanged(newPosition);
        }
    }

    private int getPositionForRatingId(int ratingId) {
        for (int i = 0; i < ageRatings.size(); i++) {
            if (ageRatings.get(i).getId() == ratingId) {
                return i;
            }
        }
        return -1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemAgeRatingBinding binding;

        ViewHolder(ItemAgeRatingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AgeRating rating) {
            binding.setAgeRating(rating);
            binding.setIsSelected(rating.getId() == selectedRatingId);
            
            // Set the badge color
            try {
                binding.cardRating.setCardBackgroundColor(Color.parseColor(rating.getDisplayColor()));
            } catch (Exception e) {
                binding.cardRating.setCardBackgroundColor(Color.parseColor("#757575"));
            }
            
            // Set the title text
            String ageText = "Ages " + rating.getMinAge();
            if (rating.getMaxAge() != null) {
                ageText += "-" + rating.getMaxAge();
            } else if (rating.getMinAge() > 0) {
                ageText += "+";
            } else if (rating.getCode() != null && rating.getCode().equals("AG_ALL")) {
                ageText = "All Ages";
            }
            binding.tvRatingName.setText(ageText);
            
            // Set the description text
            String description = "Suitable for ";
            if (rating.getCode() != null && rating.getCode().equals("AG_ALL")) {
                description = "No content restrictions";
            } else if (rating.getMaxAge() != null) {
                description += "ages " + rating.getMinAge() + " to " + rating.getMaxAge();
            } else {
                description += "ages " + rating.getMinAge() + " and above";
            }
            if (rating.getDescription() != null && !rating.getDescription().isEmpty()) {
                description += " (" + rating.getDescription() + ")";
            }
            binding.tvRatingDescription.setText(description);
            
            // Handle click
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRatingClick(rating);
                }
            });
            
            binding.radioButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRatingClick(rating);
                }
            });
            
            binding.executePendingBindings();
        }
    }
}
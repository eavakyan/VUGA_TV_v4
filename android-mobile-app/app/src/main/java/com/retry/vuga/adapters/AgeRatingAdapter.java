package com.retry.vuga.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemAgeRatingBinding;
import com.retry.vuga.model.AgeRating;
import com.retry.vuga.model.Profile;

import java.util.List;

public class AgeRatingAdapter extends RecyclerView.Adapter<AgeRatingAdapter.ViewHolder> {

    private List<AgeRating> ageRatings;
    private Profile profile;

    public AgeRatingAdapter(List<AgeRating> ageRatings, Profile profile) {
        this.ageRatings = ageRatings;
        this.profile = profile;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAgeRatingBinding binding = ItemAgeRatingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AgeRating rating = ageRatings.get(position);
        holder.bind(rating);
    }

    @Override
    public int getItemCount() {
        return ageRatings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemAgeRatingBinding binding;

        ViewHolder(ItemAgeRatingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AgeRating rating) {
            // Rating badge
            binding.tvRatingCode.setText(rating.getCode().replace("AG_", ""));
            try {
                binding.cardRating.setCardBackgroundColor(Color.parseColor(rating.getDisplayColor()));
            } catch (Exception e) {
                binding.cardRating.setCardBackgroundColor(Color.parseColor("#757575"));
            }

            // Rating info
            binding.tvRatingName.setText(rating.getName());
            if (rating.getDescription() != null && !rating.getDescription().isEmpty()) {
                binding.tvRatingDescription.setText(rating.getDescription());
                binding.tvRatingDescription.setVisibility(View.VISIBLE);
            } else {
                binding.tvRatingDescription.setVisibility(View.GONE);
            }

            // Check accessibility
            boolean isAccessible = canAccessRating(rating);
            
            // Update UI based on accessibility
            if (isAccessible) {
                binding.ivAccessStatus.setImageResource(R.drawable.ic_tick);
                binding.ivAccessStatus.setColorFilter(Color.parseColor("#4CAF50"));
                binding.getRoot().setAlpha(1.0f);
            } else {
                binding.ivAccessStatus.setImageResource(R.drawable.ic_lock);
                binding.ivAccessStatus.setColorFilter(Color.parseColor("#F44336"));
                binding.getRoot().setAlpha(0.6f);
            }

            // Set text colors based on accessibility
            int textColor = isAccessible ? 
                binding.getRoot().getContext().getResources().getColor(R.color.text_color) :
                binding.getRoot().getContext().getResources().getColor(R.color.text_color_light);
            binding.tvRatingName.setTextColor(textColor);
        }

        private boolean canAccessRating(AgeRating rating) {
            // Kids profiles can only access content for ages 12 and under
            if (profile.isKids()) {
                return rating.isKidsFriendly();
            }

            // If profile has age set, check if it meets the minimum age requirement
            if (profile.getAge() != null && profile.getAge() > 0) {
                return profile.getAge() >= rating.getMinAge();
            }

            // No age restriction if age not set
            return true;
        }
    }
}
package com.retry.vuga.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.retry.vuga.R;
import com.retry.vuga.model.Profile;
import com.retry.vuga.utils.Const;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private Context context;
    private List<Profile> profileList;
    private OnProfileClickListener listener;
    private boolean isEditMode = false;

    public interface OnProfileClickListener {
        void onProfileClick(Profile profile);
        void onDeleteClick(Profile profile);
    }

    public ProfileAdapter(Context context, List<Profile> profileList, OnProfileClickListener listener) {
        this.context = context;
        this.profileList = profileList;
        this.listener = listener;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profileList.get(position);
        
        holder.tvProfileName.setText(profile.getName());
        
        // Debug logging
        android.util.Log.d("ProfileAdapter", "Profile: " + profile.getName() + 
            ", AvatarType: " + profile.getAvatarType() + 
            ", AvatarUrl: " + profile.getAvatarUrl() +
            ", AvatarColor: " + profile.getAvatarColor());
        
        // Check if profile has a custom image URL
        String avatarUrl = profile.getAvatarUrl();
        String avatarType = profile.getAvatarType();
        
        // Only show image if it's a custom avatar with a valid S3 URL
        // Default avatars like "avatars/avatar_blue.png" should show color instead
        boolean hasValidCustomImage = avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals("null") 
            && (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://"));
            
        if (hasValidCustomImage) {
            // Custom uploaded image
            android.util.Log.d("ProfileAdapter", "Showing image for " + profile.getName() + ": " + avatarUrl);
            
            holder.cardImageHolder.setVisibility(View.VISIBLE);
            holder.viewColorAvatar.setVisibility(View.GONE);
            holder.tvInitial.setVisibility(View.GONE);
            
            // URL is already complete from API, just add cache busting
            String imageUrl = avatarUrl;
            if (!imageUrl.contains("?")) {
                imageUrl = imageUrl + "?t=" + System.currentTimeMillis();
            }
            
            android.util.Log.d("ProfileAdapter", "Final image URL: " + imageUrl);
            
            // Use RequestOptions for better control - don't use circleCrop since CardView handles the rounding
            com.bumptech.glide.request.RequestOptions requestOptions = new com.bumptech.glide.request.RequestOptions()
                .centerCrop()
                .skipMemoryCache(true)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .signature(new com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis()));
            
            Glide.with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .into(holder.imgProfile);
        } else {
            // Color avatar (default or when no image)
            android.util.Log.d("ProfileAdapter", "Showing color avatar for " + profile.getName());
            
            holder.cardImageHolder.setVisibility(View.GONE);
            holder.viewColorAvatar.setVisibility(View.VISIBLE);
            holder.tvInitial.setVisibility(View.VISIBLE);
            
            String avatarColor = profile.getAvatarColor();
            if (avatarColor == null || avatarColor.isEmpty() || avatarColor.equals("null")) {
                // Generate a color based on the name
                String[] colors = {"#FF5252", "#FF9800", "#4CAF50", "#2196F3", "#9C27B0", "#00BCD4"};
                int colorIndex = Math.abs(profile.getName().hashCode()) % colors.length;
                avatarColor = colors[colorIndex];
            }
            
            try {
                holder.viewColorAvatar.setCardBackgroundColor(Color.parseColor(avatarColor));
            } catch (Exception e) {
                holder.viewColorAvatar.setCardBackgroundColor(Color.parseColor("#FF5252"));
            }
            
            // Set initials - first letter of up to 2 words
            String name = profile.getName();
            String initials = generateInitials(name);
            holder.tvInitial.setText(initials);
        }
        
        // Show/hide delete button based on edit mode
        holder.imgDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        
        // Show kids badge if it's a kids profile
        holder.tvKidsBadge.setVisibility(profile.isKids() ? View.VISIBLE : View.GONE);
        
        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProfileClick(profile);
            }
        });
        
        holder.imgDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(profile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }
    
    private String generateInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "P";
        }
        
        String trimmedName = name.trim();
        String[] words = trimmedName.split("\\s+");
        
        if (words.length == 0) {
            return "P";
        } else if (words.length == 1) {
            // Single word - take first letter
            return words[0].substring(0, 1).toUpperCase();
        } else {
            // Multiple words - take first letter of first two words
            String firstInitial = words[0].substring(0, 1).toUpperCase();
            String secondInitial = words[1].substring(0, 1).toUpperCase();
            return firstInitial + secondInitial;
        }
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile, imgDelete;
        TextView tvProfileName, tvInitial, tvKidsBadge;
        CardView viewColorAvatar, cardImageHolder;

        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            tvProfileName = itemView.findViewById(R.id.tvProfileName);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvKidsBadge = itemView.findViewById(R.id.tvKidsBadge);
            viewColorAvatar = itemView.findViewById(R.id.viewColorAvatar);
            cardImageHolder = itemView.findViewById(R.id.cardImageHolder);
        }
    }
}
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
        
        // Set avatar based on type
        // Backend returns "default" for color avatars, "custom" for uploaded images
        if ("default".equals(profile.getAvatarType()) || "color".equals(profile.getAvatarType())) {
            // Color avatar
            holder.imgProfile.setVisibility(View.GONE);
            holder.viewColorAvatar.setVisibility(View.VISIBLE);
            holder.tvInitial.setVisibility(View.VISIBLE);
            
            try {
                holder.viewColorAvatar.setCardBackgroundColor(Color.parseColor(profile.getAvatarColor()));
            } catch (Exception e) {
                holder.viewColorAvatar.setCardBackgroundColor(Color.parseColor("#FF5252"));
            }
            
            // Set initial
            String initial = profile.getName().substring(0, 1).toUpperCase();
            holder.tvInitial.setText(initial);
        } else if ("custom".equals(profile.getAvatarType()) && !profile.getAvatarUrl().isEmpty()) {
            // Custom uploaded image
            holder.imgProfile.setVisibility(View.VISIBLE);
            holder.viewColorAvatar.setVisibility(View.GONE);
            holder.tvInitial.setVisibility(View.GONE);
            
            Glide.with(context)
                .load(profile.getAvatarUrl()) // Already has full URL for custom avatars
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(holder.imgProfile);
        } else {
            // Fallback to color avatar
            holder.imgProfile.setVisibility(View.GONE);
            holder.viewColorAvatar.setVisibility(View.VISIBLE);
            holder.tvInitial.setVisibility(View.VISIBLE);
            
            holder.viewColorAvatar.setCardBackgroundColor(Color.parseColor("#FF5252"));
            String initial = profile.getName().substring(0, 1).toUpperCase();
            holder.tvInitial.setText(initial);
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

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile, imgDelete;
        TextView tvProfileName, tvInitial, tvKidsBadge;
        CardView viewColorAvatar;

        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            tvProfileName = itemView.findViewById(R.id.tvProfileName);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvKidsBadge = itemView.findViewById(R.id.tvKidsBadge);
            viewColorAvatar = itemView.findViewById(R.id.viewColorAvatar);
        }
    }
}
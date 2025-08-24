package com.retry.vuga.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.retry.vuga.R;
import com.retry.vuga.model.LiveTvChannel;
import com.retry.vuga.utils.GlideLoader;
import java.util.ArrayList;
import java.util.List;

public class LiveTvChannelGridAdapter extends RecyclerView.Adapter<LiveTvChannelGridAdapter.ChannelViewHolder> {
    
    private List<LiveTvChannel> channels = new ArrayList<>();
    private OnChannelClickListener listener;
    
    public interface OnChannelClickListener {
        void onChannelClick(LiveTvChannel channel);
    }
    
    public LiveTvChannelGridAdapter(OnChannelClickListener listener) {
        this.listener = listener;
    }
    
    public void updateChannels(List<LiveTvChannel> channels) {
        this.channels = channels != null ? channels : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_live_tv_channel_grid, parent, false);
        return new ChannelViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        LiveTvChannel channel = channels.get(position);
        holder.bind(channel);
    }
    
    @Override
    public int getItemCount() {
        return channels.size();
    }
    
    class ChannelViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView ivChannelLogo;
        private final TextView tvChannelNumber;
        private final TextView tvChannelName;
        private final TextView tvCurrentProgram;
        private final ImageView ivBadge;
        private final Context context;
        
        ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            cardView = itemView.findViewById(R.id.card_channel);
            ivChannelLogo = itemView.findViewById(R.id.iv_channel_logo);
            tvChannelNumber = itemView.findViewById(R.id.tv_channel_number);
            tvChannelName = itemView.findViewById(R.id.tv_channel_name);
            tvCurrentProgram = itemView.findViewById(R.id.tv_current_program);
            ivBadge = itemView.findViewById(R.id.iv_badge);
        }
        
        void bind(LiveTvChannel channel) {
            // Load channel logo or thumbnail
            String imageUrl = channel.getThumbnail();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = channel.getLogoUrl();
            }
            
            if (imageUrl != null && !imageUrl.isEmpty()) {
                GlideLoader.loadIntoImageView(context, imageUrl, ivChannelLogo, R.drawable.ic_tv);
            } else {
                ivChannelLogo.setImageResource(R.drawable.ic_tv);
            }
            
            // Set channel number
            if (channel.getChannelNumber() > 0) {
                tvChannelNumber.setText(String.valueOf(channel.getChannelNumber()));
                tvChannelNumber.setVisibility(View.VISIBLE);
            } else {
                tvChannelNumber.setVisibility(View.GONE);
            }
            
            // Set channel name
            tvChannelName.setText(channel.getTitle());
            
            // Set current program
            tvCurrentProgram.setText(channel.getCurrentProgramTitle());
            
            // Set badge based on access type
            if (channel.isPremium()) {
                ivBadge.setImageResource(R.drawable.ic_crown);
                ivBadge.setVisibility(View.VISIBLE);
            } else if (channel.requiresAds()) {
                ivBadge.setImageResource(R.drawable.ic_play_circle);
                ivBadge.setVisibility(View.VISIBLE);
            } else {
                ivBadge.setVisibility(View.GONE);
            }
            
            // Set click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChannelClick(channel);
                }
            });
        }
    }
}
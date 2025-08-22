package com.retry.vuga.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.retry.vuga.R;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.utils.Const;

import java.util.ArrayList;
import java.util.List;

public class MoreEpisodesAdapter extends RecyclerView.Adapter<MoreEpisodesAdapter.ViewHolder> {
    
    private List<ContentDetail.SeasonItem.EpisodesItem> episodes = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    
    public interface OnItemClickListener {
        void onItemClick(ContentDetail.SeasonItem.EpisodesItem episode, int position);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public void setEpisodes(List<ContentDetail.SeasonItem.EpisodesItem> episodes) {
        this.episodes = episodes;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_more_episode_horizontal, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContentDetail.SeasonItem.EpisodesItem episode = episodes.get(position);
        
        // Set episode title
        String title = "Episode " + episode.getNumber();
        if (episode.getTitle() != null && !episode.getTitle().isEmpty()) {
            title = episode.getTitle();
        }
        holder.tvTitle.setText(title);
        
        // Set episode number
        holder.tvEpisodeNumber.setText("E" + episode.getNumber());
        
        // Set duration
        if (episode.getFormattedDuration() != null && !episode.getFormattedDuration().isEmpty()) {
            holder.tvDuration.setText(episode.getFormattedDuration());
            holder.tvDuration.setVisibility(View.VISIBLE);
        } else {
            holder.tvDuration.setVisibility(View.GONE);
        }
        
        // Load thumbnail
        String thumbnailUrl = episode.getThumbnail();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            if (!thumbnailUrl.startsWith("http")) {
                thumbnailUrl = Const.BASE + thumbnailUrl;
            }
            Glide.with(holder.itemView.getContext())
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(holder.ivThumbnail);
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(episode, position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return episodes.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle;
        TextView tvEpisodeNumber;
        TextView tvDuration;
        
        ViewHolder(View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvEpisodeNumber = itemView.findViewById(R.id.tv_episode_number);
            tvDuration = itemView.findViewById(R.id.tv_duration);
        }
    }
}
package com.retry.vuga.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.activities.MovieDetailActivity;
import com.retry.vuga.databinding.ItemRecentlyWatchedApiBinding;
import com.retry.vuga.model.RecentlyWatchedContent;
import com.retry.vuga.utils.Const;

import java.util.ArrayList;
import java.util.List;

public class RecentlyWatchedAPIAdapter extends RecyclerView.Adapter<RecentlyWatchedAPIAdapter.ItemHolder> {

    private List<RecentlyWatchedContent.DataItem> list = new ArrayList<>();

    public List<RecentlyWatchedContent.DataItem> getList() {
        return list;
    }

    public void setList(List<RecentlyWatchedContent.DataItem> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recently_watched_api, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItems(List<RecentlyWatchedContent.DataItem> newList) {
        this.list = new ArrayList<>();
        notifyDataSetChanged();
        if (newList != null) {
            for (int i = 0; i < newList.size(); i++) {
                this.list.add(newList.get(i));
                notifyItemInserted(this.list.size() - 1);
            }
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemRecentlyWatchedApiBinding binding;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void setData(int position) {
            RecentlyWatchedContent.DataItem item = list.get(position);

            if (binding != null) {
                // Use display poster (episode thumbnail for episodes, regular poster for content)
                binding.setContent(item);
                
                // Override the image URL to use episode thumbnail if available
                if (item.isEpisode() && item.getEpisodeThumbnail() != null && !item.getEpisodeThumbnail().isEmpty()) {
                    // This will require updating the layout binding
                }

                // Format metadata line 1: Year and duration
                String metadataLine1 = "";
                if (item.isEpisode()) {
                    // For episodes, show series title and episode duration
                    if (item.getSeriesTitle() != null && !item.getSeriesTitle().isEmpty()) {
                        metadataLine1 = item.getSeriesTitle();
                    }
                    String duration = item.getDisplayDuration();
                    if (!duration.isEmpty()) {
                        if (!metadataLine1.isEmpty()) {
                            metadataLine1 += " • ";
                        }
                        metadataLine1 += duration;
                    }
                } else {
                    // For movies/shows, show year and duration
                    metadataLine1 = item.getYearString();
                    String duration = item.getDisplayDuration();
                    if (!duration.isEmpty()) {
                        metadataLine1 += " • " + duration;
                    }
                }
                binding.tvMetadataLine1.setText(metadataLine1);

                // Format metadata line 2: Display title (includes S#E# for episodes)
                binding.tvMetadataLine2.setText(item.getDisplayTitle());

                // Set click listener
                binding.getRoot().setOnClickListener(v -> {
                    if (item.isEpisode()) {
                        // For episodes, navigate to Episode Detail View
                        // We need to fetch the episode data first or pass minimal data
                        Intent intent = new Intent(itemView.getContext(), MovieDetailActivity.class);
                        intent.putExtra(Const.DataKey.CONTENT_ID, item.getContentId());
                        intent.putExtra("EPISODE_ID", item.getEpisodeId());
                        intent.putExtra("FROM_RECENTLY_WATCHED", true);
                        intent.putExtra("WATCH_PROGRESS", item.getWatchPosition());
                        itemView.getContext().startActivity(intent);
                    } else {
                        // For movies/shows, navigate to Movie Detail View
                        Intent intent = new Intent(itemView.getContext(), MovieDetailActivity.class);
                        intent.putExtra(Const.DataKey.CONTENT_ID, item.getContentId());
                        intent.putExtra("FROM_RECENTLY_WATCHED", true);
                        intent.putExtra("WATCH_PROGRESS", item.getWatchPosition());
                        itemView.getContext().startActivity(intent);
                    }
                });
            }
        }
    }
}
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
                binding.setContent(item);

                // Format metadata line 1: Year and duration
                String metadataLine1 = item.getYearString();
                String duration = item.getFormattedDuration();
                if (!duration.isEmpty()) {
                    metadataLine1 += " • " + duration;
                }
                binding.tvMetadataLine1.setText(metadataLine1);

                // Format metadata line 2: Title
                binding.tvMetadataLine2.setText(item.getTitle());

                // Set click listener
                binding.getRoot().setOnClickListener(v -> {
                    Intent intent = new Intent(itemView.getContext(), MovieDetailActivity.class);
                    intent.putExtra(Const.DataKey.CONTENT_ID, item.getContentId());
                    itemView.getContext().startActivity(intent);
                });
            }
        }
    }
}
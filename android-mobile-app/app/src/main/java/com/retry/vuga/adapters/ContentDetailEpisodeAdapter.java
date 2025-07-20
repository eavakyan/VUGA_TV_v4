package com.retry.vuga.adapters;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemContentEpisodeBinding;
import com.retry.vuga.model.ContentDetail;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ContentDetailEpisodeAdapter extends RecyclerView.Adapter<ContentDetailEpisodeAdapter.ItemHolder> {
    List<ContentDetail.SeasonItem.EpisodesItem> list = new ArrayList<>();
    OnEpisodeClick onEpisodeClick;

    public OnEpisodeClick getOnEpisodeClick() {
        return onEpisodeClick;
    }

    public void setOnEpisodeClick(OnEpisodeClick onEpisodeClick) {
        this.onEpisodeClick = onEpisodeClick;
    }

    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content_episode, parent, false);
        return new ContentDetailEpisodeAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {
        holder.setItems(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public void updateItems(List<ContentDetail.SeasonItem.EpisodesItem> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public interface OnEpisodeClick {
        void onClick(ContentDetail.SeasonItem.EpisodesItem model, int position);
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemContentEpisodeBinding binding;

        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void setItems(int position) {

            ContentDetail.SeasonItem.EpisodesItem model = list.get(position);

            binding.tvName.setSelected(true);

            binding.setContent(model);


            binding.tvDes.setOnClickListener(v -> {
                if (getElipsized()) {
                    binding.tvDes.setMaxLines(20);
                } else {
                    binding.tvDes.setMaxLines(3);

                }
            });

            binding.getRoot().setOnClickListener(v -> {
                onEpisodeClick.onClick(model, position);

            });

        }

        private boolean getElipsized() {

            Layout l = binding.tvDes.getLayout();
            if (l != null) {
                int lines = l.getLineCount();
                if (lines > 0)
                    if (l.getEllipsisCount(lines - 1) > 0) {
                        return true;
                    }
            }

            return false;
        }
    }
}

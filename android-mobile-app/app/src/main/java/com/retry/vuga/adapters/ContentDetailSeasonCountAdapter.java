package com.retry.vuga.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemContentSeasonBinding;
import com.retry.vuga.model.ContentDetail;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ContentDetailSeasonCountAdapter extends RecyclerView.Adapter<ContentDetailSeasonCountAdapter.ItemHolder> {
    List<ContentDetail.SeasonItem> list = new ArrayList<>();
    OnItemClick onItemClick;
    int selected = 0;

    public OnItemClick getOnItemClick() {
        return onItemClick;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content_season, parent, false);
        return new ContentDetailSeasonCountAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {
        holder.setItems(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItems(List<ContentDetail.SeasonItem> list) {
        this.list = list;
        notifyItemRangeInserted(0, list.size());
    }

    public interface OnItemClick {
        void onClick(ContentDetail.SeasonItem model, int position);
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemContentSeasonBinding binding;


        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void setItems(int position) {

            ContentDetail.SeasonItem model = list.get(position);

            binding.setSelected(position == selected);
            binding.tvCount.setText(" " + (position + 1));

            binding.getRoot().setOnClickListener(v -> {

                selected = position;
                notifyDataSetChanged();
                onItemClick.onClick(model, position);

            });


        }
    }
}

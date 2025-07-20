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
import com.retry.vuga.databinding.ItemWatchlistBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.Global;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

;

public class WatchListAdapter extends RecyclerView.Adapter<WatchListAdapter.ItemHolder> {

    List<ContentDetail.DataItem> list = new ArrayList<>();
    OnItemClick onItemClick;

    public List<ContentDetail.DataItem> getList() {
        return list;
    }

    public void setOnItemClick(WatchListAdapter.OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }


    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watchlist, parent, false);
        return new WatchListAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItems(List<ContentDetail.DataItem> list) {
        this.list = list;
        notifyItemRangeInserted(0, list.size());
    }

    public void loadMoreItems(List<ContentDetail.DataItem> list) {


        for (int i = 0; i < list.size(); i++) {
            this.list.add(list.get(i));
            notifyItemInserted(this.list.size() - 1);

        }

    }

    public void clear() {


        int size = list.size();
        list.clear();
        notifyItemRangeRemoved(0, size);

    }

    public interface OnItemClick {
        void onRemoveClick(ContentDetail.DataItem model);


    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemWatchlistBinding binding;

        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void setData(int position) {

            ContentDetail.DataItem model = list.get(position);

            binding.setContent(model);
            if (model.getGenreString().isEmpty()) {
                String s = Global.getGenreStringFromIds(model.getGenreIds(), itemView.getContext());
                binding.tvGenre.setText(s);
                model.setGenreString(s);
            } else {
                binding.tvGenre.setText(model.getGenreString());

            }

            binding.getRoot().setOnClickListener(v -> {

                Intent intent = new Intent(itemView.getContext(), MovieDetailActivity.class);
                intent.putExtra(Const.DataKey.CONTENT_ID, model.getId());
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    itemView.getContext().startActivity(intent);

            });

            binding.btnWatchList.setOnClickListener(v1 -> {
                onItemClick.onRemoveClick(model);


            });

        }
    }
}

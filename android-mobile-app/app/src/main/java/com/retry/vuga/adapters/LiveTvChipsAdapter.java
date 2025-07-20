package com.retry.vuga.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.activities.ChannelByCategoriesActivity;
import com.retry.vuga.databinding.ItemLivetvChipsBinding;
import com.retry.vuga.model.LiveTv;
import com.retry.vuga.utils.Const;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LiveTvChipsAdapter extends RecyclerView.Adapter<LiveTvChipsAdapter.ItemHolder> {

    List<LiveTv.CategoryItem> list = new ArrayList<>();


    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_livetv_chips, parent, false);
        return new LiveTvChipsAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItems(List<LiveTv.CategoryItem> list) {
        this.list = list;
        notifyItemRangeInserted(0, list.size());
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemLivetvChipsBinding binding;


        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);

        }

        public void setData(int position) {

            LiveTv.CategoryItem model = list.get(position);

            binding.setModel(model);

            binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), ChannelByCategoriesActivity.class);
                intent.putExtra(Const.DataKey.CAT_ID, model.getId());
                intent.putExtra(Const.DataKey.CAT_NAME, model.getTitle());
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}

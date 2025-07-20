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
import com.retry.vuga.databinding.ItemHomeCatItemBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.utils.Const;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

;

public class HomeCatObjectAdapter extends RecyclerView.Adapter<HomeCatObjectAdapter.ItemHolder> {

    List<ContentDetail.DataItem> list = new ArrayList<>();


    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_cat_item, parent, false);
        return new HomeCatObjectAdapter.ItemHolder(view);
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

    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemHomeCatItemBinding binding;

        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void setData(int position) {
            ContentDetail.DataItem model = list.get(position);


            binding.setContent(model);

            binding.getRoot().setOnClickListener(v -> {


                Intent intent = new Intent(itemView.getContext(), MovieDetailActivity.class);
                intent.putExtra(Const.DataKey.CONTENT_ID, model.getId());
                    itemView.getContext().startActivity(intent);


            });


        }
    }
}

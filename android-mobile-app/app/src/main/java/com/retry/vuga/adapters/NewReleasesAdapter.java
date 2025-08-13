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
import com.retry.vuga.databinding.ItemNewReleasesBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.utils.Const;

import java.util.ArrayList;
import java.util.List;

public class NewReleasesAdapter extends RecyclerView.Adapter<NewReleasesAdapter.ItemHolder> {

    List<ContentDetail.DataItem> list = new ArrayList<>();

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_releases, parent, false);
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

    public void updateItems(List<ContentDetail.DataItem> list) {
        this.list = new ArrayList<>();
        notifyDataSetChanged();
        for (int i = 0; i < list.size(); i++) {
            this.list.add(list.get(i));
            notifyItemInserted(this.list.size() - 1);
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemNewReleasesBinding binding;

        public ItemHolder(@NonNull View itemView) {
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
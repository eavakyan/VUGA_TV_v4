package com.retry.vuga.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemGenreChipsBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ContentDetailGenreAdapter extends RecyclerView.Adapter<ContentDetailGenreAdapter.ItemHolder> {

    List<String> list = new ArrayList<>();

    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genre_chips, parent, false);
        return new ContentDetailGenreAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {
        holder.setItems(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItems(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    public class ItemHolder extends RecyclerView.ViewHolder {
        ItemGenreChipsBinding binding;

        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void setItems(int position) {

            binding.tvName.setText(list.get(position));

            if (position == list.size() - 1) {
                binding.view.setVisibility(View.GONE);
            } else {
                binding.view.setVisibility(View.VISIBLE);

            }

        }
    }
}

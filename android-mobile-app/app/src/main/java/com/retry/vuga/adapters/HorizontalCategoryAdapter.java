package com.retry.vuga.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemHorizontalCategoryBinding;
import com.retry.vuga.model.HomePage;

import java.util.ArrayList;
import java.util.List;

public class HorizontalCategoryAdapter extends RecyclerView.Adapter<HorizontalCategoryAdapter.ViewHolder> {

    private List<HomePage.GenreContents> genreList = new ArrayList<>();
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(HomePage.GenreContents genre, int position);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void updateGenres(List<HomePage.GenreContents> genres) {
        this.genreList.clear();
        this.genreList.addAll(genres);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHorizontalCategoryBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_horizontal_category,
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomePage.GenreContents genre = genreList.get(position);
        
        holder.binding.setGenre(genre);
        holder.binding.setIsSelected(false); // Never show as selected
        holder.binding.executePendingBindings();

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(genre, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return genreList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemHorizontalCategoryBinding binding;

        ViewHolder(ItemHorizontalCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
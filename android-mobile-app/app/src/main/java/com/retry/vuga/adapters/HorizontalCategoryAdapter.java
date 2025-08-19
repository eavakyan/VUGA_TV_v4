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

    private List<Object> itemList = new ArrayList<>();  // Can hold both static items and genres
    private OnCategoryClickListener listener;
    private OnStaticItemClickListener staticItemListener;

    // Static navigation item types
    public static class StaticNavItem {
        public String title;
        public String type;
        
        public StaticNavItem(String title, String type) {
            this.title = title;
            this.type = type;
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(HomePage.GenreContents genre, int position);
    }
    
    public interface OnStaticItemClickListener {
        void onStaticItemClick(String itemType);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnStaticItemClickListener(OnStaticItemClickListener listener) {
        this.staticItemListener = listener;
    }

    public void updateGenres(List<HomePage.GenreContents> genres) {
        this.itemList.clear();
        
        // Add static navigation items first
        itemList.add(new StaticNavItem("TV Shows", "tv_shows"));
        itemList.add(new StaticNavItem("Movies", "movies"));
        itemList.add(new StaticNavItem("Live TV", "live_tv"));
        
        // Then add genre items
        itemList.addAll(genres);
        
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
        Object item = itemList.get(position);
        
        if (item instanceof StaticNavItem) {
            // Handle static navigation items
            StaticNavItem navItem = (StaticNavItem) item;
            HomePage.GenreContents dummyGenre = new HomePage.GenreContents();
            dummyGenre.setTitle(navItem.title);
            holder.binding.setGenre(dummyGenre);
            holder.binding.setIsSelected(false);
            holder.binding.executePendingBindings();
            
            holder.itemView.setOnClickListener(v -> {
                android.util.Log.d("HorizontalCategoryAdapter", "Static item clicked: " + navItem.title + " type: " + navItem.type);
                if (staticItemListener != null) {
                    android.util.Log.d("HorizontalCategoryAdapter", "Calling listener for: " + navItem.type);
                    staticItemListener.onStaticItemClick(navItem.type);
                } else {
                    android.util.Log.d("HorizontalCategoryAdapter", "Static item listener is null!");
                }
            });
        } else if (item instanceof HomePage.GenreContents) {
            // Handle regular genre items
            HomePage.GenreContents genre = (HomePage.GenreContents) item;
            holder.binding.setGenre(genre);
            holder.binding.setIsSelected(false);
            holder.binding.executePendingBindings();

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(genre, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemHorizontalCategoryBinding binding;

        ViewHolder(ItemHorizontalCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
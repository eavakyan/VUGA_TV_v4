package com.retry.vuga.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.retry.vuga.R;
import com.retry.vuga.model.LiveTvCategory;
import java.util.ArrayList;
import java.util.List;

public class LiveTvCategoryAdapter extends RecyclerView.Adapter<LiveTvCategoryAdapter.CategoryViewHolder> {
    
    private List<LiveTvCategory> categories = new ArrayList<>();
    private OnCategoryClickListener listener;
    
    public interface OnCategoryClickListener {
        void onCategoryClick(LiveTvCategory category);
    }
    
    public LiveTvCategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }
    
    public void updateCategories(List<LiveTvCategory> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_live_tv_category_chip, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        LiveTvCategory category = categories.get(position);
        holder.bind(category);
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final Chip chip;
        
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.chip_category);
        }
        
        void bind(LiveTvCategory category) {
            chip.setText(category.getName());
            chip.setChecked(category.isSelected());
            
            // Show channel count if available
            if (category.getChannelCount() > 0) {
                chip.setText(category.getName() + " (" + category.getChannelCount() + ")");
            }
            
            chip.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }
    }
}
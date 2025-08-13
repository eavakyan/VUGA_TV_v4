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
import com.retry.vuga.databinding.ItemContentGridBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.utils.Const;
import java.util.ArrayList;
import java.util.List;

public class ContentGridAdapter extends RecyclerView.Adapter<ContentGridAdapter.ItemHolder> {

    private List<ContentDetail.DataItem> list = new ArrayList<>();

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content_grid, parent, false);
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
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
    }
    
    public void loadMoreItems(List<ContentDetail.DataItem> newItems) {
        int startPosition = this.list.size();
        this.list.addAll(newItems);
        notifyItemRangeInserted(startPosition, newItems.size());
    }
    
    public void clear() {
        this.list.clear();
        notifyDataSetChanged();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemContentGridBinding binding;

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
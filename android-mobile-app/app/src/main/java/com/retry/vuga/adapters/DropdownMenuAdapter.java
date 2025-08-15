package com.retry.vuga.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;

import java.util.ArrayList;
import java.util.List;

public class DropdownMenuAdapter extends RecyclerView.Adapter<DropdownMenuAdapter.ViewHolder> {
    
    public static class DropdownItem {
        public String title;
        public String subtitle;
        public String type; // "genre" or "distributor"
        public Object data; // Can hold genre or distributor data
        
        public DropdownItem(String title, String type, Object data) {
            this.title = title;
            this.type = type;
            this.data = data;
        }
        
        public DropdownItem(String title, String subtitle, String type, Object data) {
            this.title = title;
            this.subtitle = subtitle;
            this.type = type;
            this.data = data;
        }
    }
    
    private List<DropdownItem> items = new ArrayList<>();
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onItemClick(DropdownItem item, int position);
    }
    
    public void setItems(List<DropdownItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dropdown_menu, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DropdownItem item = items.get(position);
        holder.bind(item, position);
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvSubtitle;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvSubtitle = itemView.findViewById(R.id.tvItemSubtitle);
        }
        
        public void bind(DropdownItem item, int position) {
            tvTitle.setText(item.title);
            
            if (item.subtitle != null && !item.subtitle.isEmpty()) {
                tvSubtitle.setText(item.subtitle);
                tvSubtitle.setVisibility(View.VISIBLE);
            } else {
                tvSubtitle.setVisibility(View.GONE);
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item, position);
                }
            });
        }
    }
}
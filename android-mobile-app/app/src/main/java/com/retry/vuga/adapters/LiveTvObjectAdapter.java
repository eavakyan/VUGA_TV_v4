package com.retry.vuga.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.retry.vuga.R;
import com.retry.vuga.model.LiveTv;
import java.util.ArrayList;
import java.util.List;

/**
 * Legacy adapter for Live TV channels - kept for backward compatibility
 */
public class LiveTvObjectAdapter extends RecyclerView.Adapter<LiveTvObjectAdapter.ViewHolder> {
    
    private Context context;
    private List<LiveTv.CategoryItem.TvChannelItem> channels = new ArrayList<>();
    private OnItemClick listener;
    
    // Legacy interface for backward compatibility
    public interface OnItemClick {
        void onClick(LiveTv.CategoryItem.TvChannelItem channel);
    }
    
    public interface OnChannelClickListener {
        void onChannelClick(LiveTv.CategoryItem.TvChannelItem channel);
    }
    
    public LiveTvObjectAdapter(Context context) {
        this.context = context;
    }
    
    // Legacy constructor that accepts context or int
    public LiveTvObjectAdapter(Object contextOrInt) {
        if (contextOrInt instanceof Context) {
            this.context = (Context) contextOrInt;
        } else if (contextOrInt instanceof Integer) {
            // Legacy usage - will need context from parent activity
            this.context = null;
        }
    }
    
    public void setOnItemClick(OnItemClick listener) {
        this.listener = listener;
    }
    
    public void setOnChannelClickListener(OnChannelClickListener listener) {
        // Convert to legacy interface
        this.listener = new OnItemClick() {
            @Override
            public void onClick(LiveTv.CategoryItem.TvChannelItem channel) {
                listener.onChannelClick(channel);
            }
        };
    }
    
    public void updateData(List<LiveTv.CategoryItem.TvChannelItem> channels) {
        this.channels = channels != null ? channels : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void updateItems(List<LiveTv.CategoryItem.TvChannelItem> channels) {
        updateData(channels);
    }
    
    public void loadMoreItems(List<LiveTv.CategoryItem.TvChannelItem> newChannels) {
        if (newChannels != null) {
            this.channels.addAll(newChannels);
            notifyDataSetChanged();
        }
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = context != null ? context : parent.getContext();
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_content_grid, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LiveTv.CategoryItem.TvChannelItem channel = channels.get(position);
        holder.bind(channel);
    }
    
    @Override
    public int getItemCount() {
        return channels.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.img);
            // title = itemView.findViewById(R.id.tvTitle); // Not available in this layout
        }
        
        void bind(LiveTv.CategoryItem.TvChannelItem channel) {
            if (title != null) {
                title.setText(channel.getChannelTitle());
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(channel);
                }
            });
        }
    }
}
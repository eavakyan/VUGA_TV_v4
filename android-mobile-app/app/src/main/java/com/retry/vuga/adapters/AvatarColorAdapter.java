package com.retry.vuga.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;

import java.util.List;

public class AvatarColorAdapter extends RecyclerView.Adapter<AvatarColorAdapter.ColorViewHolder> {

    private Context context;
    private List<String> colors;
    private String selectedColor;
    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(String color);
    }

    public AvatarColorAdapter(Context context, List<String> colors, String selectedColor, OnColorSelectedListener listener) {
        this.context = context;
        this.colors = colors;
        this.selectedColor = selectedColor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_avatar_color, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        String color = colors.get(position);
        
        try {
            holder.cardColor.setCardBackgroundColor(Color.parseColor(color));
        } catch (Exception e) {
            holder.cardColor.setCardBackgroundColor(Color.parseColor("#FF5252"));
        }
        
        // Show/hide check mark
        holder.imgCheck.setVisibility(color.equals(selectedColor) ? View.VISIBLE : View.GONE);
        
        holder.itemView.setOnClickListener(v -> {
            selectedColor = color;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onColorSelected(color);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {
        CardView cardColor;
        ImageView imgCheck;

        ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            cardColor = itemView.findViewById(R.id.cardColor);
            imgCheck = itemView.findViewById(R.id.imgCheck);
        }
    }
}
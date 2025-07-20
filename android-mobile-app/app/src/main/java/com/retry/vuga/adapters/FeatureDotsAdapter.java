package com.retry.vuga.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemFeaturedPosBinding;

import java.util.ArrayList;
import java.util.List;


public class FeatureDotsAdapter extends RecyclerView.Adapter<FeatureDotsAdapter.ItemHolder> {
    private int lastSelected = 0;
    private int currentSelected = 0;
    private List<String> mList = new ArrayList<>();
    private Activity activity;

    public List<String> getmList() {
        return mList;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured_pos, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.setModal(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void updateData(List<String> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public void scrollToPos(int pos) {
        lastSelected = currentSelected;
        currentSelected = pos;
        notifyItemChanged(currentSelected);
        notifyItemChanged(lastSelected);
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private ItemFeaturedPosBinding binding;

        public ItemHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void setModal(int position) {
            String item = mList.get(position);
            if (position == currentSelected) {
                binding.imgView.setBackgroundTintList(ContextCompat.getColorStateList(
                        itemView.getContext(), R.color.text_color));


            } else {

                binding.imgView.setBackgroundTintList(ContextCompat.getColorStateList(
                        itemView.getContext(), R.color.text_color_hint));


            }
        }
    }
}



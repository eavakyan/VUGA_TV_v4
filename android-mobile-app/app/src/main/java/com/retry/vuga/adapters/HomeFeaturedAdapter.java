package com.retry.vuga.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.activities.MovieDetailActivity;
import com.retry.vuga.databinding.ItemHomeFeaturedBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.utils.Const;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;

;

public class HomeFeaturedAdapter extends RecyclerView.Adapter<HomeFeaturedAdapter.ItemHolder> {

    List<ContentDetail.DataItem> list = new ArrayList<>();
    int lastSelected = 0;
    int currantSelected = 0;

    public List<ContentDetail.DataItem> getList() {
        return list;
    }

    public void setList(List<ContentDetail.DataItem> list) {
        this.list = list;
    }

    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_featured, parent, false);
        return new HomeFeaturedAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {

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

    public void scrollToPos(int pos) {
        lastSelected = this.currantSelected;
        this.currantSelected = pos;
        notifyItemChanged(this.currantSelected);
        notifyItemChanged(lastSelected);
    }


    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemHomeFeaturedBinding binding;

        public ItemHolder(@NonNull @NotNull View itemView) {
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

        public void setBlur(BlurView blurView, ViewGroup rootView, float v) {

            final Drawable windowBackground = ((Activity) itemView.getContext()).getWindow().getDecorView().getBackground();
            BlurAlgorithm algorithm = getBlurAlgorithm();
            blurView.setBlurEnabled(true);
            blurView.setupWith(rootView, algorithm)
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(v);
        }

        @NonNull
        private BlurAlgorithm getBlurAlgorithm() {
            BlurAlgorithm algorithm;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                algorithm = new RenderEffectBlur();
            } else {
                algorithm = new RenderScriptBlur(itemView.getContext());
            }
            return algorithm;
        }
    }
}

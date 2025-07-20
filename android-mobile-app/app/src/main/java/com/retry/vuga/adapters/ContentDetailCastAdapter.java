package com.retry.vuga.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.activities.ActorDetailActivity;
import com.retry.vuga.databinding.ItemCastBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.utils.Const;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ContentDetailCastAdapter extends RecyclerView.Adapter<ContentDetailCastAdapter.ItemHolder> {
    List<ContentDetail.CastItem> list = new ArrayList<>();

    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cast, parent, false);
        return new ContentDetailCastAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {
        holder.setItems(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItems(List<ContentDetail.CastItem> list) {
        this.list = list;
        notifyItemRangeInserted(0, list.size());
    }


    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemCastBinding binding;

        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }

        public void setItems(int position) {

            ContentDetail.CastItem model = list.get(position);
            binding.setModel(model);
            binding.tvActorName.setSelected(true);
            binding.tvCharacterName.setSelected(true);


            binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), ActorDetailActivity.class);
                intent.putExtra(Const.DataKey.actor_id, model.getActor_id());
                itemView.getContext().startActivity(intent);
            });

        }
    }

}


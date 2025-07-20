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
import com.retry.vuga.databinding.ItemHomeTopTenBinding;
import com.retry.vuga.model.HomePage;
import com.retry.vuga.utils.Const;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

;

public class HomeTopItemsAdapter extends RecyclerView.Adapter<HomeTopItemsAdapter.ItemHolder> {

    List<HomePage.TopContentItem> list = new ArrayList<>();


    public List<HomePage.TopContentItem> getList() {
        return list;
    }

    public void setList(List<HomePage.TopContentItem> list) {
        this.list = list;
    }

    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_top_ten, parent, false);
        return new HomeTopItemsAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {

        holder.setData(position);

    }

    @Override
    public int getItemCount() {

        return list.size();
    }

    public void updateItems(List<HomePage.TopContentItem> list) {
        this.list.clear();
        notifyDataSetChanged();
        for (int i = 0; i < list.size(); i++) {
            this.list.add(list.get(i));
            notifyItemInserted(this.list.size() - 1);
        }
    }


    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemHomeTopTenBinding binding;

        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }

        public void setData(int position) {

            HomePage.TopContentItem model = list.get(position);

            binding.setContent(model.getContent());
            binding.tvTop10.setText(String.valueOf(model.getContent_index()));

            binding.img.setOnClickListener(v -> {


                Intent intent = new Intent(itemView.getContext(), MovieDetailActivity.class);
                intent.putExtra(Const.DataKey.CONTENT_ID, model.getContent_id());
                itemView.getContext().startActivity(intent);

            });
        }


    }
}

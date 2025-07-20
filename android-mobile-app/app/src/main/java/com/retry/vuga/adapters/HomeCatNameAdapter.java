package com.retry.vuga.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.activities.ContentByGenreActivity;
import com.retry.vuga.databinding.ItemHomeCatNameBinding;
import com.retry.vuga.model.HomePage;
import com.retry.vuga.utils.Const;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomeCatNameAdapter extends RecyclerView.Adapter<HomeCatNameAdapter.Itemholder> {


    List<HomePage.GenreContents> list = new ArrayList<>();

    @NonNull
    @NotNull
    @Override
    public Itemholder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_cat_name, parent, false);
        return new HomeCatNameAdapter.Itemholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull Itemholder holder, int position) {
        holder.setData(position);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItems(List<HomePage.GenreContents> list) {
        this.list.clear();
        notifyDataSetChanged();
        for (int i = 0; i < list.size(); i++) {
            this.list.add(list.get(i));
            notifyItemInserted(this.list.size() - 1);
        }
    }

    public class Itemholder extends RecyclerView.ViewHolder {
        ItemHomeCatNameBinding binding;
        HomeCatObjectAdapter homeCatAdapter;


        public Itemholder(@NonNull @NotNull View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
            homeCatAdapter = new HomeCatObjectAdapter();
        }

        public void setData(int position) {
            HomePage.GenreContents model = list.get(position);

            binding.setContent(model);

            binding.rvCatItem.setAdapter(homeCatAdapter);
            binding.rvCatItem.setItemAnimator(null);
            homeCatAdapter.updateItems(model.getContent());

            if (model.getContent().isEmpty()) {
                binding.getRoot().setVisibility(View.GONE);
            }
            binding.btnMore.setOnClickListener(v -> {

                Intent intent = new Intent(itemView.getContext(), ContentByGenreActivity.class);
                intent.putExtra(Const.DataKey.DATA, new Gson().toJson(model));
                itemView.getContext().startActivity(intent);
            });

        }
    }
}

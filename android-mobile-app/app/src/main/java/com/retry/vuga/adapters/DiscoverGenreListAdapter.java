package com.retry.vuga.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemGenreBinding;
import com.retry.vuga.model.AppSetting;
import com.retry.vuga.utils.SessionManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DiscoverGenreListAdapter extends RecyclerView.Adapter<DiscoverGenreListAdapter.ItemHolder> {

    public OnGenreClick onGenreClick;
    List<AppSetting.GenreItem> list = new ArrayList<>();
    int genreId = 0;

    public DiscoverGenreListAdapter(int genreId) {
        this.genreId = genreId;
    }

    public List<AppSetting.GenreItem> getList() {
        return list;
    }

    public void updateSelected(int genreId) {
        this.genreId = genreId;
        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genre, parent, false);
        return new DiscoverGenreListAdapter.ItemHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItems(List<AppSetting.GenreItem> list) {


        AppSetting.GenreItem myModel = new AppSetting.GenreItem();
        myModel.setId(0);
        myModel.setTitle("All");

        this.list.clear();
        this.list.add(myModel);
        this.list.addAll(list);


        notifyItemRangeInserted(0, this.list.size());
    }


    public interface OnGenreClick {
        void onClick(AppSetting.GenreItem model);
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        SessionManager sessionManager;
        ItemGenreBinding binding;

        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            sessionManager = new SessionManager(itemView.getContext());

        }

        public void setData(int position) {
            AppSetting.GenreItem model = list.get(position);


            binding.tvName.setText(model.getTitle());


            for (int i = 0; i < list.size(); i++) {

                if (genreId == model.getId()) {


                    binding.tvName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_color));
                    binding.tvName.setTypeface(ResourcesCompat.getFont(itemView.getContext(), R.font.outfit_semi_bold));

                } else {
                    binding.tvName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_color_light));
                    binding.tvName.setTypeface(ResourcesCompat.getFont(itemView.getContext(), R.font.outfit_light));
                }
            }


            binding.getRoot().setOnClickListener(v -> {

                onGenreClick.onClick(model);
            });

        }
    }
}

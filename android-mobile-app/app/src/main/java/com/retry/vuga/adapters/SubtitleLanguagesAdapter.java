package com.retry.vuga.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemSubtitleLanguageBinding;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SubtitleLanguagesAdapter extends RecyclerView.Adapter<SubtitleLanguagesAdapter.ItemHolder> {
    public OnLanguageClick onLanguageClick;
    List<String> list = new ArrayList<>();

    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtitle_language, parent, false);
        return new SubtitleLanguagesAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {
        holder.setItems(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItems(List<String> list) {

        this.list.clear();
        this.list.add("Disable");
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    public interface OnLanguageClick {
        void onClick(int position);
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemSubtitleLanguageBinding binding;
        SessionManager sessionManager;

        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
            sessionManager = new SessionManager(itemView.getContext());
        }

        public void setItems(int position) {

            binding.tvName.setText(list.get(position));

            for (int i = 0; i < list.size(); i++) {

                if (sessionManager.getIntValue(Const.DataKey.SUBTITLE_POSITION) == position) {

                    binding.tvName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_color));
                } else {
                    binding.tvName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_color_light));

                }


            }


            binding.getRoot().setOnClickListener(v -> {

                sessionManager.saveIntValue(Const.DataKey.SUBTITLE_POSITION, position);
                onLanguageClick.onClick(position);
            });
        }
    }

}


package com.retry.vuga.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemLanguageChangeBinding;
import com.retry.vuga.model.Language;
import com.retry.vuga.utils.SessionManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LanguagesAdapter extends RecyclerView.Adapter<LanguagesAdapter.ItemHolder> {

    public OnClick onClick;
    int selectedPos = -1;
    int lastSelected = 0;
    private List<Language> list = new ArrayList<>();

    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language_change, parent, false);
        return new ItemHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {
        holder.setModal(position);
    }

    @Override
    public int getItemCount() {
        return list.size();

    }

    public void updateItems(List<Language> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    public interface OnClick {
        void onSelect(Language model);
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        ItemLanguageChangeBinding binding;
        SessionManager sessionManager;

        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            sessionManager = new SessionManager(itemView.getContext());
        }


        public void setModal(int position) {

            Language model = list.get(position);


            if (Objects.equals(model.getId(), sessionManager.getLanguage()) && selectedPos == -1) {
                selectedPos = position;
            }

            binding.setSelected(selectedPos == position);

            binding.tvNameEng.setText(model.getEngName());
            binding.tvName.setText(model.getName());

            binding.getRoot().setOnClickListener(v -> {

                if (selectedPos != position) {
                    lastSelected = selectedPos;
                    selectedPos = position;
                    notifyItemChanged(lastSelected);
                    notifyItemChanged(selectedPos);
                    onClick.onSelect(model);
                }

            });

        }
    }
}

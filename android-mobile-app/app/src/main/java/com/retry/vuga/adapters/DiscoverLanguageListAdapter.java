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
import com.retry.vuga.databinding.ItemLanguageBinding;
import com.retry.vuga.model.AppSetting;
import com.retry.vuga.utils.SessionManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DiscoverLanguageListAdapter extends RecyclerView.Adapter<DiscoverLanguageListAdapter.ItemHolder> {

    public OnLanguageClick onLanguageClick;
    List<AppSetting.LanguageItem> list = new ArrayList<>();
    int languageId = 0;

    public DiscoverLanguageListAdapter(int languageId) {
        this.languageId = languageId;
    }

    public List<AppSetting.LanguageItem> getList() {
        return list;
    }

    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language, parent, false);
        return new DiscoverLanguageListAdapter.ItemHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public void updateSelected(int languageId) {
        this.languageId = languageId;
        notifyDataSetChanged();
    }

    public void updateItems(List<AppSetting.LanguageItem> list) {


        AppSetting.LanguageItem myModel = new AppSetting.LanguageItem();
        myModel.setId(0);
        myModel.setTitle("All");
            this.list.clear();
            this.list.add(myModel);
            this.list.addAll(list);


        notifyItemRangeInserted(0, this.list.size());
    }

    public interface OnLanguageClick {
        void onClick(AppSetting.LanguageItem model);
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        SessionManager sessionManager;
        ItemLanguageBinding binding;

        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            sessionManager = new SessionManager(itemView.getContext());

        }

        public void setData(int position) {

            AppSetting.LanguageItem model = list.get(position);
            binding.tvName.setText(model.getTitle());


            for (int i = 0; i < list.size(); i++) {

                if (languageId == model.getId()) {

                    binding.tvName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_color));
                    binding.tvName.setTypeface(ResourcesCompat.getFont(itemView.getContext(), R.font.outfit_semi_bold));

                } else {

                    binding.tvName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_color_light));
                    binding.tvName.setTypeface(ResourcesCompat.getFont(itemView.getContext(), R.font.outfit_light));
                }


            }


            binding.getRoot().setOnClickListener(v -> {
                onLanguageClick.onClick(model);
            });

        }
    }
}

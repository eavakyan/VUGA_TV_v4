package com.retry.vuga.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemLivetvCatItem1Binding;
import com.retry.vuga.databinding.ItemLivetvCatItem2Binding;
import com.retry.vuga.model.LiveTv;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LiveTvObjectAdapter extends RecyclerView.Adapter<LiveTvObjectAdapter.ItemHolder> {
    List<LiveTv.CategoryItem.TvChannelItem> list = new ArrayList<>();
    int type;
    OnItemClick onItemClick;


    public LiveTvObjectAdapter(int type) {
        this.type = type;
    }

    public OnItemClick getOnItemClick() {
        return onItemClick;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }


    @NonNull
    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_livetv_cat_item_1, parent, false);
        View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_livetv_cat_item_2, parent, false);

        if (type == 1) {
            return new ItemHolder(view1);
        }
        if (type == 2) {
            return new ItemHolder(view2);
        }

        return new ItemHolder(view1);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {


        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void clear() {
        int size = list.size();
        list.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void loadMoreItems(List<LiveTv.CategoryItem.TvChannelItem> list) {


        for (int i = 0; i < list.size(); i++) {
            this.list.add(list.get(i));
            notifyItemInserted(this.list.size() - 1);

        }

    }

    public void updateItems(List<LiveTv.CategoryItem.TvChannelItem> list) {

        this.list = list;
        notifyItemRangeInserted(0, list.size());
    }

    public interface CallBack {
        void onLastItem();
    }

    public interface OnItemClick {
        void onClick(LiveTv.CategoryItem.TvChannelItem model);
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemLivetvCatItem1Binding binding1;
        ItemLivetvCatItem2Binding binding2;
        SessionManager sessionManager;


        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            sessionManager = new SessionManager(itemView.getContext());
            if (type == 1) {

                binding1 = DataBindingUtil.bind(itemView);
            } else if (type == 2) {
                binding2 = DataBindingUtil.bind(itemView);

            } else {
                binding1 = null;
                binding2 = null;
            }
        }

        public void setData(int position) {

            if (type == 1) {


                LiveTv.CategoryItem.TvChannelItem model = list.get(position);

                binding1.setModel(model);


                if (model.getAccessType() == 2) {
                    if (sessionManager.getBooleanValue(Const.DataKey.IS_PREMIUM)) {

                        model.setAccessType(1);

                    }

                } else if (model.getAccessType() == 3) {
                    if (sessionManager.getBooleanValue(Const.DataKey.IS_PREMIUM)) {
                        model.setAccessType(1);

                    }


                }


                binding1.getRoot().setOnClickListener(v -> {


                    if (model.getSource() != null) {

                        onItemClick.onClick(model);

                    }
                });
            } else if (type == 2) {


                LiveTv.CategoryItem.TvChannelItem model = list.get(position);

                binding2.setModel(model);


                if (model.getAccessType() == 2) {
                    if (sessionManager.getBooleanValue(Const.DataKey.IS_PREMIUM)) {

                        model.setAccessType(1);

                    }

                } else if (model.getAccessType() == 3) {
                    if (sessionManager.getBooleanValue(Const.DataKey.IS_PREMIUM)) {
                        model.setAccessType(1);

                    }


                }


                binding2.getRoot().setOnClickListener(v -> {


                    if (model.getSource() != null) {

                        onItemClick.onClick(model);

                    }
                });


            }
        }


    }
}

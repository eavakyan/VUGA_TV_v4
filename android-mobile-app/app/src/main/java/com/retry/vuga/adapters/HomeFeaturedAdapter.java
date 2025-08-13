package com.retry.vuga.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.activities.BaseActivity;
import com.retry.vuga.activities.MovieDetailActivity;
import com.retry.vuga.activities.PlayerNewActivity;
import com.retry.vuga.databinding.ItemHomeFeaturedBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.utils.CallBacks;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;
import com.retry.vuga.activities.BaseActivity.OnWatchList;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

            // Set up button click listeners
            setupButtonClickListeners(model);

            // Update My List button state
            updateMyListButtonState(model);

            binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), MovieDetailActivity.class);
                intent.putExtra(Const.DataKey.CONTENT_ID, model.getId());
                itemView.getContext().startActivity(intent);
            });
        }

        private void setupButtonClickListeners(ContentDetail.DataItem model) {
            AppCompatButton btnWatchNow = itemView.findViewById(R.id.btn_watch_now);
            AppCompatButton btnMyList = itemView.findViewById(R.id.btn_my_list);

            // Watch Now button click
            btnWatchNow.setOnClickListener(v -> {
                handleWatchNowClick(model);
            });

            // My List button click
            btnMyList.setOnClickListener(v -> {
                handleMyListClick(model);
            });
        }

        private void handleWatchNowClick(ContentDetail.DataItem model) {
            // Navigate to detail page - let it handle playing
            Intent intent = new Intent(itemView.getContext(), MovieDetailActivity.class);
            intent.putExtra(Const.DataKey.CONTENT_ID, model.getId());
            itemView.getContext().startActivity(intent);
        }

        private void handleMyListClick(ContentDetail.DataItem model) {
            Context context = itemView.getContext();
            
            // Check if user is logged in
            SessionManager sessionManager = new SessionManager(context);
            if (sessionManager.getUser() == null) {
                Toast.makeText(context, "Please sign in to manage your watchlist", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isInWatchlist = isContentInWatchlist(model.getId());
            boolean newState = !isInWatchlist;
            
            // Call the API to toggle watchlist
            if (context instanceof BaseActivity) {
                BaseActivity.addRemoveWatchlist(context, model.getId(), newState, new OnWatchList() {
                    @Override
                    public void onTerminate() {
                        // Update completed
                    }
                    
                    @Override
                    public void onError() {
                        Toast.makeText(context, "Error updating watchlist", Toast.LENGTH_SHORT).show();
                    }
                    
                    @Override
                    public void onSuccess() {
                        String message = newState ? "Added to My List" : "Removed from My List";
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        
                        // Broadcast the update
                        Intent intent = new Intent("com.retry.vuga.WATCHLIST_UPDATED");
                        intent.putExtra("content_id", model.getId());
                        intent.putExtra("is_added", newState);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        
                        // Update button state
                        updateMyListButtonState(model);
                    }
                });
            }
        }

        private void updateMyListButtonState(ContentDetail.DataItem model) {
            AppCompatButton btnMyList = itemView.findViewById(R.id.btn_my_list);
            boolean isInWatchlist = isContentInWatchlist(model.getId());
            
            if (isInWatchlist) {
                btnMyList.setText("✓ MY LIST");
            } else {
                btnMyList.setText("+ MY LIST");
            }
        }

        private boolean isContentInWatchlist(int contentId) {
            SessionManager sessionManager = new SessionManager(itemView.getContext());
            if (sessionManager.getUser() == null) {
                return false;
            }

            String watchlistIds = sessionManager.getUser().getWatchlist_content_ids();
            if (watchlistIds == null || watchlistIds.isEmpty()) {
                return false;
            }

            // Check if content ID is in the comma-separated string
            String[] ids = watchlistIds.split(",");
            String targetId = String.valueOf(contentId);
            for (String id : ids) {
                if (id.trim().equals(targetId)) {
                    return true;
                }
            }
            return false;
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

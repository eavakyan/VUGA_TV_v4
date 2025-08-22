package com.retry.vuga.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.activities.EpisodeDetailActivity;
import com.retry.vuga.activities.MovieDetailActivity;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.UnifiedWatchlistItem;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.Global;
import com.retry.vuga.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UnifiedWatchlistAdapter extends RecyclerView.Adapter<UnifiedWatchlistAdapter.ItemHolder> {

    private List<UnifiedWatchlistItem> list = new ArrayList<>();
    private OnItemClick onItemClick;
    private CompositeDisposable disposable = new CompositeDisposable();
    private SessionManager sessionManager;

    public interface OnItemClick {
        void onRemoveClick(UnifiedWatchlistItem item);
        void onContentDetailFetched(ContentDetail.DataItem content, UnifiedWatchlistItem item);
    }

    public UnifiedWatchlistAdapter(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public List<UnifiedWatchlistItem> getList() {
        return list;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unified_watchlist, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItems(List<UnifiedWatchlistItem> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void loadMoreItems(List<UnifiedWatchlistItem> list) {
        for (UnifiedWatchlistItem item : list) {
            this.list.add(item);
            notifyItemInserted(this.list.size() - 1);
        }
    }

    public void clear() {
        int size = list.size();
        list.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle;
        TextView tvSubtitle;
        TextView tvRating;
        TextView tvType;
        TextView tvGenre;
        ImageView btnWatchList;
        CardView cardView;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.iv_poster);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvType = itemView.findViewById(R.id.tv_type);
            tvGenre = itemView.findViewById(R.id.tv_genre);
            btnWatchList = itemView.findViewById(R.id.btn_watchlist);
            cardView = itemView.findViewById(R.id.card_view);
        }

        public void setData(int position) {
            UnifiedWatchlistItem model = list.get(position);

            // Set title
            if (model.isEpisode()) {
                // For episodes, show series title and episode info
                String episodeInfo = "";
                if (model.getSeasonNumber() != null && model.getEpisodeNumber() != null) {
                    episodeInfo = "S" + model.getSeasonNumber() + " E" + model.getEpisodeNumber() + ": ";
                }
                tvTitle.setText(episodeInfo + model.getTitle());
                
                if (model.getSeriesTitle() != null) {
                    tvSubtitle.setText(model.getSeriesTitle());
                    tvSubtitle.setVisibility(View.VISIBLE);
                } else {
                    tvSubtitle.setVisibility(View.GONE);
                }
                
                tvType.setText("EPISODE");
                tvType.setBackgroundResource(R.drawable.bg_episode_badge);
            } else {
                // For movies/series
                tvTitle.setText(model.getTitle());
                tvSubtitle.setVisibility(View.GONE);
                
                if (model.getType() != null) {
                    if (model.getType() == 1) {
                        tvType.setText("MOVIE");
                        tvType.setBackgroundResource(R.drawable.bg_movie_badge);
                    } else if (model.getType() == 2) {
                        tvType.setText("TV SHOW");
                        tvType.setBackgroundResource(R.drawable.bg_tvshow_badge);
                    }
                }
            }

            // Set poster
            String posterUrl = model.getBestPoster();
            if (posterUrl != null && !posterUrl.isEmpty()) {
                if (!posterUrl.startsWith("http")) {
                    posterUrl = Const.BASE + posterUrl;
                }
                Glide.with(itemView.getContext())
                        .load(posterUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(ivPoster);
            }

            // Set rating
            if (model.getRatings() != null && !model.getRatings().isEmpty()) {
                try {
                    double rating = Double.parseDouble(model.getRatings());
                    if (rating > 0) {
                        tvRating.setText(String.format("%.1f", rating));
                        tvRating.setVisibility(View.VISIBLE);
                    } else {
                        tvRating.setVisibility(View.GONE);
                    }
                } catch (NumberFormatException e) {
                    tvRating.setVisibility(View.GONE);
                }
            } else {
                tvRating.setVisibility(View.GONE);
            }

            // Set genre
            if (model.getGenreIds() != null && !model.getGenreIds().isEmpty()) {
                String genreString = Global.getGenreStringFromIds(model.getGenreIds(), itemView.getContext());
                tvGenre.setText(genreString);
                tvGenre.setVisibility(View.VISIBLE);
            } else {
                tvGenre.setVisibility(View.GONE);
            }

            // Handle click
            cardView.setOnClickListener(v -> {
                if (model.isEpisode()) {
                    // Navigate to episode detail
                    navigateToEpisode(model);
                } else {
                    // Navigate to content detail
                    Intent intent = new Intent(itemView.getContext(), MovieDetailActivity.class);
                    intent.putExtra(Const.DataKey.CONTENT_ID, model.getContentId());
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    itemView.getContext().startActivity(intent);
                }
            });

            // Handle remove from watchlist
            btnWatchList.setOnClickListener(v -> {
                if (onItemClick != null) {
                    onItemClick.onRemoveClick(model);
                }
            });
        }

        private void navigateToEpisode(UnifiedWatchlistItem item) {
            // First fetch the content details to get full episode data
            if (item.getContentId() == null) return;
            
            UserRegistration.Data user = sessionManager.getUser();
            Integer profileId = user != null ? user.getLastActiveProfileId() : null;
            
            disposable.add(RetrofitClient.getService()
                    .getContentDetail(user != null ? user.getId() : 0, item.getContentId(), profileId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((contentDetail, throwable) -> {
                        if (contentDetail != null && contentDetail.getData() != null) {
                            ContentDetail.DataItem content = contentDetail.getData();
                            
                            // Find the episode in the content's seasons
                            ContentDetail.SeasonItem.EpisodesItem episode = null;
                            if (content.getSeasons() != null && item.getEpisodeId() != null) {
                                for (ContentDetail.SeasonItem season : content.getSeasons()) {
                                    if (season.getEpisodes() != null) {
                                        for (ContentDetail.SeasonItem.EpisodesItem ep : season.getEpisodes()) {
                                            if (ep.getId() == item.getEpisodeId()) {
                                                episode = ep;
                                                break;
                                            }
                                        }
                                    }
                                    if (episode != null) break;
                                }
                            }
                            
                            if (episode != null) {
                                // Navigate to episode detail
                                Intent intent = new Intent(itemView.getContext(), EpisodeDetailActivity.class);
                                intent.putExtra("EPISODE_DATA", new Gson().toJson(episode));
                                intent.putExtra(Const.DataKey.CONTENT_ID, content.getId());
                                intent.putExtra(Const.DataKey.CONTENT_NAME, content.getTitle());
                                intent.putExtra(Const.DataKey.THUMBNAIL, content.getVerticalPoster());
                                intent.putExtra("SEASON_ID", item.getSeasonNumber() != null ? item.getSeasonNumber() : 0);
                                intent.putExtra("EPISODE_NUMBER", item.getEpisodeNumber() != null ? item.getEpisodeNumber() : episode.getNumber());
                                intent.putExtra("CONTENT_DATA", new Gson().toJson(content));
                                
                                // Add subtitles if available
                                if (content.getSubtitles() != null && !content.getSubtitles().isEmpty()) {
                                    intent.putExtra(Const.DataKey.SUB_TITLES, new Gson().toJson(content.getSubtitles()));
                                }
                                
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                itemView.getContext().startActivity(intent);
                            } else {
                                // Fallback to content detail if episode not found
                                Intent intent = new Intent(itemView.getContext(), MovieDetailActivity.class);
                                intent.putExtra(Const.DataKey.CONTENT_ID, item.getContentId());
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                itemView.getContext().startActivity(intent);
                            }
                            
                            // Notify listener if needed
                            if (onItemClick != null) {
                                onItemClick.onContentDetailFetched(content, item);
                            }
                        }
                    }));
        }
    }
}
package com.retry.vuga.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemDownloadBinding;
import com.retry.vuga.databinding.ItemDownloadTitleBinding;
import com.retry.vuga.model.Downloads;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DownloadSeriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Downloads> list = new ArrayList<>();
    OnClick onClick;
    Downloads downloading_obj = null;

    public void setOnClick(OnClick onClick) {
        this.onClick = onClick;
    }

    public List<Downloads> getList() {
        return list;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, parent, false);
            return new ItemHolder(view);


        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download_title, parent, false);
            return new TitleHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            ItemHolder viewHolder = (ItemHolder) holder;
            viewHolder.setData(position);
        } else if (holder instanceof TitleHolder) {
            TitleHolder viewHolder = (TitleHolder) holder;
            viewHolder.setData(position);
        }
    }

    @Override
    public int getItemViewType(int position) {


        if (list.get(position).isSeasonTitle()) {
            return 2;
        } else {
            return 1;
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItems(List<Downloads> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    public void changeDownloadData(Downloads downloads) {
        int pos = -1;
        for (int i = 0; i < list.size(); i++) {
            if (downloads.getId() == list.get(i).getId()) {
                pos = i;
                break;
            }
        }
        if (pos == -1) {
            return;
        }
        Log.i("TAG", "myDown :changeDownloadData pending " + downloads.getId());

        downloading_obj = downloads;
        notifyItemChanged(pos);

    }


    public interface OnClick {
        void onClick(Downloads model);

        void onMenuClick(Downloads model);

        void onQueuedClick(Downloads model);

        void onInsideClick(Downloads model);

        void onDownloadClick(Downloads model);

        void onProgressClick(Downloads model);


    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        ItemDownloadBinding binding;
        SessionManager sessionManager;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            sessionManager = new SessionManager(itemView.getContext());
        }

        public void setData(int position) {


            Downloads model = list.get(position);
            binding.setContent(model);


            binding.btnProgress.setVisibility(View.GONE);
            binding.btnDownload.setVisibility(View.GONE);
            binding.btnMenu.setVisibility(View.GONE);
            binding.btnInside.setVisibility(View.GONE);
            binding.tvDownloading.setVisibility(View.GONE);
            binding.btnQueved.setVisibility(View.GONE);

            binding.tvSeasonAndEpisode.setVisibility(View.GONE);
            binding.tvDuration.setVisibility(View.GONE);
            binding.tvFileSize.setVisibility(View.GONE);
            binding.tvEpisodeCount.setVisibility(View.GONE);
            binding.tvDownloading.setVisibility(View.GONE);


            binding.tvDuration.setText(model.getDuration());

            binding.tvSeasonAndEpisode.setText("S" + model.getSeasonCount() + " E" + model.getEpisodeCount() + "  •  ");
            binding.tvFileSize.setText(model.getSize());
            binding.progress.setProgress(model.getPlayProgress());
            binding.tvSeasonAndEpisode.setVisibility(View.VISIBLE);
            binding.tvDuration.setVisibility(View.VISIBLE);
            binding.tvFileSize.setVisibility(View.VISIBLE);


            binding.tvName.setText(model.getTitle());


            List<Downloads> pendingList = sessionManager.getPendings();
            List<Downloads> downloadsList = sessionManager.getDownloads();
            Optional<Downloads> pending_obj = pendingList.stream().filter(downloads -> downloads.getId() == model.getId()).findFirst();
            Optional<Downloads> down_obj = downloadsList.stream().filter(downloads -> downloads.getId() == model.getId()).findFirst();

            if (pending_obj.isPresent()) {
                Downloads object = pending_obj.get();
                model.setDownloadStatus(object.getDownloadStatus());
                switch (object.getDownloadStatus()) {
                    case Const.DownloadStatus.START:
                    case Const.DownloadStatus.PROGRESSING:

                        binding.btnProgress.setVisibility(View.VISIBLE);
                        binding.progressBar.setProgress(object.getProgress());

                        break;
                    case Const.DownloadStatus.QUEUED:
                    case Const.DownloadStatus.PAUSED:

                        if (sessionManager.getBooleanValue(Const.DataKey.IS_DOWNLOAD_PAUSED)) {
                            binding.btnDownload.setVisibility(View.VISIBLE);
                            binding.progressbarMini.setProgress(object.getProgress());
                        } else {
                            binding.btnQueved.setVisibility(View.VISIBLE);

                        }

                        break;
                }
                binding.getRoot().setOnClickListener(v -> {
                });
            } else if (down_obj.isPresent()) {


                binding.btnMenu.setVisibility(View.VISIBLE);


                binding.getRoot().setOnClickListener(v -> {

                    onClick.onClick(model);

                });
            }

            binding.btnQueved.setOnClickListener(v -> {
                onClick.onQueuedClick(model);
            });

            binding.btnMenu.setOnClickListener(v -> {
                onClick.onMenuClick(model);
            });


            binding.btnDownload.setOnClickListener(v -> {
                onClick.onDownloadClick(model);
            });

            binding.btnProgress.setOnClickListener(v -> {
                onClick.onProgressClick(model);
            });

        }


    }


    public class TitleHolder extends RecyclerView.ViewHolder {
        ItemDownloadTitleBinding binding;
        SessionManager sessionManager;

        public TitleHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            sessionManager = new SessionManager(itemView.getContext());
        }

        public void setData(int position) {


            Downloads downloads = list.get(position);
            binding.tvTitle.setText(ContextCompat.getString(itemView.getContext(), R.string.season) + " " + downloads.getSeasonCount());

        }


    }
}

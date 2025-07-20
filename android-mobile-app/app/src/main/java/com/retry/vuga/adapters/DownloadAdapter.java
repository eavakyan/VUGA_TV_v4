package com.retry.vuga.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemDownloadBinding;
import com.retry.vuga.model.Downloads;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ItemHolder> {

    List<Downloads> list = new ArrayList<>();
    OnClick onClick;

    public void setOnClick(OnClick onClick) {
        this.onClick = onClick;
    }

    public List<Downloads> getList() {
        return list;
    }


    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, parent, false);
        return new DownloadAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.setData(position);
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


        if (downloads.getType() == 1) {
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
            notifyItemChanged(pos);


        } else {

            Optional<Downloads> seriesObj = list.stream().filter(downloads1 -> downloads1.getContentId() == downloads.getContentId()).findFirst();


            if (seriesObj.isPresent()) {
                int index = list.indexOf(seriesObj.get()); // this is only doing with series object bcz movies ma badhu session ma object
                Downloads newObj = seriesObj.get();       // 6 k nai ana per thi j nakki thay 6 , but  series ma ahi dummy object hoy 6 athi badhu menually
                newObj.setId(downloads.getId());         // check karvu pade
                newObj.setProgress(downloads.getProgress());
                newObj.setDownloadStatus(downloads.getDownloadStatus());
                list.set(index, newObj);
                notifyItemChanged(index);
            }


        }


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
            binding.progress.setProgress(model.getPlayProgress());

            if (model.getType() == 1) {

                binding.tvDuration.setText(model.getDuration());
                binding.tvFileSize.setText(model.getSize());
                binding.tvFileSize.setVisibility(View.VISIBLE);
                binding.tvDuration.setVisibility(View.VISIBLE);
                binding.viewSheetSeries.setVisibility(View.GONE);

            } else {
                binding.viewSheetSeries.setVisibility(View.VISIBLE);
                binding.tvEpisodeCount.setText(model.getType() == 2 ? model.getEpisodeList().size() + " " + (model.getEpisodeList().size() > 1 ? ContextCompat.getString(itemView.getContext(), R.string.episodes) : ContextCompat.getString(itemView.getContext(), R.string.episode)) : "");
                binding.tvEpisodeCount.setVisibility(View.VISIBLE);

            }

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
                        if (model.getType() == 1) {
                            binding.btnProgress.setVisibility(View.VISIBLE);
                            binding.progressBar.setProgress(object.getProgress());
                        } else {
                            binding.tvDownloading.setVisibility(View.VISIBLE);
                            binding.btnInside.setVisibility(View.VISIBLE);
                        }

                        break;
                    case Const.DownloadStatus.QUEUED:
                    case Const.DownloadStatus.PAUSED:
                        if (model.getType() == 1) {
                            if (sessionManager.getBooleanValue(Const.DataKey.IS_DOWNLOAD_PAUSED)) {
                                binding.btnDownload.setVisibility(View.VISIBLE);
                                binding.progressbarMini.setProgress(object.getProgress());
                            } else {
                                binding.btnQueved.setVisibility(View.VISIBLE);

                            }
                        } else {
                            binding.tvDownloading.setVisibility(View.GONE);
                            binding.btnInside.setVisibility(View.VISIBLE);

                        }
                        break;
                }


                binding.getRoot().setOnClickListener(v -> {
                    if (model.getType() == 2) {
                        onClick.onInsideClick(model);

                    }
                });
            } else if (down_obj.isPresent()) {

                if (model.getType() == 1) {
                    binding.btnMenu.setVisibility(View.VISIBLE);
                } else {
                    binding.btnInside.setVisibility(View.VISIBLE);
                }

                binding.getRoot().setOnClickListener(v -> {
                    if (model.getType() == 1) {
                        onClick.onClick(model);
                    } else {
                        onClick.onInsideClick(model);
                    }
                });
            }

            binding.btnQueved.setOnClickListener(v -> {
                onClick.onQueuedClick(model);
            });

            binding.btnMenu.setOnClickListener(v -> {
                onClick.onMenuClick(model);
            });
            binding.btnInside.setOnClickListener(v -> {
                onClick.onInsideClick(model);
            });

            binding.btnDownload.setOnClickListener(v -> {
                onClick.onDownloadClick(model);
            });

            binding.btnProgress.setOnClickListener(v -> {
                onClick.onProgressClick(model);
            });

        }


    }
}

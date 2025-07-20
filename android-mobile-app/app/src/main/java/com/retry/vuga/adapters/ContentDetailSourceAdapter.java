package com.retry.vuga.adapters;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ItemContentSourceBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.Downloads;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ContentDetailSourceAdapter extends RecyclerView.Adapter<ContentDetailSourceAdapter.ItemHolder> {

    boolean isDownloading = false;
    List<ContentDetail.SourceItem> list = new ArrayList<>();

    private OnItemClick onItemClick;

    Downloads downloading_obj = null;

    public ContentDetailSourceAdapter() {

    }

    public List<ContentDetail.SourceItem> getList() {
        return list;
    }

    public void setList(List<ContentDetail.SourceItem> list) {
        this.list = list;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content_source, parent, false);
        return new ContentDetailSourceAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ItemHolder holder, int position) {
        holder.setItems(position);
    }

    public void updateItems(List<ContentDetail.SourceItem> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return list.size();
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

        Log.i("TAG", "internet onReceive:changeDownloadData source " + downloads.getId());

        downloading_obj = downloads;
        notifyItemChanged(pos);

    }

    public void changeItem(ContentDetail.SourceItem model) {

        int pos = list.indexOf(model);
        model.setAccess_type(1);
        list.set(pos, model);
        notifyItemChanged(pos);
    }

    public interface OnItemClick {
        void onPendingDownLoad();

        void onClick(ContentDetail.SourceItem model, ItemContentSourceBinding binding);

        void onDownloadClick(ContentDetail.SourceItem model, ItemContentSourceBinding binding);

        void onPauseClick(ContentDetail.SourceItem model);

        void onResumeClick(ContentDetail.SourceItem model);

    }


    public class ItemHolder extends RecyclerView.ViewHolder {

        ItemContentSourceBinding binding;


        SessionManager sessionManager;


        public ItemHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            sessionManager = new SessionManager(itemView.getContext());
        }

        public void setItems(int position) {
            ContentDetail.SourceItem model = list.get(position);


            binding.progress.setVisibility(View.GONE);
            binding.imgPlay.setVisibility(View.GONE);
            binding.imgPause.setVisibility(View.GONE);
            binding.imgDownload.setVisibility(View.GONE);
            binding.imgCheck.setVisibility(View.GONE);
            binding.imgPending.setVisibility(View.GONE);
            binding.imgLock.setVisibility(View.GONE);
            if (model.playProgress > 0) {
                binding.pbPlay.setVisibility(View.VISIBLE);
                binding.pbPlay.setProgress(model.playProgress);
            } else {
                binding.pbPlay.setVisibility(View.GONE);
            }

            binding.progress.setProgress(model.getProgress());


// check if video is already downloaded...........

            List<Downloads> pendingList = sessionManager.getPendings();
            List<Downloads> downloadsList = sessionManager.getDownloads();


            Optional<Downloads> pending_obj = pendingList.stream().filter(downloads -> downloads.getId() == model.getId()).findFirst();
            Optional<Downloads> down_obj = downloadsList.stream().filter(downloads -> downloads.getId() == model.getId()).findFirst();
            if (pending_obj.isPresent()) {
                // have proceeded for download
                Downloads object = pending_obj.get();

                switch (object.getDownloadStatus()) {

                    case Const.DownloadStatus.START:
                        Log.i("TAG", "setItems: start");

                        binding.imgPause.setVisibility(View.VISIBLE);
                        binding.progress.setProgress(object.getProgress());
                        binding.progress.setVisibility(View.VISIBLE);
                        binding.pbPlay.setVisibility(View.GONE);
                        break;

                    case Const.DownloadStatus.QUEUED://pending
                        Log.i("TAG", "setItems: queued");
                        binding.imgPending.setVisibility(View.VISIBLE);
                        binding.imgPending.setOnClickListener(v -> {
                            onItemClick.onPendingDownLoad();
                        });

                        break;


                    case Const.DownloadStatus.PAUSED: //completed

                        binding.imgPlay.setVisibility(View.VISIBLE);
                        binding.progress.setProgress(object.getProgress());
                        binding.progress.setVisibility(View.VISIBLE);
                        binding.pbPlay.setVisibility(View.GONE);
                        break;


                    case Const.DownloadStatus.PROGRESSING: //completed

                        binding.imgPause.setVisibility(View.VISIBLE);
                        binding.progress.setProgress(object.getProgress());
                        binding.progress.setVisibility(View.VISIBLE);
                        binding.pbPlay.setVisibility(View.GONE);
                        break;


                }
            } else if (down_obj.isPresent()) {

                binding.imgCheck.setVisibility(View.VISIBLE);
            } else {

                if (model.getAccess_type() == 1) {
                    checkDownloadable(model);

                } else if (model.getAccess_type() == 2) {
                    //premium
                    if (sessionManager.getBooleanValue(Const.DataKey.IS_PREMIUM)) {

                        model.setAccess_type(1);
                        checkDownloadable(model);

                    } else {
                        binding.imgPremium.setVisibility(View.VISIBLE);
                    }

                } else if (model.getAccess_type() == 3) {
                    //lock
                    if (sessionManager.getBooleanValue(Const.DataKey.IS_PREMIUM)) {
                        checkDownloadable(model);
                        model.setAccess_type(1);

                    } else {
                        binding.imgLock.setVisibility(View.VISIBLE);

                    }


                }

            }


//.................................................
            binding.setModel(model);
//...................... Listeners  .................................

            binding.imgPlay.setOnClickListener(v -> {
                onItemClick.onResumeClick(model);
            });
            binding.imgPause.setOnClickListener(v -> {
                onItemClick.onPauseClick(model);
            });
            binding.imgLock.setOnClickListener(v -> {
                onItemClick.onDownloadClick(model, binding);
            });
            binding.getRoot().setOnClickListener(v -> {
                onItemClick.onClick(model, binding);

            });


        }

        private void checkDownloadable(ContentDetail.SourceItem model) {

            if (model.getIs_download() == 0) {
                //content is not downloadable
                binding.imgDownload.setVisibility(View.GONE);
            } else if (model.getIs_download() == 1) {

                //content is  downloadable

                if (model.getDownloadStatus() == 0) {

//                not downloaded yet
                    prepareForDownload(model);


                }

            }
        }

        private void prepareForDownload(ContentDetail.SourceItem model) {

            binding.imgDownload.setVisibility(View.VISIBLE);
            binding.imgDownload.setOnClickListener(v -> {
                if (model.getSource() != null) {
                    onItemClick.onDownloadClick(model, binding);
                }
            });
        }

    }


}

package com.retry.vuga.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.gson.Gson
import com.retry.vuga.R
import com.retry.vuga.activities.MovieDetailActivity
import com.retry.vuga.activities.PlayerNewActivity
import com.retry.vuga.databinding.ItemMovieHistoryBinding
import com.retry.vuga.model.history.MovieHistory
import com.retry.vuga.utils.Const
import com.retry.vuga.utils.CustomDialogBuilder
import com.retry.vuga.utils.SessionManager

class MovieHistoryAdapter : RecyclerView.Adapter<MovieHistoryAdapter.MovieHistoryViewHolder>() {
    private var movieHistories = ArrayList<MovieHistory>()
    var onUpdateList: OnUpdateList? = null

    interface OnUpdateList {
        fun onUpdateList(isEmpty: Boolean)
    }

    inner class MovieHistoryViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding: ItemMovieHistoryBinding = DataBindingUtil.bind(itemView)!!


        fun setModel(position: Int) {
            val model = movieHistories[position]
            model.sources?.sortedBy { sourceItem -> sourceItem.time }
            val source = model.sources?.get((model.sources?.size ?: 1) - 1)
            if (source?.playProgress != null) {
                binding.pbPlayer.progress = source.playProgress
            }
            binding.ivDelete.setOnClickListener {
                CustomDialogBuilder(itemView.context).showSimplePopup(
                    false,
                    itemView.context.resources.getString(R.string.do_you_really_want_to_delete_this_from_history),
                    object : CustomDialogBuilder.OnDismissListener {
                        override fun onPositiveDismiss() {
                            SessionManager(itemView.context).deleteMovieFromHistory(
                                model.movieId ?: -1
                            )
                            movieHistories.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, movieHistories.size)
                            onUpdateList?.onUpdateList(movieHistories.isEmpty())
                        }

                        override fun onDismiss() {

                        }
                    })
            }
            binding.root.setOnClickListener {
                val intent = Intent(
                    itemView.context,
                    PlayerNewActivity::class.java
                )
                intent.putExtra(Const.DataKey.CONTENT_SOURCE, Gson().toJson(source))
                intent.putExtra(Const.DataKey.THUMBNAIL, model.thumbnail)
                intent.putExtra(Const.DataKey.NAME, model.movieName)
                intent.putExtra(Const.DataKey.CONTENT_NAME, model.movieName)
                intent.putExtra(Const.DataKey.CONTENT_ID, model.movieId)
//                intent.putExtra(Const.DataKey.SUB_TITLES, Gson().toJson(subTitlesList))
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                itemView.context.startActivity(intent)
            }
            binding.ivInfo.setOnClickListener {
                val intent = Intent(
                    itemView.context,
                    MovieDetailActivity::class.java
                )
                intent.putExtra(Const.DataKey.CONTENT_ID, model.movieId)
                itemView.context.startActivity(intent)
            }
            binding.model = model
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieHistoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_movie_history, parent, false)
        return MovieHistoryViewHolder(view)
    }

    override fun getItemCount() = movieHistories.size

    override fun onBindViewHolder(holder: MovieHistoryViewHolder, position: Int) {
        holder.setModel(position)
    }

    fun updateData(movieHistories: ArrayList<MovieHistory>) {

        this.movieHistories.clear()
        notifyItemRangeRemoved(0, this.movieHistories.size)
        for (data in movieHistories.reversed()) {
            this.movieHistories.add(data)
            notifyItemInserted(this.movieHistories.size - 1)
        }
    }
}
package com.vugaenterprises.androidtv.ui.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vugaenterprises.androidtv.R
import com.vugaenterprises.androidtv.data.model.EpisodeItem

class EpisodeAdapter(
    private val episodes: List<EpisodeItem>,
    private val seasonNumber: Int,
    private val onEpisodeClick: (EpisodeItem) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.episode_image)
        val titleText: TextView = itemView.findViewById(R.id.episode_title)
        val durationText: TextView = itemView.findViewById(R.id.episode_duration)
        val descriptionText: TextView = itemView.findViewById(R.id.episode_description)
        val playIcon: ImageView = itemView.findViewById(R.id.play_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.episode_card, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodes[position]
        
        // Load image
        holder.imageView.load(episode.thumbnail.ifEmpty { 
            "https://via.placeholder.com/200x120/333/666?text=Episode+${episode.number}" 
        }) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.placeholder_image)
        }
        
        // Set text - format episode title as S{season}E{episode}
        val episodeTitle = if (episode.title.isNotEmpty()) {
            // "S${seasonNumber}E${episode.number}: ${episode.title}"
            "S${seasonNumber}E${episode.number}"
        } else {
            "S${seasonNumber}E${episode.number}"
        }
        holder.titleText.text = episodeTitle
        holder.durationText.text = if (episode.duration.isNullOrEmpty()) "45 min" else episode.duration
        
        // Set description - use episode description if available, otherwise use a placeholder
        val description = if (episode.description.isNotEmpty()) {
            // Limit description length to prevent overflow
            if (episode.description.length > 120) {
                episode.description.substring(0, 120) + "..."
            } else {
                episode.description
            }
        } else {
            "S${seasonNumber}E${episode.number} of the series."
        }
        holder.descriptionText.text = description
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onEpisodeClick(episode)
        }
        
        // Set focus listener for Android TV
        holder.itemView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                holder.itemView.background = ContextCompat.getDrawable(
                    holder.itemView.context, 
                    R.drawable.episode_focused_background
                )
                holder.itemView.scaleX = 1.1f
                holder.itemView.scaleY = 1.1f
                
                // Debug log for episode focus
                android.util.Log.d("EpisodeAdapter", "S${seasonNumber}E${episode.number} focused: ${episode.title}")
            } else {
                holder.itemView.background = ContextCompat.getDrawable(
                    holder.itemView.context, 
                    R.drawable.episode_normal_background
                )
                holder.itemView.scaleX = 1.0f
                holder.itemView.scaleY = 1.0f
            }
        }
        
        // Make focusable for Android TV
        holder.itemView.isFocusable = true
        holder.itemView.isFocusableInTouchMode = true
    }

    override fun getItemCount(): Int = episodes.size
} 
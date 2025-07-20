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
import com.vugaenterprises.androidtv.data.model.Content

class ContentCardAdapter : RecyclerView.Adapter<ContentCardAdapter.ContentCardViewHolder>() {
    
    private var content: List<Content> = emptyList()
    private var onContentClickListener: ((Content) -> Unit)? = null
    
    fun setContent(newContent: List<Content>) {
        content = newContent
        notifyDataSetChanged()
    }
    
    fun setOnContentClickListener(listener: (Content) -> Unit) {
        onContentClickListener = listener
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.content_card, parent, false)
        return ContentCardViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ContentCardViewHolder, position: Int) {
        val item = content[position]
        
        // Load poster image
        holder.posterImage.load(item.verticalPoster.ifEmpty { item.horizontalPoster }) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.placeholder_image)
        }
        
        // Set title
        holder.titleText.text = item.title ?: "Unknown Title"
        
        // Set rating
        if (item.ratings > 0) {
            holder.ratingText.text = String.format("%.1f", item.ratings)
            holder.ratingText.visibility = View.VISIBLE
        } else {
            holder.ratingText.visibility = View.GONE
        }
        
        // Set year
        holder.yearText.text = item.releaseYear.toString()
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onContentClickListener?.invoke(item)
        }
        
        // Set focus listener for Android TV - same pattern as episode cards
        holder.itemView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                holder.itemView.background = ContextCompat.getDrawable(
                    holder.itemView.context, 
                    R.drawable.episode_focused_background
                )
                holder.itemView.scaleX = 1.1f
                holder.itemView.scaleY = 1.1f
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
    
    override fun getItemCount(): Int = content.size
    
    class ContentCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImage: ImageView = itemView.findViewById(R.id.posterImage)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val ratingText: TextView = itemView.findViewById(R.id.ratingText)
        val yearText: TextView = itemView.findViewById(R.id.yearText)
    }
} 
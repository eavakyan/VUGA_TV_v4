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

class FeaturedSliderAdapter : RecyclerView.Adapter<FeaturedSliderAdapter.FeaturedSliderViewHolder>() {
    
    private var content: List<Content> = emptyList()
    private var onContentClickListener: ((Content) -> Unit)? = null
    
    fun setContent(newContent: List<Content>) {
        content = newContent
        notifyDataSetChanged()
    }
    
    fun setOnContentClickListener(listener: (Content) -> Unit) {
        onContentClickListener = listener
        android.util.Log.d("FeaturedSliderAdapter", "Content click listener set")
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedSliderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.featured_slider_item, parent, false)
        return FeaturedSliderViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: FeaturedSliderViewHolder, position: Int) {
        val item = content[position]
        
        // Load background image
        holder.backgroundImage.load(item.horizontalPoster.ifEmpty { item.verticalPoster }) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.placeholder_image)
        }
        
        // Set title
        holder.titleText.text = item.title ?: "Unknown Title"
        
        // Set description
        holder.descriptionText.text = item.description ?: "No description available"
        
        // Set rating
        if (item.ratings > 0) {
            holder.ratingText.text = "★ ${String.format("%.1f", item.ratings)}"
            holder.ratingText.visibility = View.VISIBLE
        } else {
            holder.ratingText.visibility = View.GONE
        }
        
        // Set year and duration
        val details = buildString {
            append(item.releaseYear)
            if (!item.duration.isNullOrEmpty()) {
                append(" • ${item.duration}")
            }
        }
        holder.detailsText.text = details
        
        // Set click listener
        holder.itemView.setOnClickListener {
            android.util.Log.d("FeaturedSliderAdapter", "Featured item clicked: ${item.title}")
            onContentClickListener?.invoke(item)
        }
        
        // Add key event listener for Android TV remote
        holder.itemView.setOnKeyListener { view, keyCode, event ->
            when {
                // Handle ENTER/SELECT for clicking
                (keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER || 
                 keyCode == android.view.KeyEvent.KEYCODE_ENTER) && 
                 event.action == android.view.KeyEvent.ACTION_DOWN -> {
                    android.util.Log.d("FeaturedSliderAdapter", "Featured item ENTER pressed: ${item.title}")
                    onContentClickListener?.invoke(item)
                    true
                }
                // Handle UP navigation - let it bubble up to parent
                keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP && 
                event.action == android.view.KeyEvent.ACTION_DOWN -> {
                    android.util.Log.d("FeaturedSliderAdapter", "UP pressed on featured item - bubbling up")
                    // Return false to let the event bubble up to the parent
                    false
                }
                else -> false
            }
        }
        
        // Set focus listener for Android TV - same pattern as episode cards
        holder.itemView.setOnFocusChangeListener { _, hasFocus ->
            android.util.Log.d("FeaturedSliderAdapter", "Featured item focus changed: ${item.title}, hasFocus: $hasFocus")
            if (hasFocus) {
                holder.itemView.background = ContextCompat.getDrawable(
                    holder.itemView.context, 
                    R.drawable.episode_focused_background
                )
                holder.itemView.scaleX = 1.05f
                holder.itemView.scaleY = 1.05f
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
        
        // Override default padding/margins if needed
        holder.itemView.nextFocusUpId = View.NO_ID // Allow UP navigation to bubble up
    }
    
    override fun getItemCount(): Int = content.size
    
    class FeaturedSliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val backgroundImage: ImageView = itemView.findViewById(R.id.backgroundImage)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        val ratingText: TextView = itemView.findViewById(R.id.ratingText)
        val detailsText: TextView = itemView.findViewById(R.id.detailsText)
    }
} 
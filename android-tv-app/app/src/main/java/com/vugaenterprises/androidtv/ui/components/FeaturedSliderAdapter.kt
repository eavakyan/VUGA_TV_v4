package com.vugaenterprises.androidtv.ui.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vugaenterprises.androidtv.R
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.utils.TimeUtils

class FeaturedSliderAdapter(
    private val useSplitLayout: Boolean = true, 
    private val useImmersiveLayout: Boolean = false
) : RecyclerView.Adapter<FeaturedSliderAdapter.FeaturedSliderViewHolder>() {
    
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
        val layoutRes = when {
            useImmersiveLayout -> R.layout.featured_slider_item_immersive
            useSplitLayout -> R.layout.featured_slider_item_split
            else -> R.layout.featured_slider_item
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return FeaturedSliderViewHolder(view, useSplitLayout, useImmersiveLayout)
    }
    
    override fun onBindViewHolder(holder: FeaturedSliderViewHolder, position: Int) {
        val item = content[position]
        
        when {
            useImmersiveLayout -> {
                // Immersive layout: Load trailer video background with poster fallback
                // Only auto-play if this is the first/focused item for performance
                val shouldAutoPlay = position == 0
                holder.trailerBackgroundView?.setContent(
                    trailerUrl = item.trailerUrl,
                    posterUrl = item.horizontalPoster.ifEmpty { item.verticalPoster },
                    autoPlay = shouldAutoPlay
                )
                
                // Load vertical poster overlay
                holder.posterImage?.load(item.verticalPoster.ifEmpty { item.horizontalPoster }) {
                    crossfade(true)
                    placeholder(R.drawable.placeholder_image)
                    error(R.drawable.placeholder_image)
                }
                
                // Set metadata with proper formatting
                holder.yearText?.text = if (item.releaseYear > 0) item.releaseYear.toString() else "N/A"
                holder.durationText?.text = TimeUtils.formatRuntimeFromString(item.duration)
                
                // Setup action buttons
                holder.playButton?.setOnClickListener {
                    android.util.Log.d("FeaturedSliderAdapter", "Play button clicked: ${item.title}")
                    onContentClickListener?.invoke(item)
                }
                
                holder.infoButton?.setOnClickListener {
                    android.util.Log.d("FeaturedSliderAdapter", "Info button clicked: ${item.title}")
                    onContentClickListener?.invoke(item)
                }
            }
            
            useSplitLayout -> {
                // Split layout: Load poster on the left
                holder.posterImage?.load(item.verticalPoster.ifEmpty { item.horizontalPoster }) {
                    crossfade(true)
                    placeholder(R.drawable.placeholder_image)
                    error(R.drawable.placeholder_image)
                }
                
                // Handle trailer placeholder for now (could be enhanced with WebView/VideoView later)
                holder.trailerPlaceholder?.text = if (item.trailerUrl.isNotEmpty()) {
                    "Trailer Available"
                } else {
                    "No Trailer"
                }
                
                // Setup action buttons
                holder.playButton?.setOnClickListener {
                    android.util.Log.d("FeaturedSliderAdapter", "Play button clicked: ${item.title}")
                    onContentClickListener?.invoke(item)
                }
                
                holder.infoButton?.setOnClickListener {
                    android.util.Log.d("FeaturedSliderAdapter", "Info button clicked: ${item.title}")
                    onContentClickListener?.invoke(item)
                }
            }
            
            else -> {
                // Original layout: Load background image
                holder.backgroundImage?.load(item.verticalPoster.ifEmpty { item.horizontalPoster }) {
                    crossfade(true)
                    placeholder(R.drawable.placeholder_image)
                    error(R.drawable.placeholder_image)
                }
            }
        }
        
        // Set title
        holder.titleText.text = item.title ?: "Unknown Title"
        
        // Set description
        holder.descriptionText.text = item.description ?: "No description available"
        
        // Set rating
        if (item.ratings > 0) {
            val ratingText = if (useImmersiveLayout) {
                // For immersive layout, show just the number (star is separate)
                String.format("%.1f", item.ratings)
            } else {
                // For other layouts, include the star
                "★ ${String.format("%.1f", item.ratings)}"
            }
            holder.ratingText?.text = ratingText
            holder.ratingText?.visibility = View.VISIBLE
        } else {
            holder.ratingText?.visibility = View.GONE
        }
        
        // Set year and duration
        val details = buildString {
            if (item.releaseYear > 0) append(item.releaseYear)
            if (!item.duration.isNullOrEmpty()) {
                if (item.releaseYear > 0) append(" • ")
                append(item.duration)
            }
        }
        holder.detailsText?.text = details
        
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
        
        // Set focus listener for Android TV
        if (useImmersiveLayout) {
            // For immersive layout, handle video focus and button focus
            holder.itemView.setOnFocusChangeListener { _, hasFocus ->
                android.util.Log.d("FeaturedSliderAdapter", "Immersive item focus changed: ${item.title}, hasFocus: $hasFocus")
                
                // Control video playback based on focus
                holder.trailerBackgroundView?.onFocusChanged(hasFocus)
                
                if (hasFocus) {
                    holder.itemView.scaleX = 1.02f
                    holder.itemView.scaleY = 1.02f
                    // Give focus to play button by default
                    holder.playButton?.requestFocus()
                } else {
                    holder.itemView.scaleX = 1.0f
                    holder.itemView.scaleY = 1.0f
                }
            }
            
            // Set focus listeners for action buttons
            holder.playButton?.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    holder.playButton?.scaleX = 1.1f
                    holder.playButton?.scaleY = 1.1f
                } else {
                    holder.playButton?.scaleX = 1.0f
                    holder.playButton?.scaleY = 1.0f
                }
            }
            
            holder.infoButton?.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    holder.infoButton?.scaleX = 1.1f
                    holder.infoButton?.scaleY = 1.1f
                } else {
                    holder.infoButton?.scaleX = 1.0f
                    holder.infoButton?.scaleY = 1.0f
                }
            }
            
        } else if (useSplitLayout) {
            // For split layout, focus on the whole container but highlight play button
            holder.itemView.setOnFocusChangeListener { _, hasFocus ->
                android.util.Log.d("FeaturedSliderAdapter", "Featured item focus changed: ${item.title}, hasFocus: $hasFocus")
                if (hasFocus) {
                    holder.itemView.scaleX = 1.02f
                    holder.itemView.scaleY = 1.02f
                    // Give focus to play button by default
                    holder.playButton?.requestFocus()
                } else {
                    holder.itemView.scaleX = 1.0f
                    holder.itemView.scaleY = 1.0f
                }
            }
            
            // Set focus listeners for action buttons
            holder.playButton?.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    holder.playButton?.scaleX = 1.1f
                    holder.playButton?.scaleY = 1.1f
                } else {
                    holder.playButton?.scaleX = 1.0f
                    holder.playButton?.scaleY = 1.0f
                }
            }
            
            holder.infoButton?.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    holder.infoButton?.scaleX = 1.1f
                    holder.infoButton?.scaleY = 1.1f
                } else {
                    holder.infoButton?.scaleX = 1.0f
                    holder.infoButton?.scaleY = 1.0f
                }
            }
        } else {
            // Original focus handling for traditional layout
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
        }
        
        // Make focusable for Android TV
        if (useImmersiveLayout) {
            // Make buttons focusable in immersive layout
            holder.playButton?.isFocusable = true
            holder.playButton?.isFocusableInTouchMode = true
            holder.infoButton?.isFocusable = true  
            holder.infoButton?.isFocusableInTouchMode = true
            
            // Set up navigation between buttons
            holder.playButton?.nextFocusRightId = holder.infoButton?.id ?: View.NO_ID
            holder.infoButton?.nextFocusLeftId = holder.playButton?.id ?: View.NO_ID
            
            // Allow UP navigation to bubble up from buttons
            holder.playButton?.nextFocusUpId = View.NO_ID
            holder.infoButton?.nextFocusUpId = View.NO_ID
            
        } else if (useSplitLayout) {
            // Make buttons focusable in split layout
            holder.playButton?.isFocusable = true
            holder.playButton?.isFocusableInTouchMode = true
            holder.infoButton?.isFocusable = true  
            holder.infoButton?.isFocusableInTouchMode = true
            
            // Set up navigation between buttons
            holder.playButton?.nextFocusRightId = holder.infoButton?.id ?: View.NO_ID
            holder.infoButton?.nextFocusLeftId = holder.playButton?.id ?: View.NO_ID
            
            // Allow UP navigation to bubble up from buttons
            holder.playButton?.nextFocusUpId = View.NO_ID
            holder.infoButton?.nextFocusUpId = View.NO_ID
        } else {
            holder.itemView.isFocusable = true
            holder.itemView.isFocusableInTouchMode = true
            // Allow UP navigation to bubble up
            holder.itemView.nextFocusUpId = View.NO_ID
        }
    }
    
    override fun getItemCount(): Int = content.size
    
    override fun onViewRecycled(holder: FeaturedSliderViewHolder) {
        super.onViewRecycled(holder)
        // Stop video playback when view is recycled to save resources
        if (useImmersiveLayout) {
            holder.trailerBackgroundView?.stopVideo()
            android.util.Log.d("FeaturedSliderAdapter", "Video stopped for recycled view")
        }
    }
    
    override fun onViewAttachedToWindow(holder: FeaturedSliderViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (useImmersiveLayout) {
            android.util.Log.d("FeaturedSliderAdapter", "View attached to window")
        }
    }
    
    override fun onViewDetachedFromWindow(holder: FeaturedSliderViewHolder) {
        super.onViewDetachedFromWindow(holder)
        // Pause video when view is detached to save battery/performance
        if (useImmersiveLayout) {
            holder.trailerBackgroundView?.pauseVideo()
            android.util.Log.d("FeaturedSliderAdapter", "Video paused for detached view")
        }
    }
    
    class FeaturedSliderViewHolder(
        itemView: View, 
        private val isSplitLayout: Boolean, 
        private val isImmersiveLayout: Boolean = false
    ) : RecyclerView.ViewHolder(itemView) {
        // Common elements
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        val ratingText: TextView? = itemView.findViewById(R.id.ratingText)
        val detailsText: TextView? = itemView.findViewById(R.id.detailsText)
        
        // Original layout elements
        val backgroundImage: ImageView? = itemView.findViewById(R.id.backgroundImage)
        
        // Split layout elements
        val posterImage: ImageView? = itemView.findViewById(R.id.posterImage)
        val trailerContainer: FrameLayout? = itemView.findViewById(R.id.trailerContainer)
        val trailerPlaceholder: TextView? = itemView.findViewById(R.id.trailerPlaceholder)
        val playButton: Button? = itemView.findViewById(R.id.playButton)
        val infoButton: Button? = itemView.findViewById(R.id.infoButton)
        
        // Immersive layout elements
        val trailerBackgroundView: TrailerBackgroundView? = itemView.findViewById(R.id.trailerBackgroundView)
        val yearText: TextView? = itemView.findViewById(R.id.yearText)
        val durationText: TextView? = itemView.findViewById(R.id.durationText)
    }
} 
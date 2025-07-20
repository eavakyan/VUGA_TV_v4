package com.vugaenterprises.androidtv.ui.screens

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vugaenterprises.androidtv.R
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.data.model.CastItem
import com.vugaenterprises.androidtv.ui.components.CastMemberAdapter

class ContentInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val backButton: ImageView
    private val backgroundImage: ImageView
    private val posterImage: ImageView
    private val titleText: TextView
    private val ratingText: TextView
    private val yearText: TextView
    private val durationText: TextView
    private val genreText: TextView
    private val typeText: TextView
    private val descriptionText: TextView
    private val fullDescriptionText: TextView
    private val castRecyclerView: RecyclerView
    private val castCrewContainer: LinearLayout
    private val seasonsInfoContainer: LinearLayout
    private val seasonsInfoText: TextView
    private val detailYearText: TextView
    private val detailRatingText: TextView
    private val detailDurationText: TextView
    private val durationContainer: LinearLayout
    private val contentIdText: TextView
    
    private var onBackClick: (() -> Unit)? = null
    private var onCastMemberClick: ((CastItem) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.content_info_view, this, true)
        
        backButton = findViewById(R.id.backButton)
        backgroundImage = findViewById(R.id.backgroundImage)
        posterImage = findViewById(R.id.posterImage)
        titleText = findViewById(R.id.titleText)
        ratingText = findViewById(R.id.ratingText)
        yearText = findViewById(R.id.yearText)
        durationText = findViewById(R.id.durationText)
        genreText = findViewById(R.id.genreText)
        typeText = findViewById(R.id.typeText)
        descriptionText = findViewById(R.id.descriptionText)
        fullDescriptionText = findViewById(R.id.fullDescriptionText)
        castRecyclerView = findViewById(R.id.castRecyclerView)
        castCrewContainer = findViewById(R.id.castCrewContainer)
        seasonsInfoContainer = findViewById(R.id.seasonsInfoContainer)
        seasonsInfoText = findViewById(R.id.seasonsInfoText)
        detailYearText = findViewById(R.id.detailYearText)
        detailRatingText = findViewById(R.id.detailRatingText)
        detailDurationText = findViewById(R.id.detailDurationText)
        durationContainer = findViewById(R.id.durationContainer)
        contentIdText = findViewById(R.id.contentIdText)
        
        // Setup RecyclerView
        castRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
        // Setup click listeners
        backButton.setOnClickListener {
            onBackClick?.invoke()
        }
        
        // Handle back key press for the entire view
        isFocusableInTouchMode = true
        isFocusable = true
        
        // Override key event handling to intercept back button
        setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_DOWN) {
                onBackClick?.invoke()
                true
            } else {
                false
            }
        }
        
        // Setup button focus listener
        backButton.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                backButton.scaleX = 1.1f
                backButton.scaleY = 1.1f
            } else {
                backButton.scaleX = 1.0f
                backButton.scaleY = 1.0f
            }
        }
        
        // Auto-focus back button
        backButton.post {
            backButton.requestFocus()
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            onBackClick?.invoke()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    
    override fun dispatchKeyEvent(event: android.view.KeyEvent?): Boolean {
        if (event?.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_DOWN) {
            onBackClick?.invoke()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    fun setOnBackClick(listener: () -> Unit) {
        this.onBackClick = listener
    }
    
    fun setOnCastMemberClick(listener: (CastItem) -> Unit) {
        this.onCastMemberClick = listener
    }

    fun setContent(content: Content) {
        // Load background image
        val backgroundUrl = if (content.horizontalPoster.isNotEmpty()) {
            content.horizontalPoster
        } else {
            content.verticalPoster
        }
        
        backgroundImage.load(backgroundUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.placeholder_image)
        }
        
        // Load poster image
        val posterUrl = if (content.verticalPoster.isNotEmpty()) {
            content.verticalPoster
        } else {
            content.horizontalPoster
        }
        
        posterImage.load(posterUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.placeholder_image)
        }
        
        // Set basic info
        titleText.text = content.title ?: "Unknown Title"
        
        if (content.ratings > 0) {
            ratingText.text = "★ ${String.format("%.1f", content.ratings)}"
            ratingText.visibility = View.VISIBLE
        } else {
            ratingText.visibility = View.GONE
        }
        
        yearText.text = content.releaseYear.toString()
        
        val duration = content.duration ?: ""
        if (duration.isNotEmpty()) {
            durationText.text = duration
            durationText.visibility = View.VISIBLE
        } else {
            durationText.visibility = View.GONE
        }
        
        // Set genres
        val genres = content.genreList.joinToString(", ")
        if (genres.isNotEmpty()) {
            genreText.text = genres
            genreText.visibility = View.VISIBLE
        } else {
            genreText.visibility = View.GONE
        }
        
        // Set type - check if content has seasons/episodes to determine if it's a TV show
        val isActuallyTVShow = content.isShow == 1 && content.seasons.isNotEmpty()
        typeText.text = if (isActuallyTVShow) "TV Show" else "Movie"
        
        // Set description (short version in hero section)
        val description = content.description ?: "No description available"
        descriptionText.text = description
        
        // Set full description (detailed version in synopsis section)
        fullDescriptionText.text = description
        
        // Set cast information
        if (content.contentCast.isNotEmpty()) {
            val adapter = CastMemberAdapter(content.contentCast.take(10)) { castMember ->
                onCastMemberClick?.invoke(castMember)
            }
            castRecyclerView.adapter = adapter
            castCrewContainer.visibility = View.VISIBLE
        } else {
            castCrewContainer.visibility = View.GONE
        }
        
        // Set seasons info for TV shows
        if (content.isShow == 1 && content.seasons.isNotEmpty()) {
            val seasonsInfo = buildString {
                append("${content.seasons.size} Season${if (content.seasons.size > 1) "s" else ""}\n\n")
                content.seasons.forEach { season ->
                    append("${season.title.ifEmpty { "Season ${season.id}" }}: ${season.episodes.size} Episode${if (season.episodes.size > 1) "s" else ""}\n")
                    if (season.episodes.isNotEmpty()) {
                        season.episodes.take(3).forEach { episode ->
                            append("  • Episode ${episode.number}: ${episode.title}\n")
                        }
                        if (season.episodes.size > 3) {
                            append("  • ... and ${season.episodes.size - 3} more episodes\n")
                        }
                    }
                    append("\n")
                }
            }
            seasonsInfoText.text = seasonsInfo.trim()
            seasonsInfoContainer.visibility = View.VISIBLE
        } else {
            seasonsInfoContainer.visibility = View.GONE
        }
        
        // Set detail info
        detailYearText.text = content.releaseYear.toString()
        
        if (content.ratings > 0) {
            detailRatingText.text = "${String.format("%.1f", content.ratings)}/10"
        } else {
            detailRatingText.text = "Not rated"
        }
        
        if (duration.isNotEmpty()) {
            detailDurationText.text = duration
            durationContainer.visibility = View.VISIBLE
        } else {
            durationContainer.visibility = View.GONE
        }
        
        contentIdText.text = content.contentId.toString()
    }
} 
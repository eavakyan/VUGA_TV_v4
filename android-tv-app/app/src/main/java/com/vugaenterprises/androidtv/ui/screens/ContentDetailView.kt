package com.vugaenterprises.androidtv.ui.screens

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vugaenterprises.androidtv.R
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.data.model.SeasonItem
import com.vugaenterprises.androidtv.data.model.EpisodeItem
import com.vugaenterprises.androidtv.data.model.CastItem
import com.vugaenterprises.androidtv.ui.components.ContentCardAdapter
import com.vugaenterprises.androidtv.ui.components.EpisodeAdapter
import com.vugaenterprises.androidtv.ui.components.CastMemberAdapter

class ContentDetailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val backButton: ImageView
    private val backgroundImage: ImageView
    private val posterImage: ImageView
    private val titleText: TextView
    private val descriptionText: TextView
    private val ratingText: TextView
    private val yearText: TextView
    private val durationText: TextView
    private val genreText: TextView
    private val castText: TextView
    private val playButton: TextView
    private val moreInfoButton: TextView
    private val seasonsContainer: LinearLayout
    private val seasonSpinnerContainer: LinearLayout
    private val seasonSpinner: Spinner
    private val episodesRecyclerView: RecyclerView
    private val castCrewContainer: LinearLayout
    private val castRecyclerView: RecyclerView
    private val relatedContainer: LinearLayout
    private val relatedRecyclerView: RecyclerView
    
    private var onBackClick: (() -> Unit)? = null
    private var onPlayClick: ((Content) -> Unit)? = null
    private var onSeasonClick: ((SeasonItem) -> Unit)? = null
    private var onEpisodeClick: ((EpisodeItem) -> Unit)? = null
    private var onContentClick: ((Content) -> Unit)? = null
    private var onMoreInfoClick: ((Content) -> Unit)? = null
    private var onCastMemberClick: ((CastItem) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.content_detail_view, this, true)
        
        backButton = findViewById(R.id.backButton)
        backgroundImage = findViewById(R.id.backgroundImage)
        posterImage = findViewById(R.id.posterImage)
        titleText = findViewById(R.id.titleText)
        descriptionText = findViewById(R.id.descriptionText)
        ratingText = findViewById(R.id.ratingText)
        yearText = findViewById(R.id.yearText)
        durationText = findViewById(R.id.durationText)
        genreText = findViewById(R.id.genreText)
        castText = findViewById(R.id.castText)
        playButton = findViewById(R.id.playButton)
        moreInfoButton = findViewById(R.id.moreInfoButton)
        seasonsContainer = findViewById(R.id.seasonsContainer)
        seasonSpinnerContainer = findViewById(R.id.seasonSpinnerContainer)
        seasonSpinner = findViewById(R.id.seasonSpinner)
        episodesRecyclerView = findViewById(R.id.episodesRecyclerView)
        castCrewContainer = findViewById(R.id.castCrewContainer)
        castRecyclerView = findViewById(R.id.castRecyclerView)
        relatedContainer = findViewById(R.id.relatedContainer)
        relatedRecyclerView = findViewById(R.id.relatedRecyclerView)
        
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

    fun setOnPlayClick(listener: (Content) -> Unit) {
        this.onPlayClick = listener
    }

    fun setOnSeasonClick(listener: (SeasonItem) -> Unit) {
        this.onSeasonClick = listener
    }
    
    fun setOnEpisodeClick(listener: (EpisodeItem) -> Unit) {
        this.onEpisodeClick = listener
    }
    
    fun setOnContentClick(listener: (Content) -> Unit) {
        this.onContentClick = listener
    }
    
    fun setOnMoreInfoClick(listener: (Content) -> Unit) {
        this.onMoreInfoClick = listener
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
        
        // Set text content
        titleText.text = content.title ?: "Unknown Title"
        descriptionText.text = content.description ?: "No description available"
        
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
        val genres = content.genreList.take(3).joinToString(", ")
        if (genres.isNotEmpty()) {
            genreText.text = genres
            genreText.visibility = View.VISIBLE
        } else {
            genreText.visibility = View.GONE
        }
        
        // Set cast information (now in hero section)
        val cast = content.contentCast.take(5).joinToString(", ") { it.actor.name }
        if (cast.isNotEmpty()) {
            castText.text = "Cast: $cast"
            castText.visibility = View.VISIBLE
        } else {
            castText.visibility = View.GONE
        }
        
        // Setup cast & crew section
        if (content.contentCast.isNotEmpty()) {
            val castAdapter = CastMemberAdapter(content.contentCast.take(10)) { castMember ->
                onCastMemberClick?.invoke(castMember)
            }
            castRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            castRecyclerView.adapter = castAdapter
            castCrewContainer.visibility = View.VISIBLE
            
            android.util.Log.d("ContentDetailView", "Cast section set up with ${content.contentCast.size} cast members")
        } else {
            castCrewContainer.visibility = View.GONE
        }
        
        // Setup related content
        if (content.moreLikeThis.isNotEmpty()) {
            val relatedAdapter = ContentCardAdapter()
            relatedAdapter.setContent(content.moreLikeThis)
            relatedAdapter.setOnContentClickListener { relatedContent ->
                onContentClick?.invoke(relatedContent)
            }
            relatedRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            relatedRecyclerView.adapter = relatedAdapter
            relatedContainer.visibility = View.VISIBLE
        } else {
            relatedContainer.visibility = View.GONE
        }
        
        // Handle seasons for TV shows
        if (content.isShow == 1 && content.seasons.isNotEmpty()) {
            seasonsContainer.visibility = View.VISIBLE
            
            // Setup season spinner (hide if only one season)
            if (content.seasons.size == 1) {
                // Hide spinner container entirely for single season shows
                seasonSpinnerContainer.visibility = View.GONE
                android.util.Log.d("ContentDetailView", "Hiding season spinner - single season show")
            } else {
                // Show and setup spinner for multi-season shows
                seasonSpinnerContainer.visibility = View.VISIBLE
                val seasonNames = content.seasons.map { it.title.ifEmpty { "Season ${it.id}" } }
                
                val seasonAdapter = android.widget.ArrayAdapter(context, android.R.layout.simple_spinner_item, seasonNames)
                seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                seasonSpinner.adapter = seasonAdapter
                seasonSpinner.isEnabled = true
                seasonSpinner.alpha = 1.0f
            }
            
            // Setup episodes for first season
            if (content.seasons.isNotEmpty()) {
                val firstSeason = content.seasons.first()
                val episodeAdapter = EpisodeAdapter(firstSeason.episodes, firstSeason.id) { episode ->
                    onEpisodeClick?.invoke(episode)
                }
                episodesRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                episodesRecyclerView.adapter = episodeAdapter
                
                // Add focus listener for debugging navigation
                episodesRecyclerView.setOnFocusChangeListener { _, hasFocus ->
                    android.util.Log.d("ContentDetailView", "Episodes RecyclerView focus changed: $hasFocus")
                }
                
                // Debug log for episode setup
                android.util.Log.d("ContentDetailView", "First season has ${content.seasons.first().episodes.size} episodes")
                android.util.Log.d("ContentDetailView", "Episode adapter set with ${content.seasons.first().episodes.size} episodes")
            }
            
            // Season spinner listener
            seasonSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    if (position < content.seasons.size) {
                        val selectedSeason = content.seasons[position]
                        val episodeAdapter = EpisodeAdapter(selectedSeason.episodes, selectedSeason.id) { episode ->
                            onEpisodeClick?.invoke(episode)
                        }
                        episodesRecyclerView.adapter = episodeAdapter
                    }
                }
                
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
            
            // For TV shows, play button shows which episode it will play
            val firstEpisode = content.seasons.first().episodes.firstOrNull()
            if (firstEpisode != null) {
                playButton.text = "▶ Play S${content.seasons.first().id}E${firstEpisode.number}"
                playButton.setOnClickListener {
                    onEpisodeClick?.invoke(firstEpisode)
                }
            } else {
                playButton.text = "▶ Play"
                playButton.setOnClickListener {
                    onPlayClick?.invoke(content)
                }
            }
        } else {
            seasonsContainer.visibility = View.GONE
            
            // For movies, play button starts playback directly
            playButton.text = "▶ Play"
            playButton.setOnClickListener {
                onPlayClick?.invoke(content)
            }
        }
        
        // Setup button focus listeners
        playButton.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                playButton.scaleX = 1.1f
                playButton.scaleY = 1.1f
            } else {
                playButton.scaleX = 1.0f
                playButton.scaleY = 1.0f
            }
        }
        
        moreInfoButton.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                moreInfoButton.scaleX = 1.1f
                moreInfoButton.scaleY = 1.1f
            } else {
                moreInfoButton.scaleX = 1.0f
                moreInfoButton.scaleY = 1.0f
            }
        }
        
        backButton.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                backButton.scaleX = 1.1f
                backButton.scaleY = 1.1f
            } else {
                backButton.scaleX = 1.0f
                backButton.scaleY = 1.0f
            }
        }
        
        // Setup More Info button click
        moreInfoButton.setOnClickListener {
            onMoreInfoClick?.invoke(content)
        }
        
        // Set up navigation between elements - default to cast or related
        val hasCast = content.contentCast.isNotEmpty()
        val hasRelated = content.moreLikeThis.isNotEmpty()
        
        val nextFocusFromButtons = when {
            hasCast -> R.id.castRecyclerView
            hasRelated -> R.id.relatedRecyclerView
            else -> View.NO_ID
        }
        
        playButton.nextFocusDownId = nextFocusFromButtons
        moreInfoButton.nextFocusDownId = nextFocusFromButtons
        
        // For TV shows, set up proper focus navigation flow
        if (content.isShow == 1 && content.seasons.isNotEmpty()) {
            if (content.seasons.size == 1) {
                // Single season: Skip spinner, go directly to episodes
                playButton.nextFocusDownId = R.id.episodesRecyclerView
                moreInfoButton.nextFocusDownId = R.id.episodesRecyclerView
                
                // Episodes -> Cast -> Related
                val nextFromEpisodes = when {
                    hasCast -> R.id.castRecyclerView
                    hasRelated -> R.id.relatedRecyclerView
                    else -> View.NO_ID
                }
                episodesRecyclerView.nextFocusDownId = nextFromEpisodes
                
                // Allow navigation back up directly to buttons
                episodesRecyclerView.nextFocusUpId = R.id.playButton
                
                android.util.Log.d("ContentDetailView", "Focus navigation set for single season show")
            } else {
                // Multiple seasons: Include spinner in navigation flow
                playButton.nextFocusDownId = R.id.seasonSpinner
                moreInfoButton.nextFocusDownId = R.id.seasonSpinner
                seasonSpinner.nextFocusDownId = R.id.episodesRecyclerView
                
                // Episodes -> Cast -> Related
                val nextFromEpisodes = when {
                    hasCast -> R.id.castRecyclerView
                    hasRelated -> R.id.relatedRecyclerView
                    else -> View.NO_ID
                }
                episodesRecyclerView.nextFocusDownId = nextFromEpisodes
                
                // Allow navigation back up
                episodesRecyclerView.nextFocusUpId = R.id.seasonSpinner
                
                android.util.Log.d("ContentDetailView", "Focus navigation set for multi-season show")
            }
        }
        
        // Set up cast to related navigation
        if (hasCast && hasRelated) {
            castRecyclerView.nextFocusDownId = R.id.relatedRecyclerView
            relatedRecyclerView.nextFocusUpId = R.id.castRecyclerView
        }
        
        // Set up cast back navigation
        if (hasCast) {
            if (content.isShow == 1 && content.seasons.isNotEmpty()) {
                castRecyclerView.nextFocusUpId = R.id.episodesRecyclerView
            } else {
                castRecyclerView.nextFocusUpId = R.id.playButton
            }
        }
        
        // Auto-focus play button
        playButton.post {
            playButton.requestFocus()
        }
    }
} 
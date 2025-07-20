package com.vugaenterprises.androidtv.ui.screens

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import android.widget.ProgressBar
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.PagerSnapHelper
import com.vugaenterprises.androidtv.R
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.ui.components.ContentCardAdapter
import com.vugaenterprises.androidtv.ui.components.FeaturedSliderAdapter

class HomeScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val titleText: TextView
    private val searchButton: ImageView
    private val loadingIndicator: ProgressBar
    private val errorText: TextView
    private val contentContainer: LinearLayout
    
    private var onContentClick: ((Content) -> Unit)? = null
    private var onNavigateToSearch: (() -> Unit)? = null
    private var onNavigateToProfile: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.home_screen_view, this, true)
        
        titleText = findViewById(R.id.titleText)
        searchButton = findViewById(R.id.searchButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        errorText = findViewById(R.id.errorText)
        contentContainer = findViewById(R.id.contentContainer)
        
        // Setup click listeners
        searchButton.setOnClickListener {
            onNavigateToSearch?.invoke()
        }
        
        // Make search button focusable
        searchButton.isFocusable = true
        searchButton.isFocusableInTouchMode = true
        
        // Setup focus listener for search button
        searchButton.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchButton.scaleX = 1.1f
                searchButton.scaleY = 1.1f
                searchButton.alpha = 1.0f
            } else {
                searchButton.scaleX = 1.0f
                searchButton.scaleY = 1.0f
                searchButton.alpha = 0.7f
            }
        }
    }
    
    fun setOnContentClick(listener: (Content) -> Unit) {
        this.onContentClick = listener
    }
    
    fun setOnNavigateToSearch(listener: () -> Unit) {
        this.onNavigateToSearch = listener
    }
    
    fun setOnNavigateToProfile(listener: () -> Unit) {
        this.onNavigateToProfile = listener
    }
    
    fun showLoading() {
        loadingIndicator.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        contentContainer.visibility = View.GONE
    }
    
    fun showError(error: String) {
        loadingIndicator.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        errorText.text = error
        contentContainer.visibility = View.GONE
    }
    
    fun updateContent(
        featuredContent: List<Content>,
        trendingContent: List<Content>,
        newContent: List<Content>,
        continueWatching: List<Content>,
        recommendations: List<Content>
    ) {
        loadingIndicator.visibility = View.GONE
        errorText.visibility = View.GONE
        contentContainer.visibility = View.VISIBLE
        
        // Clear existing content
        contentContainer.removeAllViews()
        
        // Add featured content slider if available
        if (featuredContent.isNotEmpty()) {
            addFeaturedSection(featuredContent)
        }
        
        // Add content rows
        if (trendingContent.isNotEmpty()) {
            addContentRow("Trending", trendingContent)
        }
        
        if (recommendations.isNotEmpty()) {
            addContentRow("Watchlist", recommendations)
        }
        
        if (continueWatching.isNotEmpty()) {
            addContentRow("Continue Watching", continueWatching)
        }
        
        if (newContent.isNotEmpty()) {
            addContentRow("New Releases", newContent)
        }
    }
    
    private fun addFeaturedSection(content: List<Content>) {
        val featuredView = FeaturedSliderView(context)
        featuredView.setContent(content)
        featuredView.setOnContentClick { content ->
            onContentClick?.invoke(content)
        }
        contentContainer.addView(featuredView)
        
        // Ensure the featured section gets focus when it's the first element
        featuredView.post {
            // Request focus on the RecyclerView specifically, not the container
            val recyclerView = featuredView.findViewById<RecyclerView>(R.id.featuredRecyclerView)
            if (recyclerView != null) {
                recyclerView.requestFocus()
                android.util.Log.d("HomeScreenView", "Requested focus on featured RecyclerView")
            } else {
                featuredView.requestFocus()
                android.util.Log.d("HomeScreenView", "Requested focus on featured section")
            }
        }
    }
    
    private fun addContentRow(title: String, content: List<Content>) {
        val rowView = ContentRowView(context)
        rowView.setTitle(title)
        rowView.setContent(content)
        rowView.setOnContentClick { content ->
            onContentClick?.invoke(content)
        }
        contentContainer.addView(rowView)
    }
}

class ContentRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val titleText: TextView
    private val recyclerView: RecyclerView
    private val adapter: ContentCardAdapter
    
    private var onContentClick: ((Content) -> Unit)? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.content_row_view, this, true)
        
        titleText = findViewById(R.id.rowTitle)
        recyclerView = findViewById(R.id.contentRecyclerView)
        
        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = ContentCardAdapter()
        recyclerView.adapter = adapter
        
        // Enable focus for Android TV
        recyclerView.isFocusable = true
        recyclerView.isFocusableInTouchMode = true
        
        adapter.setOnContentClickListener { content ->
            onContentClick?.invoke(content)
        }
    }
    
    fun setTitle(title: String) {
        titleText.text = title
    }
    
    fun setContent(content: List<Content>) {
        adapter.setContent(content)
    }
    
    fun setOnContentClick(listener: (Content) -> Unit) {
        this.onContentClick = listener
    }
}

class FeaturedSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val recyclerView: RecyclerView
    private val adapter: FeaturedSliderAdapter
    private val autoSlideHandler = Handler(Looper.getMainLooper())
    private var autoSlideRunnable: Runnable? = null
    private var currentPosition = 0
    private var isAutoSliding = true
    private var isProgrammaticFocusChange = false
    
    private var onContentClick: ((Content) -> Unit)? = null
    
    companion object {
        private const val AUTO_SLIDE_DELAY = 5000L // 5 seconds
    }

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.featured_slider_view, this, true)
        
        recyclerView = findViewById(R.id.featuredRecyclerView)
        
        // Setup RecyclerView with PagerSnapHelper for single item display
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        
        // Add snap helper to make it behave like a ViewPager
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
        
        adapter = FeaturedSliderAdapter()
        recyclerView.adapter = adapter
        
        // Enable focus for Android TV
        recyclerView.isFocusable = true
        recyclerView.isFocusableInTouchMode = true
        
        adapter.setOnContentClickListener { content ->
            android.util.Log.d("FeaturedSlider", "Content click callback received: ${content.title}")
            onContentClick?.invoke(content)
        }
        
        // Add scroll listener to track position changes
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        // User started scrolling - pause auto slide
                        android.util.Log.d("FeaturedSlider", "User started dragging - pausing auto-slide")
                        pauseAutoSlide()
                    }
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        // Scrolling finished - update current position
                        updateCurrentPosition()
                        android.util.Log.d("FeaturedSlider", "Scroll idle - position updated to $currentPosition")
                        
                        // Only resume auto-slide if still in focus
                        if (recyclerView.hasFocus()) {
                            autoSlideHandler.postDelayed({
                                if (recyclerView.hasFocus()) {
                                    resumeAutoSlide()
                                }
                            }, 2000L)
                        }
                    }
                }
            }
        })
        
        // Add focus listener to control auto slide based on focus state
        recyclerView.setOnFocusChangeListener { _, hasFocus ->
            if (!isProgrammaticFocusChange) {
                if (hasFocus) {
                    android.util.Log.d("FeaturedSlider", "RecyclerView gained focus (user) - enabling auto-slide")
                    // Start auto-slide after a short delay to allow focus to settle
                    autoSlideHandler.postDelayed({
                        if (recyclerView.hasFocus()) {
                            resumeAutoSlide()
                        }
                    }, 1000L)
                } else {
                    android.util.Log.d("FeaturedSlider", "RecyclerView lost focus (user) - pausing auto-slide")
                    pauseAutoSlide()
                }
            } else {
                android.util.Log.d("FeaturedSlider", "RecyclerView focus changed (programmatic) - ignoring")
            }
        }
        
        // Add key listener to RecyclerView to handle navigation and clicks
        recyclerView.setOnKeyListener { _, keyCode, event ->
            android.util.Log.d("FeaturedSlider", "RecyclerView key event: keyCode=$keyCode, action=${event.action}")
            
            if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                    android.view.KeyEvent.KEYCODE_ENTER -> {
                        // Handle click on current item
                        val focusedChild = recyclerView.focusedChild
                        if (focusedChild != null) {
                            android.util.Log.d("FeaturedSlider", "RecyclerView triggering click on focused child")
                            focusedChild.performClick()
                            return@setOnKeyListener true
                        }
                    }
                    
                    android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                        // Navigate to previous item or loop to end
                        android.util.Log.d("FeaturedSlider", "LEFT pressed - navigating to previous item")
                        if (currentPosition > 0) {
                            currentPosition--
                        } else {
                            // Loop to last item when at the beginning
                            currentPosition = adapter.itemCount - 1
                        }
                        recyclerView.smoothScrollToPosition(currentPosition)
                        pauseAutoSlide()
                        // Resume auto-slide after user interaction
                        autoSlideHandler.postDelayed({
                            if (recyclerView.hasFocus()) {
                                resumeAutoSlide()
                            }
                        }, 3000L)
                        return@setOnKeyListener true
                    }
                    
                    android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        // Navigate to next item or loop to beginning
                        android.util.Log.d("FeaturedSlider", "RIGHT pressed - navigating to next item")
                        if (currentPosition < adapter.itemCount - 1) {
                            currentPosition++
                        } else {
                            // Loop back to first item when at the end
                            currentPosition = 0
                        }
                        recyclerView.smoothScrollToPosition(currentPosition)
                        pauseAutoSlide()
                        // Resume auto-slide after user interaction
                        autoSlideHandler.postDelayed({
                            if (recyclerView.hasFocus()) {
                                resumeAutoSlide()
                            }
                        }, 3000L)
                        return@setOnKeyListener true
                    }
                }
            }
            false
        }
        
        // Make the entire FeaturedSliderView focusable
        isFocusable = true
        isFocusableInTouchMode = true
    }
    
    fun setContent(content: List<Content>) {
        adapter.setContent(content)
        currentPosition = 0
        android.util.Log.d("FeaturedSlider", "Setting ${content.size} featured items")
        if (content.isNotEmpty()) {
            // Request focus on the first item to make it immediately clickable
            post {
                transferFocusToCurrentPosition()
                android.util.Log.d("FeaturedSlider", "Initial focus set on position $currentPosition")
                // Start auto-slide after initial setup
                autoSlideHandler.postDelayed({
                    startAutoSlide()
                }, 2000L) // 2 second delay for initial auto-slide
            }
        }
    }
    
    fun setOnContentClick(listener: (Content) -> Unit) {
        this.onContentClick = listener
        android.util.Log.d("FeaturedSlider", "FeaturedSliderView onContentClick listener set")
    }
    
    private fun startAutoSlide() {
        if (adapter.itemCount <= 1) return
        
        // Clear any existing timer first
        pauseAutoSlide()
        
        isAutoSliding = true
        autoSlideRunnable = Runnable {
            slideToNext()
        }
        autoSlideHandler.postDelayed(autoSlideRunnable!!, AUTO_SLIDE_DELAY)
        android.util.Log.d("FeaturedSlider", "Started auto-slide timer")
    }
    
    private fun slideToNext() {
        // Only slide if we have items, auto-sliding is enabled, and RecyclerView still has focus
        if (adapter.itemCount <= 1 || !isAutoSliding || !recyclerView.hasFocus()) {
            android.util.Log.d("FeaturedSlider", "Stopping auto-slide: itemCount=${adapter.itemCount}, isAutoSliding=$isAutoSliding, hasFocus=${recyclerView.hasFocus()}")
            return
        }
        
        currentPosition = (currentPosition + 1) % adapter.itemCount
        
        android.util.Log.d("FeaturedSlider", "Auto-sliding to position $currentPosition of ${adapter.itemCount}")
        
        // Create smooth scroller
        val smoothScroller = object : LinearSmoothScroller(context) {
            override fun getHorizontalSnapPreference(): Int = SNAP_TO_START
            
            override fun onTargetFound(targetView: android.view.View, state: RecyclerView.State, action: RecyclerView.SmoothScroller.Action) {
                super.onTargetFound(targetView, state, action)
                // After scrolling completes, transfer focus to the new visible item
                targetView.post {
                    transferFocusToCurrentPosition()
                }
            }
        }
        smoothScroller.targetPosition = currentPosition
        recyclerView.layoutManager?.startSmoothScroll(smoothScroller)
        
        // Schedule next slide only if still auto-sliding and still has focus
        if (isAutoSliding && autoSlideRunnable != null && recyclerView.hasFocus()) {
            autoSlideHandler.postDelayed(autoSlideRunnable!!, AUTO_SLIDE_DELAY)
        }
    }
    
    private fun pauseAutoSlide() {
        isAutoSliding = false
        autoSlideRunnable?.let { 
            autoSlideHandler.removeCallbacks(it) 
            android.util.Log.d("FeaturedSlider", "Paused auto-slide timer")
        }
    }
    
    private fun resumeAutoSlide() {
        // Only start auto-slide if we have multiple items and not already sliding
        if (adapter.itemCount > 1 && !isAutoSliding) {
            // Clear any existing callbacks first
            autoSlideRunnable?.let { autoSlideHandler.removeCallbacks(it) }
            
            isAutoSliding = true
            autoSlideRunnable = Runnable {
                slideToNext()
            }
            autoSlideHandler.postDelayed(autoSlideRunnable!!, AUTO_SLIDE_DELAY)
            android.util.Log.d("FeaturedSlider", "Resumed auto-slide timer")
        } else {
            android.util.Log.d("FeaturedSlider", "Cannot resume auto-slide: itemCount=${adapter.itemCount}, isAutoSliding=$isAutoSliding")
        }
    }
    
    private fun updateCurrentPosition() {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
        layoutManager?.let {
            val oldPosition = currentPosition
            currentPosition = it.findFirstVisibleItemPosition()
            if (currentPosition == RecyclerView.NO_POSITION) {
                currentPosition = 0
            }
            
            // If position changed during manual scroll, transfer focus
            if (oldPosition != currentPosition) {
                android.util.Log.d("FeaturedSlider", "Position changed from $oldPosition to $currentPosition, transferring focus")
                transferFocusToCurrentPosition()
            }
        }
    }
    
    private fun transferFocusToCurrentPosition() {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
        layoutManager?.let {
            val viewAtPosition = it.findViewByPosition(currentPosition)
            if (viewAtPosition != null && viewAtPosition.isFocusable) {
                isProgrammaticFocusChange = true
                viewAtPosition.requestFocus()
                android.util.Log.d("FeaturedSlider", "Transferred focus to position $currentPosition")
                // Reset flag after a short delay
                post {
                    isProgrammaticFocusChange = false
                }
            } else {
                android.util.Log.d("FeaturedSlider", "Could not find focusable view at position $currentPosition")
            }
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pauseAutoSlide()
    }
} 
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
import com.vugaenterprises.androidtv.data.model.GenreContents
import com.vugaenterprises.androidtv.ui.components.ContentCardAdapter
import com.vugaenterprises.androidtv.ui.components.FeaturedSliderAdapter

open class HomeScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val loadingIndicator: ProgressBar
    private val errorText: TextView
    protected val contentContainer: LinearLayout
    
    private var onContentClick: ((Content) -> Unit)? = null
    private var onNavigateToSearch: (() -> Unit)? = null
    private var onNavigateToProfile: (() -> Unit)? = null
    protected var onNavigateUpCallback: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.home_screen_view, this, true)
        
        loadingIndicator = findViewById(R.id.loadingIndicator)
        errorText = findViewById(R.id.errorText)
        contentContainer = findViewById(R.id.contentContainer)
        
        // Make sure we can intercept key events
        isFocusable = true
        isFocusableInTouchMode = false
    }
    
    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        android.util.Log.d("HomeScreenView", "dispatchKeyEvent: keyCode=${event.keyCode}, action=${event.action}")
        
        if (event.keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP && 
            event.action == android.view.KeyEvent.ACTION_DOWN) {
            
            // Check if any child has focus
            val focusedChild = findFocus()
            android.util.Log.d("HomeScreenView", "Focused child: $focusedChild")
            
            if (focusedChild != null) {
                // Check if we're at the top of content
                // First check if there's a featured slider as the first child
                if (contentContainer.childCount > 0) {
                    val firstChild = contentContainer.getChildAt(0)
                    android.util.Log.d("HomeScreenView", "First child in container: ${firstChild.javaClass.simpleName}")
                    
                    // Check if focused element is within the first content
                    var parent = focusedChild.parent
                    while (parent != null && parent != this) {
                        if ((parent is FeaturedSliderView && firstChild is FeaturedSliderView) ||
                            (parent is ContentRowView && firstChild == parent)) {
                            android.util.Log.d("HomeScreenView", "UP pressed on first content - navigating to nav bar")
                            onNavigateUpCallback?.invoke()
                            return true
                        }
                        parent = parent.parent
                    }
                }
            }
        }
        
        return super.dispatchKeyEvent(event)
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
    
    fun setOnNavigateUp(listener: () -> Unit) {
        this.onNavigateUpCallback = listener
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
        recommendations: List<Content>,
        categoryContent: List<GenreContents> = emptyList()
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
        
        // Add content rows - only show non-empty sections
        if (trendingContent.isNotEmpty()) {
            addContentRow("Trending", trendingContent)
        }
        
        // Only show watchlist if user has items in their watchlist
        // The recommendations parameter contains the user's watchlist from the API
        if (recommendations.isNotEmpty()) {
            addContentRow("My List", recommendations)
        }
        
        // Only show continue watching if user is logged in and has items  
        if (continueWatching.isNotEmpty()) {
            addContentRow("Continue Watching", continueWatching)
        }
        
        if (newContent.isNotEmpty()) {
            addContentRow("New Releases", newContent)
        }
        
        // Add individual category rows - only show categories with content
        categoryContent.forEach { genreContent ->
            if (genreContent.contents.isNotEmpty()) {
                addContentRow(genreContent.title, genreContent.contents)
            }
        }
    }
    
    private fun addFeaturedSection(content: List<Content>) {
        val featuredView = FeaturedSliderView(context)
        featuredView.setContent(content)
        featuredView.setOnContentClick { content ->
            onContentClick?.invoke(content)
        }
        contentContainer.addView(featuredView)
        
        // Don't force focus - let natural focus flow work
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
        
        // Enable focus for Android TV but don't force touch mode
        recyclerView.isFocusable = true
        recyclerView.descendantFocusability = android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS
        
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
        
        adapter = FeaturedSliderAdapter(useSplitLayout = false, useImmersiveLayout = true)
        recyclerView.adapter = adapter
        
        // Enable focus for Android TV but don't force touch mode
        recyclerView.isFocusable = true
        recyclerView.descendantFocusability = android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS
        
        // Override dispatchKeyEvent to handle UP navigation
        recyclerView.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP && 
                event.action == android.view.KeyEvent.ACTION_DOWN) {
                android.util.Log.d("FeaturedSlider", "UP key detected in RecyclerView - bubbling up")
                // Return false to let the event bubble up to parent
                false
            } else {
                false
            }
        }
        
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
            if (hasFocus) {
                android.util.Log.d("FeaturedSlider", "RecyclerView gained focus - enabling auto-slide")
                // Start auto-slide after a short delay to allow focus to settle
                autoSlideHandler.postDelayed({
                    if (recyclerView.hasFocus()) {
                        resumeAutoSlide()
                    }
                }, 1000L)
            } else {
                android.util.Log.d("FeaturedSlider", "RecyclerView lost focus - pausing auto-slide")
                pauseAutoSlide()
            }
        }
        
        // Don't override key handling at RecyclerView level - let items handle their own events
    }
    
    fun setContent(content: List<Content>) {
        adapter.setContent(content)
        currentPosition = 0
        android.util.Log.d("FeaturedSlider", "Setting ${content.size} featured items")
        if (content.isNotEmpty()) {
            // Start auto-slide after initial setup
            autoSlideHandler.postDelayed({
                startAutoSlide()
            }, 2000L) // 2 second delay for initial auto-slide
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
                // Don't force focus transfer - let natural focus flow work
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
            currentPosition = it.findFirstVisibleItemPosition()
            if (currentPosition == RecyclerView.NO_POSITION) {
                currentPosition = 0
            }
            android.util.Log.d("FeaturedSlider", "Position updated to $currentPosition")
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pauseAutoSlide()
    }
} 
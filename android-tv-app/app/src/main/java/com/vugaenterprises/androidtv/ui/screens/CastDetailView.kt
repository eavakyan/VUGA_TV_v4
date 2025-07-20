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
import coil.transform.CircleCropTransformation
import com.vugaenterprises.androidtv.R
import com.vugaenterprises.androidtv.data.model.ActorItem
import com.vugaenterprises.androidtv.data.model.Content
import com.vugaenterprises.androidtv.data.model.CastItem
import com.vugaenterprises.androidtv.ui.components.ContentCardAdapter

class CastDetailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val backButton: ImageView
    private val actorImage: ImageView
    private val actorName: TextView
    private val characterName: TextView
    private val actorBiography: TextView
    private val otherContentContainer: LinearLayout
    private val otherContentRecyclerView: RecyclerView
    
    private var onBackClick: (() -> Unit)? = null
    private var onContentClick: ((Content) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.cast_detail_view, this, true)
        
        backButton = findViewById(R.id.backButton)
        actorImage = findViewById(R.id.actorImage)
        actorName = findViewById(R.id.actorName)
        characterName = findViewById(R.id.characterName)
        actorBiography = findViewById(R.id.actorBiography)
        otherContentContainer = findViewById(R.id.otherContentContainer)
        otherContentRecyclerView = findViewById(R.id.otherContentRecyclerView)
        
        // Setup RecyclerView
        otherContentRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
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
    
    fun setOnContentClick(listener: (Content) -> Unit) {
        this.onContentClick = listener
    }

    fun setCastMember(castMember: CastItem, otherContent: List<Content>) {
        val actor = castMember.actor
        
        // Load actor image with circular crop
        actorImage.load(actor.image) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
            error(R.drawable.placeholder_image)
            transformations(CircleCropTransformation())
        }
        
        // Set actor name
        actorName.text = actor.name
        
        // Set character name
        if (castMember.characterName.isNotEmpty()) {
            characterName.text = "as ${castMember.characterName}"
            characterName.visibility = View.VISIBLE
        } else {
            characterName.visibility = View.GONE
        }
        
        // Set biography (placeholder for now)
        actorBiography.text = "Professional actor known for various roles in film and television."
        
        // Setup other content
        if (otherContent.isNotEmpty()) {
            val adapter = ContentCardAdapter()
            adapter.setContent(otherContent)
            adapter.setOnContentClickListener { content ->
                onContentClick?.invoke(content)
            }
            otherContentRecyclerView.adapter = adapter
            otherContentContainer.visibility = View.VISIBLE
        } else {
            otherContentContainer.visibility = View.GONE
        }
    }
} 
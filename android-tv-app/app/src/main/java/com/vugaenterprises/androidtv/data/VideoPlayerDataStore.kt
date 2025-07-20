package com.vugaenterprises.androidtv.data

import com.vugaenterprises.androidtv.data.model.Content
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoPlayerDataStore @Inject constructor() {
    private var currentContent: Content? = null
    
    fun setCurrentContent(content: Content) {
        currentContent = content
    }
    
    fun getCurrentContent(): Content? {
        return currentContent
    }
    
    fun clearCurrentContent() {
        currentContent = null
    }
} 
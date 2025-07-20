package com.vugaenterprises.androidtv.data

import com.vugaenterprises.androidtv.data.model.CastItem
import com.vugaenterprises.androidtv.data.model.Content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CastDetailDataStore @Inject constructor() {
    
    private val _currentCastMember = MutableStateFlow<CastItem?>(null)
    val currentCastMember: StateFlow<CastItem?> = _currentCastMember.asStateFlow()
    
    private val _relatedContent = MutableStateFlow<List<Content>>(emptyList())
    val relatedContent: StateFlow<List<Content>> = _relatedContent.asStateFlow()
    
    fun setCurrentCastMember(castMember: CastItem, content: List<Content> = emptyList()) {
        _currentCastMember.value = castMember
        _relatedContent.value = content
    }
    
    fun clearCurrentCastMember() {
        _currentCastMember.value = null
        _relatedContent.value = emptyList()
    }
} 
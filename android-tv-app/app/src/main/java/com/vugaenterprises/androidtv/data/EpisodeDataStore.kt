package com.vugaenterprises.androidtv.data

import com.vugaenterprises.androidtv.data.model.EpisodeItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodeDataStore @Inject constructor() {
    private val _selectedEpisode = MutableStateFlow<EpisodeItem?>(null)
    val selectedEpisode: StateFlow<EpisodeItem?> = _selectedEpisode.asStateFlow()
    
    fun setSelectedEpisode(episode: EpisodeItem?) {
        _selectedEpisode.value = episode
    }
    
    fun clearSelectedEpisode() {
        _selectedEpisode.value = null
    }
} 
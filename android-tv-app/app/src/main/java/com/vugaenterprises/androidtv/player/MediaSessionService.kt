package com.vugaenterprises.androidtv.player

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MediaSessionService : MediaSessionService() {
    // This service will be implemented when we add full media playback functionality
    // For now, it's a placeholder to satisfy the manifest requirements

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        // Return null for now (stub)
        return null
    }
} 
package com.vugaenterprises.androidtv.player

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MediaBrowserService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
} 
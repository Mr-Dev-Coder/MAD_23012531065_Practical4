package com.example.mad_23012531065_practical4

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class AlarmService : Service() {
    lateinit var mediaPlayer: MediaPlayer

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(!this :: mediaPlayer.isInitialized){
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
        }
        mediaPlayer.start()
        return START_STICKY
    }

    override fun onDestroy() {
        if (this :: mediaPlayer.isInitialized){
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        super.onDestroy()
    }
}

package com.example.mad_23012531065_practical4

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmService : Service() {

    private val TAG = "AlarmService"
    private var mediaPlayer: MediaPlayer? = null
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "AlarmServiceChannel"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand received")
        if (intent != null) {
            val action = intent.getStringExtra("Service1")
            Log.d(TAG, "Action: $action")

            when (action?.uppercase()) {
                "START" -> {
                    Log.d(TAG, "START action processed")
                    val soundUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.alarm)
                    
                    mediaPlayer?.release() // Release any existing player
                    mediaPlayer = MediaPlayer().apply {
                        try {
                            setDataSource(applicationContext, soundUri)
                            isLooping = true
                            prepareAsync()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error setting data source or preparing media player", e)
                            stopSelf() 
                            return@apply
                        }
                    }
                    mediaPlayer?.setOnPreparedListener {
                        Log.d(TAG, "MediaPlayer prepared, starting playback.")
                        it.start()
                        startForeground(NOTIFICATION_ID, createNotification("Alarm is ringing!"))
                    }
                    mediaPlayer?.setOnErrorListener { mp, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what: $what, extra: $extra")
                        stopSelf() 
                        true
                    }
                }
                "STOP_SOUND", "STOP" -> { 
                    Log.d(TAG, "STOP_SOUND/STOP action processed")
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                    stopForeground(true)
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        stopForeground(true) 
    }
}

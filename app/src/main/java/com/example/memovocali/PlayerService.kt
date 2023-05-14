package com.example.memovocali

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager

class PlayerService:Service() {
//TODO implementare il player
    private var myPlayer: MediaPlayer? = null
    private var isPlaying = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
/*
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        if (intent.getBooleanExtra(PLAY_START, false)) play()
        return START_STICKY
    }

    override fun onCreate()
    {
        super.onCreate()

        // Create the NotificationChannel, but only on API level 26+ because
        // the NotificationChannel class is new and not in the support library.
        // See https://developer.android.com/training/notify-user/channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun play()
    {
        if (isPlaying) return
        isPlaying = true

        // Music downloaded from "Public Domain 4U"
        // https://publicdomain4u.com/paul-whiteman-orchestra-doo-wacka-doo-mp3-download
        myPlayer = MediaPlayer.create(this, R.raw.doowackadoo)
        myPlayer!!.isLooping = true
        // myPlayer holds the PARTIAL_WAKE_LOCK lock to ensure that the CPU continues running
        // during playback. myPlayer holds the lock while playing and releases it when paused
        // or stopped
        myPlayer!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        myPlayer!!.start()
        // I used the not-null assertion operator (!!) instead of the elvis operator (?)
        // for the mutable property myPlayer so the app crashes if the MediaPlayer is not
        // available, hence the user realizes that something went wrong

        // Build a notification with basic info about the song
        val notificationBuilder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                Notification.Builder(applicationContext, CHANNEL_ID)
            else  // Deprecation warning left on purpose for educational reasons
                Notification.Builder(applicationContext)
        notificationBuilder.setContentTitle(getString(R.string.song_artist))
        notificationBuilder.setContentText(getString(R.string.song_title))
        notificationBuilder.setSmallIcon(R.drawable.ic_play_circle_filled)
        val notification = notificationBuilder.build() // Requires API level 16
        // Runs this service in the foreground,
        // supplying the ongoing notification to be shown to the user
        val notificationID = 5786423 // An ID for this notification unique within the app
        startForeground(notificationID, notification)
    }

    private fun stop()
    {
        if (isPlaying) {
            isPlaying = false
            myPlayer?.release()
            myPlayer = null
            stopForeground(true)
        }
    }

    override fun onDestroy()
    {
        stop()
        super.onDestroy()
    }

    companion object
    {
        private const val CHANNEL_ID = "simplebgplayer"
        const val PLAY_START = "BGPlayStart"
        const val PLAY_STOP = "BGPlayStop"
    }*/
}
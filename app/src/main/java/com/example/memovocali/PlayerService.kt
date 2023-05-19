package com.example.memovocali

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import android.widget.RemoteViews

class PlayerService: Service() {
    private var myPlayer: MediaPlayer? = null
    private var isPlaying = false
    private lateinit var path:String
    private lateinit var title:String
    private var duration: Int=0
    private lateinit var view:RemoteViews
    private var time:CountDownTimer?=null
    private var seek:Int=0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        path=intent.getStringExtra("recordPath")!!
        title=intent.getStringExtra("recordTitle")!!
        duration=intent.getIntExtra("recordDuration",0)
        seek=intent.getIntExtra("seek",0)
        if(seek==0)
            play()
        else
        {
            myPlayer?.pause()
            myPlayer?.seekTo(seek)
            myPlayer?.start()
        }
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
        myPlayer= MediaPlayer()
        myPlayer?.setDataSource(path+title)
        myPlayer?.prepare()
        myPlayer?.setOnCompletionListener {
            stop()
        }
        // myPlayer holds the PARTIAL_WAKE_LOCK lock to ensure that the CPU continues running
        // during playback. myPlayer holds the lock while playing and releases it when paused
        // or stopped
        myPlayer!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        myPlayer?.start()
        val notificationID = 5786423// An ID for this notification unique within the app
        // Build a notification with basic info about the song
        val notificationBuilder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                Notification.Builder(applicationContext, CHANNEL_ID)
            else  // Deprecation warning left on purpose for educational reasons
                Notification.Builder(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setBadgeIconType(Notification.BADGE_ICON_SMALL)
        }
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
        notificationBuilder.setContentTitle(title)
        notificationBuilder.setProgress(duration,0,true)

        val notification = notificationBuilder.build() // Requires API level 16
        startForeground(notificationID, notification)
    }

    private fun stop()
    {
        if (isPlaying) {
            time?.cancel()
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
    }
}
package com.example.memovocali

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.*
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

class PlayerService: Service() {
    private var myPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var path:String=""
    private var title:String=""
    private var duration: Int=0
    private val mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: PlayerService
            get() = this@PlayerService
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onCreate()
    {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.channel_name)
            val description = getString(R.string.channel_description, title)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun startPlay(t:String, p:String, d:Int)
    {
        if(isPlaying)
            return
        title=t
        path=p
        duration=d
        play()
    }

    fun stopPlay()
    {
        stop()
    }

    private fun play()
    {
        isPlaying = true
        myPlayer= MediaPlayer()
        myPlayer?.setDataSource(path+title)
        myPlayer?.prepare()
        myPlayer?.setOnCompletionListener {
            stop()
        }
        myPlayer!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        myPlayer?.start()
        val notificationBuilder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                Notification.Builder(applicationContext, CHANNEL_ID)
            else
                Notification.Builder(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setBadgeIconType(Notification.BADGE_ICON_SMALL)
        }
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
        notificationBuilder.setContentTitle(title)
        notificationBuilder.setProgress(duration,0,true)
        val notification = notificationBuilder.build()
        startForeground(notificationID, notification)
    }

    private fun stop()
    {
        if (isPlaying) {
            isPlaying = false
            myPlayer?.stop()
            myPlayer?.release()
            myPlayer = null
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }

    fun seekTo(seek:Int)
    {
        myPlayer?.pause()
        myPlayer?.seekTo(seek)
        myPlayer?.start()
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun getProgress():Int
    {
        return myPlayer?.currentPosition ?: 0
    }

    fun getTitle():String
    {
        return title
    }

    companion object
    {
        private const val CHANNEL_ID = "MediaPlayer"
        private const val notificationID=5786423
    }
}
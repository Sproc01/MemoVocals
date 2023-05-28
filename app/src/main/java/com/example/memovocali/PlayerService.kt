package com.example.memovocali

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.*

class PlayerService: Service() {
    private var myPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var path:String=""
    private var title:String=""
    private var duration: Int=0
    private val mBinder: IBinder = LocalBinder()

    /**
     * inner class to represent the interface that must be used to control the service when a client is bind to it
     */
    inner class LocalBinder : Binder() {
        /**
         * function to get the service
         * @return the service
         */
        val service: PlayerService
            get() = this@PlayerService
    }

    /**
     * function call when a client bind to the service
     * @param intent intent of the client
     * @return the binder of the service
     */
    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    /**
     * function call when the service is started
     * @param intent intent of the service
     * @param flags flags of the service
     * @param startId startId of the service
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    /**
     * function call when the service is created
     */
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

    /**
     * public function to start the player and create the notification
     * @param t title of the audio
     * @param p path of the audio
     * @param d duration of the audio
     */
    fun startPlay(t:String, p:String, d:Int)
    {
        if(isPlaying)
            return
        title=t
        path=p
        duration=d
        play()
    }

    /**
     * public function to stop the player and remove the notification
     */
    fun stopPlay()
    {
        stop()
    }

    /**
     * function to play the audio and create the notification, entering in foreground
     */
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

    /**
     * function to stop the player
     */
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

    /**
     * function call when all the client unbind from the service
     */
    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }

    /**
     * function to seek the player from outside the service
     */
    fun seekTo(seek:Int)
    {
        myPlayer?.pause()
        myPlayer?.seekTo(seek)
        myPlayer?.start()
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    /**
     * function to get the progress of the player
     * @return the progress of the player
     */
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
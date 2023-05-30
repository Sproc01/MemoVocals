package com.example.memovocali

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager


class PlayerService: Service() {
    private var myPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var path:String=""
    private var title:String=""
    private var duration: Int=0
    private var audioManager: AudioManager? = null
    private var audioRequest: AudioFocusRequest? = null
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
            stop()
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
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val focusChangeListener = OnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> {
                    }
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        stop() //loss audio focus for an unbounded amount of time
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        stop() //loss audio focus for a short time
                    }
                }
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioRequest=AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
            val result = audioManager?.requestAudioFocus(audioRequest!!)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // have audio focus now.
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
            } else {
                // haven't audio focus.
            }
        }
    }

    fun isPlaying():Boolean
    {
        return isPlaying
    }

    fun getTitle():String
    {
        return title
    }

    /**
     * function to stop the player
     */
    private fun stop()
    {
        if (isPlaying) {
            isPlaying = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager?.abandonAudioFocusRequest(audioRequest!!)
            }
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

    /**
     * function to get the progress of the player
     * @return the progress of the player
     */
    fun getProgress():Int
    {
        return myPlayer?.currentPosition ?: 0
    }

    companion object
    {
        private const val CHANNEL_ID = "MediaPlayer"
        private const val notificationID=5786423
    }
}
package com.example.memovocali

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager

/**
 * class that represent the interface that must be implemented in order to manage callback of the service
 */
interface ServiceListener{
    /**
     * function to call when the service lose the audiofocus
     */
    fun onAudioFocusLose()
}

/**
 * class that represent the service that manage the player,
 * it is a foreground service
 */
class PlayerService: Service() {
    private var myPlayer: MediaPlayer? = null
    private var path:String=""
    private var title:String=""
    private var audioManager: AudioManager? = null
    private var audioRequest: AudioFocusRequest? = null
    private var serviceCallbacks: ServiceListener? = null
    private val mBinder: IBinder = LocalBinder()
    private var isPaused: Boolean = false


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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    /**
     * function to set the callback of the service
     * @param callbacks class that implements the interface for callback of the service
     */
    fun setCallbacks(callbacks: ServiceListener?) {
        serviceCallbacks = callbacks
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onCreate() {
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
     */
    fun startPlay(t:String, p:String) {
        if(myPlayer?.isPlaying==true)
            stopPlay()
        title=t
        path=p
        myPlayer= MediaPlayer()
        myPlayer?.setDataSource(path+title)
        myPlayer?.prepare()
        myPlayer?.setOnCompletionListener {
            stopPlay()
        }
        myPlayer!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val focusChangeListener = OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    stopPlay() //loss audio focus for an unbounded amount of time
                    serviceCallbacks?.onAudioFocusLose()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    stopPlay() //loss audio focus for a short time
                    serviceCallbacks?.onAudioFocusLose()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
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
                notificationBuilder.setLargeIcon(
                    Icon.createWithResource(
                        applicationContext,
                        R.drawable.baseline_audiotrack_24
                    )
                )
                notificationBuilder.setContentText("Playing")
                notificationBuilder.style = Notification.MediaStyle()
                val intent = Intent(applicationContext, DetailActivity::class.java)
                intent.putExtra("recordName", title)
                intent.putExtra("recordPath", path)
                val pendingIntent = PendingIntent.getActivity(
                    applicationContext, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                notificationBuilder.setContentIntent(pendingIntent)
                val notification = notificationBuilder.build()
                startForeground(notificationID, notification)
            } else {
                // haven't audio focus.
            }
        }
    }

    /**
     * public function to stop the player and remove the notification
     */
    fun stopPlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager?.abandonAudioFocusRequest(audioRequest!!)
        }
        title=""
        path=""
        myPlayer?.stop()
        myPlayer?.release()
        myPlayer = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * public function to pause the player
     */
    fun pausePlay() {
        if (myPlayer?.isPlaying==true) {
            myPlayer?.pause()
            isPaused=true
        }
    }

    /**
     * public function to resume the player
     */
    fun resumePlay() {
        if (myPlayer?.isPlaying==false) {
            myPlayer?.start()
            isPaused=false
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopPlay()
    }
    /**
     * function to check if the player is playing
     * @return true if the player is playing, false otherwise
     */
    fun isPlaying():Boolean {
        return myPlayer?.isPlaying==true
    }

    /**
     * function to get the title of the audio that is playing
     */
    fun getTitle():String {
        return title
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }

    /**
     * public function to seek the player
     * @param seek the position to seek
     */
    fun seekTo(seek:Int) {
        myPlayer?.pause()
        myPlayer?.seekTo(seek)
        myPlayer?.start()
    }

    /**
     * function to get the progress of the player
     * @return the progress of the player
     */
    fun getProgress():Int {
        return myPlayer?.currentPosition ?: 0
    }

    /**
     * function to get if the player is paused
     * @return true if the player is paused, false otherwise
     */
    fun isPaused():Boolean {
        return isPaused
    }

    companion object {
        /**
         * constant for the channel id
         */
        private const val CHANNEL_ID = "MediaPlayer"
        /**
         * constant for the notification id
         */
        private const val notificationID=5786423
    }
}
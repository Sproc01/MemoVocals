package com.example.memovocali

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.os.StatFs
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DetailActivity : AppCompatActivity(),ServiceListener {

    private var title: TextView? = null
    private var txtDuration: TextView? = null
    private var txtProgress: TextView? = null
    private var duration = 0
    private var buSubstitute: Button? = null
    private var buPlay: Button? = null
    private var buPausePlay: Button? = null
    private var seekDetailB: SeekBar? = null
    private var time: Timer? = null
    private var mService:PlayerService?=null
    private var mBound=false
    private var mBinder: PlayerService.LocalBinder?=null
    private lateinit var path:String
    private lateinit var recordtitle:String
    private var thS:ServiceThread?=null
    /**
     * object that manage the connection to the service
     */
    private var mConnection= object: ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mBinder=service as PlayerService.LocalBinder
            mBound=true
            mService=mBinder?.service
            if(mService?.isPlaying()!! && mService?.getTitle()==recordtitle){
                //there is a service and it is playing
                seekDetailB?.max = duration
                seekDetailB?.progress = mService?.getProgress() ?: 0
                seekDetailB?.visibility = SeekBar.VISIBLE
                buPausePlay?.visibility = Button.VISIBLE
                buPlay?.visibility = Button.INVISIBLE
                buSubstitute?.visibility = Button.INVISIBLE
                txtProgress?.text=String.format("00:%02d", (seekDetailB?.progress!!) / 1000)
                txtProgress?.visibility=TextView.VISIBLE
                time=Timer((duration - seekDetailB?.progress!!).toLong())
                time?.start()
            }
            else if(mService?.isPaused()!! && mService?.getTitle()==recordtitle) {
                //there is a service but it is paused
                seekDetailB?.max = duration
                seekDetailB?.progress = mService?.getProgress() ?: 0
                seekDetailB?.visibility = SeekBar.VISIBLE
                buPausePlay?.visibility = Button.INVISIBLE
                buSubstitute?.visibility = Button.VISIBLE
                txtProgress?.text=String.format("00:%02d", (seekDetailB?.progress!!) / 1000)
                txtProgress?.visibility=TextView.VISIBLE
            }
            else if(thS!=null)
                mService?.startPlay(recordtitle, path)//there are no service so you pressed the play button and start a new thread
            //set the callbacks for lose the audio focus
            mService?.setCallbacks(this@DetailActivity)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            stopPlay()
            mBound=false
            mService=null
            mBinder=null
        }
    }

    /**
     * thread that launch a new service
     */
    inner class ServiceThread:Thread(){
        override fun run() {
            val i=Intent(applicationContext, PlayerService::class.java)
            startService(i)
            applicationContext.bindService(i, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * class that manage the timer when an audio is playing
     */
    inner class Timer(x: Long) : CountDownTimer(x, 100) {

        override fun onTick(millisUntilFinished: Long) {
            seekDetailB?.progress = seekDetailB?.progress?.plus(100)!!
            txtProgress?.text=String.format("00:%02d", (seekDetailB?.progress!!) / 1000)
        }
        override fun onFinish() {
            stopPlay()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        //set a new action bar that has the back button
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        //initialize variables referring to the layout
        title = findViewById(R.id.NameRecordDetail)
        txtDuration = findViewById(R.id.RecordDuration)
        buSubstitute = findViewById(R.id.buttonSubstitute)
        buPausePlay = findViewById(R.id.buttonPauseDetail)
        buPlay = findViewById(R.id.buttonPlayDetail)
        seekDetailB = findViewById(R.id.progressBarDetail)
        txtProgress=findViewById(R.id.txtProgress)

        //read data from intent
        recordtitle = (intent.getStringExtra("recordName") ?: "")
        path = (intent.getStringExtra("recordPath") ?: "")
        title?.text = getString(R.string.TitleDetail, recordtitle.replace(".aac", ""))

        //get the duration of the audio
        val dataMedia=MediaMetadataRetriever()
        dataMedia.setDataSource(path+recordtitle)
        duration=dataMedia.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt()?:0
        txtDuration?.text = String.format("00:%02d", duration / 1000)

        //bind the service to found if an audio is playing
        applicationContext.bindService(Intent(this, PlayerService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        seekDetailB?.visibility = SeekBar.INVISIBLE
        buPausePlay?.visibility = Button.INVISIBLE
        txtProgress?.visibility=TextView.INVISIBLE

        seekDetailB?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtProgress?.text=String.format("00:%02d", progress / 1000)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mService?.seekTo(seekBar.progress)
                if(mService?.isPaused() == false)
                {
                    time?.cancel()
                    time = Timer((duration - seekBar.progress).toLong())
                    time?.start()
                    mService?.resumePlay()
                }
            }
        })

        buSubstitute?.setOnClickListener {
            if(mBound && mService?.isPaused()==true)
                stopPlay()
            if(thS==null)//if thS is null it means that this instance of detailActivty doesn't have a service playing
            {
                //check if the app have the permission to record
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PermissionChecker.PERMISSION_GRANTED)
                    return@setOnClickListener
                //check if there is enough space to record(15 MB)
                val stat = StatFs(path)
                val megAvailable = stat.availableBytes/1000000
                if(megAvailable>size) {
                    val intent=Intent(this, RecordingActivity::class.java)
                    intent.putExtra("title", recordtitle)
                    intent.putExtra("path", path)
                    startActivity(intent)
                }
                else {
                    val error= MaterialAlertDialogBuilder(applicationContext)
                    error.setTitle(getString(R.string.DialogSpace))
                    error.setMessage(getString(R.string.errorEnoughSpace))
                    error.setPositiveButton(getString(R.string.Ok),null)
                    error.show()
                }
            }
        }

        buPlay?.setOnClickListener {
            //check if the app has the permission to start a foreground service
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)!=
                PermissionChecker.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)!=
                PermissionChecker.PERMISSION_GRANTED)
                return@setOnClickListener
            if(mService?.isPaused() == true && mService?.getTitle()==recordtitle) {
                //if paused it will resume the playback
                mService?.resumePlay()
                time = Timer((duration - seekDetailB?.progress!!).toLong())
                time?.start()
            }
            else {//if not it start a new playback
                if(thS==null && mBound)//unbind from the existing service and start a new one in a separate thread
                    applicationContext.unbindService(mConnection)
                thS=ServiceThread()
                thS?.start()

                //start timer
                time = Timer(duration.toLong())
                time?.start()

                //update the interface
                seekDetailB?.max = duration
                seekDetailB?.progress = 0
                seekDetailB?.visibility = SeekBar.VISIBLE
                txtProgress?.visibility=TextView.VISIBLE
            }
            buPausePlay?.visibility = Button.VISIBLE
            buPlay?.visibility = Button.INVISIBLE
            buSubstitute?.visibility = Button.INVISIBLE
        }

        buPausePlay?.setOnClickListener {//pause the playback
            mService?.pausePlay()
            time?.cancel()
            time = null
            buPlay?.visibility = Button.VISIBLE
            buPausePlay?.visibility = Button.INVISIBLE
            buSubstitute?.visibility = Button.VISIBLE
        }
    }

    /**
     * function that is called when the service must be stopped
     */
    private fun stopPlay()
    {
        if(mBound)
        {
            //stop the service and unbind
            mService?.stopPlay()

            applicationContext.unbindService(mConnection)
            mBound = false

            //stop the thread
            thS?.interrupt()
            thS=null

            //stop the timer
            time?.cancel()

            //update the interface
            seekDetailB?.visibility = SeekBar.INVISIBLE
            buPausePlay?.visibility = Button.INVISIBLE
            buPlay?.visibility = Button.VISIBLE
            buSubstitute?.visibility = Button.VISIBLE
            txtProgress?.visibility=TextView.INVISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        if(mBound && mService?.isPaused()==true && mService?.getTitle()==recordtitle && !isChangingConfigurations)
            stopPlay()//if is paused and the activity will no longer exist stop the service
        else if(mBound)
            applicationContext.unbindService(mConnection)
        time?.cancel()
    }
    /**
     * function that is called when the back button is pressed
     */
    override fun onSupportNavigateUp(): Boolean {
        //back button pressed so the activity must be destroyed
        finish()
        return true
    }

    override fun onAudioFocusLose() {
        //when service lose the audio focus update the interface and unbind
        time?.cancel()
        time = null
        buPlay?.visibility = Button.VISIBLE
        buPausePlay?.visibility = Button.INVISIBLE
        buSubstitute?.visibility = Button.VISIBLE
    }

    companion object{
        private const val size=15
    }
}

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
import java.io.File

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
                mService?.setCallbacks(this@DetailActivity)
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
                mService?.setCallbacks(this@DetailActivity)
            }
            else {
                seekDetailB?.visibility = SeekBar.INVISIBLE
                buPausePlay?.visibility = Button.INVISIBLE
                txtProgress?.visibility=TextView.INVISIBLE
                buPlay?.visibility = Button.VISIBLE
                buSubstitute?.visibility = Button.VISIBLE
            }
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
            mService?.setCallbacks(this@DetailActivity)
            mService?.startPlay(recordtitle, path)
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

        val file= File(path+recordtitle)
        if(!file.exists()) {
            //if the file doesn't exist,the user wil be informed and the activity is closed
            val error= MaterialAlertDialogBuilder(this)
            error.setTitle(getString(R.string.FileNotExistErrorTitle))
            error.setMessage(getString(R.string.notExist))
            error.setPositiveButton(getString(R.string.Ok)) { _, _ -> finish() }
            error.show()
        }
        else {
            //get the duration of the audio only if the file exist
            val dataMedia=MediaMetadataRetriever()
            dataMedia.setDataSource(path+recordtitle)
            duration=dataMedia.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt()?:0
            txtDuration?.text = String.format("00:%02d", duration / 1000)
        }

        seekDetailB?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtProgress?.text=String.format("00:%02d", progress / 1000)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mService?.seekTo(seekBar.progress)
                if(mService?.isPaused() == false)
                {//if the audio weren't playing before the seekbar was moved, the audio will continue
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
            //if thS is null it means that this instance of detailActivty doesn't have a service playing
            if(thS==null) {
                //check if the app have the permission to record
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PermissionChecker.PERMISSION_GRANTED) {//if the permission is not granted the user will be informed
                    val error= MaterialAlertDialogBuilder(this)
                    error.setTitle(getString(R.string.errorNoPermission))
                    error.setMessage(getString(R.string.errorNoPermissionAudio))
                    error.setPositiveButton(getString(R.string.Ok),null)
                    error.show()
                    return@setOnClickListener
                }
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
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)!=
                PermissionChecker.PERMISSION_GRANTED) { //if the permission is not granted the user will be informed
                //this permission is requested because the service use the notification to communicate with the user
                val error= MaterialAlertDialogBuilder(this)
                error.setTitle(getString(R.string.errorNoPermission))
                error.setMessage(getString(R.string.errorNoPermissionNotification))
                error.setPositiveButton(getString(R.string.Ok),null)
                error.show()
                return@setOnClickListener
            }
            if(mService?.isPaused() == true && mService?.getTitle()==recordtitle) {
                //if paused it will resume the playback
                mService?.resumePlay()
                time = Timer((duration - seekDetailB?.progress!!).toLong())
                time?.start()
            }
            else {//if not it start a new playback
                if(thS==null && mBound)//stop the existing service and start a new one in a separate thread
                    mService?.stop()
                if(!mBound)//if the service is destroy by the system for whatever reason before start playing, bind it again
                    applicationContext.bindService(Intent(this, PlayerService::class.java), mConnection, Context.BIND_AUTO_CREATE)

                //start the service in a separate thread
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
        if(mBound) {
            //stop the service
            mService?.stop()

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
            stopPlay()//if is paused and the service will be stopped
        if(mBound) {//if only bound unbind so if the service is not playing it will be destroyed
            applicationContext.unbindService(mConnection)
            mBound=false
        }
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

    override fun onResume(){
        super.onResume()
        if(!mBound) { //bind the service to found if an audio is playing or not
            applicationContext.bindService(Intent(this, PlayerService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onAudioFocusLose() {
        //when service lose the audio focus update the interface
        time?.cancel()
        time = null
        buPlay?.visibility = Button.VISIBLE
        buPausePlay?.visibility = Button.INVISIBLE
        buSubstitute?.visibility = Button.VISIBLE
    }

    companion object{
        /**
         * Size of the file in MB
         */
        private const val size=15
    }
}

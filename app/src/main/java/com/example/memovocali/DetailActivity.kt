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
import android.view.View
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
    private var txtpath: TextView? = null
    private var txtDuration: TextView? = null
    private var duration = 0
    private var buSubstitute: Button? = null
    private var buPlay: Button? = null
    private var buStopPlay: Button? = null
    private var seekDetailB: SeekBar? = null
    private var time: Timer? = null
    private var mService:PlayerService?=null
    private var mBound=false
    private var mBinder: PlayerService.LocalBinder?=null
    private lateinit var path:String
    private lateinit var recordtitle:String
    private var thS:ServiceThread?=null

    /**
     * thread that launch a new service
     */
    inner class ServiceThread:Thread(){
        /**
         * function that launch the service
         */
        override fun run() {
            val i=Intent(applicationContext, PlayerService::class.java)
            startService(i)
            applicationContext.bindService(i, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * object that manage the connection to the service
     */
    private var mConnection= object: ServiceConnection {
        /**
         * function that is called when the service is connected
         */
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mBinder=service as PlayerService.LocalBinder
            mBound=true
            mService=mBinder?.service
            if(mService?.isPlaying()!! && mService?.getTitle()==recordtitle){
                seekDetailB?.max = duration
                seekDetailB?.progress = mService?.getProgress() ?: 0
                seekDetailB?.visibility = SeekBar.VISIBLE
                buStopPlay?.visibility = Button.VISIBLE
                buSubstitute?.visibility = Button.INVISIBLE
                time=Timer((duration - seekDetailB?.progress!!).toLong())
                time?.start()
            }
            else if(thS!=null)
                mService?.startPlay(recordtitle, path)
            mService?.setCallbacks(this@DetailActivity)
        }

        /**
         * function that is called when the service is disconnected
         */
        override fun onServiceDisconnected(name: ComponentName) {
            mBound=false
            buStopPlay?.callOnClick()
        }
    }

    /**
     * class that manage the timer when an audio is playing
     */
    inner class Timer(x: Long) : CountDownTimer(x, 100) {
        /**
         * function that is called when the timer tick
         */
        override fun onTick(millisUntilFinished: Long) {
            seekDetailB?.progress = seekDetailB?.progress?.plus(100)!!
        }

        /**
         * function that is called when the timer finish
         */
        override fun onFinish() {
            buStopPlay?.callOnClick()
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
        txtpath = findViewById(R.id.RecordPath)
        txtDuration = findViewById(R.id.RecordDuration)
        buSubstitute = findViewById(R.id.buttonSubstitute)
        buStopPlay = findViewById(R.id.buttonStopDetail)
        buPlay = findViewById(R.id.buttonPlayDetail)
        seekDetailB = findViewById(R.id.progressBarDetail)

        //read data from intent
        recordtitle = (intent.getStringExtra("recordName") ?: "")
        path = (intent.getStringExtra("recordPath") ?: "")
        txtpath?.text = path
        title?.text = getString(R.string.TitleDetail, recordtitle)

        //get the duration of the audio
        val dataMedia=MediaMetadataRetriever()
        dataMedia.setDataSource(path+recordtitle)
        duration=dataMedia.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt()?:0
        txtDuration?.text = String.format("00:%02d", duration / 1000)

        //restore instance state
        if (savedInstanceState != null && recordtitle==savedInstanceState.getString("title")) {
            applicationContext.bindService(Intent(this, PlayerService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        } else {
            //there isn't an instance state
            //bind the service to found if an audio is playing
            applicationContext.bindService(Intent(this, PlayerService::class.java), mConnection, Context.BIND_AUTO_CREATE)
            seekDetailB?.visibility = View.INVISIBLE
            buStopPlay?.visibility = View.INVISIBLE
        }

        /**
         * function that is called when the seekbar is clicked
         */
        seekDetailB?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            /**
             * function that is called when the seekbar progress change
             */
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

            /**
             * function that is called when the seekbar start to be clicked
             */
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            /**
             * function that is called when the seekbar stop to be clicked
             */
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mService?.seekTo(seekBar.progress)
                time?.cancel()
                time = Timer((duration - seekBar.progress).toLong())
                time?.start()
            }
        })

        /**
         * function that is called when the substitute button is clicked
         */
        buSubstitute?.setOnClickListener {
            if(thS==null)//if thS is null it means that this instance of detailActivty doesn't have a service playing
            {
                //check if the app have the permission to record
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PermissionChecker.PERMISSION_GRANTED)
                    return@setOnClickListener
                //check if there is enough space to record(15 MB)
                val stat = StatFs(path)
                val megAvailable = stat.availableBytes/1000000
                if(megAvailable>15) {

                    val intent=Intent(this, RecordingActivity::class.java)
                    intent.putExtra("title", recordtitle)
                    intent.putExtra("path", path)
                    startActivity(intent)
                }
                else {
                    val error= MaterialAlertDialogBuilder(applicationContext)
                    error.setTitle(getString(R.string.DialogSpace))
                    error.setMessage(getString(R.string.errorEnoughSpace))
                    error.setPositiveButton(getString(R.string.labelOk),null)
                    error.show()
                }
            }
        }

        /**
         * function that is called when the play button is clicked
         */
        buPlay?.setOnClickListener {
            //check if the app has the permission to start a foreground service
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)!=
                PermissionChecker.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)!=
                PermissionChecker.PERMISSION_GRANTED)
                return@setOnClickListener
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
            buStopPlay?.visibility = View.VISIBLE
            seekDetailB?.visibility = View.VISIBLE
            buPlay?.visibility = View.INVISIBLE
            buSubstitute?.visibility = View.INVISIBLE
        }

        buStopPlay?.setOnClickListener {
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
            seekDetailB?.visibility = View.INVISIBLE
            buStopPlay?.visibility = View.INVISIBLE
            buPlay?.visibility = View.VISIBLE
            buSubstitute?.visibility = View.VISIBLE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        //back button pressed so the activity must be destroyed
        finish()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(mBound && thS!=null)//if service is playing save the title
        {
            outState.putString("title", recordtitle)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //stop the timer if it is running
        time?.cancel()
    }

    override fun onAudioFocusLose() {
        //when service lose the audio focus update the interface and unbind
        buStopPlay?.callOnClick()
    }
}

package com.example.memovocali

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

    inner class ServiceThread:Thread(){
        override fun run() {
            val i=Intent(applicationContext, PlayerService::class.java)
            startService(i)
            applicationContext.bindService(i, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private var mConnection= object: ServiceConnection {
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
                mService?.startPlay(recordtitle, path, duration)
            mService?.setCallbacks(this@DetailActivity)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mBound=false
            buStopPlay?.callOnClick()
        }
    }

    inner class Timer(x: Long) : CountDownTimer(x, 100) {
        override fun onTick(millisUntilFinished: Long) {
            seekDetailB?.progress = seekDetailB?.progress?.plus(100)!!
        }

        override fun onFinish() {
            buStopPlay?.callOnClick()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

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

        //restore instance state
        if (savedInstanceState != null && recordtitle==savedInstanceState.getString("title")) {
            applicationContext.bindService(Intent(this, PlayerService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        } else {
            //there isn't an instance state
            applicationContext.bindService(Intent(this, PlayerService::class.java), mConnection, Context.BIND_AUTO_CREATE)
            seekDetailB?.visibility = View.INVISIBLE
            buStopPlay?.visibility = View.INVISIBLE
        }

        seekDetailB?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mService?.seekTo(seekBar.progress)
                time?.cancel()
                time = Timer((duration - seekBar.progress).toLong())
                time?.start()
            }
        })

        buSubstitute?.setOnClickListener {
            if(thS==null)
            {
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

        buPlay?.setOnClickListener {
            if(thS==null && mBound)
                applicationContext.unbindService(mConnection)
            thS=ServiceThread()
            thS?.start()
            seekDetailB?.max = duration
            time = Timer(duration.toLong())
            seekDetailB?.progress = 0
            time?.start()
            buStopPlay?.visibility = View.VISIBLE
            seekDetailB?.visibility = View.VISIBLE
            buPlay?.visibility = View.INVISIBLE
            buSubstitute?.visibility = View.INVISIBLE
        }

        buStopPlay?.setOnClickListener {
            mService?.stopPlay()
            applicationContext.unbindService(mConnection)
            thS?.interrupt()
            thS=null
            mBound = false
            time?.cancel()
            seekDetailB?.visibility = View.INVISIBLE
            buStopPlay?.visibility = View.INVISIBLE
            buPlay?.visibility = View.VISIBLE
            buSubstitute?.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        val dataMedia=MediaMetadataRetriever()
        dataMedia.setDataSource(path+recordtitle)
        duration=dataMedia.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt()?:0
        txtDuration?.text = String.format("00:%02d", duration / 1000)
    }

    override fun onPause() {
        super.onPause()
        time?.cancel()
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(mBound && thS!=null)
        {
            outState.putString("title", recordtitle)
        }
    }

    override fun onAudioFocusLose() {
        buStopPlay?.callOnClick()
    }
}

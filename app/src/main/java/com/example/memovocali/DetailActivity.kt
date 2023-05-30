package com.example.memovocali

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.log10

class DetailActivity : AppCompatActivity() {

    private var title: TextView? = null
    private var txtpath: TextView? = null
    private var txtDuration: TextView? = null
    private var duration = 0
    private var buSubstitute: Button? = null
    private var buPlay: Button? = null
    private var buStopSubstitute: Button? = null
    private var buStopPlay: Button? = null
    private var seekDetailB: SeekBar? = null
    private var txtRecordGoing: TextView? = null
    private var time: Timer? = null
    private var noiseIndicator: ProgressBar? = null
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
            mService=mBinder?.service
            mBound=true
            mService?.startPlay(recordtitle, path, duration)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mBound=false
            buStopPlay?.callOnClick()
        }
    }

    inner class Timer(x: Long, private val flagRecording: Boolean) : CountDownTimer(x, 100) {
        override fun onTick(millisUntilFinished: Long) {
            noiseIndicator?.progress = 20*log10(amplitude().toDouble()).toInt()
            seekDetailB?.progress = seekDetailB?.progress?.plus(100)!!
            if (flagRecording)
                txtRecordGoing?.text = getString(
                    R.string.Recording,
                    String.format("00:%02d", ((seekDetailB?.progress?.div(1000) ?: 0)))
                )
        }

        override fun onFinish() {
            if (flagRecording)
                buStopSubstitute?.callOnClick()
            else
                buStopPlay?.callOnClick()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        //initialize variables referring to the layout
        title = findViewById(R.id.NameRecordDetail)
        txtpath = findViewById(R.id.RecordPath)
        txtDuration = findViewById(R.id.RecordDuration)
        buSubstitute = findViewById(R.id.buttonSubstitute)
        buStopSubstitute = findViewById(R.id.buttonStopSubstitute)
        buStopPlay = findViewById(R.id.buttonStopDetail)
        buPlay = findViewById(R.id.buttonPlayDetail)
        seekDetailB = findViewById(R.id.progressBarDetail)
        txtRecordGoing = findViewById(R.id.textViewRecordingDetail)
        noiseIndicator = findViewById(R.id.NoiseLevelIndicatorDetail)

        //read data from intent
        recordtitle = (intent.getStringExtra("recordName") ?: "")
        path = (intent.getStringExtra("recordPath") ?: "")
        duration = (intent.getIntExtra("recordDuration", 0))
        txtpath?.text = path
        txtDuration?.text = String.format("00:%02d", duration / 1000)
        title?.text = getString(R.string.TitleDetail, recordtitle)

        //restore instance state
        if (savedInstanceState != null && recordtitle==savedInstanceState.getString("title")) {
            mBinder = savedInstanceState.getBinder("mBinder") as PlayerService.LocalBinder?
            mService = mBinder?.service
            applicationContext.bindService(Intent(this, PlayerService::class.java), mConnection, Context.BIND_AUTO_CREATE)
            seekDetailB?.max = duration
            seekDetailB?.progress = mService?.getProgress() ?: 0
            seekDetailB?.visibility = SeekBar.VISIBLE
            buStopPlay?.visibility = Button.VISIBLE
            buSubstitute?.visibility = Button.INVISIBLE
            time=Timer((duration - seekDetailB?.progress!!).toLong(), false)
            time?.start()
        } else {
            //there isn't an instance state
            seekDetailB?.visibility = View.INVISIBLE
            buStopPlay?.visibility = View.INVISIBLE
        }
        txtRecordGoing?.visibility = View.INVISIBLE
        noiseIndicator?.visibility = View.INVISIBLE
        buStopSubstitute?.visibility = View.INVISIBLE

        seekDetailB?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mService?.seekTo(seekBar.progress)
                time?.cancel()
                time = Timer((duration - seekBar.progress).toLong(), false)
                time?.start()
            }
        })

        buSubstitute?.setOnClickListener {
            if (startRecord(path, recordtitle, applicationContext) == 0 && !mBound){
                seekDetailB?.max = 30000
                time = Timer(31000, true)
                seekDetailB?.progress = 0
                time?.start()
                txtDuration?.text = "--:--"
                seekDetailB?.visibility = View.VISIBLE
                buStopSubstitute?.visibility = View.VISIBLE
                buSubstitute?.visibility = View.INVISIBLE
                buPlay?.visibility = View.INVISIBLE
                buStopPlay?.visibility = View.INVISIBLE
                txtRecordGoing?.visibility = View.VISIBLE
                noiseIndicator?.visibility = View.VISIBLE
                seekDetailB?.isEnabled = false
            }
        }

        buStopSubstitute?.setOnClickListener {
            if(mBound)
                return@setOnClickListener
            val r= stopRecord() ?: return@setOnClickListener
            time?.cancel()
            time = null
            val dataMedia= MediaMetadataRetriever()
            dataMedia.setDataSource(r.getPath()+r.getTitle())
            duration = dataMedia.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt()?:0
            txtDuration?.text = String.format("00:%02d", duration / 1000)
            buStopSubstitute?.visibility = View.INVISIBLE
            buSubstitute?.visibility = View.VISIBLE
            buPlay?.visibility = View.VISIBLE
            buStopPlay?.visibility = View.INVISIBLE
            seekDetailB?.visibility = View.INVISIBLE
            txtRecordGoing?.visibility = View.INVISIBLE
            noiseIndicator?.visibility = View.INVISIBLE
            seekDetailB?.isEnabled = true
        }

        buPlay?.setOnClickListener {
            thS=ServiceThread()
            thS?.start()
            seekDetailB?.max = duration
            time = Timer(duration.toLong(), false)
            seekDetailB?.progress = 0
            time?.start()
            buStopPlay?.visibility = View.VISIBLE
            seekDetailB?.visibility = View.VISIBLE
            buPlay?.visibility = View.INVISIBLE
            buStopSubstitute?.visibility = View.INVISIBLE
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
            buStopSubstitute?.visibility = View.INVISIBLE
            buSubstitute?.visibility = View.VISIBLE

        }
    }

    override fun onPause() {
        super.onPause()
        buStopSubstitute?.callOnClick()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(mBound)
        {
            outState.putBinder("service", mBinder)
            outState.putString("title", recordtitle)
        }
    }
}

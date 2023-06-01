package com.example.memovocali

import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PersistableBundle
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.log10

class RecordingActivity : AppCompatActivity() {

    private lateinit var title:String
    private lateinit var path:String
    private var buStop:ImageButton?=null
    private var txtRecordGoing: TextView?=null
    private var seekMainB: SeekBar?=null
    private var noiseIndicator1: ProgressBar?=null
    private var noiseIndicator2: ProgressBar?=null
    private var noiseIndicator3: ProgressBar?=null
    private var noiseIndicator4: ProgressBar?=null
    private var noiseIndicator5: ProgressBar?=null
    private var txtTitle:TextView?=null
    private var recorder: MediaRecorder?=null

    /**
     * Function for start the record
     */
    private fun startRecord(){
        //construct and start of the record
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        }
        else
            MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder?.setMaxDuration(30000)
        recorder?.setOutputFile(path+ title)
        recorder?.prepare()
        recorder?.start()
    }

    /**
     * Function for stop the record
     */
    private fun stopRecord(){
        recorder?.stop()
        recorder?.release()
        recorder=null
    }

    /**
     * Function to get the amplitude of the registration
     */
    private fun amplitude():Int{
        return recorder?.maxAmplitude?:0
    }


    private var timer: CountDownTimer =object: CountDownTimer(31000, 100) {

        override fun onTick(millisUntilFinished: Long) {
            noiseIndicator3?.progress=20* log10(amplitude().toDouble()).toInt()
            noiseIndicator2?.progress=noiseIndicator3?.progress?.div(2)?:0
            noiseIndicator1?.progress=noiseIndicator2?.progress?.div(2)?:0
            noiseIndicator4?.progress=noiseIndicator3?.progress?.div(2)?:0
            noiseIndicator5?.progress=noiseIndicator4?.progress?.div(2)?:0
            seekMainB?.progress=(30000-millisUntilFinished).toInt()
            val s="00:"+String.format("%02d",(30000-millisUntilFinished)/1000)
            txtRecordGoing?.text=getString(R.string.Recording,s)
        }

        override fun onFinish() {
            //call method to restore visibility
            buStop?.callOnClick()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)

        if(savedInstanceState!=null){
            finish()
        }
        else
        {
            val actionBar: ActionBar? = supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)

            buStop=findViewById(R.id.button_Stop)
            txtRecordGoing=findViewById(R.id.textViewRecording)
            seekMainB=findViewById(R.id.seekBar)
            noiseIndicator1=findViewById(R.id.NoiseLevelIndicator1)
            noiseIndicator2=findViewById(R.id.NoiseLevelIndicator2)
            noiseIndicator3=findViewById(R.id.NoiseLevelIndicator3)
            noiseIndicator4=findViewById(R.id.NoiseLevelIndicator4)
            noiseIndicator5=findViewById(R.id.NoiseLevelIndicator5)
            txtTitle=findViewById(R.id.textViewTitle)

            seekMainB?.isEnabled=false
            seekMainB?.max=30000

            title=intent.getStringExtra("title")?:""
            path=intent.getStringExtra("path")?:""
            txtTitle?.text=title.replace(".aac","")
            startRecord()
            timer.start()
        }

        buStop?.setOnClickListener {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
        stopRecord()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isRecording",true)
    }
    override fun onSupportNavigateUp(): Boolean {
        buStop?.callOnClick()
        return true
    }
}
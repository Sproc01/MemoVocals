package com.example.memovocali

import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
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
    private var timer:TimerRecording?=null

    /**
     * Timer for the recording
     */
    inner class TimerRecording(x: Long) : CountDownTimer(x, 100) {
        override fun onTick(millisUntilFinished: Long) {
            noiseIndicator3?.progress=20* log10(amplitude().toDouble()).toInt()
            noiseIndicator2?.progress=noiseIndicator3?.progress?.div(2)?:0
            noiseIndicator1?.progress=noiseIndicator2?.progress?.div(2)?:0
            noiseIndicator4?.progress=noiseIndicator3?.progress?.div(2)?:0
            noiseIndicator5?.progress=noiseIndicator4?.progress?.div(2)?:0
            seekMainB?.progress=seekMainB?.progress?.plus(100)!!
            val s="00:"+String.format("%02d",seekMainB?.progress?.div(1000)!!)
            txtRecordGoing?.text=getString(R.string.Recording,s)
        }

        override fun onFinish() {
            //call method to close the activty
            buStop?.callOnClick()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)

            //set the back button
            val actionBar: ActionBar? = supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)

            //initialize variables referring to the layout
            buStop=findViewById(R.id.button_Stop)
            txtRecordGoing=findViewById(R.id.textViewRecording)
            seekMainB=findViewById(R.id.seekBar)
            noiseIndicator1=findViewById(R.id.NoiseLevelIndicator1)
            noiseIndicator2=findViewById(R.id.NoiseLevelIndicator2)
            noiseIndicator3=findViewById(R.id.NoiseLevelIndicator3)
            noiseIndicator4=findViewById(R.id.NoiseLevelIndicator4)
            noiseIndicator5=findViewById(R.id.NoiseLevelIndicator5)
            txtTitle=findViewById(R.id.textViewTitle)

            //set the seekbar
            seekMainB?.isEnabled=false
            seekMainB?.max=30000

            //read data from intent
            title=intent.getStringExtra("title")?:""
            path=intent.getStringExtra("path")?:""
            txtTitle?.text=title.replace(".aac","")
        
            if(savedInstanceState!=null){
                seekMainB?.progress=savedInstanceState.getInt("progress")
                timer=TimerRecording((31000-seekMainB?.progress!!).toLong())
                timer?.start()
            }
            else
            {
                startRecord(path, title, applicationContext)
                timer=TimerRecording(31000)
                timer?.start()
            }


        /**
         * Function call when the stop button is pressed
         */
        buStop?.setOnClickListener {
            stopRecord()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        //recording is possible only if the activity stay in foreground
        timer?.cancel()
        if(!isChangingConfigurations)
            buStop?.callOnClick()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("progress",seekMainB?.progress?:0)
    }

    override fun onSupportNavigateUp(): Boolean {
        //if the back button is pressed, call the stop button
        buStop?.callOnClick()
        return true
    }
}
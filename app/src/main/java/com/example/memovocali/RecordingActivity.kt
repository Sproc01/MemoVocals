package com.example.memovocali

import android.os.Bundle
import android.os.CountDownTimer
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
    private var noiseIndicator: ProgressBar?=null
    private var txtTitle:TextView?=null
    private var timer: CountDownTimer =object: CountDownTimer(31000, 100) {

        override fun onTick(millisUntilFinished: Long) {
            noiseIndicator?.progress=20* log10(amplitude().toDouble()).toInt()
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

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)


        buStop=findViewById(R.id.button_Stop)
        txtRecordGoing=findViewById(R.id.textViewRecording)
        seekMainB=findViewById(R.id.seekBar)
        noiseIndicator=findViewById(R.id.NoiseLevelIndicator)
        txtTitle=findViewById(R.id.textViewTitle)

        seekMainB?.isEnabled=false
        seekMainB?.max=30000


        title=intent.getStringExtra("title")?:""
        path=intent.getStringExtra("path")?:""
        txtTitle?.text=title.replace(".aac","")
        startRecord(path,title, applicationContext)
        timer.start()

        buStop?.setOnClickListener {
            timer.cancel()
            stopRecord()
            finish()
        }


    }

    override fun onPause() {
        super.onPause()
        buStop?.callOnClick()
    }

    override fun onSupportNavigateUp(): Boolean {
        buStop?.callOnClick()
        finish()
        return true
    }
}
package com.example.memovocali

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {

    private var buClose: Button ? =null
    private var title:TextView? =null
    private var txtpath:TextView? =null
    private var txtDuration:TextView? =null
    private var buSubstitute:Button? =null
    private var buStopSubstitute:Button? =null
    private var buStopPlay:Button? =null
    private var buPlay:Button? =null
    var progB: SeekBar? =null

    /*class timer(private val x:Long, private val flag:Boolean):CountDownTimer(x,500)
    {
        override fun onTick(millisUntilFinished: Long) {
            progB?.progress=(x-millisUntilFinished).toInt()
        }

        override fun onFinish() {
            if(flag)
                buStopSubstitute?.callOnClick()
            else
                buStopPlay?.callOnClick()
        }

    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        //initialize variables referring to the layout
        buClose =findViewById(R.id.buttonClose)
        title=findViewById(R.id.NameRecordDetail)
        txtpath=findViewById(R.id.RecordPath)
        txtDuration=findViewById(R.id.RecordDuration)
        buSubstitute=findViewById(R.id.buttonSubstitute)
        buStopSubstitute=findViewById(R.id.buttonStopSubstitute)
        buStopPlay=findViewById(R.id.buttonStopDetail)
        buPlay=findViewById(R.id.buttonPlayDetail)
        progB =findViewById(R.id.progressBarDetail)

        //timer: one for record, one for player
        var timerPlay:CountDownTimer?=null
        val timerRecord:CountDownTimer=object : CountDownTimer(30000,1000){
            override fun onTick(millisUntilFinished: Long) {
                progB?.progress=(30000-millisUntilFinished).toInt()
            }

            override fun onFinish() {
                buStopSubstitute?.callOnClick()
            }
        }

        //components invisible
        progB?.visibility= View.INVISIBLE
        buStopPlay?.visibility= View.INVISIBLE
        buStopSubstitute?.visibility= View.INVISIBLE
        //read intent from data
        val name=(intent.getStringExtra("recordName") ?: "")
        val path=(intent.getStringExtra("recordPath") ?: "")
        var duration=(intent.getIntExtra("recordDuration",0))
        txtpath?.text=path
        txtDuration?.text=String.format("00:%02d", duration/1000)
        title?.text=getString(R.string.TitleDetail,name)

        buClose?.setOnClickListener{
            stopPlay()
            finish()
        }

        progB?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seekPlay(seekBar.progress)
                /*timer?.cancel()
                timer=null
                time=timer(seekBar.progress*1000, false)*/
            }
        })

        buSubstitute?.setOnClickListener{
            if(startRecord(path, name)==0)
            {
                progB?.max=30000
                timerRecord.start()
                txtDuration?.text="--:--"
                progB?.visibility= View.VISIBLE
                buStopSubstitute?.visibility= View.VISIBLE
                buSubstitute?.visibility= View.INVISIBLE
                buPlay?.visibility= View.INVISIBLE
                buStopPlay?.visibility= View.INVISIBLE
                buClose?.visibility= View.INVISIBLE
                progB?.isEnabled=false
            }
        }

        buStopSubstitute?.setOnClickListener{
            val r=stopRecord()
            timerRecord.cancel()
            duration=r.getDuration()
            txtDuration?.text=String.format("00:%02d", duration/1000)
            buStopSubstitute?.visibility= View.INVISIBLE
            buSubstitute?.visibility= View.VISIBLE
            buPlay?.visibility= View.VISIBLE
            buStopPlay?.visibility= View.INVISIBLE
            buClose?.visibility= View.VISIBLE
            progB?.visibility= View.INVISIBLE
            progB?.isEnabled=true
        }

        buPlay?.setOnClickListener{
            if(startPlay(path + name)==0) {
                progB?.max=duration
                timerPlay = object : CountDownTimer(duration.toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        progB?.progress = duration - millisUntilFinished.toInt()
                    }

                    override fun onFinish() {
                        buStopPlay?.callOnClick()
                    }
                }
                timerPlay?.start()
                buStopPlay?.visibility = View.VISIBLE
                progB?.visibility= View.VISIBLE
                buPlay?.visibility = View.INVISIBLE
                buStopSubstitute?.visibility = View.INVISIBLE
                buSubstitute?.visibility = View.INVISIBLE
            }
        }
        buStopPlay?.setOnClickListener{
            stopPlay()
            progB?.visibility= View.INVISIBLE
            timerPlay?.cancel()
            buStopPlay?.visibility= View.INVISIBLE
            buPlay?.visibility= View.VISIBLE
            buStopSubstitute?.visibility= View.INVISIBLE
            buSubstitute?.visibility= View.VISIBLE
        }
    }
}
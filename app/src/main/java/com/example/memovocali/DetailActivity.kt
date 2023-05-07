package com.example.memovocali

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import java.io.File

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        //initialize variables referring to the layout
        val buClose: Button =findViewById(R.id.buttonClose)
        val title:TextView=findViewById(R.id.NameRecordDetail)
        val txtpath:TextView=findViewById(R.id.RecordPath)
        val txtDuration:TextView=findViewById(R.id.RecordDuration)
        val buSubstitute:Button=findViewById(R.id.buttonSubstitute)
        val buStopSubstitute:Button=findViewById(R.id.buttonStopSubstitute)
        val buStopPlay:Button=findViewById(R.id.buttonStopDetail)
        val buPlay:Button=findViewById(R.id.buttonPlayDetail)
        val progB: ProgressBar =findViewById(R.id.progressBarDetail)
        //timer: one for record, one for player
        var timerPlay:CountDownTimer?=null
        val timerRecord:CountDownTimer=object : CountDownTimer(30000,1000){
            override fun onTick(millisUntilFinished: Long) {
                progB.progress=30-(millisUntilFinished/1000).toInt()
            }

            override fun onFinish() {
                buStopSubstitute.callOnClick()
            }
        }

        //components invisible
        progB.visibility= View.INVISIBLE
        buStopPlay.visibility= View.INVISIBLE
        buStopSubstitute.visibility= View.INVISIBLE
        //read intent from data
        val name=(intent.getStringExtra("recordName") ?: "")
        val path=(intent.getStringExtra("recordPath") ?: "")
        var duration=(intent.getIntExtra("recordDuration",0))
        txtpath.text=path
        txtpath.isEnabled=false
        txtDuration.text=String.format("00:%02d", duration/1000)
        title.text=getString(R.string.TitleDetail,name)

        buClose.setOnClickListener{
            stopPlay()
            finish()
        }

        buSubstitute.setOnClickListener{
            //val file= File(path,name)
            //file.delete()
            if(startRecord(path, name)==0)
            {
                progB.max=30
                timerRecord.start()
                txtDuration.text="--:--"
                progB.visibility= View.VISIBLE
                buStopSubstitute.visibility= View.VISIBLE
                buSubstitute.visibility= View.INVISIBLE
                buPlay.visibility= View.INVISIBLE
                buStopPlay.visibility= View.INVISIBLE
                buClose.visibility= View.INVISIBLE
            }
        }

        buStopSubstitute.setOnClickListener{
            val r=stopRecord()
            timerRecord.cancel()
            duration=r.getDuration()
            txtDuration.text=String.format("00:%02d", duration/1000)
            buStopSubstitute.visibility= View.INVISIBLE
            buSubstitute.visibility= View.VISIBLE
            buPlay.visibility= View.VISIBLE
            buStopPlay.visibility= View.INVISIBLE
            buClose.visibility= View.VISIBLE
            progB.visibility= View.INVISIBLE
        }

        buPlay.setOnClickListener{
            if(startPlay(path + name)==0) {
                progB.max=duration/1000
                timerPlay = object : CountDownTimer(duration.toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        progB.progress = (duration - millisUntilFinished.toInt())/1000
                    }

                    override fun onFinish() {
                        buStopPlay.callOnClick()
                    }
                }
                timerPlay?.start()
                buStopPlay.visibility = View.VISIBLE
                progB.visibility= View.VISIBLE
                buPlay.visibility = View.INVISIBLE
                buStopSubstitute.visibility = View.INVISIBLE
                buSubstitute.visibility = View.INVISIBLE
            }
        }
        buStopPlay.setOnClickListener{
            stopPlay()
            progB.visibility= View.INVISIBLE
            timerPlay?.cancel()
            buStopPlay.visibility= View.INVISIBLE
            buPlay.visibility= View.VISIBLE
            buStopSubstitute.visibility= View.INVISIBLE
            buSubstitute.visibility= View.VISIBLE
        }
    }
}
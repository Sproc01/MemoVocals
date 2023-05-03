package com.example.memovocali

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.io.File

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val buClose: Button =findViewById(R.id.buttonClose)
        val title:TextView=findViewById(R.id.NameRecordDetail)
        val txtpath:TextView=findViewById(R.id.RecordPath)
        val txtDuration:TextView=findViewById(R.id.RecordDuration)
        val buSubstitute:Button=findViewById(R.id.buttonSubstitute)
        val buStopSubstitute:Button=findViewById(R.id.buttonStopSubstitute)
        val buStopPlay:Button=findViewById(R.id.buttonStopDetail)
        val buPlay:Button=findViewById(R.id.buttonPlayDetail)
        var timerPlay:CountDownTimer?=null
        val timerRecord:CountDownTimer=object : CountDownTimer(30000,1000){
            override fun onTick(millisUntilFinished: Long) {
                txtDuration.text=String.format("00:%02d", 30-millisUntilFinished/1000)
            }

            override fun onFinish() {
                buStopSubstitute.callOnClick()
            }
        }


        //buttons invisible
        buStopPlay.visibility= View.INVISIBLE
        buStopSubstitute.visibility= View.INVISIBLE
        //read intent from data
        val name=(intent.getStringExtra("recordName") ?: "")
        val path=(intent.getStringExtra("recordPath") ?: "")
        val duration=(intent.getIntExtra("recordDuration",0))
        txtpath.text=path
        txtpath.isEnabled=false
        txtDuration.text=String.format("00:%02d", duration/1000)
        title.text=getString(R.string.TitleDetail,name)

        buClose.setOnClickListener{
            stopPlay()
            finish()
        }

        buSubstitute.setOnClickListener{
            if(path!="" && name!="") {
                val file= File(path,name)
                file.delete()
                txtDuration.text=""
                startRecord(path, name)
                timerRecord.start()
                buStopSubstitute.visibility= View.VISIBLE
                buSubstitute.visibility= View.INVISIBLE
                buPlay.visibility= View.INVISIBLE
                buStopPlay.visibility= View.INVISIBLE
                buClose.visibility= View.INVISIBLE
            }
        }

        buStopSubstitute.setOnClickListener{
            //TODO update duration in recordList della main activity
            val r=stopRecord()
            timerRecord.cancel()
            buStopSubstitute.visibility= View.INVISIBLE
            buSubstitute.visibility= View.VISIBLE
            buPlay.visibility= View.VISIBLE
            buStopPlay.visibility= View.INVISIBLE
            buClose.visibility= View.VISIBLE
        }

        buPlay.setOnClickListener{
            if(path!="" && name!="") {
                startPlay(path + name)
                buStopPlay.visibility = View.VISIBLE
                buPlay.visibility = View.INVISIBLE
                buStopSubstitute.visibility = View.INVISIBLE
                buSubstitute.visibility = View.INVISIBLE
                timerPlay = object : CountDownTimer(duration.toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        buStopPlay.callOnClick()
                    }
                }
                timerPlay?.start()
            }
        }
        buStopPlay.setOnClickListener{
            stopPlay()
            timerPlay?.cancel()
            buStopPlay.visibility= View.INVISIBLE
            buPlay.visibility= View.VISIBLE
            buStopSubstitute.visibility= View.INVISIBLE
            buSubstitute.visibility= View.VISIBLE
        }
    }
}
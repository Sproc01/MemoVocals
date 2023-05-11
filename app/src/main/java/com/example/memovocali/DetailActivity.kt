package com.example.memovocali

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Timer(private val x:Long, private val flag:Boolean,private val activity:DetailActivity):CountDownTimer(x,500)
    {
        override fun onTick(millisUntilFinished: Long) {
            activity.progB?.progress=activity.progB?.progress?.plus(500)!!
        }

        override fun onFinish() {
            if(flag)
                activity.buStopSubstitute?.callOnClick()
            else
                activity.buStopPlay?.callOnClick()
        }

    }

class DetailActivity : AppCompatActivity() {

    private var buClose: Button ? =null
    private var title:TextView? =null
    private var txtpath:TextView? =null
    private var txtDuration:TextView? =null
    private var buSubstitute:Button? =null
    private var buPlay:Button? =null
    internal var buStopSubstitute:Button? =null
    internal var buStopPlay:Button? =null
    internal var progB: SeekBar? =null

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
        var time:Timer?=null

        //components invisible
        progB?.visibility= View.INVISIBLE
        buStopPlay?.visibility= View.INVISIBLE
        buStopSubstitute?.visibility= View.INVISIBLE

        //read data from intent
        val name=(intent.getStringExtra("recordName") ?: "")
        val path=(intent.getStringExtra("recordPath") ?: "")
        var duration=(intent.getIntExtra("recordDuration",0))
        txtpath?.text=path
        txtDuration?.text=String.format("00:%02d", duration/1000)
        title?.text=getString(R.string.TitleDetail,name)

        buClose?.setOnClickListener{
            stopRecord()
            finish()
        }

        progB?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                pausePlay()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seekPlay(seekBar.progress)
                time?.cancel()
                time=Timer(duration.toLong()-seekBar.progress, false,this@DetailActivity)
                time?.start()
            }
        })

        buSubstitute?.setOnClickListener{
            if(startRecord(path, name)==0)
            {
                progB?.max=30000
                time=Timer(30000, true,this)
                progB?.progress=0
                time?.start()
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
            time?.cancel()
            time=null
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
                time=Timer(duration.toLong(), false,this)
                progB?.progress=0
                time?.start()
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
            time?.cancel()
            time=null
            buStopPlay?.visibility= View.INVISIBLE
            buPlay?.visibility= View.VISIBLE
            buStopSubstitute?.visibility= View.INVISIBLE
            buSubstitute?.visibility= View.VISIBLE
        }
    }
}
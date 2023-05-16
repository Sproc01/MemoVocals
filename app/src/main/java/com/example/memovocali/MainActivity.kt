package com.example.memovocali

import android.Manifest
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    private var buAdd:Button?=null
    private var buStop:Button?=null
    private val records:MutableList<Record> = mutableListOf()
    private var progB: SeekBar?=null
    private var dataMedia:MediaMetadataRetriever?=null
    private var rc:RecyclerView?=null
    private var txtRecordGoing:TextView?=null
    private var noiseIndicator: ProgressBar?=null
    private var timer: CountDownTimer=object: CountDownTimer(31000, 100) {

        override fun onTick(millisUntilFinished: Long) {
            noiseIndicator?.progress=amplitude()
            progB?.progress=(30000-millisUntilFinished).toInt()
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
        setContentView(R.layout.activity_main)

        //initialize variables referring to the layout
        buAdd=findViewById(R.id.action_button_Add)
        buStop=findViewById(R.id.action_button_Stop)
        progB=findViewById(R.id.progressBar)
        rc=findViewById(R.id.recyclerView)
        txtRecordGoing=findViewById(R.id.textViewRecording)
        noiseIndicator=findViewById(R.id.NoiseLevelIndicator)

        //check permission
        /*if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PermissionChecker.PERMISSION_GRANTED)
        {
            buAdd?.visibility=Button.INVISIBLE
            buStop?.visibility=Button.INVISIBLE
            progB?.visibility=SeekBar.INVISIBLE
            txtRecordGoing?.visibility=TextView.VISIBLE
            rc?.visibility=RecyclerView.INVISIBLE
            txtRecordGoing?.text=getString(R.string.Permission)
        }*/

        //read files in the directory if present otherwise it create a new directory
        val file= File(applicationContext.filesDir,"Memo")
        if(!file.exists())
            file.mkdir()
        else
            for (f in file.listFiles()!!){
                dataMedia=MediaMetadataRetriever()
                dataMedia?.setDataSource(f.absolutePath)
                records.add(Record(f.name,file.absolutePath+File.separator,
                    dataMedia?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0))
            }

        rc?.adapter=RecordAdapter(records)
        buStop?.visibility=Button.INVISIBLE
        progB?.visibility=SeekBar.INVISIBLE
        txtRecordGoing?.visibility=TextView.INVISIBLE
        noiseIndicator?.visibility=ProgressBar.INVISIBLE
        progB?.isEnabled=false
        progB?.max=30000

        buAdd?.setOnClickListener {
            if (startRecord(
                    applicationContext.filesDir.toString() + File.separator + "Memo" + File.separator,
                    Calendar.getInstance().time.toString().replace(":","").replace("GMT+","") + ".aac"
                , applicationContext) == 0
            ) {
                //reset seek and progress bar
                progB?.progress=0
                noiseIndicator?.progress=0
                //start a timer to limit 30 second for the record
                timer.start()
                //setting visibility
                buStop?.visibility = Button.VISIBLE
                buAdd?.visibility=Button.INVISIBLE
                progB?.visibility = SeekBar.VISIBLE
                txtRecordGoing?.visibility=TextView.VISIBLE
                noiseIndicator?.visibility=ProgressBar.VISIBLE
            }
        }

        buStop?.setOnClickListener{
            val r=stopRecord()
            (rc?.adapter as RecordAdapter).addRecord(r!!)
            //stop timer
            timer.cancel()
            //setting visibility
            buStop?.visibility=Button.INVISIBLE
            buAdd?.visibility=Button.VISIBLE
            progB?.visibility=SeekBar.INVISIBLE
            noiseIndicator?.visibility=ProgressBar.INVISIBLE
            txtRecordGoing?.visibility=TextView.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PermissionChecker.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE)

        //update duration of the records if one is modified in the detailActivity
        for (i in records) {
            dataMedia = MediaMetadataRetriever()
            dataMedia?.setDataSource(i.getPath()+i.getTitle())
            i.updateDuration(
                dataMedia?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toInt() ?: 0
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //TODO save state
    }

    override fun onPause() {
        super.onPause()
        if(buStop?.visibility==Button.VISIBLE)
            buStop?.callOnClick()
    }

    //TODO manage others life state activity
    companion object{
        private const val REQUEST_CODE = 12345
    }


}
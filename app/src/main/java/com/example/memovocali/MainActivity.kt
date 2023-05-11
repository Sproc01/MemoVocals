package com.example.memovocali

import android.Manifest
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File


class MainActivity : AppCompatActivity() {

    private var buAdd:FloatingActionButton?=null
    private var buStop:Button?=null
    private val RecordList:MutableList<Record> = mutableListOf()
    private var txtName:TextView?=null
    private var progB: SeekBar?=null
    private var dataMedia:MediaMetadataRetriever?=null
    private var rc:RecyclerView?=null
    private var timer: CountDownTimer =object: CountDownTimer(30000, 1000) {

        override fun onTick(millisUntilFinished: Long) {
            progB?.progress=(30000-millisUntilFinished).toInt()
        }

        override fun onFinish() {
            progB?.progress=30
            //call method to restore visibility
            buStop?.callOnClick()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize variables referring to the layout
        buAdd=findViewById(R.id.floating_action_button_Add)
        buStop=findViewById(R.id.action_button_Stop)
        progB=findViewById(R.id.progressBar)
        rc=findViewById(R.id.recyclerView)

        //read files in the directory if present otherwise it create a new directory
        val file= File(applicationContext.filesDir,"Memo")
        if(!file.exists())
            file.mkdir()
        else
            for (f in file.listFiles()!!){
                dataMedia=MediaMetadataRetriever()
                dataMedia?.setDataSource(f.absolutePath)
                RecordList.add(Record(f.name,file.absolutePath+File.separator,
                    dataMedia?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0))
            }

        rc?.adapter=RecordAdapter(RecordList)
        buStop?.visibility=Button.INVISIBLE
        progB?.visibility=SeekBar.INVISIBLE
        txtName?.visibility=TextView.INVISIBLE
        progB?.isEnabled=false
        progB?.max=30000

        buAdd?.setOnClickListener {
            val dialog=MaterialAlertDialogBuilder(this)
            val viewDialog=LayoutInflater.from(this).inflate(R.layout.dialog,null)
            dialog.setView(viewDialog)
            txtName=viewDialog.findViewById(R.id.input)
            dialog.setTitle(getString(R.string.Dialogtitle))
            dialog.setPositiveButton(getString(R.string.labelOk)
            ) { DialogInterface, i ->
                if(txtName?.text.toString().isNotEmpty())
                {
                    if(txtName?.text.toString().contains(".aac"))
                        txtName?.text=txtName?.text.toString().replace(".aac","")
                    for(i in RecordList)
                        if(txtName?.text.toString()+".aac"==i.getTitle())
                        {
                            val error=MaterialAlertDialogBuilder(this)
                            error.setTitle(getString(R.string.DialogErrorTitle))
                            error.setMessage(getString(R.string.errorAlreadyPresent))
                            error.setPositiveButton(getString(R.string.labelOk),null)
                            error.show()
                            return@setPositiveButton
                        }
                    if(startRecord(applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator,txtName?.text.toString()+".aac")==0) {
                        //start a timer to limit 30 second for the record
                        timer.start()
                        buStop?.visibility=Button.VISIBLE
                        buAdd?.hide()
                        progB?.visibility = SeekBar.VISIBLE
                    }
                }
                else {
                    val error=MaterialAlertDialogBuilder(this)
                    error.setTitle(getString(R.string.DialogErrorTitle))
                    error.setMessage(getString(R.string.errorInsert))
                    error.setPositiveButton(getString(R.string.labelOk),null)
                    error.show()
                }
            }
            dialog.setNegativeButton(getString(R.string.labelCancel),null)
            dialog.show()
        }

        buStop?.setOnClickListener{
            val r=stopRecord()
            (rc?.adapter as RecordAdapter).addRecord(r)
            buStop?.visibility=Button.INVISIBLE
            timer.cancel()
            buAdd?.show()
            progB?.visibility=SeekBar.INVISIBLE
            txtName?.hint=getString(R.string.labelInput)
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PermissionChecker.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE)

        //update duration of the records if one is modified in the detailActivity
        for (i in RecordList) {
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
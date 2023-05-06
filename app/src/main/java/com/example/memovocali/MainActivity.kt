package com.example.memovocali

import android.Manifest
import android.content.DialogInterface
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File


class MainActivity : AppCompatActivity() {

    private var buAdd:FloatingActionButton?=null
    private var buStop:FloatingActionButton?=null
    private val RecordList:MutableList<Record> = mutableListOf()
    private var txtName:TextView?=null
    private var progB: ProgressBar?=null
    private var dataMedia:MediaMetadataRetriever?=null
    private var rc:RecyclerView?=null
    private var timer: CountDownTimer =object: CountDownTimer(30000, 1000) {

        override fun onTick(millisUntilFinished: Long) {
            progB?.progress=30-(millisUntilFinished/1000).toInt()
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
        buStop=findViewById(R.id.floating_action_button_Stop)
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
        //restore state if necessary
        /*if(savedInstanceState!=null)
        {
            txtName?.text=savedInstanceState.getString("txtName")
            txtName?.hint=savedInstanceState.getString("txtNameHint")
            buAdd?.visibility=savedInstanceState.getInt("buAddVisibility")
            buStop?.visibility=savedInstanceState.getInt("buStopVisibility")
            progB?.visibility=savedInstanceState.getInt("progBVisibility")
        }
        else
        {
            buStop?.hide()
            progB?.visibility=ProgressBar.INVISIBLE
        }*/
        buStop?.hide()
        progB?.visibility=ProgressBar.INVISIBLE
        txtName?.visibility=TextView.INVISIBLE

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
                    try {
                        startRecord(applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator,txtName?.text.toString()+".aac")
                        //start a timer to limit 30 second for the record
                        timer.start()
                        buStop?.show()
                        buAdd?.hide()
                        progB?.visibility=ProgressBar.VISIBLE
                    }
                    catch (e:FileExistException){
                        val error=MaterialAlertDialogBuilder(this)
                        error.setTitle(getString(R.string.DialogErrorTitle))
                        error.setMessage(getString(R.string.errorAlreadyPresent))
                        error.setPositiveButton(getString(R.string.labelOk),null)
                        error.show()
                    }
                }
                else
                {
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
            buStop?.hide()
            timer.cancel()
            buAdd?.show()
            progB?.visibility=ProgressBar.INVISIBLE
            txtName?.hint=getString(R.string.labelInput)
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PermissionChecker.PERMISSION_GRANTED)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PermissionChecker.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
            else
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE)
        }
        else
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PermissionChecker.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
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
        /*outState.putString("name",txtName?.text.toString())
        outState.putInt("visibilityProgress",progB?.visibility ?: ProgressBar.INVISIBLE)
        outState.putInt("visibilityButtonStop",buStop?.visibility ?: FloatingActionButton.INVISIBLE)
        outState.putInt("visibilityButtonAdd",buAdd?.visibility ?: FloatingActionButton.VISIBLE)*/
        //TODO save state
    }

    override fun onPause() {
        super.onPause()
        if(buStop?.visibility==FloatingActionButton.VISIBLE)
            buStop?.callOnClick()
    }

    //TODO manage others life state activity
    companion object{
        private const val REQUEST_CODE = 12345
    }


}
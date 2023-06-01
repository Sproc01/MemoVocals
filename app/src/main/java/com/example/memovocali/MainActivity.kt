package com.example.memovocali

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StatFs
import android.util.Log
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    private var buNewRecord:ImageButton?=null
    private val records:MutableList<Record> = mutableListOf()
    private var rc:RecyclerView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize variables referring to the layout
        buNewRecord=findViewById(R.id.action_button_new)
        rc=findViewById(R.id.recyclerView)

        //check permission
        /*if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PermissionChecker.PERMISSION_GRANTED)
        {
            buNewRecord?.visibility=Button.INVISIBLE
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
                records.add(Record(f.name,file.absolutePath+File.separator))
            }
        records.sortBy{ it.getTitle() }
        rc?.adapter=RecordAdapter(records)


        buNewRecord?.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PermissionChecker.PERMISSION_GRANTED)
                return@setOnClickListener
            val intent= Intent(this,RecordingActivity::class.java)
            val title=Calendar.getInstance().time.toString().replace(":","").replace("GMT+","") + ".aac"
            val path=applicationContext.filesDir.toString() + File.separator + "Memo" + File.separator
            val stat = StatFs(path)
            val megAvailable = stat.availableBytes/1000000
            if(megAvailable>15)
            {
                intent.putExtra("title",title)
                intent.putExtra("path",path)
                startActivity(intent)
                (rc?.adapter as RecordAdapter).addRecord(Record(title,path))
            }
            else
            {
                val error= MaterialAlertDialogBuilder(applicationContext)
                error.setTitle(getString(R.string.DialogSpace))
                error.setMessage(getString(R.string.errorEnoughSpace))
                error.setPositiveButton(getString(R.string.labelOk),null)
                error.show()
            }

        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PermissionChecker.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)!=
                PermissionChecker.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)!=
                PermissionChecker.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.FOREGROUND_SERVICE), REQUEST_CODE)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG,"onSaveInstanceState")
        //TODO maybe save state if necessary
    }

    override fun onPause() {
        Log.d(TAG,"onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG,"onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG,"onDestroy")
        super.onDestroy()
    }
    //TODO maybe manage others life state activity
    companion object{
        private const val REQUEST_CODE = 12345
        private val TAG=MainActivity::class.java.simpleName
    }


}
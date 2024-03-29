package com.example.memovocali

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StatFs
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.DateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private var buNewRecord:ImageButton?=null
    private val records:MutableList<Record> = mutableListOf()
    private var rc:RecyclerView?=null

    @SuppressLint("NotifyDataSetChanged") //I use it because i reorder the list so the recycler view must be updated
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize variables referring to the layout
        buNewRecord=findViewById(R.id.action_button_new)
        rc=findViewById(R.id.recyclerView)

        //read files in the directory if present otherwise it create a new directory
        val file= File(applicationContext.filesDir,"Memo")
        if(!file.exists())
            file.mkdir()
        else
            for (f in file.listFiles()!!){
                records.add(Record(f.name,file.absolutePath+File.separator))
            }
        //sort the list
        records.sortBy{ it.getTitle().uppercase() }
        //set the adapter
        rc?.adapter=RecordAdapter(records)

        buNewRecord?.setOnClickListener {
            //check permission
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PermissionChecker.PERMISSION_GRANTED) { //if the app haven't the permission dialog to inform the user
                val error= MaterialAlertDialogBuilder(this)
                error.setTitle(getString(R.string.errorNoPermission))
                error.setMessage(getString(R.string.errorNoPermissionAudio))
                error.setPositiveButton(getString(R.string.Ok),null)
                error.show()
                return@setOnClickListener
            }

            //start the recording activity
            val intent= Intent(this,RecordingActivity::class.java)
            val title=if(Locale.getDefault().language == "it" || Locale.getDefault().language == "en")
                (DateFormat.getDateInstance().format(Calendar.getInstance().time)+" "+DateFormat.getTimeInstance().format(Calendar.getInstance().time)).replace(":"," ").replace(",","")+".aac"
            else
                Calendar.getInstance().time.toString().replace(":"," ").replace("GMT+","") + ".aac"
            val path=applicationContext.filesDir.toString() + File.separator + "Memo" + File.separator

            //check if there is enough space
            val stat = StatFs(path)
            val megAvailable = stat.availableBytes/1000000
            if(megAvailable>size) {
                intent.putExtra("title",title)
                intent.putExtra("path",path)
                startActivity(intent)
                //update the recycler view and the list
                (rc?.adapter as RecordAdapter).addRecord(Record(title,path))
                records.sortBy{ it.getTitle().uppercase() }
                rc?.adapter?.notifyDataSetChanged()
            }
            else {//error message
                val error= MaterialAlertDialogBuilder(this)
                error.setTitle(getString(R.string.DialogSpace))
                error.setMessage(getString(R.string.errorEnoughSpace))
                error.setPositiveButton(getString(R.string.Ok),null)
                error.show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //ask permission if the app haven't them
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PermissionChecker.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)!=
                PermissionChecker.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
        }
    }

    companion object{
        /**
         * Request code for permission
         */
        private const val REQUEST_CODE = 12345

        /**
         * Size of the file in MB
         */
        private const val size=15
    }


}
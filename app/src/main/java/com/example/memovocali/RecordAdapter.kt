package com.example.memovocali

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaMetadataRetriever
import android.os.CountDownTimer
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class RecordAdapter(private val Records:MutableList<Record> =mutableListOf()): RecyclerView.Adapter<RecordAdapter.ViewHolderRecord>()
{
    override fun getItemCount():Int {
        return Records.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderRecord {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_item, parent, false)
        return ViewHolderRecord(view, this, parent)
    }

    fun removeRecord(record: Record){
        val file= File(record.getPath()+record.getTitle())
        val position=Records.indexOf(record)
        Records.remove(record)
        notifyItemRemoved(position)
        file.delete()
    }

    fun addRecord(record:Record){
        Records.add(record)
        notifyItemInserted(Records.size-1)
    }

    override fun onBindViewHolder(holder: ViewHolderRecord, position: Int) {
        holder.bind(Records[position],position)
    }

    class ViewHolderRecord(itemView: View, private val rA:RecordAdapter, private val parent:ViewGroup): RecyclerView.ViewHolder(itemView) {

        private val txtTitle:TextView=itemView.findViewById(R.id.txtTitle)
        private var pos:Int=0
        private val buDelete:Button=itemView.findViewById(R.id.buttonDelete)
        //private val buPlay:Button=itemView.findViewById(R.id.buttonPlay)
        //private val buStopPlay:Button=itemView.findViewById(R.id.buttonStopPlay)
        private val txtName:TextView=itemView.findViewById(R.id.txtTitle)
        //private val seekb:SeekBar =itemView.findViewById(R.id.seekBar)
        private val buOpen:Button=itemView.findViewById(R.id.buttonOpen)
        //private var timer:Time?=null
        private lateinit var record:Record
        private lateinit var mService: PlayerService

       /* private val mConnection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as PlayerService.LocalBinder
                mService = binder.service
                //setIsPlaying(mService.isPlaying())
            }
            override fun onServiceDisconnected(name: ComponentName) { //setIsPlaying(mService.isPlaying())
             }
        }*/

        init{

            itemView.setOnClickListener{
                buOpen.callOnClick()
            }

            buOpen.setOnClickListener{
                val intent= Intent(parent.context,DetailActivity::class.java)
                intent.putExtra("recordName", record.getTitle())
                intent.putExtra("recordPath", record.getPath())
                val dataMedia=MediaMetadataRetriever()
                dataMedia.setDataSource(record.getPath()+record.getTitle())
                intent.putExtra("recordDuration", dataMedia.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt())
                parent.context.startActivity(intent)
            }

            txtName.setOnEditorActionListener { v, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        var txt=txtName.text.toString()
                        for(i in txt)
                            if(!(i in 'A'..'Z' || i in 'a'..'z' || i in '0'..'9' || i==' '))
                            {
                                txtName.error=parent.context.getString(R.string.errorInvalidName)
                                return@setOnEditorActionListener false
                            }
                        if(txt=="")
                            return@setOnEditorActionListener false
                        val n=record.getTitle()
                        if(txtName.text.toString().contains(".aac"))
                            txtName.text=txtName.text.toString().replace(".aac","")
                        if(!(txt.contains(".aac")))
                            txt+=".aac"
                        if(txt==n)
                            return@setOnEditorActionListener false
                        val file= File(parent.context.applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator+n)
                        val newFile:File = File(parent.context.applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator+txt)
                        if(newFile.exists())
                        {
                            v.text=n.subSequence(0,n.length-4)
                            val error= MaterialAlertDialogBuilder(parent.context)
                            error.setTitle(parent.context.getString(R.string.DialogErrorTitle))
                            error.setMessage(parent.context.getString(R.string.errorAlreadyPresent))
                            error.setPositiveButton(parent.context.getString(R.string.labelOk),null)
                            error.show()
                            false
                        }
                        else
                        {
                            file.renameTo(newFile)
                            v.clearFocus()
                            record.setTitle(txt)
                            val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(v.windowToken, 0)
                            true
                        }
                    }
                    else -> false
                }
            }

            buDelete.setOnClickListener {
                rA.removeRecord(record)
            }


        }

        fun bind(r:Record,position:Int)
        {
            txtTitle.text=r.getTitle().subSequence(0,r.getTitle().length-4)
            pos=position
            record=r
        }
    }
}
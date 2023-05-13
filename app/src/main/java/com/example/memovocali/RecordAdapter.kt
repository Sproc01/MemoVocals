package com.example.memovocali

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
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

    fun removeRecord(position: Int, file:File){
        file.delete()
        Records.removeAt(position)
        notifyItemRemoved(position)
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
        private val buPlay:Button=itemView.findViewById(R.id.buttonPlay)
        private val buStopPlay:Button=itemView.findViewById(R.id.buttonStopPlay)
        private val txtName:TextView=itemView.findViewById(R.id.txtTitle)
        private val seekb:SeekBar =itemView.findViewById(R.id.seekBar)
        private val buOpen:Button=itemView.findViewById(R.id.buttonOpen)
        var timer:Time?=null

        init{
            seekb.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    pausePlay()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    seekPlay(seekBar.progress)
                    timer?.cancel()
                    timer=Time(rA.Records[getPos()].getDuration()-(seekb.progress), this@ViewHolderRecord)
                    timer?.start()

                }
            })

            buOpen.setOnClickListener{
                val intent= Intent(parent.context,DetailActivity::class.java)
                intent.putExtra("recordName", rA.Records[getPos()].getTitle())
                intent.putExtra("recordPath", rA.Records[getPos()].getPath())
                intent.putExtra("recordDuration", rA.Records[getPos()].getDuration())
                parent.context.startActivity(intent)
            }

            txtName.setOnEditorActionListener { v, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        if(txtName.text.toString()=="")
                            return@setOnEditorActionListener false
                        val n=rA.Records[getPos()].getTitle()
                        if(txtName.text.toString()==n)
                            return@setOnEditorActionListener false
                        val file= File(parent.context.applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator+n)
                        val newFile:File = if(txtName.text.toString().contains(".aac"))
                            File(parent.context.applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator+v.text.toString())
                        else
                            File(parent.context.applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator+v.text.toString()+".aac")
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
                            if(txtName.text.toString().contains(".aac"))
                                txtName.text=txtName.text.toString().replace(".aac","")
                            v.clearFocus()
                            if(txtName.text.toString().contains(".aac"))
                                rA.Records[getPos()].setTitle(v.text.toString())
                            else
                                rA.Records[getPos()].setTitle(v.text.toString()+".aac")
                            val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(v.windowToken, 0)
                            true
                        }
                    }
                    else -> false
                }
            }

            buDelete.setOnClickListener {
                rA.removeRecord(getPos(),File(parent.context.applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator+rA.Records[getPos()].getTitle()))
            }

            buPlay.setOnClickListener{
                val file= File(parent.context.applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator+rA.Records[getPos()].getTitle())
                if(startPlay(file.absolutePath)==0)
                {
                    seekb.progress=0
                    seekb.max=rA.Records[getPos()].getDuration()
                    timer=Time(rA.Records[getPos()].getDuration(), this@ViewHolderRecord)
                    timer?.start()
                    seekb.visibility=SeekBar.VISIBLE
                    buStopPlay.visibility=Button.VISIBLE
                    buPlay.visibility=Button.INVISIBLE
                    buDelete.visibility=Button.INVISIBLE
                    buOpen.visibility=Button.INVISIBLE
                }
            }

            buStopPlay.setOnClickListener{
                stopPlay()
                timer?.cancel()
                seekb.visibility=SeekBar.INVISIBLE
                buStopPlay.visibility=Button.INVISIBLE
                buPlay.visibility=Button.VISIBLE
                buDelete.visibility=Button.VISIBLE
                buOpen.visibility=Button.VISIBLE
            }

            seekb.visibility=View.INVISIBLE
            buStopPlay.visibility=View.INVISIBLE
        }

        fun getPos():Int{
            return pos
        }

        fun bind(record:Record,position:Int)
        {
            txtTitle.text=record.getTitle().subSequence(0,record.getTitle().length-4)
            pos=position
        }

        class Time(x:Int, private val vA: ViewHolderRecord):CountDownTimer(x.toLong(),100)
        {
            override fun onTick(millisUntilFinished: Long) {
                vA.seekb.progress = vA.seekb.progress.plus(100)
            }

            override fun onFinish(){
                vA.buStopPlay.callOnClick()
            }
        }
    }
}
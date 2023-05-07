package com.example.memovocali

import android.content.Intent
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class RecordAdapter(private val Records:MutableList<Record> =mutableListOf()): RecyclerView.Adapter<RecordAdapter.ViewHolderRecord>()
{
    override fun getItemCount():Int {
        return Records.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderRecord {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_item, parent, false)
        val h=ViewHolderRecord(view)
        val buDelete:Button=view.findViewById(R.id.buttonDelete)
        val buPlay:Button=view.findViewById(R.id.buttonPlay)
        val buStopPlay:Button=view.findViewById(R.id.buttonStopPlay)
        val txtName:TextView=view.findViewById(R.id.txtTitle)
        var timer:CountDownTimer?=null

        buStopPlay.visibility=View.INVISIBLE

        txtName.setOnClickListener{
            val intent= Intent(parent.context,DetailActivity::class.java)
            intent.putExtra("recordName", Records[h.getPos()].getTitle())
            intent.putExtra("recordPath", Records[h.getPos()].getPath())
            intent.putExtra("recordDuration", Records[h.getPos()].getDuration())
            parent.context.startActivity(intent)
        }

        buDelete.setOnClickListener {
            removeRecord(h.getPos(),File(parent.context.applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator+h.getTxt()) )
        }

        buPlay.setOnClickListener{
            val file= File(parent.context.applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator+h.getTxt())
            if(startPlay(file.absolutePath)==0)
            {
                buStopPlay.visibility=View.VISIBLE
                buPlay.visibility=View.INVISIBLE
                buDelete.visibility=View.INVISIBLE

                timer=object:CountDownTimer(Records[h.getPos()].getDuration().toLong(),1000){
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        buStopPlay.callOnClick()
                    }
                }
                timer?.start()
            }
        }

        buStopPlay.setOnClickListener{
            stopPlay()
            timer?.cancel()
            buStopPlay.visibility=View.INVISIBLE
            buPlay.visibility=View.VISIBLE
            buDelete.visibility=View.VISIBLE
        }
        return h
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


    class ViewHolderRecord(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val txtTitle:TextView=itemView.findViewById(R.id.txtTitle)
        private var pos:Int=0

        fun getTxt():String{
            return txtTitle.text.toString()
        }

        fun getPos():Int{
            return pos
        }

        fun bind(record:Record,position:Int)
        {
            txtTitle.text=record.getTitle()
            pos=position
        }
    }
}
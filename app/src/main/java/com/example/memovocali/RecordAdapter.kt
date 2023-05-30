package com.example.memovocali

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
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
        private val txtName:TextView=itemView.findViewById(R.id.txtTitle)
        private val buOpen:Button=itemView.findViewById(R.id.buttonOpen)
        private lateinit var record:Record

        init{

            itemView.setOnLongClickListener{
                buOpen.callOnClick()
            }

            txtName.setOnLongClickListener(){
                buOpen.callOnClick()
            }

            buOpen.setOnClickListener{
                val intent= Intent(parent.context,DetailActivity::class.java)
                intent.putExtra("recordName", record.getTitle())
                intent.putExtra("recordPath", record.getPath())
                parent.context.startActivity(intent)
            }

            txtName.setOnFocusChangeListener { v, hasFocus ->
                if(!hasFocus)
                    changeName()
            }

            txtName.setOnEditorActionListener { _, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        txtName.clearFocus()
                        val imm = txtName.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(txtName.windowToken, 0)
                        true
                    }
                    else -> false
                    }

                }
            buDelete.setOnClickListener {
                rA.removeRecord(record)
            }


        }
        private fun changeName():Boolean{
            var txt=txtName.text.toString()
            for(i in txt)
                if(!(i in 'A'..'Z' || i in 'a'..'z' || i in '0'..'9' || i==' '))
                {
                    txtName.error=parent.context.getString(R.string.errorInvalidName)
                    return false
                }
            if(txt=="")
                return false
            val n=record.getTitle()
            if(txtName.text.toString().contains(".aac"))
                txtName.text=txtName.text.toString().replace(".aac","")
            if(!(txt.contains(".aac")))
                txt+=".aac"
            if(txt==n)
                return false
            val file= File(parent.context.applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator+n)
            val newFile:File = File(parent.context.applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator+txt)
            if(newFile.exists())
            {
                txtName.text=n.subSequence(0,n.length-4)
                val error= MaterialAlertDialogBuilder(parent.context)
                error.setTitle(parent.context.getString(R.string.DialogErrorTitle))
                error.setMessage(parent.context.getString(R.string.errorAlreadyPresent))
                error.setPositiveButton(parent.context.getString(R.string.labelOk),null)
                error.show()
                return false
            }
            else
            {
                file.renameTo(newFile)
                record.setTitle(txt)
                return true
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
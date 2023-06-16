package com.example.memovocali

import android.annotation.SuppressLint
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

/**
 * class that represent the adapter for the recyclerview
 * @param Records list of records
 */
class RecordAdapter(private val Records:MutableList<Record> =mutableListOf()): RecyclerView.Adapter<RecordAdapter.ViewHolderRecord>() {
    override fun getItemCount():Int {
        return Records.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderRecord {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_item, parent, false)
        return ViewHolderRecord(view, this, parent)
    }

    /**
     * function that removes a record from the list, from the directory and from the recyclerview
     * @param record record to remove
     * @see Record
     */
    fun removeRecord(record: Record){
        val file= File(record.getPath()+record.getTitle())
        val position=Records.indexOf(record)
        Records.remove(record)
        notifyItemRemoved(position)
        file.delete()
    }

    /**
     * function that adds a record to the list and to the recyclerview
     * @param record record to add
     * @see Record
     */
    fun addRecord(record:Record){
        Records.add(record)
        notifyItemInserted(Records.size-1)
    }

    override fun onBindViewHolder(holder: ViewHolderRecord, position: Int) {
        holder.bind(Records[position])
    }

    /**
     * class that represent the viewholder for the recyclerview
     */
    class ViewHolderRecord(itemView: View, private val rA:RecordAdapter, private val parent:ViewGroup): RecyclerView.ViewHolder(itemView) {

        private val txtTitle:TextView=itemView.findViewById(R.id.txtTitle)
        private val buDelete:Button=itemView.findViewById(R.id.buttonDelete)
        private val txtName:TextView=itemView.findViewById(R.id.txtTitle)
        private val buOpen:Button=itemView.findViewById(R.id.buttonOpen)
        private lateinit var record:Record

        init{
            /**
             * function called when the user long press the item
             */
            itemView.setOnLongClickListener{
                buOpen.callOnClick()
            }

            /**
             * function called when the user long press the title of the item
             */
            txtName.setOnLongClickListener{
                buOpen.callOnClick()
            }

            /**
             * function called when the user click on the open button
             */
            buOpen.setOnClickListener{
                val intent= Intent(parent.context,DetailActivity::class.java)
                intent.putExtra("recordName", record.getTitle())
                intent.putExtra("recordPath", record.getPath())
                parent.context.startActivity(intent)
            }

            /**
             * function called when the focus change on the title of the item
             */
            txtName.setOnFocusChangeListener { _, hasFocus ->
                if(!hasFocus)
                    changeName()
            }

            /**
             * function called when the user press the done button on the keyboard
             */
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

            /**
             * function called when the user click on the delete button
             */
            buDelete.setOnClickListener {
                val confirm= MaterialAlertDialogBuilder(parent.context)
                confirm.setTitle(parent.context.getString(R.string.deleteTitle))
                confirm.setMessage(parent.context.getString(R.string.deletePressed))
                confirm.setPositiveButton(parent.context.getString(R.string.Ok)) { _, _ ->
                    rA.removeRecord(record)
                }
                confirm.setNegativeButton(parent.context.getString(R.string.Cancel), null)
                confirm.show()
            }


        }

        /**
         * function that change the name of the record
         */
        @SuppressLint("NotifyDataSetChanged") //I use it because i reorder the list so the recycler view must be updated
        private fun changeName(){
            var txt=txtName.text.toString()
            for(i in txt)//only certain characters are allowed
                if(!(i in 'A'..'Z' || i in 'a'..'z' || i in '0'..'9' || i==' ')) {
                    txtName.error=parent.context.getString(R.string.errorInvalidName)
                    return
                }
            if(txt=="")//the name can't be empty
            {
                txtName.error=parent.context.getString(R.string.errorEmptyTitle)
                return
            }

            val n=record.getTitle()
            if(!(txt.contains(".aac")))
                txt+=".aac"
            if(txt==n)//if the name is the same of the old one, nothing change
                return

            val file= File(parent.context.applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator+n)
            val newFile = File(parent.context.applicationContext.filesDir.toString()+File.separator+"Memo"+File.separator+txt)

            //if the name is already present in the directory, an error is shown
            if(newFile.exists()) {
                txtName.text=n.subSequence(0,n.length-4)
                val error= MaterialAlertDialogBuilder(parent.context)
                error.setTitle(parent.context.getString(R.string.DialogErrorTitle))
                error.setMessage(parent.context.getString(R.string.errorAlreadyPresent))
                error.setPositiveButton(parent.context.getString(R.string.Ok),null)
                error.show()
                return
            }
            else {
                //rename the file
                file.renameTo(newFile)
                //update the record
                record.setTitle(txt)
                //update the recyclerview
                rA.Records.sortBy { it.getTitle().uppercase() }
                rA.notifyDataSetChanged()
                return
            }
        }

        /**
         * function that fill the viewholder with the data of the record in the specified position
         * @param r record to show
         * @see Record
         */
        fun bind(r:Record) {
            txtTitle.text=r.getTitle().subSequence(0,r.getTitle().length-4)
            record=r
        }
    }
}
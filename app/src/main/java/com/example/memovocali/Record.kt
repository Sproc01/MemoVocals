package com.example.memovocali

/**
 * Class for manage a single record with all the data necessary for the application
 */
class Record(private var title:String, private val filename:String, private var duration:Int) {

    /**
     * @return the title of the record
     */
    fun getTitle():String { return title }

    /**
     * set a new title of the record
     */
    fun setTitle(newTitle:String) { title=newTitle }

    /**
     * @return the path of the record
     */
    fun getPath():String { return filename }

    /**
     * @return the duration of the record
     */
    fun getDuration():Int { return duration }

    fun updateDuration(newDuration:Int) { duration=newDuration }

}
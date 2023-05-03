package com.example.memovocali

/**
 * Class for manage a single record with all the data necessary for the application
 */
class Record(private val title:String, private val filename:String, private val duration:Int) {

    /**
     * @return the title of the record
     */
    fun getTitle():String { return title }

    /**
     * @return the path of the record
     */
    fun getPath():String { return filename }

    /**
     * @return the duration of the record
     */
    fun getDuration():Int { return duration }

}
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

    /**
     * set a new duration of the record
     */
    fun updateDuration(newDuration:Int) { duration=newDuration }

    /**
     * @return true if two record are equals, false otherwise
     */
    override fun equals(other: Any?): Boolean {
        if(other is Record) {
            return title == other.title && filename == other.filename && duration == other.duration
        }
        return false
    }

    /**
     * @return the hashcode of the record
     */
    override fun hashCode(): Int {
        return title.hashCode()+filename.hashCode()+duration
    }

}
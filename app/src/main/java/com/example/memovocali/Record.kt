package com.example.memovocali

/**
 * Class for manage a single record with all the data necessary for the application
 */
class Record(private var title:String, private val path:String) {
    /**
     * @return the title of the record
     */
    fun getTitle():String { return title }

    /**
     * set a new title of the record
     * @param newTitle new title of the record
     */
    fun setTitle(newTitle:String) { title=newTitle }

    /**
     * @return the path of the record
     */
    fun getPath():String { return path }

    /**
     * @return true if two record are equals, false otherwise
     * @param other record to compare
     */
    override fun equals(other: Any?): Boolean {
        if(other is Record) {
            return title == other.title && path == other.path
        }
        return false
    }

    /**
     * @return the hashcode of the record
     */
    override fun hashCode(): Int {
        return title.hashCode()+path.hashCode()
    }

}
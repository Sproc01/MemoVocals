package com.example.memovocali

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Build
import android.os.StatFs


private var Recorder:MediaRecorder?=null
private var path:String?=null
private var title:String?=null

/**
 * Function for start the record
 */
fun startRecord(p:String, name:String, context: Context):Int{
    // Check if there is a record or a playback in progress
    if(Recorder!=null)
        return -1
    // Check if there is enough space
    val stat = StatFs(p)
    val megAvailable = stat.availableBytes/1048576
    if(megAvailable<10)
        return -1
    //construct and start of the record
    Recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    }
    else
        MediaRecorder()
    Recorder?.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
    Recorder?.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
    Recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
    Recorder?.setMaxDuration(30000)
    title=name
    path=p
    Recorder?.setOutputFile(path+ title)
    Recorder?.prepare()
    Recorder?.start()
    return 0
}

/**
 * Function for stop the record
 */
fun stopRecord():Record?{
    Recorder?.stop()
    Recorder?.release()
    val data= MediaMetadataRetriever()
    var r:Record?=null
    if(path!=null || title!=null)
    {
        data.setDataSource(path+title)
        r=Record(title!!,path!!,data.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt())
    }
    Recorder=null
    path=null
    title=null
    return r
}

/**
 * Function to get the amplitude of the registration
 */
fun amplitude():Int{
    return Recorder?.maxAmplitude?:0
}
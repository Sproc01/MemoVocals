package com.example.memovocali

import android.content.Context
import android.media.MediaRecorder
import android.os.Build

private var recorder: MediaRecorder?=null
private var path:String=""
private var title:String=""

/**
 * Function for start the record
 */
@Suppress("DEPRECATION") //suppress all the deprecation because the app support older version that request the deprecated methods
fun startRecord(p:String, t:String, applicationContext: Context){
    //construct and start of the record
    recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        MediaRecorder(applicationContext)
    else
        MediaRecorder()
    path =p
    title =t
    recorder?.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
    recorder?.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
    recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
    recorder?.setMaxDuration(30000)
    recorder?.setOutputFile(path + title)
    recorder?.prepare()
    recorder?.start()
}

/**
 * Function for stop the record
 */
fun stopRecord(){
    recorder?.stop()
    recorder?.release()
    recorder =null
}

/**
 * Function to get the max amplitude from the previous call, if it is the first call it will return 0
 */
fun amplitude():Int{
    return recorder?.maxAmplitude?:0
}
package com.example.memovocali

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.CountDownTimer
import java.io.File

private var Recorder:MediaRecorder?=null
private var p:String?=null
private var title:String?=null
private var player:MediaPlayer?=null

/**
 * Exception for file already exists in the directory
 */
class FileExistException(message:String):Exception(message)

/**
 * Function for start the record
 */
fun startRecord(path:String, name:String){
    Recorder = MediaRecorder()
    Recorder?.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
    Recorder?.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
    Recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
    Recorder?.setMaxDuration(30000)
    title=name
    p=path
    val file= File(p,title)
    if(!file.exists()) {
        Recorder?.setOutputFile(file.absolutePath)
        Recorder?.prepare()
        Recorder?.start()
    }
    else throw FileExistException("File already exists")
}

/**
 * Function for stop the record
 */
fun stopRecord():Record{
    Recorder?.stop()
    Recorder?.release()
    val data=MediaMetadataRetriever()
    data.setDataSource(p+title)
    val r=Record(title!!,p!!,
        data.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0)
    Recorder=null
    return r
}

/**
 * Function for pause the record
 */
fun pauseRecord(){
    Recorder?.pause()
}

/**
 * Function for resume the record
 */
fun resumeRecord(){
    Recorder?.resume()
}

/**
 * Function for start the play
 */
fun startPlay(path:String){
    player= MediaPlayer()
    player?.setDataSource(path)
    player?.prepare()
    player?.start()

    /*player?.setOnCompletionListener {
        stopPlay()
    }*/
}

/**
 * Function for stop the play
 */
fun stopPlay(){
    player?.stop()
    player?.release()
    player=null
}


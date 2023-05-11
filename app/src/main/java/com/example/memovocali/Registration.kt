package com.example.memovocali

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.CountDownTimer
import java.io.File

private var Recorder:MediaRecorder?=null
private var path:String?=null
private var title:String?=null
private var player:MediaPlayer?=null

/**
 * Function for start the record
 */
fun startRecord(p:String, name:String):Int{
    if(Recorder!=null || player!=null)
        return -1
    Recorder = MediaRecorder()
    Recorder?.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
    Recorder?.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
    Recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
    Recorder?.setMaxDuration(30000)
    title=name
    path=p
    //val file= File(path,name)
    Recorder?.setOutputFile(path+ title)
    Recorder?.prepare()
    Recorder?.start()
    return 0
}

/**
 * Function for stop the record
 */
fun stopRecord():Record{
    Recorder?.stop()
    Recorder?.release()
    val data=MediaMetadataRetriever()
    data.setDataSource(path+title)
    val r=Record(title!!,path!!,
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
fun startPlay(path:String):Int{
    if(player!=null || Recorder!=null)
        return -1
    player= MediaPlayer()
    player?.setDataSource(path)
    player?.prepare()
    player?.start()
    return 0
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

fun pausePlay(){
    player?.pause()
}

fun resumePlay(){
    player?.start()
}

fun seekPlay(sec:Int){
    player?.pause()
    player?.seekTo(sec*1000)
    player?.start()
}


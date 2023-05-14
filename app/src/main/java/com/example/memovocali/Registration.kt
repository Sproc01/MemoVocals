package com.example.memovocali

import android.app.Application
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build


private var Recorder:MediaRecorder?=null
private var path:String?=null
private var title:String?=null
private var player:MediaPlayer?=null

/**
 * Function for start the record
 */
fun startRecord(p:String, name:String, context: Context):Int{
    if(Recorder!=null || player!=null)
        return -1
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
    //val file= File(path,name)
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

fun seekPlay(sec:Int){
    player?.pause()
    player?.seekTo(sec)
    player?.setOnSeekCompleteListener {
        player?.start()
    }
}


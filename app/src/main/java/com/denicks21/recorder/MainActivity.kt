package com.denicks21.recorder

import android.Manifest.permission
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    lateinit var startTV: TextView
    lateinit var stopTV: TextView
    lateinit var playTV: TextView
    lateinit var stopplayTV: TextView
    lateinit var statusTV: TextView
    lateinit var resumeTV: TextView
    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private var isrecording=false
    private var isplaying=false
    private var hasOneRecording=false
    private var length: Int = 0
    var mFileName: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTV = findViewById(R.id.idTVstatus)
        startTV = findViewById(R.id.btnRecord)
        stopTV = findViewById(R.id.btnStop)
        playTV = findViewById(R.id.btnPlay)
        stopplayTV = findViewById(R.id.btnStopPlay)
        //resumeTV = findViewById(R.id.btnresume)

        startTV.setOnClickListener {
            if (!isrecording){
                startRecording()
            }
        }

        stopTV.setOnClickListener {
            pauseRecording()
            hasOneRecording=true
        }

        playTV.setOnClickListener {
            if(!isplaying and hasOneRecording){
                playAudio()
                playTV.setBackgroundResource(R.drawable.baseline_pause_24)
            }
            else{
                pause()
                playTV.setBackgroundResource(R.drawable.btn_rec_play)
            }

        }
        /*
        resumeTV.setOnClickListener{
            pausePlaying()
        }

         */
        stopplayTV.setOnClickListener {
            stopplaying()
        }
    }

    private fun startRecording() {

        // Check permissions
        if (CheckPermissions()) {

            // Save file
            mFileName = File(getExternalFilesDir("")?.absolutePath,"Record.3gp")

            // If file exists then increment counter
            var n = 0
            while (mFileName!!.exists()) {
                n++
                mFileName = File(getExternalFilesDir("")?.absolutePath,"Record$n.3gp")
            }

            // Initialize the class MediaRecorder
            mRecorder = MediaRecorder()

            // Set source to get audio
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)

            // Set the format of the file
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

            // Set the audio encoder
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            // Set the save path
            mRecorder!!.setOutputFile(mFileName)
            try {
                // Preparation of the audio file
                mRecorder!!.prepare()
            } catch (e: IOException) {
                Log.e("TAG", "prepare() failed")
            }
            // Start the audio recording
            mRecorder!!.start()
            statusTV.text = "Recording in progress"
            isrecording=true
        } else {
            // Request permissions
            RequestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If permissions accepted ->
        when (requestCode) {
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.size > 0) {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord && permissionToStore) {

                    // Message
                    Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_LONG).show()

                } else {

                    // Message
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun CheckPermissions(): Boolean {

        // Check permissions
        val result =
            ContextCompat.checkSelfPermission(applicationContext, permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun RequestPermissions() {

        // Request permissions
        ActivityCompat.requestPermissions(this,
            arrayOf(permission.RECORD_AUDIO, permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_AUDIO_PERMISSION_CODE)
    }

    fun playAudio() {

        // Use the MediaPlayer class to listen to recorded audio files
        mPlayer = MediaPlayer()
        try {
            // Preleva la fonte del file audio
            mPlayer!!.setDataSource(mFileName.toString())

            // Fetch the source of the mPlayer
            mPlayer!!.prepare()

            // Start the mPlayer
            mPlayer!!.start()
            statusTV.text = "Listening recording"
            isplaying=true
        } catch (e: IOException) {
            Log.e("TAG", "prepare() failed")
        }
    }

    fun pauseRecording() {

        // Stop recording
        if (mFileName == null) {

            // Message
            Toast.makeText(getApplicationContext(), "Registration not started", Toast.LENGTH_LONG).show()

        } else {
            mRecorder!!.stop()

            // Message to confirm save file
            val savedUri = Uri.fromFile(mFileName)
            val msg = "File saved: " + savedUri!!.lastPathSegment
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()

            // Release the class mRecorder
            mRecorder!!.release()
            mRecorder = null
            statusTV.text = "Recording interrupted"
            isrecording=false
        }
    }

    fun pausePlaying(botonpausa:Boolean) {
        Log.i("reproducir",botonpausa.toString())

        if (botonpausa){
            if (isplaying){
                length=mPlayer!!.getCurrentPosition()
                mPlayer!!.pause()
                //length=mPlayer!!.getCurrentPosition()
                isplaying=false
            }
            else{
                //mPlayer!!.seekTo(length)
                mPlayer!!.start()
                mPlayer!!.seekTo(length)

                isplaying=true
            }
        }
        else{
            if(isplaying){
                mPlayer!!.stop()
                mPlayer!!.release()
                mPlayer=null
                statusTV.text = "Listening stopped"
                isplaying =false
            }
        }


    }
    fun pause(){
        if (isplaying){
            length=mPlayer!!.getCurrentPosition()
            mPlayer!!.pause()
            //length=mPlayer!!.getCurrentPosition()
            isplaying=false
        }
        else{
            //mPlayer!!.seekTo(length)
            mPlayer!!.start()
            mPlayer!!.seekTo(length)
            isplaying=true
        }
    }
    fun stopplaying(){
        mPlayer!!.stop()
        mPlayer!!.release()
        mPlayer=null
        statusTV.text = "Listening stopped"
    }

    companion object {
        const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }
}
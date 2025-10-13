package com.krishna.soslocation

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RecorderActivity : AppCompatActivity() {

    private lateinit var recordButton: Button
    private lateinit var stopButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var timerTextView: TextView

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var recordingTimeSeconds = 0
    private var timerHandler: Handler? = null
    private var timerRunnable: Runnable? = null
    private var currentAudioFile: File? = null

    companion object {
        private const val AUDIO_PERMISSION_CODE = 200
        private var isRecordingActive = false
        private var globalRecordingFile: File? = null

        fun isRecordingActive(): Boolean {
            return isRecordingActive
        }

        fun startRecordingFromSOS(context: Context) {
            val intent = Intent(context, RecorderActivity::class.java).apply {
                putExtra("AUTO_START", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }

        fun stopGlobalRecording() {
            isRecordingActive = false
            globalRecordingFile = null
        }

        fun getCurrentRecordingFile(): File? {
            return globalRecordingFile
        }
    }

    private val sosStopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "STOP_RECORDING") {
                stopRecording()
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorder)

        initializeViews()
        setupClickListeners()

        // Register broadcast receiver for SOS stop events
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(sosStopReceiver, IntentFilter("STOP_RECORDING"))

        // Check if auto-start from SOS
        if (intent.getBooleanExtra("AUTO_START", false)) {
            if (checkAudioPermission()) {
                startRecording()
            } else {
                requestAudioPermission()
            }
        }
    }

    private fun initializeViews() {
        recordButton = findViewById(R.id.recordButton)
        stopButton = findViewById(R.id.stopRecordButton)
        statusTextView = findViewById(R.id.recordingStatusText)
        timerTextView = findViewById(R.id.timerText)

        updateUI()
    }

    private fun setupClickListeners() {
        recordButton.setOnClickListener {
            if (!isRecording) {
                if (checkAudioPermission()) {
                    startRecording()
                } else {
                    requestAudioPermission()
                }
            }
        }

        stopButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            }
        }
    }

    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            AUDIO_PERMISSION_CODE
        )
    }

    private fun startRecording() {
        if (isRecording) return

        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)

                // Create unique filename with timestamp
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val audioFileName = "SOS_RECORDING_$timeStamp.m4a"

                currentAudioFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), audioFileName)
                setOutputFile(currentAudioFile?.absolutePath)

                prepare()
                start()
            }

            isRecording = true
            isRecordingActive = true
            globalRecordingFile = currentAudioFile
            recordingTimeSeconds = 0
            startTimer()
            updateUI()

            Toast.makeText(this, "🎤 Recording started", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            Toast.makeText(this, "Failed to start recording: ${e.message}", Toast.LENGTH_SHORT).show()
            cleanupRecorder()
        } catch (e: IllegalStateException) {
            Toast.makeText(this, "Recording not available: ${e.message}", Toast.LENGTH_SHORT).show()
            cleanupRecorder()
        }
    }

    private fun stopRecording() {
        if (!isRecording) return

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cleanupRecorder()
            isRecording = false
            isRecordingActive = false
            stopTimer()
            updateUI()

            Toast.makeText(this, "⏹️ Recording stopped", Toast.LENGTH_SHORT).show()

            // Send recording if it was started from SOS
            if (intent.getBooleanExtra("AUTO_START", false) && currentAudioFile?.exists() == true) {
                sendRecordingToContacts()
            }
        }
    }

    private fun sendRecordingToContacts() {
        val sharedPreferences = getSharedPreferences("SOS_CONTACTS", Context.MODE_PRIVATE)
        val contacts = sharedPreferences.getStringSet("contacts", emptySet()) ?: emptySet()

        if (contacts.isEmpty() || currentAudioFile == null || !currentAudioFile!!.exists()) {
            return
        }

        Toast.makeText(this, "📤 Sending recording to contacts...", Toast.LENGTH_SHORT).show()

        // In a real implementation, you would upload to a server and share link
        // or use MMS (which requires different permissions and carrier support)
        // For now, we'll just notify that recording is ready
        val smsManager = getSystemService(SmsManager::class.java)
        val message = """
            🚨 EMERGENCY - Audio Recording Available
            
            An audio recording was captured during the SOS emergency.
            Please check your messages for the audio file or contact emergency services.
            
            Recording duration: ${formatTime(recordingTimeSeconds)}
        """.trimIndent()

        for (contact in contacts) {
            if (contact.isNotEmpty()) {
                try {
                    smsManager.sendTextMessage(contact, null, message, null, null)
                } catch (e: Exception) {
                    // MMS sending would require different implementation
                }
            }
        }
    }

    private fun startTimer() {
        timerHandler = Handler(Looper.getMainLooper())
        timerRunnable = object : Runnable {
            override fun run() {
                recordingTimeSeconds++
                updateTimerDisplay()
                timerHandler?.postDelayed(this, 1000)
            }
        }
        timerHandler?.post(timerRunnable!!)
    }

    private fun stopTimer() {
        timerHandler?.removeCallbacks(timerRunnable ?: return)
        recordingTimeSeconds = 0
        updateTimerDisplay()
    }

    private fun updateTimerDisplay() {
        timerTextView.text = formatTime(recordingTimeSeconds)
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    private fun updateUI() {
        if (isRecording) {
            recordButton.visibility = Button.GONE
            stopButton.visibility = Button.VISIBLE
            statusTextView.text = "🔴 Recording in progress..."
            statusTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.recording_active))
        } else {
            recordButton.visibility = Button.VISIBLE
            stopButton.visibility = Button.GONE
            statusTextView.text = "Ready to record"
            statusTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.recording_idle))
        }
    }

    private fun cleanupRecorder() {
        mediaRecorder?.release()
        mediaRecorder = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            AUDIO_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (intent.getBooleanExtra("AUTO_START", false)) {
                        startRecording()
                    }
                } else {
                    Toast.makeText(this, "Audio permission required for recording", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sosStopReceiver)
        cleanupRecorder()
    }
}



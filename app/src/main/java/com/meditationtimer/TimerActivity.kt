package com.meditationtimer

import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class TimerActivity : AppCompatActivity() {

    private var timeSelected: Long = 0
    private var timeCountDown: CountDownTimer? = null
    private var timeProgress = 0L
    private var pauseOffset: Long = 0
    private var isStart = true
    private var isDarkMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(if (isDarkMode) R.style.AppTheme_Dark else R.style.AppTheme)
        setContentView(R.layout.timer_activity)

        val doNotDisturbButton: Button = findViewById(R.id.btnDoNotDisturb)
        doNotDisturbButton.setOnClickListener {
            toggleDoNotDisturbMode()
        }

        val addBtn: AppCompatButton = findViewById(R.id.btnAdd)
        addBtn.setOnClickListener {
            setTimeFunction()
        }

        val startBtn: Button = findViewById(R.id.btnPlayPause)
        startBtn.setOnClickListener {
            startTimerSetup()
        }

        val resetBtn: ImageButton = findViewById(R.id.ib_reset)
        resetBtn.setOnClickListener {
            resetTime()
        }

        val modeButton: AppCompatButton = findViewById(R.id.btnMode)
        modeButton.text = if (isDarkMode) "Light Mode" else "Dark Mode"
        modeButton.setOnClickListener {
            isDarkMode = !isDarkMode
            recreate()
        }
    }

    private fun resetTime() {
        if (timeCountDown != null) {
            timeCountDown!!.cancel()
            timeProgress = 0
            timeSelected = 0
            pauseOffset = 0
            timeCountDown = null
            val startBtn: Button = findViewById(R.id.btnPlayPause)
            startBtn.text = "Start"
            isStart = true
            val progressBar = findViewById<ProgressBar>(R.id.pbTimer)
            progressBar.progress = 0
            val timeLeftTv: TextView = findViewById(R.id.tvTimeLeft)
            timeLeftTv.text = "00:00"
            Toast.makeText(this, "Timer set to default", Toast.LENGTH_SHORT).show()
        }
    }

    private fun timePause() {
        if (timeCountDown != null) {
            timeCountDown!!.cancel()
        }
    }

    private fun startTimerSetup() {
        val startBtn: Button = findViewById(R.id.btnPlayPause)
        if (timeSelected > timeProgress) {
            if (isStart) {
                startBtn.text = "Pause"
                startTimer(pauseOffset)
                isStart = false
            } else {
                isStart = true
                startBtn.text = "Resume"
                timePause()
            }
        } else {
            Toast.makeText(this, "Please set timer", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTimer(pauseOffsetL: Long) {
        val progressBar = findViewById<ProgressBar>(R.id.pbTimer)
        progressBar.progress = timeProgress.toInt()
        timeCountDown = object : CountDownTimer(
            (timeSelected * 1000).toLong() - pauseOffsetL * 1000, 1000
        ) {
            override fun onTick(p0: Long) {
                timeProgress++
                pauseOffset = timeSelected - p0 / 1000
                progressBar.progress = (timeSelected - timeProgress).toInt()

                // Calculate minutes and seconds
                val minutes = (timeSelected - timeProgress) / 60
                val seconds = (timeSelected - timeProgress) % 60

                val timeLeftTv: TextView = findViewById(R.id.tvTimeLeft)
                timeLeftTv.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                resetTime()
                Toast.makeText(this@TimerActivity, "Times Up!", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun setTimeFunction() {
        val timeDialog = Dialog(this)
        timeDialog.setContentView(R.layout.add_dialog)
        val etMinutes = timeDialog.findViewById<EditText>(R.id.etMinutes)
        val etSeconds = timeDialog.findViewById<EditText>(R.id.etSeconds)
        val timeLeftTv: TextView = findViewById(R.id.tvTimeLeft)
        val btnStart: Button = findViewById(R.id.btnPlayPause)
        val progressBar = findViewById<ProgressBar>(R.id.pbTimer)
        timeDialog.findViewById<Button>(R.id.btnOk).setOnClickListener {
            val minutesText = etMinutes.text.toString()
            val secondsText = etSeconds.text.toString()

            val minutes = if (minutesText.isNotEmpty()) minutesText.toInt() else 0
            val seconds = if (secondsText.isNotEmpty()) secondsText.toInt() else 0
            val totalTime = minutes * 60 + seconds

            if (totalTime > 0) {
                resetTime()
                timeLeftTv.text = String.format("%02d:%02d", minutes, seconds)
                btnStart.text = "Start"
                timeSelected = totalTime.toLong()
                progressBar.max = totalTime
            } else {
                Toast.makeText(this, "Please enter time duration", Toast.LENGTH_SHORT).show()
            }

            timeDialog.dismiss()
        }
        timeDialog.show()
    }

    private fun toggleDoNotDisturbMode() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager.isNotificationPolicyAccessGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val policy =
                    NotificationManager.Policy(
                        NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS,
                        0,
                        NotificationManager.Policy.SUPPRESSED_EFFECT_AMBIENT,
                    )
                notificationManager.notificationPolicy = policy
            }

            val doNotDisturbButton: Button = findViewById(R.id.btnDoNotDisturb)
            val isDoNotDisturbEnabled = notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE

            if (isDoNotDisturbEnabled) {
                // Do Not Disturb is enabled, so disable it
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                doNotDisturbButton.text = "Do Not Disturb"
            } else {
                // Do Not Disturb is disabled, so enable it
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                doNotDisturbButton.text = "Disable Do Not Disturb"
            }
        } else {
            // Request permission to access notification policy
            val intent = Intent(
                Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            )
            startActivity(intent)
        }
    }

}
package com.example.mad_23012531065_practical4

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var cardCreateAlarmLayout: MaterialCardView // Layout for creating alarm
    private lateinit var cardActiveAlarmLayout: MaterialCardView // Layout for showing active alarm
    private lateinit var btnCreateAlarm: MaterialButton
    private lateinit var btnCancelAlarm: MaterialButton
    private lateinit var textViewAlarmTime: TextView // TextView in cardActiveAlarmLayout to show the set time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cardCreateAlarmLayout = findViewById(R.id.alram_create)
        cardActiveAlarmLayout = findViewById(R.id.alarm_main)
        btnCreateAlarm = findViewById(R.id.create_alarm)
        btnCancelAlarm = findViewById(R.id.cancel_alarm)
        textViewAlarmTime = findViewById(R.id.alarm_time)

        // Initial visibility (alarm_main is set to gone in XML)
        cardCreateAlarmLayout.visibility = View.VISIBLE
        cardActiveAlarmLayout.visibility = View.GONE

        btnCreateAlarm.setOnClickListener {
            showTimerDialog()
        }

        btnCancelAlarm.setOnClickListener {
            cardActiveAlarmLayout.visibility = View.GONE
            cardCreateAlarmLayout.visibility = View.VISIBLE 
            setAlarm(0L, "STOP")
        }
    }

    private fun showTimerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute -> sendDialogDataToActivity(selectedHour, selectedMinute) },
            hour,
            minutes,
            false
        ).show()
    }

    private fun sendDialogDataToActivity(hour: Int, minute: Int) {
        val alarmCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (alarmCalendar.timeInMillis <= System.currentTimeMillis()) {
            alarmCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        textViewAlarmTime.text = "Alarm At : " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(alarmCalendar.time)

        cardActiveAlarmLayout.visibility = View.VISIBLE

        setAlarm(alarmCalendar.timeInMillis, "START")
    }

    private fun setAlarm(millisTime: Long, action: String) {
        val intent = Intent(this, AlarmBroadcastReciever::class.java).apply {
            putExtra("Service1", action)
            putExtra("ALARM_TIME_MILLIS", millisTime) // Pass time for service if needed
        }

        val requestCode = 12345
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if ("START".equals(action, ignoreCase = true)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millisTime, pendingIntent)
            Toast.makeText(this, "Alarm set for " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(millisTime), Toast.LENGTH_LONG).show()
        } else if ("STOP".equals(action, ignoreCase = true)) {
            alarmManager.cancel(pendingIntent)
            val stopServiceIntent = Intent(this, AlarmService::class.java).apply {
                putExtra("Service1", "STOP_SOUND")
            }
            startService(stopServiceIntent)
            Toast.makeText(this, "Alarm cancelled.", Toast.LENGTH_SHORT).show()
        }
    }
}

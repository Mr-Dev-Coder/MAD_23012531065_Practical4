package com.example.mad_23012531065_practical4

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmBroadcastReciever : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, AlarmService::class.java)
        // Pass along any extras from the original intent if needed by the service
        if (intent.extras != null) {
            serviceIntent.putExtras(intent.extras!!)
        }
        context.startService(serviceIntent)
    }
}
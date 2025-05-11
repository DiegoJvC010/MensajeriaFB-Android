package com.example.mensajeriafb

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(msg: RemoteMessage) {
        // 1) Lee siempre de msg.data (data-only)
        Log.d("FCM_SERVICE", "data payload keys=${msg.data.keys} values=${msg.data}")
        val title = msg.data["title"] ?: "Sin título"
        val body  = msg.data["body"]  ?: "Sin contenido"

        // 2) Persiste inmediatamente en SharedPreferences
        getSharedPreferences("fcm_prefs", MODE_PRIVATE)
            .edit()
            .putString("last_title", title)
            .putString("last_body",  body)
            .apply()

        // 2) Broadcast en caliente
        sendBroadcast(Intent("FCM_MESSAGE").apply {
            setPackage(packageName); putExtra("title", title); putExtra("body", body)
        })

        // 3) Notificación
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val pending = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java).putExtras(Bundle().apply {
                putString("title", title); putString("body", body)
            }),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            nm.createNotificationChannel(NotificationChannel("default", "FCM", IMPORTANCE_HIGH))

        nm.notify(0, NotificationCompat.Builder(this, "default")
            .setContentTitle(title).setContentText(body)
            .setSmallIcon(R.drawable.nnoti_foreground)
            .setContentIntent(pending).setAutoCancel(true).build()
        )
    }
}
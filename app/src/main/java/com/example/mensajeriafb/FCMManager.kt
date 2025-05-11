package com.example.mensajeriafb

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.MutableState
import androidx.core.content.ContextCompat

class FCMManager(
    private val activity: Activity,
    private val tokenState: MutableState<String>,
    private val messageState: MutableState<Pair<String,String>?>,
) {
    private val prefs by lazy {
        activity.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent) {
            intent.extras?.takeIf { it.containsKey("title") }?.let {
                messageState.value = it.getString("title")!! to it.getString("body","")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun start() = activity.apply {
        // 1) Carga y registra en un solo bloque
        prefs.getString("last_title", null)?.let { t ->
            messageState.value = t to prefs.getString("last_body","")!!
        }
        registerReceiver(receiver, IntentFilter("FCM_MESSAGE"), Context.RECEIVER_NOT_EXPORTED)

        // 2) Obtén token
        Firebase.messaging.token
            .addOnSuccessListener { tokenState.value = it }
            .addOnFailureListener { tokenState.value = "Error al obtener token" }
    }

    fun stop() = activity.unregisterReceiver(receiver)

    fun handleLaunchIntent(intent: Intent) = intent.extras?.takeIf { it.containsKey("title") }?.let {
        messageState.value = it.getString("title")!! to it.getString("body","")
        intent.removeExtra("title"); intent.removeExtra("body")
    }
}
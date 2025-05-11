package com.example.mensajeriafb

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("fcm_prefs", MODE_PRIVATE) }
    private val tokenState   = mutableStateOf("Obteniendo token…")
    private val messageState = mutableStateOf<Pair<String,String>?>(null)
    private val fcmManager by lazy { FCMManager(this, tokenState, messageState) }
    private val notifPerm = registerForActivityResult(ActivityResultContracts.RequestPermission()){}

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        // Permisos
        if (Build.VERSION.SDK_INT>=TIRAMISU &&
            ContextCompat.checkSelfPermission(this,POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED)
            notifPerm.launch(POST_NOTIFICATIONS)

        // Config FCM
        fcmManager.start()
        fcmManager.handleLaunchIntent(intent)

        // UI
        setContent { MensajeriaApp(tokenState.value, messageState.value) }
    }

    override fun onNewIntent(i: Intent) {
        super.onNewIntent(i); fcmManager.handleLaunchIntent(i)
    }
    override fun onDestroy() { super.onDestroy(); fcmManager.stop() }
}
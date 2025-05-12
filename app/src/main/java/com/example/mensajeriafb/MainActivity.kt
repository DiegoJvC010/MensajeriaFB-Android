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
    //obtener objeto de preferencias para guardar datos simples
    private val prefs by lazy { getSharedPreferences("fcm_prefs", MODE_PRIVATE) }
    //estado para mostrar token de FCM en la interfaz
    private val tokenState   = mutableStateOf("Obteniendo token...")
    //estado para mostrar el ultimo mensaje recibido (titulo, cuerpo)
    private val messageState = mutableStateOf<Pair<String,String>?>(null)
    //instancia del gestor de FCM que registra token y mensajes
    private val fcmManager by lazy { FCMManager(this, tokenState, messageState) }
    //lanzador para pedir permiso de notificacion en Android 13+
    private val notifPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)

        // si version >= 13 y falta permiso de notificacion
        if (Build.VERSION.SDK_INT >= TIRAMISU &&
            ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // solicitar permiso al usuario
            notifPerm.launch(POST_NOTIFICATIONS)
        }

        //iniciar proceso de FCM: obtencion de token y registro de listener
        fcmManager.start()
        //revisar si la app se inicio desde una notificacion
        fcmManager.handleLaunchIntent(intent)

        //configurar la UI con Jetpack Compose
        setContent { MensajeriaApp(
                tokenState.value,      // mostrar token actual
                messageState.value   // mostrar mensaje actual
            )
        }
    }

    //se llama cuando llega una nueva Intent y la actividad ya esta abierta
    //--una Intent es un objeto que lleva datos (extras) para indicar
    //--por que se abrio o que debe procesar la actividad
    override fun onNewIntent(i: Intent) {
        super.onNewIntent(i)
        //aqui 'i' es la Intent enviada por el sistema al tocar
        //la notificacion, contiene datos como titulo y cuerpo
        fcmManager.handleLaunchIntent(i)
    }

    //cuando la actividad se destruye
    override fun onDestroy() {
        super.onDestroy()
        //detener el gestor de FCM para quitar listeners y
        //evitar fugas de memoria cuando la pantalla ya no existe
        fcmManager.stop()
    }
}
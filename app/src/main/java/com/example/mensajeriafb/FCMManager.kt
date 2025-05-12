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

//FCMManager gestiona la integracion con Firebase Cloud Messaging
//se ocupa de obtener el token, escuchar mensajes mientras la app esta activa
//y procesar la apertura de la app desde una notificacion
class FCMManager(
    private val activity: Activity,
    private val tokenState: MutableState<String>,              //estado para mostrar el token en la UI
    private val messageState: MutableState<Pair<String,String>?> //estado para mostrar el ultimo mensaje (titulo, cuerpo)
) {
    //SharedPreferences para guardar y recuperar el ultimo mensaje entre sesiones
    private val prefs by lazy {
        activity.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
    }

    //receptor interno que escucha los broadcasts con accion "FCM_MESSAGE"
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent) {
            //si la intent trae extras con la clave 'title', significa que entro un mensaje push
            intent.extras?.takeIf { it.containsKey("title") }?.let {
                messageState.value = //actualizar la UI con el mensaje
                    it.getString("title")!! to // titulo del mensaje
                            it.getString("body","") // cuerpo del mensaje
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    //inicia el gestor de FCM:
    //1) Carga el ultimo mensaje guardado
    //2) Registra el receptor para recibir mensajes mientras la app esta activa
    //3) Solicita el token a Firebase y lo muestra o indica error
    fun start() = activity.apply {
        //1) ver si hay mensaje previo guardado y mostrarlo
        prefs.getString("last_title", null)?.let { savedTitle ->
            val savedBody = prefs.getString("last_body", "")!!
            messageState.value = savedTitle to savedBody
        }

        //2) registrar el receptor para FCM_MESSAGE
        registerReceiver(
            receiver,
            IntentFilter("FCM_MESSAGE"),
            Context.RECEIVER_NOT_EXPORTED
        )

        //3) pedir token de FCM y actualizar el estado correspondiente
        Firebase.messaging.token
            .addOnSuccessListener { token ->
                tokenState.value = token             //mostrar token obtenido
            }
            .addOnFailureListener {
                tokenState.value = "Error al obtener token"  //indicar fallo
            }
    }

    //detiene el receptor para evitar fugas de memoria cuando la actividad se cierra
    fun stop() = activity.unregisterReceiver(receiver)

    //procesa la intent si la app se abre desde una notificacion en la barra
    fun handleLaunchIntent(intent: Intent) = intent.extras?.takeIf { it.containsKey("title") }?.let {
        messageState.value = //actualizar UI al abrir
            it.getString("title")!! to //titulo de la notificacion
                    it.getString("body", "")  //cuerpo de la notificacion
        //Quitar extras para no procesarlos de nuevo
        intent.removeExtra("title"); intent.removeExtra("body")
    }
}
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

//MyFirebaseMessagingService gestiona los mensajes entrantes de Firebase Cloud Messaging
//Guarda datos en preferencias, envia broadcast interno y muestra la notificacion al usuario
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(msg: RemoteMessage) {
        //obtener siempre la parte "data" del mensaje
        Log.d("FCM_SERVICE", "data payload keys=${msg.data.keys} values=${msg.data}")
        val title = msg.data["title"] ?: "Sin titulo"   //titulo del mensaje o valor por defecto
        val body  = msg.data["body"]  ?: "Sin contenido" //cuerpo del mensaje o valor por defecto

        //Guarda inmediatamente el ultimo mensaje en SharedPreferences
        //Esto asegura que el titulo y cuerpo se conserven
        //para mostrarlos la proxima vez que se abra la app
        getSharedPreferences("fcm_prefs", MODE_PRIVATE)
            .edit()
            .putString("last_title", title)
            .putString("last_body", body)
            .apply()

        //Envia un broadcast interno para actualizar la UI si la app esta abierta
        //El receptor interno escucha esta accion y refresca la pantalla con el mensaje
        sendBroadcast(Intent("FCM_MESSAGE").apply {
            setPackage(packageName)
            putExtra("title", title)
            putExtra("body", body)
        })

        //Mostrar la notificacion estandar en la barra de estado
        //Esto crea un aviso fuera de la app para el usuario
        showNotification(title, body)
    }

    //Metodo que prepara y envia la notificacion al sistema
    private fun showNotification(title: String, body: String) {
        //Intent para abrir MainActivity al tocar la notificacion
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("title", title)
            putExtra("body",  body)
        }

        //PendingIntent que encapsula el Intent anterior
        //contiene contexto, codigo (0),el Intent y las flags para actualizar e inmutabilidad
        val pending = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //Instancia de NotificationManager para enviar notificaciones
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Canal de notificacion 'default' necesario en Android Oreo (8.0, API 26) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default",           //identificador interno del canal
                "Notificaciones FCM",//nombre visible en ajustes de sistema
                IMPORTANCE_HIGH       //nivel de alerta alto para sonido y banners
            )
            nm.createNotificationChannel(channel)
        }

        //Construccion de la notificacion con titulo, texto, icono y accion registrada
        val notification = NotificationCompat.Builder(this, "default")
            .setContentTitle(title)                //texto principal de la notificacion
            .setContentText(body)                  //descripcion o cuerpo de la notificacion
            .setSmallIcon(R.drawable.nnoti_foreground) //icono que aparece en la barra de estado
            .setContentIntent(pending)             //PendingIntent al tocar la notificacion
            .setAutoCancel(true)                   //cierre automatico al interactuar
            .build()

        val uniqueId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        nm.notify(uniqueId, notification)
    }
}
package com.example.mensajeriafb

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mensajeriafb.ui.theme.MensajeriaFBTheme

@Composable
fun MensajeriaApp(token: String, lastMsg: Pair<String, String>?) {
    //se crea estado de scroll para cuando el contenido sea mas largo que la pantalla
    val scrollState = rememberScrollState()

    //aplica el tema de la app
    MensajeriaFBTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                Color(0xFF0A1A2A)
                            )
                        )
                    )
            ) {
                //columna principal con espacio y scroll habilitado
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AppHeader()                  //encabezado de la aplicacion
                    TokenSection(token)          //seccion para mostrar y copiar token
                    MessageSection(lastMsg)      //seccion para ultimo mensaje
                }
            }
        }
    }
}

@Composable
fun AppHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "FCM Mensajeria",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TokenSection(token: String) {
    //obtener manager del portapapeles
    val clipboard = LocalClipboardManager.current

    //tarjeta para mostrar token con fondo semitransparente
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Token FCM:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            //caja para mostrar el token y boton de copiar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF051A29))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        token,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        //copiar token al portapapeles
                        clipboard.setText(AnnotatedString(token))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copiar token",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MessageSection(lastMsg: Pair<String, String>?) {
    //tarjeta para mostrar ultimo mensaje o indicador de vacio
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //fila con icono y etiqueta de mensaje
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Ultimo mensaje:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (lastMsg != null) {
                MessageContent(lastMsg)      //mostrar contenido si existe mensaje
            } else {
                EmptyMessageContent()        //mostrar texto de sin mensajes
            }
        }
    }
}

@Composable
fun MessageContent(lastMsg: Pair<String, String>) {
    //seccion con titulo y cuerpo del mensaje
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF051A29))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //mostrar titulo del mensaje
        Column {
            Text(
                "Titulo:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                lastMsg.first,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }

        Divider(
            color = Color(0xFF1A2C3A),
            modifier = Modifier.padding(vertical = 4.dp)
        )

        //mostrar cuerpo del mensaje
        Column {
            Text(
                "Cuerpo:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                lastMsg.second,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
fun EmptyMessageContent() {
    //caja centrada para indicar que no hay mensajes aun
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF051A29))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "— Aun no hay mensajes —",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

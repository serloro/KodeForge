package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Diálogo para insertar un enlace HTML.
 * 
 * @param onDismiss Callback al cancelar
 * @param onConfirm Callback al confirmar (url, text)
 */
@Composable
fun InsertLinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var url by remember { mutableStateOf("https://") }
    var text by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Insertar enlace",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // URL
                OutlinedTextField(
                    value = url,
                    onValueChange = { 
                        url = it
                        errorMessage = null
                    },
                    label = { Text("URL") },
                    placeholder = { Text("https://ejemplo.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Texto del enlace
                OutlinedTextField(
                    value = text,
                    onValueChange = { 
                        text = it
                        errorMessage = null
                    },
                    label = { Text("Texto del enlace") },
                    placeholder = { Text("Haz clic aquí") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Mensaje de error
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color(0xFFF44336),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        url.isBlank() -> errorMessage = "La URL es obligatoria"
                        text.isBlank() -> errorMessage = "El texto del enlace es obligatorio"
                        !url.startsWith("http://") && !url.startsWith("https://") -> 
                            errorMessage = "La URL debe empezar con http:// o https://"
                        else -> onConfirm(url, text)
                    }
                }
            ) {
                Text("Insertar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


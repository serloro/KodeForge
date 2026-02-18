package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Diálogo para crear una nueva página Info.
 * 
 * Campos:
 * - Título en español
 * - Título en inglés
 * 
 * @param onDismiss Callback al cancelar
 * @param onConfirm Callback al confirmar (titleEs, titleEn)
 */
@Composable
fun CreatePageDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var titleEs by remember { mutableStateOf("") }
    var titleEn by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nueva página",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título español
                OutlinedTextField(
                    value = titleEs,
                    onValueChange = { 
                        titleEs = it
                        errorMessage = null
                    },
                    label = { Text("Título (Español)") },
                    placeholder = { Text("ej: Introducción") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Título inglés
                OutlinedTextField(
                    value = titleEn,
                    onValueChange = { 
                        titleEn = it
                        errorMessage = null
                    },
                    label = { Text("Título (English)") },
                    placeholder = { Text("ej: Introduction") },
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
                        titleEs.isBlank() -> errorMessage = "El título en español es obligatorio"
                        titleEn.isBlank() -> errorMessage = "El título en inglés es obligatorio"
                        else -> onConfirm(titleEs, titleEn)
                    }
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


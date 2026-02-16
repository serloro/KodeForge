package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.InfoPage

/**
 * Diálogo de confirmación para eliminar una página Info.
 * 
 * @param page Página a eliminar
 * @param onDismiss Callback al cancelar
 * @param onConfirm Callback al confirmar eliminación
 */
@Composable
fun DeletePageDialog(
    page: InfoPage,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displaySmall
            )
        },
        title = {
            Text(
                text = "¿Eliminar página?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Estás a punto de eliminar la página:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = page.title["es"] ?: page.title["en"] ?: page.slug,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


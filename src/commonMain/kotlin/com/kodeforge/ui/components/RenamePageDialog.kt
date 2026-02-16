package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.InfoPage

/**
 * Diálogo para renombrar una página Info.
 * 
 * Campos:
 * - Slug (URL amigable)
 * - Título en español
 * - Título en inglés
 * 
 * @param page Página a renombrar
 * @param onDismiss Callback al cancelar
 * @param onConfirm Callback al confirmar (slug, titleEs, titleEn)
 */
@Composable
fun RenamePageDialog(
    page: InfoPage,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var slug by remember { mutableStateOf(page.slug) }
    var titleEs by remember { mutableStateOf(page.title["es"] ?: "") }
    var titleEn by remember { mutableStateOf(page.title["en"] ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Renombrar página",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Slug
                OutlinedTextField(
                    value = slug,
                    onValueChange = { 
                        slug = it.lowercase().replace(" ", "-")
                        errorMessage = null
                    },
                    label = { Text("Slug (URL)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = "Solo letras minúsculas, números y guiones",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                )
                
                // Título español
                OutlinedTextField(
                    value = titleEs,
                    onValueChange = { 
                        titleEs = it
                        errorMessage = null
                    },
                    label = { Text("Título (Español)") },
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
                        slug.isBlank() -> errorMessage = "El slug es obligatorio"
                        titleEs.isBlank() -> errorMessage = "El título en español es obligatorio"
                        titleEn.isBlank() -> errorMessage = "El título en inglés es obligatorio"
                        else -> onConfirm(slug, titleEs, titleEn)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


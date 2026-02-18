@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.kodeforge.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.sftp.RemoteFile

/**
 * Componente para mostrar un archivo o directorio remoto.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun RemoteFileItem(
    file: RemoteFile,
    selected: Boolean,
    onSelect: () -> Unit,
    onOpen: () -> Unit,
    onDownload: (() -> Unit)? = null,
    onCopyPath: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val menuExpanded = remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            // En Desktop, evitar pointerInput/awaitPointerEventScope (puede variar por versión).
            // Con onPointerEvent es suficiente para capturar click derecho sin romper la selección.
            .onPointerEvent(PointerEventType.Press) { event ->
                if (event.buttons.isSecondaryPressed) {
                    menuExpanded.value = true
                }
            }
            .combinedClickable(
                onClick = { onSelect() },
                onDoubleClick = { onOpen() }
            )
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        // Context menu
        Box {
            DropdownMenu(
                expanded = menuExpanded.value,
                onDismissRequest = { menuExpanded.value = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (file.isDirectory) "Abrir carpeta" else "Abrir") },
                    onClick = {
                        menuExpanded.value = false
                        onOpen()
                    },
                    leadingIcon = {
                        Text(if (file.isDirectory) "📁" else "📄")
                    }
                )

                DropdownMenuItem(
                    text = { Text("Descargar") },
                    enabled = !file.isDirectory && onDownload != null,
                    onClick = {
                        menuExpanded.value = false
                        onDownload?.invoke()
                    },
                    leadingIcon = { Text("⬇") }
                )

                DropdownMenuItem(
                    text = { Text("Copiar ruta") },
                    enabled = onCopyPath != null,
                    onClick = {
                        menuExpanded.value = false
                        onCopyPath?.invoke()
                    },
                    leadingIcon = { Text("⧉") }
                )
            }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Text(
                text = if (file.isDirectory) "📁" else "📄",
                fontSize = 18.sp,
                modifier = Modifier.width(24.dp)
            )
            
            // Información del archivo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre
                Text(
                    text = file.name,
                    fontSize = 14.sp,
                    fontWeight = if (file.isDirectory) FontWeight.Bold else FontWeight.Normal,
                    color = Color(0xFF212121)
                )
                
                // Detalles (solo para archivos)
                if (!file.isDirectory) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = formatFileSize(file.size),
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                        if (file.modifiedAt.isNotEmpty()) {
                            Text(
                                text = file.modifiedAt,
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }
            
            // Permisos (opcional)
            if (file.permissions.isNotEmpty()) {
                Text(
                    text = file.permissions,
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
        }
    }
}

/**
 * Formatea el tamaño del archivo en formato legible.
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

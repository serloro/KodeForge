package com.kodeforge.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.sftp.RemoteFile

/**
 * Componente para mostrar un archivo o directorio remoto.
 */
@Composable
fun RemoteFileItem(
    file: RemoteFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Icon(
                imageVector = if (file.isDirectory) Icons.Default.KeyboardArrowRight else Icons.Default.Info,
                contentDescription = if (file.isDirectory) "Directorio" else "Archivo",
                tint = if (file.isDirectory) Color(0xFFFFA726) else Color(0xFF90CAF9),
                modifier = Modifier.size(24.dp)
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


package com.kodeforge.ui.components

import androidx.compose.foundation.background
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
import com.kodeforge.domain.model.SftpConnection

/**
 * Componente para mostrar un item de conexión SFTP en una lista.
 */
@Composable
fun SftpConnectionItem(
    connection: SftpConnection,
    onConnect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información de la conexión
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre
                Text(
                    text = connection.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Host y puerto
                Text(
                    text = "${connection.host}:${connection.port}",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Username y auth type
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Usuario: ${connection.username}",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                    
                    // Badge de auth type
                    Surface(
                        color = when (connection.auth.type) {
                            "password" -> Color(0xFFE3F2FD)
                            "key" -> Color(0xFFFFF3E0)
                            else -> Color(0xFFE0E0E0)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = when (connection.auth.type) {
                                "password" -> "Password"
                                "key" -> "Key"
                                else -> "None"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = when (connection.auth.type) {
                                "password" -> Color(0xFF1976D2)
                                "key" -> Color(0xFFF57C00)
                                else -> Color(0xFF616161)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onConnect,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Conectar"
                    )
                }
                
                IconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFF1976D2)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar conexión"
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar conexión"
                    )
                }
            }
        }
    }
}


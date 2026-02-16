package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.DbConnection

/**
 * Item de conexión de base de datos en la lista.
 */
@Composable
fun DbConnectionItem(
    connection: DbConnection,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = connection.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tipo de BD
                    Box(
                        modifier = Modifier
                            .background(
                                color = getDbTypeColor(connection.type),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = connection.type.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                    
                    Text(
                        text = "${connection.host}:${connection.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
                
                Text(
                    text = "DB: ${connection.database} • User: ${connection.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF999999)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("✏️", style = MaterialTheme.typography.bodySmall)
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("🗑️", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/**
 * Retorna un color según el tipo de base de datos.
 */
private fun getDbTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "postgres", "postgresql" -> Color(0xFF336791)
        "mysql" -> Color(0xFF00758F)
        "sqlite" -> Color(0xFF003B57)
        "oracle" -> Color(0xFFF80000)
        "sqlserver", "mssql" -> Color(0xFFCC2927)
        "mariadb" -> Color(0xFF003545)
        "mongodb" -> Color(0xFF47A248)
        else -> Color(0xFF666666)
    }
}


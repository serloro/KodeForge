package com.kodeforge.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.HttpRequest

/**
 * Item de historial de request.
 * 
 * @param request Request HTTP
 * @param isSelected Si está seleccionado
 * @param onClick Callback al hacer click
 */
@Composable
fun RequestHistoryItem(
    request: HttpRequest,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
    val borderColor = if (isSelected) Color(0xFF2196F3) else Color(0xFFE0E0E0)
    
    val statusColor = request.response?.let { response ->
        when (response.status) {
            in 200..299 -> Color(0xFF4CAF50)
            in 300..399 -> Color(0xFFFF9800)
            in 400..499 -> Color(0xFFF44336)
            in 500..599 -> Color(0xFFD32F2F)
            else -> Color(0xFF666666)
        }
    } ?: Color(0xFF999999)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Método + URL
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge del método
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF2196F3)
                ) {
                    Text(
                        text = request.method,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // URL
                Text(
                    text = request.url,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = Color(0xFF1A1A1A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Status + Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status
                if (request.response != null) {
                    Text(
                        text = "${request.response.status}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                } else {
                    Text(
                        text = "Sin respuesta",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = Color(0xFF999999)
                    )
                }
                
                // Timestamp
                Text(
                    text = request.at.substring(11, 16), // HH:MM
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}


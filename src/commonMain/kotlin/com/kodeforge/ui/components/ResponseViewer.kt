package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.HttpResponse

/**
 * Visor de respuesta HTTP.
 * 
 * @param response Respuesta HTTP a mostrar
 */
@Composable
fun ResponseViewer(
    response: HttpResponse?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Respuesta:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            if (response == null) {
                Text(
                    text = "No hay respuesta aún. Envía una request para ver la respuesta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF999999)
                )
            } else {
                // Status
                val statusColor = when (response.status) {
                    in 200..299 -> Color(0xFF4CAF50)
                    in 300..399 -> Color(0xFFFF9800)
                    in 400..499 -> Color(0xFFF44336)
                    in 500..599 -> Color(0xFFD32F2F)
                    else -> Color(0xFF666666)
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Status:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "${response.status}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                
                // Headers
                if (response.headers.isNotEmpty()) {
                    Divider()
                    
                    Text(
                        text = "Headers:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF666666)
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F7FA)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            response.headers.forEach { (key, value) ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "$key:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF666666)
                                    )
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF1A1A1A)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Body
                if (response.body != null) {
                    Divider()
                    
                    Text(
                        text = "Body:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF666666)
                    )
                    
                    val scrollState = rememberScrollState()
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFAFAFA)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .verticalScroll(scrollState)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = response.body,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF1A1A1A)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "(Sin body)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF999999)
                    )
                }
            }
        }
    }
}


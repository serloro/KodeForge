package com.kodeforge.ui.components

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
import com.kodeforge.domain.model.CapturedRequest

/**
 * Detalle de una request capturada.
 */
@Composable
fun CapturedRequestDetail(
    request: CapturedRequest?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        if (request == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Text(
                    text = "Selecciona una request capturada para ver los detalles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF999999)
                )
            }
        } else {
            val scrollState = rememberScrollState()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Método + Path
                Text(
                    text = "${request.method} ${request.path}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                // Timestamp
                Text(
                    text = "Capturada: ${request.at}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
                
                Divider()
                
                // Headers
                if (request.headers.isNotEmpty()) {
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
                            request.headers.forEach { (key, value) ->
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
                } else {
                    Text(
                        text = "Sin headers",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF999999)
                    )
                }
                
                Divider()
                
                // Body
                Text(
                    text = "Body:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF666666)
                )
                
                if (request.body != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFAFAFA)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = request.body,
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


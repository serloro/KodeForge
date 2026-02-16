package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kodeforge.database.QueryResult

/**
 * Tabla para mostrar resultados de queries.
 */
@Composable
fun QueryResultsTable(
    result: QueryResult,
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
            // Header con stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (result.success) "✅ Resultados" else "❌ Error",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (result.success) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                
                if (result.success) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "${result.rowCount} filas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = "${result.executionTimeMs}ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                }
            }
            
            Divider()
            
            if (result.success) {
                // Tabla de resultados
                if (result.rows.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F7FA)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Query ejecutada correctamente.\nNo hay resultados para mostrar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF666666),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Tabla scrolleable
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .border(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                        ) {
                            // Header de la tabla
                            Row(
                                modifier = Modifier.background(Color(0xFFF5F7FA))
                            ) {
                                result.columns.forEach { column ->
                                    Box(
                                        modifier = Modifier
                                            .width(150.dp)
                                            .padding(8.dp)
                                            .border(0.5.dp, Color(0xFFE0E0E0))
                                    ) {
                                        Text(
                                            text = column,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1A1A1A)
                                        )
                                    }
                                }
                            }
                            
                            // Filas
                            result.rows.forEach { row ->
                                Row {
                                    row.forEach { cell ->
                                        Box(
                                            modifier = Modifier
                                                .width(150.dp)
                                                .padding(8.dp)
                                                .border(0.5.dp, Color(0xFFE0E0E0))
                                        ) {
                                            Text(
                                                text = cell,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF1A1A1A)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Info adicional
                    if (result.rowCount >= 1000) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3E0)
                            )
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "⚠️ Se muestran las primeras 1000 filas. La query puede tener más resultados.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }
            } else {
                // Mostrar error
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = result.error ?: "Error desconocido",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}


package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.HttpRequest

/**
 * Lista de historial de requests.
 * 
 * @param requests Lista de requests
 * @param selectedRequestId ID de request seleccionada
 * @param onRequestSelect Callback al seleccionar request
 * @param onClearHistory Callback al limpiar historial
 */
@Composable
fun RequestHistoryList(
    requests: List<HttpRequest>,
    selectedRequestId: String?,
    onRequestSelect: (HttpRequest) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(300.dp)
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Historial",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            if (requests.isNotEmpty()) {
                IconButton(
                    onClick = onClearHistory,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Limpiar historial",
                        tint = Color(0xFFF44336)
                    )
                }
            }
        }
        
        // Lista
        if (requests.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F7FA)
                )
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "No hay requests en el historial.\nEnvía una request para empezar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(requests.reversed()) { request ->
                    RequestHistoryItem(
                        request = request,
                        isSelected = request.id == selectedRequestId,
                        onClick = { onRequestSelect(request) }
                    )
                }
            }
        }
    }
}


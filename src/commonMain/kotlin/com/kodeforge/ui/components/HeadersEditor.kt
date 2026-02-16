package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Editor de headers HTTP (key/value).
 * 
 * @param headers Lista de headers como pares (key, value)
 * @param onHeadersChange Callback cuando cambian los headers
 */
@Composable
fun HeadersEditor(
    headers: List<Pair<String, String>>,
    onHeadersChange: (List<Pair<String, String>>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Headers:",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF666666)
        )
        
        // Lista de headers
        headers.forEachIndexed { index, (key, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { newKey ->
                        val updated = headers.toMutableList()
                        updated[index] = newKey to value
                        onHeadersChange(updated)
                    },
                    label = { Text("Key") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = value,
                    onValueChange = { newValue ->
                        val updated = headers.toMutableList()
                        updated[index] = key to newValue
                        onHeadersChange(updated)
                    },
                    label = { Text("Value") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                IconButton(
                    onClick = {
                        val updated = headers.toMutableList()
                        updated.removeAt(index)
                        onHeadersChange(updated)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar header",
                        tint = Color(0xFFF44336)
                    )
                }
            }
        }
        
        // Botón añadir header
        TextButton(
            onClick = {
                onHeadersChange(headers + ("" to ""))
            }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text("Añadir header")
        }
    }
}


package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.DbConnection
import com.kodeforge.domain.model.SavedQuery

/**
 * Formulario para crear o editar una query guardada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedQueryForm(
    query: SavedQuery?,
    connections: List<DbConnection>,
    onSave: (name: String, connectionId: String, sql: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(query?.name ?: "") }
    var connectionId by remember { mutableStateOf(query?.connectionId ?: connections.firstOrNull()?.id ?: "") }
    var sql by remember { mutableStateOf(query?.sql ?: "") }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expandedConnection by remember { mutableStateOf(false) }
    
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
                text = if (query == null) "Nueva Query" else "Editar Query",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                label = { Text("Nombre") },
                placeholder = { Text("Health Check") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null
            )
            
            // Connection
            if (connections.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "⚠️ No hay conexiones disponibles. Crea una conexión primero.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = expandedConnection,
                    onExpandedChange = { expandedConnection = !expandedConnection }
                ) {
                    val selectedConnection = connections.find { it.id == connectionId }
                    
                    OutlinedTextField(
                        value = selectedConnection?.name ?: "Seleccionar conexión",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Conexión") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedConnection) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedConnection,
                        onDismissRequest = { expandedConnection = false }
                    ) {
                        connections.forEach { connection ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = connection.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${connection.type} • ${connection.host}:${connection.port}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF666666)
                                        )
                                    }
                                },
                                onClick = {
                                    connectionId = connection.id
                                    expandedConnection = false
                                }
                            )
                        }
                    }
                }
            }
            
            // SQL
            OutlinedTextField(
                value = sql,
                onValueChange = {
                    sql = it
                    errorMessage = null
                },
                label = { Text("SQL") },
                placeholder = { Text("SELECT * FROM users;") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10,
                supportingText = {
                    Text(
                        text = "Escribe tu consulta SQL aquí",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            )
            
            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336)
                )
            }
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = {
                        // Validaciones
                        if (name.isBlank()) {
                            errorMessage = "El nombre es obligatorio"
                            return@Button
                        }
                        if (connectionId.isBlank()) {
                            errorMessage = "Selecciona una conexión"
                            return@Button
                        }
                        if (sql.isBlank()) {
                            errorMessage = "El SQL es obligatorio"
                            return@Button
                        }
                        
                        try {
                            onSave(name, connectionId, sql)
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Error al guardar"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = connections.isNotEmpty()
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}


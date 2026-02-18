package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.database.QueryExecutor
import com.kodeforge.database.QueryResult
import com.kodeforge.domain.model.DbConnection
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.DbToolUseCases
import kotlinx.coroutines.launch

/**
 * Componente para ejecutar queries SQL.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryRunner(
    workspace: Workspace,
    projectId: String,
    connections: List<DbConnection>,
    initialConnectionId: String? = null,
    preselectedConnectionId: String? = null,
    initialSql: String = "",
    onWorkspaceUpdate: (Workspace) -> Unit,
    modifier: Modifier = Modifier
) {
    val useCases = remember { DbToolUseCases() }
    val queryExecutor = remember { QueryExecutor() }
    val scope = rememberCoroutineScope()
    
    var selectedConnectionId by remember { mutableStateOf(preselectedConnectionId ?: initialConnectionId ?: connections.firstOrNull()?.id ?: "") }
    var sql by remember { mutableStateOf(initialSql) }
    var isExecuting by remember { mutableStateOf(false) }
    var queryResult by remember { mutableStateOf<QueryResult?>(null) }
    var expandedConnection by remember { mutableStateOf(false) }
    
    val selectedConnection = connections.find { it.id == selectedConnectionId }
    
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
                text = "Query Runner",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            // Selector de conexión
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
                                    selectedConnectionId = connection.id
                                    expandedConnection = false
                                }
                            )
                        }
                    }
                }
                
                // Indicador de soporte
                if (selectedConnection != null) {
                    val isSupported = queryExecutor.isSupported(selectedConnection.type)
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSupported) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        )
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = if (isSupported) {
                                    "✅ ${selectedConnection.type.uppercase()} soportado en este target"
                                } else {
                                    "⚠️ ${selectedConnection.type.uppercase()} no soportado en este target aún. Solo SQLite está disponible actualmente."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSupported) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }
                    }
                }
            }
            
            // Editor SQL
            OutlinedTextField(
                value = sql,
                onValueChange = { sql = it },
                label = { Text("SQL") },
                placeholder = { Text("SELECT * FROM users;") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10,
                enabled = !isExecuting
            )
            
            // Botón ejecutar
            Button(
                onClick = {
                    if (selectedConnection != null && sql.isNotBlank()) {
                        isExecuting = true
                        scope.launch {
                            try {
                                val result = queryExecutor.execute(selectedConnection, sql)
                                queryResult = result
                                
                                // Guardar en historial
                                val historyResult = useCases.addExecutionToHistory(
                                    workspace = workspace,
                                    projectId = projectId,
                                    connectionId = selectedConnection.id,
                                    sql = sql,
                                    success = result.success,
                                    rowCount = result.rowCount,
                                    executionTimeMs = result.executionTimeMs,
                                    error = result.error
                                )
                                
                                historyResult.onSuccess { updatedWorkspace ->
                                    onWorkspaceUpdate(updatedWorkspace)
                                }
                            } catch (e: Exception) {
                                queryResult = QueryResult(
                                    success = false,
                                    error = "Error inesperado: ${e.message}"
                                )
                            } finally {
                                isExecuting = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExecuting && selectedConnection != null && sql.isNotBlank()
            ) {
                if (isExecuting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ejecutando...")
                } else {
                    Text("▶️ Ejecutar Query")
                }
            }
        }
    }
    
    // Mostrar resultados
    if (queryResult != null) {
        Spacer(modifier = Modifier.height(16.dp))
        QueryResultsTable(result = queryResult!!)
    }
}


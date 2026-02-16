package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.MockRoute
import com.kodeforge.domain.model.HttpResponse

/**
 * Diálogo para crear o editar una ruta del mock server.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockRouteDialog(
    route: MockRoute?,
    onDismiss: () -> Unit,
    onSave: (MockRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    var method by remember { mutableStateOf(route?.method ?: "GET") }
    var path by remember { mutableStateOf(route?.path ?: "/") }
    var status by remember { mutableStateOf(route?.response?.status?.toString() ?: "200") }
    var headers by remember { mutableStateOf(route?.response?.headers?.toList() ?: emptyList()) }
    var body by remember { mutableStateOf(route?.response?.body ?: "") }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.width(600.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Text(
                    text = if (route == null) "Nueva Ruta" else "Editar Ruta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                // Método
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Método HTTP:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF666666)
                    )
                    
                    var expanded by remember { mutableStateOf(false) }
                    val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = method,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            methods.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m) },
                                    onClick = {
                                        method = m
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Path
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Path:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF666666)
                    )
                    OutlinedTextField(
                        value = path,
                        onValueChange = { path = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("/api/users") }
                    )
                }
                
                Divider()
                
                // Response - Status
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Response Status:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF666666)
                    )
                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("200") }
                    )
                }
                
                // Response - Headers
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Response Headers:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF666666)
                    )
                    HeadersEditor(
                        headers = headers,
                        onHeadersChange = { headers = it }
                    )
                }
                
                // Response - Body
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Response Body:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF666666)
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        placeholder = { Text("{ \"message\": \"OK\" }") }
                    )
                }
                
                // Error
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF44336)
                    )
                }
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            // Validación
                            if (path.isBlank()) {
                                errorMessage = "El path es obligatorio"
                                return@Button
                            }
                            if (!path.startsWith("/")) {
                                errorMessage = "El path debe empezar con /"
                                return@Button
                            }
                            val statusInt = status.toIntOrNull()
                            if (statusInt == null || statusInt < 100 || statusInt > 599) {
                                errorMessage = "Status inválido (100-599)"
                                return@Button
                            }
                            
                            val newRoute = MockRoute(
                                id = route?.id ?: java.util.UUID.randomUUID().toString(),
                                method = method,
                                path = path,
                                response = HttpResponse(
                                    status = statusInt,
                                    body = body.ifBlank { null },
                                    headers = headers.toMap()
                                )
                            )
                            
                            onSave(newRoute)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}


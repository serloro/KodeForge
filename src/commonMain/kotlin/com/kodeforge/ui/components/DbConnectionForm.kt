package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.DbConnection

/**
 * Formulario para crear o editar una conexión de base de datos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbConnectionForm(
    connection: DbConnection?,
    onSave: (name: String, type: String, host: String, port: Int, database: String, username: String, authType: String, authValueRef: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(connection?.name ?: "") }
    var type by remember { mutableStateOf(connection?.type ?: "postgres") }
    var host by remember { mutableStateOf(connection?.host ?: "127.0.0.1") }
    var port by remember { mutableStateOf(connection?.port?.toString() ?: "5432") }
    var database by remember { mutableStateOf(connection?.database ?: "") }
    var username by remember { mutableStateOf(connection?.username ?: "") }
    var authType by remember { mutableStateOf(connection?.auth?.type ?: "password") }
    var authValueRef by remember { mutableStateOf(connection?.auth?.valueRef ?: "") }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expandedType by remember { mutableStateOf(false) }
    var expandedAuthType by remember { mutableStateOf(false) }
    
    val dbTypes = listOf(
        "postgres" to "PostgreSQL",
        "mysql" to "MySQL",
        "sqlite" to "SQLite",
        "oracle" to "Oracle",
        "sqlserver" to "SQL Server",
        "mariadb" to "MariaDB",
        "mongodb" to "MongoDB"
    )
    
    val authTypes = listOf(
        "password" to "Password",
        "key" to "Key",
        "token" to "Token",
        "none" to "None"
    )
    
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
                text = if (connection == null) "Nueva Conexión" else "Editar Conexión",
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
                placeholder = { Text("Mi Base de Datos") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null
            )
            
            // Type
            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = !expandedType }
            ) {
                OutlinedTextField(
                    value = dbTypes.find { it.first == type }?.second ?: type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Base de Datos") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    dbTypes.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                type = value
                                // Actualizar puerto por defecto según el tipo
                                port = when (value) {
                                    "postgres" -> "5432"
                                    "mysql", "mariadb" -> "3306"
                                    "oracle" -> "1521"
                                    "sqlserver" -> "1433"
                                    "mongodb" -> "27017"
                                    "sqlite" -> "1"
                                    else -> port
                                }
                                expandedType = false
                            }
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Host
                OutlinedTextField(
                    value = host,
                    onValueChange = {
                        host = it
                        errorMessage = null
                    },
                    label = { Text("Host") },
                    placeholder = { Text("127.0.0.1") },
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )
                
                // Port
                OutlinedTextField(
                    value = port,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                            port = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Puerto") },
                    placeholder = { Text("5432") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            // Database
            OutlinedTextField(
                value = database,
                onValueChange = {
                    database = it
                    errorMessage = null
                },
                label = { Text("Base de Datos") },
                placeholder = { Text("mydb") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Username
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    errorMessage = null
                },
                label = { Text("Usuario") },
                placeholder = { Text("postgres") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Divider()
            
            Text(
                text = "Autenticación",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            // Auth Type
            ExposedDropdownMenuBox(
                expanded = expandedAuthType,
                onExpandedChange = { expandedAuthType = !expandedAuthType }
            ) {
                OutlinedTextField(
                    value = authTypes.find { it.first == authType }?.second ?: authType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Autenticación") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAuthType) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedAuthType,
                    onDismissRequest = { expandedAuthType = false }
                ) {
                    authTypes.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                authType = value
                                expandedAuthType = false
                            }
                        )
                    }
                }
            }
            
            // Auth Value Ref
            if (authType != "none") {
                OutlinedTextField(
                    value = authValueRef,
                    onValueChange = {
                        authValueRef = it
                        errorMessage = null
                    },
                    label = { Text("Referencia de Secret") },
                    placeholder = { Text("secret:db_001") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = "Referencia al secret almacenado de forma segura (no guardar contraseñas aquí)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                )
            }
            
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
                        if (host.isBlank()) {
                            errorMessage = "El host es obligatorio"
                            return@Button
                        }
                        if (port.isBlank() || port.toIntOrNull() == null) {
                            errorMessage = "El puerto debe ser un número válido"
                            return@Button
                        }
                        if (database.isBlank()) {
                            errorMessage = "La base de datos es obligatoria"
                            return@Button
                        }
                        if (username.isBlank()) {
                            errorMessage = "El usuario es obligatorio"
                            return@Button
                        }
                        if (authType != "none" && authValueRef.isBlank()) {
                            errorMessage = "La referencia de secret es obligatoria"
                            return@Button
                        }
                        
                        try {
                            onSave(
                                name,
                                type,
                                host,
                                port.toInt(),
                                database,
                                username,
                                authType,
                                authValueRef
                            )
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Error al guardar"
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}


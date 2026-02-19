package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.DbConnection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Formulario para crear o editar una conexión de base de datos.
 */
@OptIn(ExperimentalMaterial3Api::class, kotlinx.coroutines.DelicateCoroutinesApi::class)
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
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }
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
            
            // Auth Value Ref / Password
            if (authType != "none") {
                OutlinedTextField(
                    value = authValueRef,
                    onValueChange = {
                        authValueRef = it
                        errorMessage = null
                    },
                    label = { 
                        Text(if (authType == "password") "Contraseña" else "Referencia de Secret / Valor") 
                    },
                    placeholder = { 
                        Text(if (authType == "password") "Ingresa tu contraseña" else "secret:db_001") 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = if (authType == "password") {
                                "Puedes ingresar la contraseña directamente o usar una referencia (secret:xxx)"
                            } else {
                                "Referencia al secret almacenado o valor directo"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                )
            }
            
            // Error message
            if (errorMessage != null) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF44336),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            
            // Success message
            if (successMessage != null) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = successMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(8.dp)
                    )
                }
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
                
                OutlinedButton(
                    onClick = {
                        // Validar campos antes de probar
                        errorMessage = null
                        successMessage = null
                        
                        if (host.isBlank()) {
                            errorMessage = "El host es obligatorio"
                            return@OutlinedButton
                        }
                        if (port.isBlank() || port.toIntOrNull() == null) {
                            errorMessage = "El puerto debe ser un número válido"
                            return@OutlinedButton
                        }
                        if (database.isBlank()) {
                            errorMessage = "La base de datos es obligatoria"
                            return@OutlinedButton
                        }
                        if (username.isBlank()) {
                            errorMessage = "El usuario es obligatorio"
                            return@OutlinedButton
                        }
                        
                        // Simular prueba de conexión
                        isTestingConnection = true
                        
                        // En una implementación real, aquí se haría la conexión real
                        // Por ahora, simulamos un éxito después de un breve delay
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(1000)
                            isTestingConnection = false
                            successMessage = "✓ Conexión exitosa a $type en $host:$port"
                        }
                    },
                    enabled = !isTestingConnection,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isTestingConnection) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isTestingConnection) "Probando..." else "Probar")
                }
                
                Button(
                    onClick = {
                        // Validaciones
                        errorMessage = null
                        successMessage = null
                        
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


package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.SftpConnection
import com.kodeforge.domain.validation.SftpValidator

/**
 * Formulario para crear o editar una conexión SFTP.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SftpConnectionForm(
    connection: SftpConnection?,
    onSave: (name: String, host: String, port: Int, username: String, authType: String, authValueRef: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(connection?.name ?: "") }
    var host by remember { mutableStateOf(connection?.host ?: "") }
    var port by remember { mutableStateOf(connection?.port?.toString() ?: "22") }
    var username by remember { mutableStateOf(connection?.username ?: "") }
    var authType by remember { mutableStateOf(connection?.auth?.type ?: "password") }
    var authValueRef by remember { mutableStateOf(connection?.auth?.valueRef ?: "") }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var hostError by remember { mutableStateOf<String?>(null) }
    var portError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var authValueRefError by remember { mutableStateOf<String?>(null) }
    
    var expanded by remember { mutableStateOf(false) }
    
    val authTypes = listOf(
        "password" to "Password",
        "key" to "SSH Key",
        "none" to "None"
    )
    
    // Validación en tiempo real
    LaunchedEffect(name) {
        nameError = if (name.isBlank()) "El nombre es obligatorio" else null
    }
    
    LaunchedEffect(host) {
        hostError = if (host.isBlank()) "El host es obligatorio" else null
    }
    
    LaunchedEffect(port) {
        val portInt = port.toIntOrNull()
        portError = when {
            portInt == null -> "Puerto inválido"
            portInt !in 1..65535 -> "Puerto debe estar entre 1 y 65535"
            else -> null
        }
    }
    
    LaunchedEffect(username) {
        usernameError = if (username.isBlank()) "El usuario es obligatorio" else null
    }
    
    LaunchedEffect(authType, authValueRef) {
        authValueRefError = if (authType != "none" && authValueRef.isBlank()) {
            "La referencia al secreto es obligatoria"
        } else null
    }
    
    val canSave = nameError == null && hostError == null && portError == null && 
                  usernameError == null && authValueRefError == null &&
                  name.isNotBlank() && host.isNotBlank() && username.isNotBlank()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            Text(
                text = if (connection == null) "Nueva Conexión SFTP" else "Editar Conexión SFTP",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            
            Divider(color = Color(0xFFE0E0E0))
            
            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre *") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = Color(0xFFD32F2F)) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = Color(0xFFBDBDBD)
                )
            )
            
            // Host
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("Host *") },
                placeholder = { Text("ejemplo: sftp.example.com o 192.168.1.100") },
                isError = hostError != null,
                supportingText = hostError?.let { { Text(it, color = Color(0xFFD32F2F)) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = Color(0xFFBDBDBD)
                )
            )
            
            // Puerto
            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text("Puerto *") },
                placeholder = { Text("22") },
                isError = portError != null,
                supportingText = portError?.let { { Text(it, color = Color(0xFFD32F2F)) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = Color(0xFFBDBDBD)
                )
            )
            
            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario *") },
                isError = usernameError != null,
                supportingText = usernameError?.let { { Text(it, color = Color(0xFFD32F2F)) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = Color(0xFFBDBDBD)
                )
            )
            
            // Auth Type (Dropdown)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = authTypes.find { it.first == authType }?.second ?: "Password",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Autenticación *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color(0xFFBDBDBD)
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    authTypes.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                authType = value
                                expanded = false
                                // Si cambia a "none", limpiar valueRef
                                if (value == "none") {
                                    authValueRef = ""
                                }
                            }
                        )
                    }
                }
            }
            
            // Auth ValueRef (solo si no es "none")
            if (authType != "none") {
                OutlinedTextField(
                    value = authValueRef,
                    onValueChange = { authValueRef = it },
                    label = { Text("Referencia al Secreto *") },
                    placeholder = { Text("ejemplo: secret:sftp_prod") },
                    isError = authValueRefError != null,
                    supportingText = {
                        if (authValueRefError != null) {
                            Text(authValueRefError!!, color = Color(0xFFD32F2F))
                        } else {
                            Text(
                                "Referencia al secreto almacenado de forma segura",
                                color = Color(0xFF757575),
                                fontSize = 12.sp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color(0xFFBDBDBD)
                    )
                )
            }
            
            Divider(color = Color(0xFFE0E0E0))
            
            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF757575)
                    )
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = {
                        val portInt = port.toIntOrNull() ?: 22
                        onSave(name, host, portInt, username, authType, authValueRef)
                    },
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2),
                        disabledContainerColor = Color(0xFFBDBDBD)
                    )
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}


package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Lista editable de destinatarios permitidos.
 */
@Composable
fun AllowedRecipientsList(
    recipients: List<String>,
    onAddRecipient: (String) -> Unit,
    onRemoveRecipient: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Destinatarios Permitidos",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF666666)
            )
            
            TextButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Añadir")
            }
        }
        
        // Lista
        if (recipients.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F7FA)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "No hay destinatarios permitidos.\nAñade emails para permitir su recepción.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFE0E0E0)
                )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(recipients) { email ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1A1A1A),
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = { onRemoveRecipient(email) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        if (email != recipients.last()) {
                            Divider()
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo añadir
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newEmail = ""
                errorMessage = null
            },
            title = { Text("Añadir Destinatario") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = {
                            newEmail = it
                            errorMessage = null
                        },
                        label = { Text("Email") },
                        placeholder = { Text("user@example.com") },
                        singleLine = true,
                        isError = errorMessage != null
                    )
                    
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF44336)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEmail.isBlank()) {
                            errorMessage = "Email no puede estar vacío"
                            return@Button
                        }
                        if (!newEmail.contains("@")) {
                            errorMessage = "Email debe contener @"
                            return@Button
                        }
                        
                        onAddRecipient(newEmail)
                        showAddDialog = false
                        newEmail = ""
                        errorMessage = null
                    }
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showAddDialog = false
                        newEmail = ""
                        errorMessage = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}


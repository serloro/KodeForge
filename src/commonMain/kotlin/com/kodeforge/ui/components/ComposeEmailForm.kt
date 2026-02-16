package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Formulario para componer y enviar un email.
 */
@Composable
fun ComposeEmailForm(
    allowedRecipients: List<String>,
    onSendEmail: (to: String, subject: String, body: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var to by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
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
                text = "Componer Email",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            // To
            OutlinedTextField(
                value = to,
                onValueChange = {
                    to = it
                    errorMessage = null
                    successMessage = null
                },
                label = { Text("Para (To)") },
                placeholder = { Text("recipient@example.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null
            )
            
            // Sugerencia de destinatarios permitidos
            if (allowedRecipients.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F7FA)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Destinatarios permitidos:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF666666)
                        )
                        allowedRecipients.forEach { email ->
                            TextButton(
                                onClick = { to = email },
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            
            // Subject
            OutlinedTextField(
                value = subject,
                onValueChange = {
                    subject = it
                    errorMessage = null
                    successMessage = null
                },
                label = { Text("Asunto (Subject)") },
                placeholder = { Text("Email de prueba") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Body
            OutlinedTextField(
                value = body,
                onValueChange = {
                    body = it
                    errorMessage = null
                    successMessage = null
                },
                label = { Text("Cuerpo (Body)") },
                placeholder = { Text("Escribe el contenido del email...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 10
            )
            
            // Mensajes
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336)
                )
            }
            
            if (successMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = successMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
            
            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        to = ""
                        subject = ""
                        body = ""
                        errorMessage = null
                        successMessage = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }
                
                Button(
                    onClick = {
                        // Validaciones
                        if (to.isBlank()) {
                            errorMessage = "El destinatario es obligatorio"
                            return@Button
                        }
                        if (!to.contains("@")) {
                            errorMessage = "Email inválido (debe contener @)"
                            return@Button
                        }
                        if (subject.isBlank()) {
                            errorMessage = "El asunto es obligatorio"
                            return@Button
                        }
                        if (body.isBlank()) {
                            errorMessage = "El cuerpo es obligatorio"
                            return@Button
                        }
                        
                        // Enviar
                        try {
                            onSendEmail(to, subject, body)
                            successMessage = "✓ Email enviado correctamente a $to"
                            errorMessage = null
                            
                            // Limpiar después de 2 segundos
                            // (En producción usarías LaunchedEffect)
                        } catch (e: Exception) {
                            errorMessage = "Error al enviar: ${e.message}"
                            successMessage = null
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Enviar")
                }
            }
            
            // Nota informativa
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "ℹ️ Los emails se envían al servidor SMTP local (si está habilitado) o se simulan. Aparecerán en el inbox automáticamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}


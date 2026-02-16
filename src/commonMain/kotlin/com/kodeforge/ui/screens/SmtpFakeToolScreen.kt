package com.kodeforge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.SmtpFakeUseCases
import com.kodeforge.smtp.SmtpServerManager

/**
 * Pantalla del tool SMTP Fake con configuración e inbox.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmtpFakeToolScreen(
    workspace: Workspace,
    projectId: String,
    smtpServerManager: SmtpServerManager,
    onWorkspaceUpdate: (Workspace) -> Unit,
    modifier: Modifier = Modifier
) {
    val project = workspace.projects.find { it.id == projectId }
    val smtpTool = project?.tools?.smtpFake
    
    if (project == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Proyecto no encontrado",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF999999)
            )
        }
        return
    }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Configuración", "Inbox", "Enviar")
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFF2196F3)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        
        // Contenido del tab
        when (selectedTab) {
            0 -> ConfigurationTab(
                workspace = workspace,
                projectId = projectId,
                smtpTool = smtpTool,
                smtpServerManager = smtpServerManager,
                onWorkspaceUpdate = onWorkspaceUpdate
            )
            1 -> InboxTab(
                workspace = workspace,
                projectId = projectId,
                smtpTool = smtpTool,
                onWorkspaceUpdate = onWorkspaceUpdate
            )
            2 -> SendTab(
                workspace = workspace,
                projectId = projectId,
                smtpTool = smtpTool,
                onWorkspaceUpdate = onWorkspaceUpdate
            )
        }
    }
}

/**
 * Tab de configuración del servidor SMTP.
 */
@Composable
private fun ConfigurationTab(
    workspace: Workspace,
    projectId: String,
    smtpTool: SmtpFakeTool?,
    smtpServerManager: SmtpServerManager,
    onWorkspaceUpdate: (Workspace) -> Unit
) {
    val useCases = SmtpFakeUseCases()
    
    var listenHost by remember { mutableStateOf(smtpTool?.listenHost ?: "127.0.0.1") }
    var listenPort by remember { mutableStateOf(smtpTool?.listenPort?.toString() ?: "2525") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val isEnabled = smtpTool?.enabled ?: false
    val recipients = smtpTool?.allowedRecipients ?: emptyList()
    val isServerRunning = smtpServerManager.isServerRunning(projectId)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Estado del servidor
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isEnabled) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Estado del Servidor",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = when {
                            !isEnabled -> "Deshabilitado"
                            isServerRunning -> "Servidor corriendo en $listenHost:$listenPort"
                            else -> "Habilitado (servidor detenido)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
                
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { enabled ->
                        val port = listenPort.toIntOrNull() ?: 2525
                        
                        val result = if (enabled) {
                            useCases.enableSmtpServer(
                                workspace = workspace,
                                projectId = projectId,
                                listenHost = listenHost,
                                listenPort = port
                            )
                        } else {
                            useCases.disableSmtpServer(workspace, projectId)
                        }
                        
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            
                            // Iniciar/detener servidor real
                            if (enabled) {
                                try {
                                    smtpServerManager.startServer(
                                        projectId = projectId,
                                        workspace = updatedWorkspace,
                                        host = listenHost,
                                        port = port,
                                        allowedRecipients = recipients,
                                        onWorkspaceUpdate = onWorkspaceUpdate
                                    )
                                } catch (e: Exception) {
                                    errorMessage = "Error al iniciar servidor: ${e.message}"
                                }
                            } else {
                                smtpServerManager.stopServer(projectId)
                            }
                        }.onFailure { errorMessage = it.message }
                    }
                )
            }
        }
        
        // Configuración de red
        Card(
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
                    text = "Configuración de Red",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                // Listen Host
                OutlinedTextField(
                    value = listenHost,
                    onValueChange = { listenHost = it },
                    label = { Text("Listen Host") },
                    placeholder = { Text("127.0.0.1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Listen Port
                OutlinedTextField(
                    value = listenPort,
                    onValueChange = { listenPort = it },
                    label = { Text("Listen Port") },
                    placeholder = { Text("2525") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Botón aplicar
                Button(
                    onClick = {
                        val port = listenPort.toIntOrNull()
                        if (port == null) {
                            errorMessage = "Puerto inválido"
                            return@Button
                        }
                        
                        val result = if (smtpTool == null) {
                            useCases.enableSmtpServer(
                                workspace = workspace,
                                projectId = projectId,
                                listenHost = listenHost,
                                listenPort = port
                            )
                        } else {
                            useCases.updateSmtpConfig(
                                workspace = workspace,
                                projectId = projectId,
                                listenHost = listenHost,
                                listenPort = port
                            )
                        }
                        
                        result.onSuccess {
                            onWorkspaceUpdate(it)
                            errorMessage = null
                        }.onFailure {
                            errorMessage = it.message
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aplicar Configuración")
                }
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
        
        // Destinatarios permitidos
        Card(
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
                com.kodeforge.ui.components.AllowedRecipientsList(
                    recipients = recipients,
                    onAddRecipient = { email ->
                        // Inicializar si no existe
                        val initResult = if (smtpTool == null) {
                            useCases.enableSmtpServer(workspace, projectId)
                        } else {
                            Result.success(workspace)
                        }
                        
                        initResult.onSuccess { ws ->
                            val result = useCases.addAllowedRecipient(ws, projectId, email)
                            result.onSuccess { onWorkspaceUpdate(it) }
                                .onFailure { errorMessage = it.message }
                        }
                    },
                    onRemoveRecipient = { email ->
                        val result = useCases.removeAllowedRecipient(workspace, projectId, email)
                        result.onSuccess { onWorkspaceUpdate(it) }
                            .onFailure { errorMessage = it.message }
                    }
                )
            }
        }
        
        // Nota
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isServerRunning) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            )
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isServerRunning) {
                        "✅ Servidor SMTP corriendo. Los emails enviados a $listenHost:$listenPort serán capturados automáticamente."
                    } else {
                        "ℹ️ Habilita el servidor para comenzar a capturar emails. Los emails se guardarán en el inbox automáticamente."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

/**
 * Tab del inbox de emails capturados.
 */
@Composable
private fun InboxTab(
    workspace: Workspace,
    projectId: String,
    smtpTool: SmtpFakeTool?,
    onWorkspaceUpdate: (Workspace) -> Unit
) {
    val useCases = SmtpFakeUseCases()
    
    var selectedEmailId by remember { mutableStateOf<String?>(null) }
    var filterRecipient by remember { mutableStateOf("") }
    
    val inbox = smtpTool?.storedInbox ?: emptyList()
    
    val filteredInbox = if (filterRecipient.isBlank()) {
        inbox
    } else {
        inbox.filter { email ->
            email.to.any { it.contains(filterRecipient, ignoreCase = true) }
        }
    }
    
    val selectedEmail = filteredInbox.find { it.id == selectedEmailId }
    
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Lista de emails (izquierda)
        Column(
            modifier = Modifier
                .width(350.dp)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inbox (${filteredInbox.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                if (inbox.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val result = useCases.clearInbox(workspace, projectId)
                            result.onSuccess {
                                onWorkspaceUpdate(it)
                                selectedEmailId = null
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar inbox",
                            tint = Color(0xFFF44336)
                        )
                    }
                }
            }
            
            // Filtro
            OutlinedTextField(
                value = filterRecipient,
                onValueChange = { filterRecipient = it },
                label = { Text("Filtrar por destinatario") },
                placeholder = { Text("user@example.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Lista
            if (filteredInbox.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F7FA)
                    )
                ) {
                    Box(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = if (inbox.isEmpty()) {
                                "No hay emails en el inbox.\nEl servidor capturará emails automáticamente cuando esté implementado."
                            } else {
                                "No hay emails que coincidan con el filtro."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredInbox.reversed()) { email ->
                        com.kodeforge.ui.components.EmailListItem(
                            email = email,
                            isSelected = email.id == selectedEmailId,
                            onClick = { selectedEmailId = email.id }
                        )
                    }
                }
            }
        }
        
        Divider(modifier = Modifier.width(1.dp).fillMaxHeight())
        
        // Detalle (derecha)
        com.kodeforge.ui.components.EmailDetail(
            email = selectedEmail,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        )
    }
}

/**
 * Tab para enviar emails.
 */
@Composable
private fun SendTab(
    workspace: Workspace,
    projectId: String,
    smtpTool: SmtpFakeTool?,
    onWorkspaceUpdate: (Workspace) -> Unit
) {
    val allowedRecipients = smtpTool?.allowedRecipients ?: emptyList()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Información
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "📧 Envía emails de prueba que serán capturados automáticamente en el inbox.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }
        }
        
        // Formulario de composición
        com.kodeforge.ui.components.ComposeEmailForm(
            allowedRecipients = allowedRecipients,
            onSendEmail = { to, subject, body ->
                val emailSender = com.kodeforge.smtp.EmailSender()
                
                val result = emailSender.sendEmail(
                    workspace = workspace,
                    projectId = projectId,
                    from = "user@kodeforge.local",
                    to = to,
                    subject = subject,
                    body = body
                )
                
                result.onSuccess { updatedWorkspace ->
                    onWorkspaceUpdate(updatedWorkspace)
                }.onFailure { error ->
                    throw error
                }
            }
        )
    }
}


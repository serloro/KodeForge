package com.kodeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Workspace
import com.kodeforge.smtp.SmtpServerManager
import com.kodeforge.ui.components.ToolLayout
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Pantalla placeholder para tools (T8).
 * 
 * Muestra:
 * - Título del tool
 * - Descripción breve
 * - "En construcción"
 * 
 * Para "info": renderiza InfoToolScreen
 * 
 * @param toolType Tipo de tool (smtp, rest, sftp, bbdd, tasks, info)
 * @param project Proyecto actual
 * @param workspace Workspace actual
 * @param onWorkspaceUpdate Callback para actualizar workspace
 * @param onBack Callback para volver
 * @param onToolClick Callback para cambiar de herramienta
 * @param onBackToHub Callback para volver al hub del proyecto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScreen(
    toolType: String,
    project: Project,
    workspace: Workspace,
    smtpServerManager: SmtpServerManager,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit,
    onToolClick: (String) -> Unit = {},
    onBackToHub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Si es "info", renderizar InfoToolScreen
    if (toolType == "info") {
        InfoToolScreen(
            workspace = workspace,
            project = project,
            onWorkspaceUpdate = onWorkspaceUpdate,
            onBack = onBack,
            onToolClick = onToolClick,
            onBackToHub = onBackToHub,
            modifier = modifier
        )
        return
    }
    
    // Si es "rest", renderizar RestSoapToolScreen
    if (toolType == "rest") {
        RestSoapToolScreen(
            workspace = workspace,
            projectId = project.id,
            project = project,
            onWorkspaceUpdate = onWorkspaceUpdate,
            onBack = onBack,
            onToolClick = onToolClick,
            onBackToHub = onBackToHub,
            modifier = modifier
        )
        return
    }
    
    // Si es "smtp", renderizar SmtpFakeToolScreen
    if (toolType == "smtp") {
        SmtpFakeToolScreen(
            workspace = workspace,
            projectId = project.id,
            project = project,
            smtpServerManager = smtpServerManager,
            onWorkspaceUpdate = onWorkspaceUpdate,
            onBack = onBack,
            onToolClick = onToolClick,
            onBackToHub = onBackToHub,
            modifier = modifier
        )
        return
    }
    
    // Si es "bbdd", renderizar DbToolScreen
    if (toolType == "bbdd") {
        DbToolScreen(
            workspace = workspace,
            projectId = project.id,
            project = project,
            onWorkspaceUpdate = onWorkspaceUpdate,
            onBack = onBack,
            onToolClick = onToolClick,
            onBackToHub = onBackToHub,
            modifier = modifier
        )
        return
    }
    
    // Si es "sftp", renderizar SftpToolScreen
    if (toolType == "sftp") {
        SftpToolScreen(
            workspace = workspace,
            projectId = project.id,
            project = project,
            onWorkspaceUpdate = onWorkspaceUpdate,
            onBack = onBack,
            onToolClick = onToolClick,
            onBackToHub = onBackToHub,
            modifier = modifier
        )
        return
    }

    // Si es "tasks", mostrar gestión de tareas del proyecto
    if (toolType == "tasks") {
        ManageTasksScreen(
            workspace = workspace,
            project = project,
            onWorkspaceUpdate = onWorkspaceUpdate,
            onBack = onBack,
            modifier = modifier
        )
        return
    }
    
    // Para otros tools, mostrar placeholder
    val toolConfig = toolConfigs[toolType] ?: ToolConfig(
        id = toolType,
        title = "Tool Desconocido",
        description = "Herramienta no configurada",
        icon = "🔧"
    )
    
    ToolLayout(
        project = project,
        toolTitle = toolConfig.title,
        selectedToolId = toolType,
        onBack = onBack,
        onToolClick = onToolClick,
        onBackToHub = onBackToHub,
        modifier = modifier
    ) {
        // Contenido placeholder
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono grande
            Text(
                text = toolConfig.icon,
                fontSize = 80.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Título
            Text(
                text = toolConfig.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Descripción
            Text(
                text = toolConfig.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // "En construcción"
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🚧 En construcción 🚧",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Esta funcionalidad estará disponible próximamente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Info adicional
            Text(
                text = "Proyecto: ${project.name}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF999999),
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}

/**
 * Configuración de un tool.
 */
private data class ToolConfig(
    val id: String,
    val title: String,
    val description: String,
    val icon: String
)

/**
 * Configuraciones de todos los tools disponibles.
 */
private val toolConfigs = mapOf(
    "tempo1" to ToolConfig(
        id = "tempo1",
        title = "Tempo - Gestión Tarea 1",
        description = "Herramienta de gestión de tiempo y tareas",
        icon = "📅"
    ),
    "tempo2" to ToolConfig(
        id = "tempo2",
        title = "Tempo - Hory Franquimonos",
        description = "Herramienta de seguimiento de horas",
        icon = "⏱️"
    ),
    "smtp" to ToolConfig(
        id = "smtp",
        title = "SMTP Fake",
        description = "Servidor SMTP falso para testing de correos electrónicos",
        icon = "📧"
    ),
    "rest" to ToolConfig(
        id = "rest",
        title = "REST/SOAP API",
        description = "Cliente HTTP y Mock Server para APIs REST y SOAP",
        icon = "🔌"
    ),
    "ajustes" to ToolConfig(
        id = "ajustes",
        title = "Ajustes",
        description = "Configuración general de la aplicación",
        icon = "⚙️"
    ),
    "info" to ToolConfig(
        id = "info",
        title = "Info - Documentación",
        description = "Editor WYSIWYG multiidioma para páginas de información",
        icon = "ℹ️"
    ),
    "sftp" to ToolConfig(
        id = "sftp",
        title = "SFTP/SSH",
        description = "Conexión SFTP y explorador de archivos remoto",
        icon = "📁"
    ),
    "bbdd" to ToolConfig(
        id = "bbdd",
        title = "Base de Datos",
        description = "Conexiones a bases de datos y editor de consultas",
        icon = "🗄️"
    ),
    "tasks" to ToolConfig(
        id = "tasks",
        title = "Gestión de Tareas",
        description = "Sincronización con GitHub Issues y otros sistemas",
        icon = "✅"
    )
)


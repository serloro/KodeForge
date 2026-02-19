package com.kodeforge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.DbConnection
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.DbToolUseCases
import com.kodeforge.ui.components.ToolLayout
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Pantalla de la herramienta de Base de Datos.
 * 
 * Flujo mejorado:
 * 1. Vista inicial: Lista de conexiones (crear/editar/eliminar)
 * 2. Al conectar: Vista de trabajo con tabs (Ejecutar SQL / SQL Guardadas)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbToolScreen(
    workspace: Workspace,
    projectId: String,
    project: Project,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit,
    onToolClick: (String) -> Unit = {},
    onBackToHub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val useCases = remember { DbToolUseCases() }
    val dbTool = useCases.getDbTool(workspace, projectId)
    val connections = dbTool?.connections ?: emptyList()
    
    // Estado: null = lista de conexiones, connectionId = conectado a esa BD
    var connectedConnectionId by remember { mutableStateOf<String?>(null) }
    var showConnectionForm by remember { mutableStateOf(false) }
    var editingConnection by remember { mutableStateOf<DbConnection?>(null) }
    var connectionToDelete by remember { mutableStateOf<DbConnection?>(null) }
    
    ToolLayout(
        project = project,
        toolTitle = "Base de Datos",
        selectedToolId = "bbdd",
        onBack = onBack,
        onToolClick = onToolClick,
        onBackToHub = onBackToHub,
        modifier = modifier
    ) {
        if (connectedConnectionId == null) {
            // VISTA 1: Lista de conexiones
            ConnectionsListView(
                connections = connections,
                onConnect = { connection ->
                    connectedConnectionId = connection.id
                },
                onCreateConnection = {
                    editingConnection = null
                    showConnectionForm = true
                },
                onEditConnection = { connection ->
                    editingConnection = connection
                    showConnectionForm = true
                },
                onDeleteConnection = { connection ->
                    connectionToDelete = connection
                }
            )
        } else {
            // VISTA 2: Conectado a una BD
            val connectedConnection = connections.find { it.id == connectedConnectionId }
            if (connectedConnection != null) {
                ConnectedView(
                    workspace = workspace,
                    projectId = projectId,
                    connection = connectedConnection,
                    dbTool = dbTool,
                    onWorkspaceUpdate = onWorkspaceUpdate,
                    onDisconnect = { connectedConnectionId = null }
                )
            }
        }
    }
    
    // Dialog: Crear/Editar Conexión
    if (showConnectionForm) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { 
                showConnectionForm = false
                editingConnection = null
            }
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.width(600.dp)
            ) {
                com.kodeforge.ui.components.DbConnectionForm(
                    connection = editingConnection,
                    onSave = { name, type, host, port, database, username, authType, authValueRef ->
                        val result = if (editingConnection == null) {
                            // Crear nueva
                            useCases.addConnection(
                                workspace = workspace,
                                projectId = projectId,
                                name = name,
                                type = type,
                                host = host,
                                port = port,
                                database = database,
                                username = username,
                                authType = authType,
                                authValueRef = authValueRef
                            )
                        } else {
                            // Editar existente
                            useCases.updateConnection(
                                workspace = workspace,
                                projectId = projectId,
                                connectionId = editingConnection!!.id,
                                name = name,
                                type = type,
                                host = host,
                                port = port,
                                database = database,
                                username = username,
                                authType = authType,
                                authValueRef = authValueRef
                            )
                        }
                        
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            showConnectionForm = false
                            editingConnection = null
                        }
                    },
                    onCancel = { 
                        showConnectionForm = false
                        editingConnection = null
                    }
                )
            }
        }
    }
    
    // Dialog: Confirmar eliminación
    if (connectionToDelete != null) {
        AlertDialog(
            onDismissRequest = { connectionToDelete = null },
            title = { Text("Eliminar Conexión") },
            text = {
                Text("¿Estás seguro de que quieres eliminar la conexión '${connectionToDelete!!.name}'?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = useCases.deleteConnection(
                            workspace = workspace,
                            projectId = projectId,
                            connectionId = connectionToDelete!!.id
                        )
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            connectionToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { connectionToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * VISTA 1: Lista de conexiones disponibles
 */
@Composable
private fun ConnectionsListView(
    connections: List<DbConnection>,
    onConnect: (DbConnection) -> Unit,
    onCreateConnection: () -> Unit,
    onEditConnection: (DbConnection) -> Unit,
    onDeleteConnection: (DbConnection) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (connections.isEmpty()) {
            // Estado vacío
            EmptyConnectionsState(onCreateConnection = onCreateConnection)
        } else {
            // Lista de conexiones
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF8F9FA),
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Conexiones de Base de Datos",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = KodeForgeColors.TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${connections.size} ${if (connections.size == 1) "conexión configurada" else "conexiones configuradas"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KodeForgeColors.TextSecondary
                            )
                        }
                        
                        Button(
                            onClick = onCreateConnection,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KodeForgeColors.Primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Nueva Conexión")
                        }
                    }
                }
                
                // Lista
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(connections) { connection ->
                        DbConnectionCard(
                            connection = connection,
                            onConnect = { onConnect(connection) },
                            onEdit = { onEditConnection(connection) },
                            onDelete = { onDeleteConnection(connection) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card de conexión mejorada visualmente
 */
@Composable
private fun DbConnectionCard(
    connection: DbConnection,
    onConnect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header con icono y nombre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono del tipo de BD
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = getDbTypeColor(connection.type).copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = getDbIcon(connection.type),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            text = connection.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = KodeForgeColors.TextPrimary
                        )
                        
                        // Badge del tipo
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = getDbTypeColor(connection.type)
                        ) {
                            Text(
                                text = connection.type.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                // Botones de acción
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Text("✏️", style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = onDelete) {
                        Text("🗑️", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Información de la conexión
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ConnectionInfoRow(
                    label = "Host",
                    value = "${connection.host}:${connection.port}"
                )
                ConnectionInfoRow(
                    label = "Base de Datos",
                    value = connection.database
                )
                ConnectionInfoRow(
                    label = "Usuario",
                    value = connection.username
                )
                ConnectionInfoRow(
                    label = "Autenticación",
                    value = connection.auth.type.replaceFirstChar { it.uppercase() }
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Botón conectar
            Button(
                onClick = onConnect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KodeForgeColors.Primary
                )
            ) {
                Text("🔌 Conectar", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

/**
 * Fila de información de conexión
 */
@Composable
private fun ConnectionInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = KodeForgeColors.TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = KodeForgeColors.TextPrimary,
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * Estado vacío cuando no hay conexiones
 */
@Composable
private fun EmptyConnectionsState(
    onCreateConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Icono grande
            Surface(
                shape = MaterialTheme.shapes.large,
                color = Color(0xFFF5F7FA),
                modifier = Modifier.size(120.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "🗄️",
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No hay conexiones configuradas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = KodeForgeColors.TextPrimary
                )
                
                Text(
                    text = "Crea tu primera conexión a base de datos para comenzar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = KodeForgeColors.TextSecondary
                )
            }
            
            Button(
                onClick = onCreateConnection,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KodeForgeColors.Primary
                )
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Crear Primera Conexión")
            }
        }
    }
}

/**
 * VISTA 2: Vista conectada con tabs
 */
@Composable
private fun ConnectedView(
    workspace: Workspace,
    projectId: String,
    connection: DbConnection,
    dbTool: com.kodeforge.domain.model.DbTool?,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Ejecutar SQL", "SQL Guardadas", "Historial")
    
    Column(modifier = modifier.fillMaxSize()) {
        // Header de conexión activa
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1976D2),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDisconnect) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Desconectar",
                            tint = Color.White
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Conectado a: ${connection.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${connection.type.uppercase()} • ${connection.host}:${connection.port} • ${connection.database}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                
                Text(
                    text = getDbIcon(connection.type),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = KodeForgeColors.Primary
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
            0 -> ExecuteSqlTab(
                workspace = workspace,
                projectId = projectId,
                connection = connection,
                onWorkspaceUpdate = onWorkspaceUpdate
            )
            1 -> SavedSqlTab(
                workspace = workspace,
                projectId = projectId,
                connection = connection,
                dbTool = dbTool,
                onWorkspaceUpdate = onWorkspaceUpdate
            )
            2 -> HistoryTab(
                dbTool = dbTool,
                connection = connection
            )
        }
    }
}

/**
 * Tab para ejecutar SQL
 */
@Composable
private fun ExecuteSqlTab(
    workspace: Workspace,
    projectId: String,
    connection: DbConnection,
    onWorkspaceUpdate: (Workspace) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        com.kodeforge.ui.components.QueryRunner(
            workspace = workspace,
            projectId = projectId,
            connections = listOf(connection),
            onWorkspaceUpdate = onWorkspaceUpdate,
            preselectedConnectionId = connection.id
        )
    }
}

/**
 * Tab para SQL guardadas
 */
@Composable
private fun SavedSqlTab(
    workspace: Workspace,
    projectId: String,
    connection: DbConnection,
    dbTool: com.kodeforge.domain.model.DbTool?,
    onWorkspaceUpdate: (Workspace) -> Unit,
    modifier: Modifier = Modifier
) {
    val useCases = remember { DbToolUseCases() }
    val allQueries = dbTool?.savedQueries ?: emptyList()
    val queriesForConnection = allQueries.filter { it.connectionId == connection.id }
    
    var selectedQueryId by remember { mutableStateOf<String?>(null) }
    var showCreateForm by remember { mutableStateOf(false) }
    var queryToDelete by remember { mutableStateOf<com.kodeforge.domain.model.SavedQuery?>(null) }
    
    if (queriesForConnection.isEmpty() && !showCreateForm) {
        // Estado vacío
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "📝",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "No hay SQL guardadas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = KodeForgeColors.TextPrimary
                )
                Text(
                    text = "Guarda tus queries más usadas para acceder rápidamente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KodeForgeColors.TextSecondary
                )
                
                Button(
                    onClick = { showCreateForm = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KodeForgeColors.Primary
                    )
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar SQL")
                }
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Lista de queries (izquierda)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SQL Guardadas (${queriesForConnection.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Button(
                        onClick = { showCreateForm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KodeForgeColors.Primary
                        )
                    ) {
                        Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Nueva")
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(queriesForConnection) { query ->
                        com.kodeforge.ui.components.SavedQueryItem(
                            query = query,
                            connectionName = connection.name,
                            isSelected = query.id == selectedQueryId,
                            onClick = { selectedQueryId = query.id },
                            onEdit = {
                                // TODO: Implementar edición
                            },
                            onDelete = {
                                queryToDelete = query
                            }
                        )
                    }
                }
            }
            
            Divider(modifier = Modifier.width(1.dp).fillMaxHeight())
            
            // Detalle o formulario (derecha)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (showCreateForm) {
                    com.kodeforge.ui.components.SavedQueryForm(
                        query = null,
                        connections = listOf(connection),
                        onSave = { name, connectionId, sql ->
                            val result = useCases.addSavedQuery(
                                workspace = workspace,
                                projectId = projectId,
                                name = name,
                                connectionId = connectionId,
                                sql = sql
                            )
                            
                            result.onSuccess { updatedWorkspace ->
                                onWorkspaceUpdate(updatedWorkspace)
                                showCreateForm = false
                            }
                        },
                        onCancel = { showCreateForm = false }
                    )
                } else {
                    val selectedQuery = queriesForConnection.find { it.id == selectedQueryId }
                    
                    if (selectedQuery != null) {
                        SavedQueryDetail(
                            query = selectedQuery,
                            workspace = workspace,
                            projectId = projectId,
                            connection = connection,
                            onWorkspaceUpdate = onWorkspaceUpdate
                        )
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F7FA)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Selecciona una SQL para ver sus detalles",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Dialog: Confirmar eliminación de query
    if (queryToDelete != null) {
        AlertDialog(
            onDismissRequest = { queryToDelete = null },
            title = { Text("Eliminar SQL Guardada") },
            text = {
                Text("¿Estás seguro de que quieres eliminar '${queryToDelete!!.name}'?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val result = useCases.deleteSavedQuery(
                            workspace = workspace,
                            projectId = projectId,
                            queryId = queryToDelete!!.id
                        )
                        result.onSuccess { updatedWorkspace ->
                            onWorkspaceUpdate(updatedWorkspace)
                            queryToDelete = null
                            selectedQueryId = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { queryToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Detalle de una SQL guardada
 */
@Composable
private fun SavedQueryDetail(
    query: com.kodeforge.domain.model.SavedQuery,
    workspace: Workspace,
    projectId: String,
    connection: DbConnection,
    onWorkspaceUpdate: (Workspace) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = query.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Divider()
            
            Text(
                text = "SQL Query",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = KodeForgeColors.TextSecondary
            )
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F7FA)
                )
            ) {
                Text(
                    text = query.sql,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            Button(
                onClick = {
                    // TODO: Ejecutar esta query en el tab de Ejecutar SQL
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KodeForgeColors.Primary
                )
            ) {
                Text("▶️ Ejecutar Query")
            }
        }
    }
}

/**
 * Tab de historial
 */
@Composable
private fun HistoryTab(
    dbTool: com.kodeforge.domain.model.DbTool?,
    connection: DbConnection,
    modifier: Modifier = Modifier
) {
    val historyForConnection = dbTool?.executionHistory?.filter { 
        it.connectionId == connection.id 
    } ?: emptyList()
    
    if (historyForConnection.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📊",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "No hay historial de ejecuciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KodeForgeColors.TextPrimary
                )
                Text(
                    text = "Ejecuta queries para ver el historial aquí",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KodeForgeColors.TextSecondary
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(historyForConnection.reversed()) { execution ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (execution.success) Color(0xFFF1F8F4) else Color(0xFFFFF4F4)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (execution.success) "✅ Exitosa" else "❌ Error",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (execution.success) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Text(
                                text = execution.executedAt,
                                style = MaterialTheme.typography.bodySmall,
                                color = KodeForgeColors.TextSecondary
                            )
                        }
                        
                        Text(
                            text = execution.sql,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            maxLines = 2
                        )
                        
                        if (execution.success) {
                            Text(
                                text = "${execution.rowCount} filas • ${execution.executionTimeMs}ms",
                                style = MaterialTheme.typography.bodySmall,
                                color = KodeForgeColors.TextSecondary
                            )
                        } else if (execution.error != null) {
                            Text(
                                text = "Error: ${execution.error}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC62828),
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Obtiene el icono emoji para un tipo de base de datos
 */
private fun getDbIcon(type: String): String {
    return when (type.lowercase()) {
        "postgres", "postgresql" -> "🐘"
        "mysql" -> "🐬"
        "sqlite" -> "📦"
        "oracle" -> "🔴"
        "sqlserver", "mssql" -> "🟦"
        "mariadb" -> "🦭"
        "mongodb" -> "🍃"
        else -> "🗄️"
    }
}

/**
 * Retorna un color según el tipo de base de datos
 */
private fun getDbTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "postgres", "postgresql" -> Color(0xFF336791)
        "mysql" -> Color(0xFF00758F)
        "sqlite" -> Color(0xFF003B57)
        "oracle" -> Color(0xFFF80000)
        "sqlserver", "mssql" -> Color(0xFFCC2927)
        "mariadb" -> Color(0xFF003545)
        "mongodb" -> Color(0xFF47A248)
        else -> Color(0xFF666666)
    }
}

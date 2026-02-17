package com.kodeforge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.*
import com.kodeforge.domain.usecases.RestSoapUseCases
import com.kodeforge.ui.components.ToolLayout
import com.kodeforge.ui.theme.KodeForgeColors
import java.time.Instant

/**
 * Pantalla del tool REST/SOAP con tabs para cliente, capturas y rutas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestSoapToolScreen(
    workspace: Workspace,
    projectId: String,
    project: Project,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit,
    onToolClick: (String) -> Unit = {},
    onBackToHub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Inicializar el tool si no existe
    val restSoapTool = project.tools?.restSoap ?: RestSoapTool(enabled = true)
    
    // Si el tool no estaba inicializado, actualizarlo en el workspace
    if (project.tools?.restSoap == null) {
        val updatedProject = project.copy(
            tools = (project.tools ?: ProjectTools()).copy(restSoap = restSoapTool)
        )
        val updatedWorkspace = workspace.copy(
            projects = workspace.projects.map { if (it.id == project.id) updatedProject else it }
        )
        onWorkspaceUpdate(updatedWorkspace)
    }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Cliente", "Capturas", "Rutas")
    
    ToolLayout(
        project = project,
        toolTitle = "REST/SOAP API",
        selectedToolId = "rest",
        onBack = onBack,
        onToolClick = onToolClick,
        onBackToHub = onBackToHub,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
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
            0 -> ClientTab(
                workspace = workspace,
                projectId = projectId,
                restSoapTool = restSoapTool,
                onWorkspaceUpdate = onWorkspaceUpdate
            )
            1 -> CapturesTab(
                workspace = workspace,
                projectId = projectId,
                restSoapTool = restSoapTool,
                onWorkspaceUpdate = onWorkspaceUpdate
            )
            2 -> RoutesTab(
                workspace = workspace,
                projectId = projectId,
                restSoapTool = restSoapTool,
                onWorkspaceUpdate = onWorkspaceUpdate
            )
        }
        }
    }
}

/**
 * Tab del cliente REST/SOAP.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientTab(
    workspace: Workspace,
    projectId: String,
    restSoapTool: RestSoapTool,
    onWorkspaceUpdate: (Workspace) -> Unit
) {
    var clientType by remember { mutableStateOf("REST") }
    var method by remember { mutableStateOf("GET") }
    var url by remember { mutableStateOf("") }
    var headers by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var body by remember { mutableStateOf("") }
    var response by remember { mutableStateOf<HttpResponse?>(null) }
    var selectedHistoryId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Historial (izquierda)
        com.kodeforge.ui.components.RequestHistoryList(
            requests = restSoapTool.clientHistory,
            selectedRequestId = selectedHistoryId,
            onRequestSelect = { request ->
                selectedHistoryId = request.id
                clientType = request.type
                method = request.method
                url = request.url
                headers = request.headers.toList()
                body = request.body ?: ""
                response = request.response
            },
            onClearHistory = {
                val useCases = RestSoapUseCases()
                val result = useCases.clearHistory(workspace, projectId)
                result.onSuccess { onWorkspaceUpdate(it) }
            }
        )
        
        Divider(modifier = Modifier.width(1.dp).fillMaxHeight())
        
        // Cliente (centro)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selector tipo
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = clientType == "REST",
                    onClick = { clientType = "REST" },
                    label = { Text("REST") }
                )
                FilterChip(
                    selected = clientType == "SOAP",
                    onClick = { clientType = "SOAP" },
                    label = { Text("SOAP") }
                )
            }
            
            // Método (solo REST)
            if (clientType == "REST") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("GET", "POST", "PUT", "DELETE").forEach { m ->
                        FilterChip(
                            selected = method == m,
                            onClick = { method = m },
                            label = { Text(m) }
                        )
                    }
                }
            }
            
            // URL
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("URL") },
                placeholder = { Text("http://localhost:8080/api/users") }
            )
            
            // Headers
            Text(
                text = "Headers:",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF666666)
            )
            com.kodeforge.ui.components.HeadersEditor(
                headers = headers,
                onHeadersChange = { headers = it }
            )
            
            // Body
            Text(
                text = "Body:",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF666666)
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text(if (clientType == "SOAP") "<soap:Envelope>...</soap:Envelope>" else "{ \"key\": \"value\" }") }
            )
            
            // Error
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336)
                )
            }
            
            // Botón enviar
            Button(
                onClick = {
                    // Validación
                    if (url.isBlank()) {
                        errorMessage = "La URL es obligatoria"
                        return@Button
                    }
                    
                    errorMessage = null
                    
                    // Simular respuesta (modo simulado)
                    val simulatedResponse = HttpResponse(
                        status = 200,
                        body = "{ \"message\": \"Respuesta simulada (modo simulado activo)\" }",
                        headers = mapOf("Content-Type" to "application/json")
                    )
                    
                    response = simulatedResponse
                    
                    // Guardar en historial
                    val useCases = RestSoapUseCases()
                    val result = useCases.addRequestToHistory(
                        workspace = workspace,
                        projectId = projectId,
                        type = clientType,
                        method = method,
                        url = url,
                        headers = headers.toMap(),
                        body = body.ifBlank { null },
                        response = simulatedResponse
                    )
                    
                    result.onSuccess { updatedWorkspace ->
                        onWorkspaceUpdate(updatedWorkspace)
                        // Obtener el ID de la última request añadida
                        val lastRequest = updatedWorkspace.projects
                            .find { it.id == projectId }
                            ?.tools?.restSoap?.clientHistory?.lastOrNull()
                        selectedHistoryId = lastRequest?.id
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar (Modo Simulado)")
            }
            
            // Respuesta
            if (response != null) {
                Divider()
                Text(
                    text = "Respuesta:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                com.kodeforge.ui.components.ResponseViewer(
                    response = response
                )
            }
        }
    }
}

/**
 * Tab de capturas del mock server.
 */
@Composable
private fun CapturesTab(
    workspace: Workspace,
    projectId: String,
    restSoapTool: RestSoapTool,
    onWorkspaceUpdate: (Workspace) -> Unit
) {
    var selectedCaptureId by remember { mutableStateOf<String?>(null) }
    var filterMethod by remember { mutableStateOf("") }
    var filterPath by remember { mutableStateOf("") }
    
    val capturedRequests = restSoapTool.mockServer?.capturedRequests ?: emptyList()
    
    val filteredCaptures = capturedRequests.filter { capture ->
        (filterMethod.isBlank() || capture.method.contains(filterMethod, ignoreCase = true)) &&
        (filterPath.isBlank() || capture.path.contains(filterPath, ignoreCase = true))
    }
    
    val selectedCapture = filteredCaptures.find { it.id == selectedCaptureId }
    
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Lista de capturas (izquierda)
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
                    text = "Capturas (${filteredCaptures.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                if (capturedRequests.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val useCases = RestSoapUseCases()
                            val result = useCases.clearCapturedRequests(workspace, projectId)
                            result.onSuccess {
                                onWorkspaceUpdate(it)
                                selectedCaptureId = null
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar capturas",
                            tint = Color(0xFFF44336)
                        )
                    }
                }
            }
            
            // Filtros
            OutlinedTextField(
                value = filterMethod,
                onValueChange = { filterMethod = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Filtrar por método") },
                placeholder = { Text("GET, POST...") }
            )
            
            OutlinedTextField(
                value = filterPath,
                onValueChange = { filterPath = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Filtrar por path") },
                placeholder = { Text("/api/...") }
            )
            
            // Lista
            if (filteredCaptures.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F7FA)
                    )
                ) {
                    Box(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = if (capturedRequests.isEmpty()) {
                                "No hay capturas.\nInicia el mock server y envía requests."
                            } else {
                                "No hay capturas que coincidan con los filtros."
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
                    items(filteredCaptures.reversed()) { capture ->
                        com.kodeforge.ui.components.CapturedRequestItem(
                            request = capture,
                            isSelected = capture.id == selectedCaptureId,
                            onClick = { selectedCaptureId = capture.id }
                        )
                    }
                }
            }
        }
        
        Divider(modifier = Modifier.width(1.dp).fillMaxHeight())
        
        // Detalle (derecha)
        com.kodeforge.ui.components.CapturedRequestDetail(
            request = selectedCapture,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        )
    }
}

/**
 * Tab de rutas del mock server.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutesTab(
    workspace: Workspace,
    projectId: String,
    restSoapTool: RestSoapTool,
    onWorkspaceUpdate: (Workspace) -> Unit
) {
    var showRouteDialog by remember { mutableStateOf(false) }
    var editingRoute by remember { mutableStateOf<MockRoute?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var routeToDelete by remember { mutableStateOf<MockRoute?>(null) }
    
    val mockServer = restSoapTool.mockServer
    val routes = mockServer?.routes ?: emptyList()
    val mode = mockServer?.mode ?: "catchAll"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con modo y botón
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Rutas del Mock Server",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                Text(
                    text = "Modo actual: ${if (mode == "defined") "Defined (usa rutas)" else "Catch All (captura todo)"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Selector de modo
                FilterChip(
                    selected = mode == "catchAll",
                    onClick = {
                        val useCases = RestSoapUseCases()
                        // Inicializar mockServer si no existe
                        val initResult = if (mockServer == null) {
                            useCases.enableMockServer(workspace, projectId)
                        } else {
                            Result.success(workspace)
                        }
                        initResult.onSuccess { ws ->
                            val result = useCases.setMockServerMode(ws, projectId, "catchAll")
                            result.onSuccess { onWorkspaceUpdate(it) }
                        }
                    },
                    label = { Text("Catch All") }
                )
                
                FilterChip(
                    selected = mode == "defined",
                    onClick = {
                        val useCases = RestSoapUseCases()
                        // Inicializar mockServer si no existe
                        val initResult = if (mockServer == null) {
                            useCases.enableMockServer(workspace, projectId)
                        } else {
                            Result.success(workspace)
                        }
                        initResult.onSuccess { ws ->
                            val result = useCases.setMockServerMode(ws, projectId, "defined")
                            result.onSuccess { onWorkspaceUpdate(it) }
                        }
                    },
                    label = { Text("Defined") }
                )
                
                // Botón nueva ruta
                Button(
                    onClick = {
                        editingRoute = null
                        showRouteDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nueva ruta",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nueva Ruta")
                }
            }
        }
        
        // Lista de rutas
        if (routes.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F7FA)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay rutas definidas.\nCrea una ruta para empezar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(routes) { route ->
                    com.kodeforge.ui.components.MockRouteItem(
                        route = route,
                        onClick = { /* Expandir/colapsar si se desea */ },
                        onEdit = {
                            editingRoute = route
                            showRouteDialog = true
                        },
                        onDelete = {
                            routeToDelete = route
                            showDeleteConfirm = true
                        }
                    )
                }
            }
        }
    }
    
    // Diálogo crear/editar ruta
    if (showRouteDialog) {
        com.kodeforge.ui.components.MockRouteDialog(
            route = editingRoute,
            onDismiss = { showRouteDialog = false },
            onSave = { route ->
                val useCases = RestSoapUseCases()
                
                // Inicializar mockServer si no existe
                val initResult = if (mockServer == null) {
                    useCases.enableMockServer(workspace, projectId)
                } else {
                    Result.success(workspace)
                }
                
                initResult.onSuccess { ws ->
                    val result = if (editingRoute == null) {
                        useCases.addRoute(
                            workspace = ws,
                            projectId = projectId,
                            method = route.method,
                            path = route.path,
                            responseStatus = route.response.status,
                            responseBody = route.response.body,
                            responseHeaders = route.response.headers
                        )
                    } else {
                        useCases.updateRoute(
                            workspace = ws,
                            projectId = projectId,
                            routeId = route.id,
                            method = route.method,
                            path = route.path,
                            responseStatus = route.response.status,
                            responseBody = route.response.body,
                            responseHeaders = route.response.headers
                        )
                    }
                    result.onSuccess { updatedWs ->
                        onWorkspaceUpdate(updatedWs)
                        showRouteDialog = false
                    }
                }
            }
        )
    }
    
    // Confirmación eliminar
    if (showDeleteConfirm && routeToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar Ruta") },
            text = { Text("¿Estás seguro de que quieres eliminar la ruta ${routeToDelete!!.method} ${routeToDelete!!.path}?") },
            confirmButton = {
                Button(
                    onClick = {
                        val useCases = RestSoapUseCases()
                        val result = useCases.deleteRoute(workspace, projectId, routeToDelete!!.id)
                        result.onSuccess { updatedWs ->
                            onWorkspaceUpdate(updatedWs)
                            showDeleteConfirm = false
                            routeToDelete = null
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
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


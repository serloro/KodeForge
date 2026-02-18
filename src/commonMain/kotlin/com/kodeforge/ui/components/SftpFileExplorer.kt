package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.SftpConnection
import com.kodeforge.sftp.RemoteFile
import com.kodeforge.sftp.SftpClient
import com.kodeforge.sftp.SftpConnectionState
import com.kodeforge.sftp.SftpResult
import com.kodeforge.platform.PlatformPaths
import kotlinx.coroutines.launch

/**
 * Explorador de archivos SFTP.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SftpFileExplorer(
    connection: SftpConnection,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val client = remember { SftpClient() }
    
    var connectionState by remember { mutableStateOf(SftpConnectionState.DISCONNECTED) }
    var currentPath by remember { mutableStateOf(".") }
    val backStack = remember { mutableStateListOf<String>() }
    val forwardStack = remember { mutableStateListOf<String>() }
    var files by remember { mutableStateOf<List<RemoteFile>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var selectedFileContent by remember { mutableStateOf<String?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFile by remember { mutableStateOf<RemoteFile?>(null) }

    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Función para conectar
    fun connectToServer(pwd: String) {
        scope.launch {
            isLoading = true
            errorMessage = null
            
            val result = client.connect(connection, pwd)
            
            when (result) {
                is SftpResult.Success -> {
                    connectionState = SftpConnectionState.CONNECTED
                    // Listar archivos iniciales
                    val listResult = client.listFiles(currentPath)
                    when (listResult) {
                        is SftpResult.Success -> {
                            files = listResult.data
                        }
                        is SftpResult.Error -> {
                            errorMessage = listResult.message
                        }
                    }
                }
                is SftpResult.Error -> {
                    connectionState = SftpConnectionState.ERROR
                    errorMessage = result.message
                }
            }
            
            isLoading = false
        }
    }
    
    // Función para listar archivos (sin tocar histórico)
    fun listFiles(path: String) {
        scope.launch {
            isLoading = true
            errorMessage = null
            selectedFileContent = null
            selectedFileName = null
            selectedFile = null
            
            val result = client.listFiles(path)
            
            when (result) {
                is SftpResult.Success -> {
                    files = result.data
                    currentPath = path
                }
                is SftpResult.Error -> {
                    errorMessage = result.message
                }
            }
            
            isLoading = false
        }
    }

    fun navigateTo(path: String) {
        if (path == currentPath) return
        backStack.add(currentPath)
        forwardStack.clear()
        listFiles(path)
    }

    fun navigateBack() {
        if (backStack.isEmpty()) return
        val prev = backStack.removeAt(backStack.lastIndex)
        forwardStack.add(currentPath)
        listFiles(prev)
    }

    fun navigateForward() {
        if (forwardStack.isEmpty()) return
        val next = forwardStack.removeAt(forwardStack.lastIndex)
        backStack.add(currentPath)
        listFiles(next)
    }
    
    // Función para leer archivo
    fun readFile(file: RemoteFile) {
        scope.launch {
            isLoading = true
            errorMessage = null
            
            val result = client.readFileAsText(file.path)
            
            when (result) {
                is SftpResult.Success -> {
                    selectedFileContent = result.data
                    selectedFileName = file.name
                }
                is SftpResult.Error -> {
                    errorMessage = result.message
                }
            }
            
            isLoading = false
        }
    }

    fun downloadFile(file: RemoteFile) {
        scope.launch {
            isLoading = true
            errorMessage = null
            val downloadsDir = PlatformPaths.downloadsDir()
            // Nombre único simple (evita sobreescritura sin depender de APIs JVM en commonMain)
            val base = file.name.ifEmpty { "download" }
            val dot = base.lastIndexOf('.')
            val name = if (dot > 0) base.substring(0, dot) else base
            val ext = if (dot > 0) base.substring(dot) else ""
            val suffix = kotlin.random.Random.nextInt(1000, 9999)
            val target = "$downloadsDir/${name}-$suffix$ext"

            val result = client.downloadFile(file.path, target)
            isLoading = false

            when (result) {
                is SftpResult.Success -> snackbarHostState.showSnackbar("Descargado en: $target")
                is SftpResult.Error -> errorMessage = result.message
            }
        }
    }
    
    // Función para navegar hacia atrás
    fun navigateUp() {
        if (currentPath != "." && currentPath.isNotEmpty()) {
            val parentPath = if (currentPath.contains("/")) {
                currentPath.substringBeforeLast("/")
            } else {
                "."
            }
            navigateTo(parentPath)
        }
    }
    
    // Desconectar al salir
    DisposableEffect(Unit) {
        onDispose {
            client.disconnect()
        }
    }
    
    // Mostrar diálogo de password si es necesario
    LaunchedEffect(Unit) {
        if (connection.auth.type == "password") {
            showPasswordDialog = true
        } else if (connection.auth.type == "none") {
            connectToServer("")
        } else {
            errorMessage = "Tipo de autenticación '${connection.auth.type}' no soportado en este momento"
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    // Línea 1: conexión + cerrar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = connection.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${connection.username}@${connection.host}:${connection.port}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                client.disconnect()
                                onClose()
                            }
                        ) {
                            Text("✕")
                        }
                    }

                    // Línea 2: navegación + acciones + breadcrumb
                    if (connectionState == SftpConnectionState.CONNECTED) {
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(onClick = { navigateBack() }, enabled = backStack.isNotEmpty()) {
                                Text("←")
                            }
                            IconButton(onClick = { navigateForward() }, enabled = forwardStack.isNotEmpty()) {
                                Text("→")
                            }
                            IconButton(onClick = { navigateUp() }, enabled = currentPath != "." && currentPath.isNotEmpty()) {
                                Text("↑")
                            }

                            IconButton(onClick = { listFiles(currentPath) }) {
                                Text("⟳")
                            }

                            IconButton(
                                onClick = {
                                    selectedFile?.let { if (!it.isDirectory) downloadFile(it) }
                                },
                                enabled = selectedFile != null && selectedFile?.isDirectory == false
                            ) {
                                Text("⬇")
                            }

                            IconButton(
                                onClick = {
                                    selectedFile?.let { clipboard.setText(AnnotatedString(it.path)) }
                                    scope.launch { snackbarHostState.showSnackbar("Ruta copiada") }
                                },
                                enabled = selectedFile != null
                            ) {
                                Text("⧉")
                            }

                            Spacer(Modifier.width(8.dp))

                            PathBreadcrumb(
                                path = currentPath,
                                onNavigateTo = { navigateTo(it) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        // Contenido
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                // Mostrando contenido de archivo
                selectedFileContent != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header del preview
                        Surface(
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedFileName ?: "Archivo",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF212121)
                                )
                                
                                TextButton(
                                    onClick = {
                                        selectedFileContent = null
                                        selectedFileName = null
                                    }
                                ) {
                                    Text("Cerrar")
                                }
                            }
                        }
                        
                        // Contenido del archivo
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = selectedFileContent!!,
                                fontSize = 13.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = Color(0xFF212121),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
                
                // Mostrando error
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                errorMessage = null
                                if (connectionState == SftpConnectionState.CONNECTED) {
                                    listFiles(currentPath)
                                } else {
                                    showPasswordDialog = true
                                }
                            }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
                
                // Cargando
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                // Lista de archivos
                connectionState == SftpConnectionState.CONNECTED && files.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Important: provide a stable key so remembered state inside each row
                        // (e.g., context menu expanded) doesn't get mismatched across recompositions.
                        items(items = files, key = { it.path }) { file ->
                            RemoteFileItem(
                                file = file,
                                selected = selectedFile?.path == file.path,
                                onSelect = { selectedFile = file },
                                onOpen = {
                                    selectedFile = file
                                    if (file.isDirectory) {
                                        navigateTo(file.path)
                                    } else {
                                        readFile(file)
                                    }
                                },
                                onDownload = if (!file.isDirectory) ({ downloadFile(file) }) else null,
                                onCopyPath = { clipboard.setText(AnnotatedString(file.path)) }
                            )
                        }
                    }
                }
                
                // Directorio vacío
                connectionState == SftpConnectionState.CONNECTED && files.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Directorio vacío",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo de password
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Autenticación SFTP",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Ingrese la contraseña para ${connection.username}@${connection.host}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPasswordDialog = false
                        connectToServer(password)
                    },
                    enabled = password.isNotEmpty()
                ) {
                    Text("Conectar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showPasswordDialog = false
                        onClose()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PathBreadcrumb(
    path: String,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cleaned = path.trim().trimEnd('/')
    val parts: List<String> = when {
        cleaned.isEmpty() || cleaned == "." -> emptyList()
        cleaned == "~" -> emptyList()
        else -> cleaned.split('/').filter { it.isNotBlank() }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TextButton(
            onClick = { onNavigateTo(".") },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("~", fontSize = 12.sp)
        }

        var acc = ""
        parts.forEachIndexed { index, part ->
            Text("/", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            acc = if (acc.isEmpty()) part else "$acc/$part"
            TextButton(
                onClick = { onNavigateTo(acc) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(part, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}


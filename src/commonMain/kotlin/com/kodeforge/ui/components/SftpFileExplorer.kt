package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.SftpConnection
import com.kodeforge.sftp.RemoteFile
import com.kodeforge.sftp.SftpClient
import com.kodeforge.sftp.SftpConnectionState
import com.kodeforge.sftp.SftpResult
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
    var files by remember { mutableStateOf<List<RemoteFile>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var selectedFileContent by remember { mutableStateOf<String?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    
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
    
    // Función para listar archivos
    fun listFiles(path: String) {
        scope.launch {
            isLoading = true
            errorMessage = null
            selectedFileContent = null
            selectedFileName = null
            
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
    
    // Función para navegar hacia atrás
    fun navigateUp() {
        if (currentPath != "." && currentPath.isNotEmpty()) {
            val parentPath = if (currentPath.contains("/")) {
                currentPath.substringBeforeLast("/")
            } else {
                "."
            }
            listFiles(parentPath)
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
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = Color(0xFF1976D2),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = connection.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${connection.username}@${connection.host}:${connection.port}",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                IconButton(
                    onClick = {
                        client.disconnect()
                        onClose()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }
        }
        
        // Barra de navegación
        if (connectionState == SftpConnectionState.CONNECTED) {
            Surface(
                color = Color(0xFFF5F5F5),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navigateUp() },
                        enabled = currentPath != "." && currentPath.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                    
                    Text(
                        text = if (currentPath == ".") "~" else currentPath,
                        fontSize = 14.sp,
                        color = Color(0xFF212121),
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(
                        onClick = { listFiles(currentPath) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar"
                        )
                    }
                }
            }
        }
        
        // Contenido
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
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
                        items(files) { file ->
                            RemoteFileItem(
                                file = file,
                                onClick = {
                                    if (file.isDirectory) {
                                        listFiles(file.path)
                                    } else {
                                        readFile(file)
                                    }
                                }
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


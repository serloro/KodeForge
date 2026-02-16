package com.kodeforge.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.kodeforge.data.repository.JvmFileSystemAdapter
import com.kodeforge.data.repository.WorkspaceRepository
import com.kodeforge.domain.model.Workspace
import com.kodeforge.smtp.SmtpServerManager
import com.kodeforge.ui.screens.HomeScreen
import com.kodeforge.ui.theme.KodeForgeTheme
import kotlinx.coroutines.launch

/**
 * Punto de entrada de la aplicación KodeForge (Compose Desktop).
 * 
 * T1: Implementa UI básica con sidebar.
 * Cargar workspace desde workspace.json (o specs/data-schema.json si no existe).
 */
fun main() = application {
    val windowState = rememberWindowState(width = 1400.dp, height = 900.dp)
    
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "KodeForge"
    ) {
        KodeForgeApp()
    }
}

@Composable
fun KodeForgeApp() {
    val repository = remember { WorkspaceRepository(JvmFileSystemAdapter()) }
    val smtpServerManager = remember { SmtpServerManager() }
    var workspace by remember { mutableStateOf<Workspace?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Detener todos los servidores al cerrar la aplicación
    DisposableEffect(Unit) {
        onDispose {
            smtpServerManager.stopAllServers()
        }
    }
    
    // Cargar workspace al iniciar
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // Intentar cargar workspace.json, si no existe cargar el schema inicial
                workspace = try {
                    repository.load("workspace.json")
                } catch (e: Exception) {
                    println("workspace.json no encontrado, cargando schema inicial...")
                    repository.loadInitialSchema("specs/data-schema.json")
                }
            } catch (e: Exception) {
                error = "Error al cargar workspace: ${e.message}"
                e.printStackTrace()
            }
        }
    }
    
    KodeForgeTheme {
        when {
            error != null -> {
                // Pantalla de error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: $error",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            workspace == null -> {
                // Pantalla de carga
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                // Pantalla principal
                HomeScreen(
                    workspace = workspace!!,
                    smtpServerManager = smtpServerManager,
                    onWorkspaceUpdate = { updatedWorkspace ->
                        workspace = updatedWorkspace
                        // TODO: Auto-guardar cambios
                        scope.launch {
                            try {
                                repository.save("workspace.json", updatedWorkspace)
                            } catch (e: Exception) {
                                println("Error al guardar workspace: ${e.message}")
                            }
                        }
                    }
                )
            }
        }
    }
}


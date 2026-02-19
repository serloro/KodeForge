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
import com.kodeforge.ui.error.AppErrorReporter
import com.kodeforge.ui.error.GlobalErrorHost
import com.kodeforge.ui.screens.HomeScreen
import com.kodeforge.ui.theme.KodeForgeTheme
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Punto de entrada de la aplicación KodeForge (Compose Desktop).
 * 
 * T1: Implementa UI básica con sidebar.
 * Cargar workspace desde workspace.json (o specs/data-schema.json si no existe).
 */
fun main() = application {
    // Captura excepciones no controladas (incluyendo AWT-EventQueue) para evitar que la app "casque".
    Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
        AppErrorReporter.report(throwable, context = "Excepción no controlada")
    }

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

    // Scope robusto: cualquier excepción en corutinas se reporta a UI.
    val coroutineExceptionHandler = remember {
        CoroutineExceptionHandler { _, throwable ->
            AppErrorReporter.report(throwable, context = "Error en segundo plano")
        }
    }

    val scope = remember {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + coroutineExceptionHandler)
    }
    
    // Detener todos los servidores al cerrar la aplicación
    DisposableEffect(Unit) {
        onDispose {
            smtpServerManager.stopAllServers()
        }
    }
    
    // Cargar workspace al iniciar
    LaunchedEffect(Unit) {
        scope.launch {
            runCatching {
                // Intentar cargar workspace.json
                repository.load("workspace.json")
            }.recoverCatching {
                println("workspace.json no encontrado o inválido, creando workspace inicial...")
                repository.createDefaultWorkspace()
            }.onSuccess {
                workspace = it
            }.onFailure {
                error = "Error al cargar workspace"
                AppErrorReporter.report(it, context = "Carga de workspace")
            }
        }
    }
    
    KodeForgeTheme {
        GlobalErrorHost {
            when {
                    error != null -> {
                        // Pantalla de error "suave" (la app sigue viva)
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = error!!,
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
                                scope.launch {
                                    runCatching {
                                        repository.save("workspace.json", updatedWorkspace)
                                    }.onFailure {
                                        AppErrorReporter.report(it, context = "Guardado de workspace")
                                    }
                                }
                            }
                        )
                    }
                }
        }
    }
}


package com.kodeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.InfoPage
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.InfoUseCases
import com.kodeforge.ui.components.*
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Pantalla de la herramienta Info.
 * 
 * Layout de 2 columnas:
 * - Izquierda: Lista de páginas con acciones
 * - Derecha: Visor HTML con selector de idioma
 * 
 * @param workspace Workspace actual
 * @param project Proyecto actual
 * @param onWorkspaceUpdate Callback para actualizar workspace
 * @param onBack Callback para volver atrás
 * @param onToolClick Callback para cambiar de herramienta
 * @param onBackToHub Callback para volver al hub del proyecto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoToolScreen(
    workspace: Workspace,
    project: Project,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit,
    onToolClick: (String) -> Unit = {},
    onBackToHub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infoUseCases = remember { InfoUseCases() }
    
    // Obtener páginas
    val pages = remember(workspace, project.id) {
        infoUseCases.getPages(workspace, project.id)
    }
    
    // Estado
    var selectedPageId by remember(pages) { 
        mutableStateOf(pages.firstOrNull()?.id) 
    }
    var selectedLocale by remember { mutableStateOf("es") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var pageToRename by remember { mutableStateOf<InfoPage?>(null) }
    var pageToDelete by remember { mutableStateOf<InfoPage?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    ToolLayout(
        project = project,
        toolTitle = "Info - Documentación",
        selectedToolId = "info",
        onBack = onBack,
        onToolClick = onToolClick,
        onBackToHub = onBackToHub,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(KodeForgeColors.Background)
        ) {
        
        // Mensaje de error
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = error,
                        color = Color(0xFFF44336),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { errorMessage = null }) {
                        Text("Cerrar")
                    }
                }
            }
        }
        
        // Contenido: 2 columnas
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Panel izquierdo: Lista de páginas
            InfoPageList(
                pages = pages,
                selectedPageId = selectedPageId,
                onPageSelect = { selectedPageId = it },
                onCreatePage = { showCreateDialog = true },
                onRenamePage = { pageToRename = it },
                onDeletePage = { pageToDelete = it },
                onMoveUp = { page ->
                    val currentIndex = pages.indexOf(page)
                    if (currentIndex > 0) {
                        val newOrder = pages.map { it.id }
                            .toMutableList()
                            .apply {
                                val temp = this[currentIndex]
                                this[currentIndex] = this[currentIndex - 1]
                                this[currentIndex - 1] = temp
                            }
                        
                        val result = infoUseCases.reorderPages(workspace, project.id, newOrder)
                        if (result.isSuccess) {
                            onWorkspaceUpdate(result.getOrNull()!!)
                        } else {
                            errorMessage = result.exceptionOrNull()?.message
                        }
                    }
                },
                onMoveDown = { page ->
                    val currentIndex = pages.indexOf(page)
                    if (currentIndex < pages.size - 1) {
                        val newOrder = pages.map { it.id }
                            .toMutableList()
                            .apply {
                                val temp = this[currentIndex]
                                this[currentIndex] = this[currentIndex + 1]
                                this[currentIndex + 1] = temp
                            }
                        
                        val result = infoUseCases.reorderPages(workspace, project.id, newOrder)
                        if (result.isSuccess) {
                            onWorkspaceUpdate(result.getOrNull()!!)
                        } else {
                            errorMessage = result.exceptionOrNull()?.message
                        }
                    }
                }
            )
            
            // Panel derecho: Visor HTML
            InfoPageViewer(
                page = pages.find { it.id == selectedPageId },
                selectedLocale = selectedLocale,
                onLocaleChange = { selectedLocale = it },
                onCopyTranslation = { pageId, fromLocale ->
                    val page = pages.find { it.id == pageId }
                    if (page != null) {
                        val sourceTranslation = page.translations[fromLocale]
                        if (sourceTranslation != null) {
                            val targetLocale = if (fromLocale == "es") "en" else "es"
                            
                            val result = infoUseCases.updatePage(
                                workspace = workspace,
                                projectId = project.id,
                                pageId = pageId,
                                htmlEs = if (targetLocale == "es") sourceTranslation.html else null,
                                htmlEn = if (targetLocale == "en") sourceTranslation.html else null
                            )
                            
                            if (result.isSuccess) {
                                onWorkspaceUpdate(result.getOrNull()!!)
                            } else {
                                errorMessage = result.exceptionOrNull()?.message
                            }
                        }
                    }
                },
                onSaveHtml = { pageId, locale, newHtml ->
                    val result = infoUseCases.updatePage(
                        workspace = workspace,
                        projectId = project.id,
                        pageId = pageId,
                        htmlEs = if (locale == "es") newHtml else null,
                        htmlEn = if (locale == "en") newHtml else null
                    )
                    
                    if (result.isSuccess) {
                        onWorkspaceUpdate(result.getOrNull()!!)
                    } else {
                        errorMessage = result.exceptionOrNull()?.message
                    }
                }
            )
        }
    }
    
    // Diálogo: Crear página
    if (showCreateDialog) {
        CreatePageDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { titleEs, titleEn ->
                val result = infoUseCases.createPage(
                    workspace = workspace,
                    projectId = project.id,
                    slug = "", // se generará automáticamente a partir del título
                    titleEs = titleEs,
                    titleEn = titleEn,
                    htmlEs = "<h1>$titleEs</h1><p>Contenido de la página...</p>",
                    htmlEn = "<h1>$titleEn</h1><p>Page content...</p>"
                )
                
                if (result.isSuccess) {
                    onWorkspaceUpdate(result.getOrNull()!!)
                    showCreateDialog = false
                    
                    // Seleccionar la nueva página
                    val updatedPages = infoUseCases.getPages(result.getOrNull()!!, project.id)
                    selectedPageId = updatedPages.lastOrNull()?.id
                } else {
                    errorMessage = result.exceptionOrNull()?.message
                    showCreateDialog = false
                }
            }
        )
    }
    
    // Diálogo: Renombrar página
    pageToRename?.let { page ->
        RenamePageDialog(
            page = page,
            onDismiss = { pageToRename = null },
            onConfirm = { slug, titleEs, titleEn ->
                val result = infoUseCases.updatePage(
                    workspace = workspace,
                    projectId = project.id,
                    pageId = page.id,
                    slug = slug,
                    titleEs = titleEs,
                    titleEn = titleEn
                )
                
                if (result.isSuccess) {
                    onWorkspaceUpdate(result.getOrNull()!!)
                    pageToRename = null
                } else {
                    errorMessage = result.exceptionOrNull()?.message
                    pageToRename = null
                }
            }
        )
    }
    
    // Diálogo: Eliminar página
    pageToDelete?.let { page ->
        DeletePageDialog(
            page = page,
            onDismiss = { pageToDelete = null },
            onConfirm = {
                val result = infoUseCases.deletePage(
                    workspace = workspace,
                    projectId = project.id,
                    pageId = page.id
                )
                
                if (result.isSuccess) {
                    onWorkspaceUpdate(result.getOrNull()!!)
                    
                    // Si se eliminó la página seleccionada, seleccionar otra
                    if (selectedPageId == page.id) {
                        val updatedPages = infoUseCases.getPages(result.getOrNull()!!, project.id)
                        selectedPageId = updatedPages.firstOrNull()?.id
                    }
                    
                    pageToDelete = null
                } else {
                    errorMessage = result.exceptionOrNull()?.message
                    pageToDelete = null
                }
            }
        )
        }
    }
}


package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.InfoPage

/**
 * Lista de páginas Info con scroll.
 * 
 * Muestra:
 * - Botón "Nueva página"
 * - Lista de páginas con acciones
 * 
 * @param pages Lista de páginas
 * @param selectedPageId ID de página seleccionada
 * @param onPageSelect Callback al seleccionar página
 * @param onCreatePage Callback al crear página
 * @param onRenamePage Callback al renombrar página
 * @param onDeletePage Callback al eliminar página
 * @param onMoveUp Callback al mover arriba
 * @param onMoveDown Callback al mover abajo
 */
@Composable
fun InfoPageList(
    pages: List<InfoPage>,
    selectedPageId: String?,
    onPageSelect: (String) -> Unit,
    onCreatePage: () -> Unit,
    onRenamePage: (InfoPage) -> Unit,
    onDeletePage: (InfoPage) -> Unit,
    onMoveUp: (InfoPage) -> Unit,
    onMoveDown: (InfoPage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Páginas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Botón crear
        Button(
            onClick = onCreatePage,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Nueva página")
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Lista con scroll
        if (pages.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "No hay páginas.\nCrea la primera página para empezar.",
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
                items(pages) { page ->
                    val pageIndex = pages.indexOf(page)
                    
                    InfoPageListItem(
                        page = page,
                        isSelected = page.id == selectedPageId,
                        canMoveUp = pageIndex > 0,
                        canMoveDown = pageIndex < pages.size - 1,
                        onSelect = { onPageSelect(page.id) },
                        onRename = { onRenamePage(page) },
                        onDelete = { onDeletePage(page) },
                        onMoveUp = { onMoveUp(page) },
                        onMoveDown = { onMoveDown(page) }
                    )
                }
            }
        }
    }
}


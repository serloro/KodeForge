package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Editor WYSIWYG MVP para HTML.
 * 
 * Modos:
 * - Leer: Muestra preview del HTML
 * - Editar: Editor de texto + toolbar + preview en vivo
 * 
 * Toolbar:
 * - Bold, Italic, H1, H2, List, Link
 * 
 * Persistencia:
 * - Inmediata al hacer clic en "Guardar"
 * 
 * TODO: Evolucionar a WYSIWYG real cuando esté disponible:
 * - Opción A: WebView + TinyMCE/CKEditor (cuando Compose Desktop soporte WebView)
 * - Opción B: Librería Compose-native (cuando exista)
 * - Opción C: Implementación custom con RichTextCanvas
 * 
 * @param html Contenido HTML actual
 * @param onSave Callback al guardar (nuevo HTML)
 */
@Composable
fun HtmlEditor(
    html: String,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditMode by remember { mutableStateOf(false) }
    var editedHtml by remember(html, isEditMode) { mutableStateOf(html) }
    var showLinkDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Botones de acción
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditMode) {
                // Modo editar: Guardar + Cancelar
                TextButton(onClick = {
                    editedHtml = html
                    isEditMode = false
                }) {
                    Text("Cancelar")
                }
                
                Spacer(Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        onSave(editedHtml)
                        isEditMode = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Guardar")
                }
            } else {
                // Modo leer: Editar
                Button(
                    onClick = { isEditMode = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Editar")
                }
            }
        }
        
        if (isEditMode) {
            // Modo editar: Toolbar + Editor + Preview
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Toolbar
                HtmlEditorToolbar(
                    onBoldClick = {
                        editedHtml = insertTag(editedHtml, "<strong>", "</strong>", "texto")
                    },
                    onItalicClick = {
                        editedHtml = insertTag(editedHtml, "<em>", "</em>", "texto")
                    },
                    onH1Click = {
                        editedHtml = insertTag(editedHtml, "<h1>", "</h1>", "Título")
                    },
                    onH2Click = {
                        editedHtml = insertTag(editedHtml, "<h2>", "</h2>", "Subtítulo")
                    },
                    onListClick = {
                        editedHtml = insertTag(editedHtml, "<ul>\n<li>", "</li>\n</ul>", "Item")
                    },
                    onLinkClick = {
                        showLinkDialog = true
                    }
                )
                
                // Editor + Preview (2 columnas)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Editor de texto
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFAFAFA)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Editor HTML",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF666666),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            val scrollState = rememberScrollState()
                            
                            BasicTextField(
                                value = editedHtml,
                                onValueChange = { editedHtml = it },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White)
                                    .padding(12.dp)
                                    .verticalScroll(scrollState),
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1A1A1A)
                                )
                            )
                        }
                    }
                    
                    // Preview en vivo
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFAFAFA)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Preview",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF666666),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            HtmlPreview(html = editedHtml)
                        }
                    }
                }
            }
        } else {
            // Modo leer: Solo preview
            HtmlPreview(html = html)
        }
    }
    
    // Diálogo: Insertar enlace
    if (showLinkDialog) {
        InsertLinkDialog(
            onDismiss = { showLinkDialog = false },
            onConfirm = { url, text ->
                editedHtml = insertTag(editedHtml, "<a href=\"$url\">", "</a>", text)
                showLinkDialog = false
            }
        )
    }
}

/**
 * Inserta un tag HTML en el texto.
 * 
 * Si hay selección, envuelve la selección.
 * Si no hay selección, inserta con contenido por defecto.
 * 
 * @param currentText Texto actual
 * @param openTag Tag de apertura (ej: "<strong>")
 * @param closeTag Tag de cierre (ej: "</strong>")
 * @param defaultContent Contenido por defecto si no hay selección
 * @return Nuevo texto con el tag insertado
 */
private fun insertTag(
    currentText: String,
    openTag: String,
    closeTag: String,
    defaultContent: String
): String {
    // Por simplicidad, insertar al final
    // TODO: Mejorar para insertar en posición del cursor cuando sea posible
    return if (currentText.endsWith("\n")) {
        "$currentText$openTag$defaultContent$closeTag\n"
    } else {
        "$currentText\n$openTag$defaultContent$closeTag\n"
    }
}


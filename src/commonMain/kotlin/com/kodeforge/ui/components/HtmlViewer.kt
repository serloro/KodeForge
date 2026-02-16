package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Visor HTML simple (solo lectura).
 * 
 * Renderiza HTML de forma básica eliminando tags.
 * 
 * TODO: Mejorar con librería de renderizado HTML completo.
 * 
 * @param html Contenido HTML a mostrar
 */
@Composable
fun HtmlViewer(
    html: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Renderizado básico: eliminar tags HTML y mostrar texto
            val plainText = html
                .replace("<br>", "\n")
                .replace("<br/>", "\n")
                .replace("<br />", "\n")
                .replace("</p>", "\n\n")
                .replace("</h1>", "\n\n")
                .replace("</h2>", "\n\n")
                .replace("</h3>", "\n\n")
                .replace("</li>", "\n")
                .replace("<[^>]*>".toRegex(), "")
                .trim()
            
            Text(
                text = plainText,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF1A1A1A)
            )
        }
    }
}


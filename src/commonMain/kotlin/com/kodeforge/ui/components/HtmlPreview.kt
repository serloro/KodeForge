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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Preview en vivo de HTML.
 * 
 * Renderiza HTML de forma básica mientras se edita.
 * 
 * @param html Contenido HTML a previsualizar
 */
@Composable
fun HtmlPreview(
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Parsear y renderizar HTML básico
            renderHtmlContent(html)
        }
    }
}

/**
 * Renderiza contenido HTML de forma básica.
 * 
 * Soporta:
 * - <h1>, <h2>, <h3>
 * - <p>
 * - <strong>, <b>
 * - <em>, <i>
 * - <ul>, <li>
 * - <a href="">
 */
@Composable
private fun ColumnScope.renderHtmlContent(html: String) {
    if (html.isBlank()) {
        Text(
            text = "(contenido vacío)",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCCCCCC),
            fontStyle = FontStyle.Italic
        )
        return
    }
    
    // Parseo simple de HTML
    val lines = html
        .replace("<br>", "\n")
        .replace("<br/>", "\n")
        .replace("<br />", "\n")
        .split("\n")
    
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue
        
        when {
            // Headings
            trimmed.startsWith("<h1>") -> {
                val text = trimmed.removePrefix("<h1>").removeSuffix("</h1>")
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            trimmed.startsWith("<h2>") -> {
                val text = trimmed.removePrefix("<h2>").removeSuffix("</h2>")
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            trimmed.startsWith("<h3>") -> {
                val text = trimmed.removePrefix("<h3>").removeSuffix("</h3>")
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            
            // List items
            trimmed.startsWith("<li>") -> {
                val text = trimmed.removePrefix("<li>").removeSuffix("</li>")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("•", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = cleanHtmlTags(text),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }
            
            // Paragraph
            trimmed.startsWith("<p>") -> {
                val text = trimmed.removePrefix("<p>").removeSuffix("</p>")
                Text(
                    text = cleanHtmlTags(text),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1A1A1A)
                )
            }
            
            // Ignore tags
            trimmed.startsWith("<ul>") || trimmed.startsWith("</ul>") ||
            trimmed.startsWith("<ol>") || trimmed.startsWith("</ol>") -> {
                // Skip
            }
            
            // Plain text
            else -> {
                if (trimmed.isNotEmpty() && !trimmed.startsWith("<")) {
                    Text(
                        text = cleanHtmlTags(trimmed),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }
        }
    }
}

/**
 * Limpia tags HTML inline (strong, em, a).
 */
private fun cleanHtmlTags(text: String): String {
    return text
        .replace("<strong>", "")
        .replace("</strong>", "")
        .replace("<b>", "")
        .replace("</b>", "")
        .replace("<em>", "")
        .replace("</em>", "")
        .replace("<i>", "")
        .replace("</i>", "")
        .replace(Regex("<a[^>]*>"), "")
        .replace("</a>", "")
        .replace("<[^>]*>".toRegex(), "")
        .trim()
}


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
    
    // Parsear HTML y extraer elementos
    val elements = parseHtmlElements(html)
    
    for (element in elements) {
        when (element.tag) {
            "h1" -> {
                Text(
                    text = element.content,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            "h2" -> {
                Text(
                    text = element.content,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            "h3" -> {
                Text(
                    text = element.content,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            "p" -> {
                Text(
                    text = element.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1A1A1A)
                )
            }
            "ul" -> {
                // Renderizar lista
                element.children.forEach { li ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("•", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = li.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
            }
            "text" -> {
                if (element.content.isNotBlank()) {
                    Text(
                        text = element.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }
        }
    }
}

/**
 * Elemento HTML parseado.
 */
private data class HtmlElement(
    val tag: String,
    val content: String,
    val children: List<HtmlElement> = emptyList()
)

/**
 * Parsea HTML y extrae elementos.
 */
private fun parseHtmlElements(html: String): List<HtmlElement> {
    val elements = mutableListOf<HtmlElement>()
    var remaining = html.trim()
    
    while (remaining.isNotEmpty()) {
        when {
            // H1
            remaining.startsWith("<h1>") -> {
                val endIndex = remaining.indexOf("</h1>")
                if (endIndex != -1) {
                    val content = remaining.substring(4, endIndex)
                    elements.add(HtmlElement("h1", cleanInlineTags(content)))
                    remaining = remaining.substring(endIndex + 5).trim()
                } else {
                    break
                }
            }
            // H2
            remaining.startsWith("<h2>") -> {
                val endIndex = remaining.indexOf("</h2>")
                if (endIndex != -1) {
                    val content = remaining.substring(4, endIndex)
                    elements.add(HtmlElement("h2", cleanInlineTags(content)))
                    remaining = remaining.substring(endIndex + 5).trim()
                } else {
                    break
                }
            }
            // H3
            remaining.startsWith("<h3>") -> {
                val endIndex = remaining.indexOf("</h3>")
                if (endIndex != -1) {
                    val content = remaining.substring(4, endIndex)
                    elements.add(HtmlElement("h3", cleanInlineTags(content)))
                    remaining = remaining.substring(endIndex + 5).trim()
                } else {
                    break
                }
            }
            // P
            remaining.startsWith("<p>") -> {
                val endIndex = remaining.indexOf("</p>")
                if (endIndex != -1) {
                    val content = remaining.substring(3, endIndex)
                    elements.add(HtmlElement("p", cleanInlineTags(content)))
                    remaining = remaining.substring(endIndex + 4).trim()
                } else {
                    break
                }
            }
            // UL
            remaining.startsWith("<ul>") -> {
                val endIndex = remaining.indexOf("</ul>")
                if (endIndex != -1) {
                    val ulContent = remaining.substring(4, endIndex)
                    val listItems = parseListItems(ulContent)
                    elements.add(HtmlElement("ul", "", listItems))
                    remaining = remaining.substring(endIndex + 5).trim()
                } else {
                    break
                }
            }
            // Saltar tags desconocidos
            remaining.startsWith("<") -> {
                val endIndex = remaining.indexOf(">")
                if (endIndex != -1) {
                    remaining = remaining.substring(endIndex + 1).trim()
                } else {
                    break
                }
            }
            // Texto plano
            else -> {
                val nextTagIndex = remaining.indexOf("<")
                if (nextTagIndex != -1) {
                    val text = remaining.substring(0, nextTagIndex).trim()
                    if (text.isNotBlank()) {
                        elements.add(HtmlElement("text", text))
                    }
                    remaining = remaining.substring(nextTagIndex).trim()
                } else {
                    if (remaining.isNotBlank()) {
                        elements.add(HtmlElement("text", remaining.trim()))
                    }
                    break
                }
            }
        }
    }
    
    return elements
}

/**
 * Parsea items de lista.
 */
private fun parseListItems(ulContent: String): List<HtmlElement> {
    val items = mutableListOf<HtmlElement>()
    var remaining = ulContent.trim()
    
    while (remaining.isNotEmpty()) {
        if (remaining.startsWith("<li>")) {
            val endIndex = remaining.indexOf("</li>")
            if (endIndex != -1) {
                val content = remaining.substring(4, endIndex)
                items.add(HtmlElement("li", cleanInlineTags(content)))
                remaining = remaining.substring(endIndex + 5).trim()
            } else {
                break
            }
        } else {
            // Saltar otros caracteres
            remaining = remaining.drop(1).trim()
        }
    }
    
    return items
}

/**
 * Limpia tags HTML inline (strong, em, a).
 */
private fun cleanInlineTags(text: String): String {
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

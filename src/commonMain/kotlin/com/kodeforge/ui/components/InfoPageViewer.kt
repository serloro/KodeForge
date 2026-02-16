package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.InfoPage

/**
 * Visor de página Info con selector de idioma.
 * 
 * Muestra:
 * - Selector de idioma (es/en)
 * - Contenido HTML de la página
 * - Fallback si no existe traducción
 * 
 * @param page Página a mostrar
 * @param selectedLocale Idioma seleccionado
 * @param onLocaleChange Callback al cambiar idioma
 * @param onCopyTranslation Callback al copiar traducción
 * @param onSaveHtml Callback al guardar HTML editado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoPageViewer(
    page: InfoPage?,
    selectedLocale: String,
    onLocaleChange: (String) -> Unit,
    onCopyTranslation: (String, String) -> Unit,
    onSaveHtml: (String, String, String) -> Unit, // pageId, locale, newHtml
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
    ) {
        // Selector de idioma
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            FilterChip(
                selected = selectedLocale == "es",
                onClick = { onLocaleChange("es") },
                label = { Text("Español") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2196F3),
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedLocale == "en",
                onClick = { onLocaleChange("en") },
                label = { Text("English") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2196F3),
                    selectedLabelColor = Color.White
                )
            )
        }
        
        if (page == null) {
            // Sin página seleccionada
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selecciona una página para ver su contenido",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF666666)
                    )
                }
            }
        } else {
            val translation = page.translations[selectedLocale]
            
            if (translation != null) {
                // Editor HTML con preview
                Column(modifier = Modifier.fillMaxSize()) {
                    // Título de la página
                    Text(
                        text = page.title[selectedLocale] ?: page.slug,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Editor HTML (con modo leer/editar)
                    HtmlEditor(
                        html = translation.html,
                        onSave = { newHtml ->
                            onSaveHtml(page.id, selectedLocale, newHtml)
                        }
                    )
                }
            } else {
                // Fallback: no existe traducción
                val otherLocale = if (selectedLocale == "es") "en" else "es"
                val otherTranslation = page.translations[otherLocale]
                val localeName = if (selectedLocale == "es") "español" else "inglés"
                val otherLocaleName = if (otherLocale == "es") "español" else "inglés"
                
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.displayMedium
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text(
                            text = "No hay traducción en $localeName",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            text = "Esta página aún no tiene contenido en $localeName.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666)
                        )
                        
                        if (otherTranslation != null) {
                            Spacer(Modifier.height(24.dp))
                            
                            Button(
                                onClick = { onCopyTranslation(page.id, otherLocale) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9800)
                                )
                            ) {
                                Text("Copiar desde $otherLocaleName")
                            }
                        }
                    }
                }
            }
        }
    }
}


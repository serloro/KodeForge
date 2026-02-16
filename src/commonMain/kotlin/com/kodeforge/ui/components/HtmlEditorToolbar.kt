package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Barra de herramientas del editor HTML.
 * 
 * Botones:
 * - Bold: <strong>
 * - Italic: <em>
 * - H1: <h1>
 * - H2: <h2>
 * - List: <ul><li>
 * - Link: <a href>
 * 
 * @param onBoldClick Callback al hacer clic en Bold
 * @param onItalicClick Callback al hacer clic en Italic
 * @param onH1Click Callback al hacer clic en H1
 * @param onH2Click Callback al hacer clic en H2
 * @param onListClick Callback al hacer clic en List
 * @param onLinkClick Callback al hacer clic en Link
 */
@Composable
fun HtmlEditorToolbar(
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onH1Click: () -> Unit,
    onH2Click: () -> Unit,
    onListClick: () -> Unit,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7FA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bold
            IconButton(
                onClick = onBoldClick,
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    text = "B",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A1A)
                )
            }
            
            // Italic
            IconButton(
                onClick = onItalicClick,
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    text = "I",
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A1A)
                )
            }
            
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp),
                color = Color(0xFFCCCCCC)
            )
            
            // H1
            TextButton(
                onClick = onH1Click,
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = "H1",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A1A)
                )
            }
            
            // H2
            TextButton(
                onClick = onH2Click,
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = "H2",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A1A)
                )
            }
            
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp),
                color = Color(0xFFCCCCCC)
            )
            
            // List
            IconButton(
                onClick = onListClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Lista",
                    tint = Color(0xFF1A1A1A)
                )
            }
            
            // Link
            TextButton(
                onClick = onLinkClick,
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = "🔗",
                    fontSize = 16.sp
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            // Info
            Text(
                text = "Edita el HTML directamente o usa la barra de herramientas",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666)
            )
        }
    }
}


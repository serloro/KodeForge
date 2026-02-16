package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Person
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Item de persona en el sidebar.
 * 
 * Layout refinado según specs/p1.png:
 * - Padding vertical más generoso: 11dp
 * - Avatar: 28dp (ligeramente mayor)
 * - Punto de estado: 9dp (más visible)
 * - Font-size: 14.5sp
 * - Spacing más claro entre elementos
 */
@Composable
fun PersonItem(
    person: Person,
    isIdle: Boolean,
    isOverloaded: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Color del punto de estado
    val statusColor = when {
        isOverloaded -> KodeForgeColors.PersonOverload
        isIdle -> KodeForgeColors.PersonIdle
        else -> KodeForgeColors.PersonActive
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp)) // Border radius más suave
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp), // Padding vertical mayor
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp) // Spacing mayor
    ) {
        // Punto de estado (más visible según p1.png)
        Box(
            modifier = Modifier
                .size(9.dp) // Aumentado de 8dp
                .clip(CircleShape)
                .background(statusColor)
        )
        
        // Avatar circular neutral con inicial (ligeramente mayor según p1.png)
        Box(
            modifier = Modifier
                .size(28.dp) // Aumentado de 26dp
                .clip(CircleShape)
                .background(Color(0xFFE5E5EA)), // Gris más claro (más cercano a p1.png)
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = person.displayName.take(1).uppercase(),
                color = Color(0xFF5A5A5F), // Gris medio para mejor contraste
                fontSize = 12.sp, // Proporcional al tamaño
                fontWeight = FontWeight.Bold
            )
        }
        
        // Nombre de la persona
        Text(
            text = person.displayName,
            fontSize = 14.5.sp, // Tamaño explícito para mayor control
            color = KodeForgeColors.TextPrimary,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

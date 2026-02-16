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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Person
import com.kodeforge.ui.theme.KodeForgeColors
import com.kodeforge.ui.theme.KodeForgeSpacing

/**
 * Item de persona en el sidebar.
 * 
 * Layout refinado según specs/p1.png:
 * - Altura: 40dp
 * - Padding: 12dp horizontal
 * - Avatar: 32dp con inicial
 * - Indicador de estado: círculo 8dp
 * - Spacing: 12dp entre elementos
 */
@Composable
fun PersonItem(
    person: Person,
    isIdle: Boolean,
    isOverloaded: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Color del estado
    val statusColor = when {
        isOverloaded -> KodeForgeColors.PersonOverload
        isIdle -> KodeForgeColors.PersonIdle
        else -> KodeForgeColors.PersonActive
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = KodeForgeSpacing.SM), // 12dp
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circular con inicial
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = person.displayName.take(1).uppercase(),
                color = statusColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(Modifier.width(KodeForgeSpacing.SM)) // 12dp
        
        // Nombre de la persona
        Text(
            text = person.displayName,
            fontSize = 14.sp,
            color = KodeForgeColors.TextPrimary,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(Modifier.width(KodeForgeSpacing.XS)) // 8dp
        
        // Indicador de estado (círculo de color)
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
    }
}

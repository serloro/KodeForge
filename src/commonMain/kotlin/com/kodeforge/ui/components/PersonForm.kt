package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Project

/**
 * Formulario para crear/editar una persona.
 * 
 * Según spec.md:
 * - displayName: REQUIRED
 * - hoursPerDay: REQUIRED, > 0
 * - hoursPerWeekday: opcional, horas por día de la semana
 * - role: opcional
 * - tags: opcional
 * - active: default true
 */
@Composable
fun PersonForm(
    person: Person? = null, // null = crear, no null = editar
    availableProjects: List<Project> = emptyList(), // Proyectos disponibles para asignar
    currentProjectIds: List<String> = emptyList(), // IDs de proyectos ya asignados a esta persona
    onSave: (displayName: String, hoursPerDay: Double, hoursPerWeekday: Map<Int, Double>?, role: String?, tags: List<String>, active: Boolean, projectIds: List<String>, avatar: String?) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayName by remember { mutableStateOf(person?.displayName ?: "") }
    var hoursPerDay by remember { mutableStateOf(person?.hoursPerDay?.toString() ?: "") }
    var role by remember { mutableStateOf(person?.role ?: "") }
    var tagsText by remember { mutableStateOf(person?.tags?.joinToString(", ") ?: "") }
    var active by remember { mutableStateOf(person?.active ?: true) }
    var selectedProjectIds by remember { mutableStateOf(currentProjectIds.toSet()) }
    var avatar by remember { mutableStateOf(person?.avatar ?: "") }
    var showAvatarDialog by remember { mutableStateOf(false) }
    
    // Configuración de horas por día de la semana
    var useWeekdayConfig by remember { mutableStateOf(person?.hoursPerWeekday != null) }
    val weekdayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val defaultWeekdayHours = mapOf(1 to 7.0, 2 to 7.0, 3 to 7.0, 4 to 7.0, 5 to 7.0, 6 to 0.0, 7 to 0.0)
    var weekdayHours by remember { 
        mutableStateOf(person?.hoursPerWeekday ?: defaultWeekdayHours)
    }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        Text(
            text = if (person == null) "Crear Persona" else "Editar Persona",
            style = MaterialTheme.typography.headlineSmall
        )
        
        // Mensaje de error global
        errorMessage?.let { error ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        
        // Avatar selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar preview
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD))
                    .clickable { showAvatarDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (avatar.isNotEmpty()) {
                    Text(
                        text = avatar,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = displayName.take(1).uppercase().ifEmpty { "?" },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Avatar",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Haz clic para seleccionar un emoji",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Campo: displayName (REQUIRED)
        OutlinedTextField(
            value = displayName,
            onValueChange = { 
                displayName = it
                errorMessage = null
            },
            label = { Text("Nombre *") },
            placeholder = { Text("ej: Juan Pérez") },
            singleLine = true,
            isError = displayName.trim().isEmpty(),
            supportingText = if (displayName.trim().isEmpty() && displayName.isNotEmpty()) {
                { Text("El nombre es obligatorio") }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Campo: hoursPerDay (REQUIRED, > 0)
        OutlinedTextField(
            value = hoursPerDay,
            onValueChange = { 
                // Solo permitir números y punto decimal
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                    hoursPerDay = it
                    errorMessage = null
                }
            },
            label = { Text("Horas por día (por defecto) *") },
            placeholder = { Text("ej: 8") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = hoursPerDay.toDoubleOrNull()?.let { it <= 0 || it > 24 } == true,
            supportingText = if (hoursPerDay.isEmpty() || hoursPerDay.toDoubleOrNull()?.let { it <= 0 || it > 24 } == true) {
                {
                    val value = hoursPerDay.toDoubleOrNull()
                    Text(
                        when {
                            hoursPerDay.isEmpty() -> "Debe ser mayor a 0"
                            value == null -> "Valor numérico inválido"
                            value <= 0 -> "Debe ser mayor a 0"
                            value > 24 -> "Máximo 24 horas por día"
                            else -> ""
                        }
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Checkbox: Configurar horas por día de la semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = useWeekdayConfig,
                onCheckedChange = { useWeekdayConfig = it }
            )
            Text(
                text = "Configurar horas específicas por día de la semana",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        
        // Configuración de horas por día de la semana
        if (useWeekdayConfig) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Horas por día de la semana",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    weekdayNames.forEachIndexed { index, dayName ->
                        val dayNumber = index + 1
                        val currentHours = weekdayHours[dayNumber]?.toString() ?: "0"
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(100.dp)
                            )
                            
                            OutlinedTextField(
                                value = currentHours,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                        val hours = newValue.toDoubleOrNull() ?: 0.0
                                        if (hours >= 0 && hours <= 24) {
                                            weekdayHours = weekdayHours + (dayNumber to hours)
                                        }
                                    }
                                },
                                label = { Text("Horas") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
        
        // Campo: role (opcional)
        OutlinedTextField(
            value = role,
            onValueChange = { 
                if (it.length <= 50) role = it 
            },
            label = { Text("Rol (opcional)") },
            placeholder = { Text("ej: Developer, Designer, QA") },
            singleLine = true,
            supportingText = { Text("${role.length}/50") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Campo: tags (opcional, separados por comas)
        OutlinedTextField(
            value = tagsText,
            onValueChange = { tagsText = it },
            label = { Text("Tags (opcional)") },
            placeholder = { Text("ej: frontend, react, typescript") },
            supportingText = { Text("Separados por comas (máx. 20 tags)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Checkbox: active
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = active,
                onCheckedChange = { active = it }
            )
            Text(
                text = "Activo",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        
        // Selección de proyectos
        if (availableProjects.isNotEmpty()) {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Proyectos Asignados",
                style = MaterialTheme.typography.titleSmall
            )
            
            Text(
                text = "Selecciona los proyectos en los que trabajará esta persona",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (availableProjects.isEmpty()) {
                        Text(
                            text = "No hay proyectos disponibles",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        availableProjects.forEach { project ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = project.id in selectedProjectIds,
                                    onCheckedChange = { isChecked ->
                                        selectedProjectIds = if (isChecked) {
                                            selectedProjectIds + project.id
                                        } else {
                                            selectedProjectIds - project.id
                                        }
                                    }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = project.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (project.description != null) {
                                        Text(
                                            text = project.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Botones: Cancelar | Guardar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancelar")
            }
            
            Spacer(Modifier.width(12.dp))
            
            Button(
                onClick = {
                    // Validar antes de guardar
                    val trimmedName = displayName.trim()
                    val hours = hoursPerDay.toDoubleOrNull()
                    
                    when {
                        trimmedName.isEmpty() -> {
                            errorMessage = "El nombre es obligatorio"
                        }
                        trimmedName.length > 100 -> {
                            errorMessage = "Nombre muy largo (máximo 100 caracteres)"
                        }
                        hours == null -> {
                            errorMessage = "Horas por día debe ser un número válido"
                        }
                        hours <= 0 -> {
                            errorMessage = "Horas por día debe ser mayor a 0"
                        }
                        hours > 24 -> {
                            errorMessage = "Máximo 24 horas por día"
                        }
                        else -> {
                            // Parsear tags
                            val tags = tagsText
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .take(20) // Máximo 20 tags
                            
                            // Preparar hoursPerWeekday
                            val finalWeekdayHours = if (useWeekdayConfig) weekdayHours else null
                            
                            onSave(
                                trimmedName, 
                                hours, 
                                finalWeekdayHours, 
                                role.trim().takeIf { it.isNotEmpty() }, 
                                tags, 
                                active, 
                                selectedProjectIds.toList(),
                                avatar.takeIf { it.isNotEmpty() }
                            )
                        }
                    }
                }
            ) {
                Text(if (person == null) "Crear" else "Actualizar")
            }
        }
    }
    
    // Diálogo de selección de avatar
    if (showAvatarDialog) {
        AvatarSelectorDialog(
            currentAvatar = avatar,
            onSelect = { 
                avatar = it
                showAvatarDialog = false
            },
            onCancel = { showAvatarDialog = false }
        )
    }
}

@Composable
private fun AvatarSelectorDialog(
    currentAvatar: String,
    onSelect: (String) -> Unit,
    onCancel: () -> Unit
) {
    val avatars = listOf(
        "👤", "👨", "👩", "👨‍💼", "👩‍💼", "👨‍💻", "👩‍💻", "👨‍🎨", "👩‍🎨",
        "👨‍🔧", "👩‍🔧", "👨‍🏫", "👩‍🏫", "👨‍⚕️", "👩‍⚕️", "👨‍🔬", "👩‍🔬",
        "🧑‍💼", "🧑‍💻", "🧑‍🎨", "🧑‍🔧", "🧑‍🏫", "🧑‍⚕️", "🧑‍🔬",
        "😀", "😃", "😄", "😁", "😊", "🙂", "😎", "🤓", "🧐",
        "💼", "💻", "🎨", "🔧", "📚", "🔬", "⚙️", "🚀", "⭐"
    )
    
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Seleccionar Avatar") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Selecciona un emoji como avatar:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Grid de avatares
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    avatars.chunked(6).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (emoji == currentAvatar) Color(0xFF2196F3).copy(alpha = 0.2f)
                                            else Color(0xFFF5F5F5)
                                        )
                                        .clickable { onSelect(emoji) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emoji,
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Opción de limpiar avatar
                TextButton(
                    onClick = { onSelect("") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Usar inicial del nombre")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text("Cerrar")
            }
        }
    )
}


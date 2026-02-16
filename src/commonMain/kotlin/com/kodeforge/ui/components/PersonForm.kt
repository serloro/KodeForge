package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.Person

/**
 * Formulario para crear/editar una persona.
 * 
 * Según spec.md:
 * - displayName: REQUIRED
 * - hoursPerDay: REQUIRED, > 0
 * - role: opcional
 * - tags: opcional
 * - active: default true
 */
@Composable
fun PersonForm(
    person: Person? = null, // null = crear, no null = editar
    onSave: (displayName: String, hoursPerDay: Double, role: String?, tags: List<String>, active: Boolean) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayName by remember { mutableStateOf(person?.displayName ?: "") }
    var hoursPerDay by remember { mutableStateOf(person?.hoursPerDay?.toString() ?: "") }
    var role by remember { mutableStateOf(person?.role ?: "") }
    var tagsText by remember { mutableStateOf(person?.tags?.joinToString(", ") ?: "") }
    var active by remember { mutableStateOf(person?.active ?: true) }
    
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
            label = { Text("Horas por día *") },
            placeholder = { Text("ej: 8") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = hoursPerDay.toDoubleOrNull()?.let { it <= 0 || it > 24 } == true,
            supportingText = {
                val value = hoursPerDay.toDoubleOrNull()
                when {
                    hoursPerDay.isEmpty() -> Text("Debe ser mayor a 0")
                    value == null -> Text("Valor numérico inválido")
                    value <= 0 -> Text("Debe ser mayor a 0")
                    value > 24 -> Text("Máximo 24 horas por día")
                    else -> null
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
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
                            
                            onSave(trimmedName, hours, role.trim().takeIf { it.isNotEmpty() }, tags, active)
                        }
                    }
                }
            ) {
                Text(if (person == null) "Crear" else "Actualizar")
            }
        }
    }
}


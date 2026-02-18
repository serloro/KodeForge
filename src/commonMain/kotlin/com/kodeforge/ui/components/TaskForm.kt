package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Task
import kotlin.math.ceil

/**
 * Formulario para crear/editar una tarea.
 * 
 * Según spec.md:
 * - title: REQUIRED
 * - costHours: REQUIRED (> 0)
 * - status: default "todo"
 * - priority: default 0
 * - assigneeId: opcional
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskForm(
    task: Task? = null, // null = crear, no null = editar
    availablePeople: List<Person> = emptyList(),
    onSave: (title: String, costHours: Double, description: String?, status: String, priority: Int, assigneeId: String?) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var costHours by remember { mutableStateOf(task?.costHours?.toString() ?: "") }
    var status by remember { mutableStateOf(task?.status ?: "backlog") }
    var priority by remember { mutableStateOf(task?.priority?.toString() ?: "0") }
    var assigneeId by remember { mutableStateOf(task?.assigneeId) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusExpanded by remember { mutableStateOf(false) }
    var assigneeExpanded by remember { mutableStateOf(false) }
    
    val statusOptions = listOf(
        "backlog" to "📋 Backlog",
        "in_progress" to "🔄 In Progress",
        "in_review" to "👀 In Review",
        "testing" to "🧪 Testing",
        "done" to "✅ Done"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        Text(
            text = if (task == null) "Crear Tarea" else "Editar Tarea",
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
        
        // Campo: title (REQUIRED)
        OutlinedTextField(
            value = title,
            onValueChange = { 
                title = it
                errorMessage = null
            },
            label = { Text("Título *") },
            placeholder = { Text("ej: Implementar login") },
            singleLine = true,
            isError = title.trim().isEmpty(),
            supportingText = if (title.trim().isEmpty() && title.isNotEmpty()) {
                { Text("El título es obligatorio") }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Campo: description (opcional, multiline)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción (opcional)") },
            placeholder = { Text("Detalles de la tarea...") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Campo: costHours (REQUIRED, > 0)
        OutlinedTextField(
            value = costHours,
            onValueChange = { 
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                    costHours = it
                    errorMessage = null
                }
            },
            label = { Text("Costo en horas *") },
            placeholder = { Text("ej: 8") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = costHours.toDoubleOrNull()?.let { it <= 0 || it > 1000 } == true,
            supportingText = if (costHours.isEmpty() || costHours.toDoubleOrNull()?.let { it <= 0 || it > 1000 } == true) {
                {
                    val value = costHours.toDoubleOrNull()
                    Text(
                        when {
                            costHours.isEmpty() -> "Debe ser mayor a 0"
                            value == null -> "Valor numérico inválido"
                            value <= 0 -> "Debe ser mayor a 0"
                            value > 1000 -> "Máximo 1000 horas"
                            else -> ""
                        }
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Campo: status (dropdown)
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = statusOptions.find { it.first == status }?.second ?: "⚪ Por Hacer",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    statusOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                status = value
                                statusExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Campo: priority
            OutlinedTextField(
                value = priority,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d+$"))) {
                        priority = it
                    }
                },
                label = { Text("Prioridad") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("Menor = prioritario") },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Campo: assigneeId (dropdown personas, opcional)
        ExposedDropdownMenuBox(
            expanded = assigneeExpanded,
            onExpandedChange = { assigneeExpanded = !assigneeExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = availablePeople.find { it.id == assigneeId }?.displayName ?: "Sin asignar",
                onValueChange = {},
                readOnly = true,
                label = { Text("Asignar a (opcional)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assigneeExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = assigneeExpanded,
                onDismissRequest = { assigneeExpanded = false }
            ) {
                // Opción "Sin asignar"
                DropdownMenuItem(
                    text = { Text("Sin asignar") },
                    onClick = {
                        assigneeId = null
                        assigneeExpanded = false
                    }
                )
                
                // Personas disponibles (solo activas)
                availablePeople.filter { it.active }.forEach { person ->
                    DropdownMenuItem(
                        text = { 
                            Column {
                                Text(person.displayName)
                                // Mostrar horario semanal si está configurado
                                if (person.hoursPerWeekday != null) {
                                    val weekSchedule = listOf("L", "M", "X", "J", "V", "S", "D")
                                        .mapIndexed { index, day ->
                                            val hours = person.getHoursForDay(index + 1)
                                            "$day:${hours.toInt()}h"
                                        }
                                        .joinToString(" ")
                                    Text(
                                        text = weekSchedule,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "${person.hoursPerDay}h/día disponibles",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = {
                            assigneeId = person.id
                            assigneeExpanded = false
                        }
                    )
                }
            }
        }
        
        // Mostrar estimación de tiempo si hay persona asignada y costo en horas
        val selectedPerson = availablePeople.find { it.id == assigneeId }
        val taskHours = costHours.toDoubleOrNull()
        if (selectedPerson != null && taskHours != null && taskHours > 0) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "⏱️ Estimación de tiempo",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    
                    // Calcular días laborables necesarios
                    if (selectedPerson.hoursPerWeekday != null) {
                        // Usar horario semanal
                        val hoursPerWeek = selectedPerson.hoursPerWeekday.values.sum()
                        val weeksNeeded = taskHours / hoursPerWeek
                        val daysNeeded = weeksNeeded * 7
                        
                        Text(
                            text = "Con el horario de ${selectedPerson.displayName} (${hoursPerWeek.toInt()}h/semana):",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "• Tiempo estimado: ${String.format("%.1f", weeksNeeded)} semanas (~${ceil(daysNeeded).toInt()} días naturales)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        // Usar horas por día estándar
                        val daysNeeded = ceil(taskHours / selectedPerson.hoursPerDay)
                        Text(
                            text = "Con ${selectedPerson.hoursPerDay}h/día de ${selectedPerson.displayName}:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "• Tiempo estimado: ${daysNeeded.toInt()} días laborables",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
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
                    val trimmedTitle = title.trim()
                    val hours = costHours.toDoubleOrNull()
                    val prio = priority.toIntOrNull() ?: 0
                    
                    when {
                        trimmedTitle.isEmpty() -> {
                            errorMessage = "El título es obligatorio"
                        }
                        trimmedTitle.length > 200 -> {
                            errorMessage = "Título muy largo (máximo 200 caracteres)"
                        }
                        hours == null -> {
                            errorMessage = "Costo en horas debe ser un número válido"
                        }
                        hours <= 0 -> {
                            errorMessage = "Costo en horas debe ser mayor a 0"
                        }
                        hours > 1000 -> {
                            errorMessage = "Costo en horas excesivo (máximo 1000h)"
                        }
                        prio < 0 -> {
                            errorMessage = "Prioridad debe ser mayor o igual a 0"
                        }
                        else -> {
                            onSave(
                                trimmedTitle,
                                hours,
                                description.trim().takeIf { it.isNotEmpty() },
                                status,
                                prio,
                                assigneeId
                            )
                        }
                    }
                }
            ) {
                Text(if (task == null) "Crear" else "Actualizar")
            }
        }
    }
}


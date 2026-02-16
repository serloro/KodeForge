package com.kodeforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Task
import com.kodeforge.ui.theme.KodeForgeColors

/**
 * Diálogo para asignar una tarea a una persona.
 * 
 * Según spec: "al asignar tarea → se indica costHours" (obligatorio).
 * Si la tarea ya tiene costHours, se muestra pero no se puede editar aquí
 * (se edita en el formulario principal).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTaskDialog(
    task: Task,
    availablePeople: List<Person>,
    onAssign: (personId: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPersonId by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            Text(
                text = "Asignar Tarea",
                style = MaterialTheme.typography.headlineSmall
            )
            
            // Info de la tarea
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Tarea: ${task.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KodeForgeColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "Costo: ${task.costHours} horas",
                    style = MaterialTheme.typography.bodySmall,
                    color = KodeForgeColors.TextSecondary
                )
            }
            
            Divider(color = KodeForgeColors.Divider)
            
            // Mensaje de error
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
            
            // Selector de persona
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = availablePeople.find { it.id == selectedPersonId }?.displayName ?: "Seleccionar persona...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Asignar a *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    isError = selectedPersonId == null && errorMessage != null,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Solo personas activas
                    val activePeople = availablePeople.filter { it.active }
                    
                    if (activePeople.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay personas activas disponibles") },
                            onClick = {},
                            enabled = false
                        )
                    } else {
                        activePeople.forEach { person ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE5E5EA)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = person.displayName.take(1).uppercase(),
                                                color = Color(0xFF5A5A5F),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        // Info
                                        Column {
                                            Text(
                                                text = person.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${person.hoursPerDay}h/día disponibles",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            person.role?.let { role ->
                                                Text(
                                                    text = role,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = KodeForgeColors.TextTertiary
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    selectedPersonId = person.id
                                    expanded = false
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Botones: Cancelar | Asignar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                
                Spacer(Modifier.width(12.dp))
                
                Button(
                    onClick = {
                        if (selectedPersonId == null) {
                            errorMessage = "Debes seleccionar una persona"
                        } else {
                            onAssign(selectedPersonId!!)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KodeForgeColors.Primary
                    )
                ) {
                    Text("Asignar")
                }
            }
        }
    }
}


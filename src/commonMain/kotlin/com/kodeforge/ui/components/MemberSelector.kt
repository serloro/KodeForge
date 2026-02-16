package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.Person

/**
 * Selector de miembros para un proyecto.
 * 
 * Muestra una lista de personas con checkboxes para seleccionar/deseleccionar.
 * 
 * @param people Lista de todas las personas disponibles
 * @param selectedMembers Lista de IDs de personas ya seleccionadas
 * @param onDismiss Callback al cerrar
 * @param onSave Callback al guardar con la lista actualizada
 */
@Composable
fun MemberSelector(
    people: List<Person>,
    selectedMembers: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedMembers) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Miembros") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                Text(
                    text = "Selecciona las personas que formarán parte del proyecto:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (people.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay personas disponibles.\nCrea personas primero.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(people) { person ->
                            MemberCheckboxItem(
                                person = person,
                                isSelected = person.id in currentSelection,
                                onCheckedChange = { isChecked ->
                                    currentSelection = if (isChecked) {
                                        currentSelection + person.id
                                    } else {
                                        currentSelection.filter { it != person.id }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(currentSelection) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Item de checkbox para seleccionar un miembro.
 */
@Composable
private fun MemberCheckboxItem(
    person: Person,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onCheckedChange
        )
        
        Spacer(Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = person.displayName,
                style = MaterialTheme.typography.bodyLarge
            )
            
            person.role?.let { role ->
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


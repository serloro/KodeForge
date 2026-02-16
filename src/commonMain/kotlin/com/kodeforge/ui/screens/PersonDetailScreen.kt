package com.kodeforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Workspace
import com.kodeforge.domain.usecases.PlanningUseCases
import com.kodeforge.ui.components.PersonCalendar
import com.kodeforge.ui.components.PersonSummaryCard
import com.kodeforge.ui.components.TaskListCard
import com.kodeforge.ui.theme.KodeForgeColors
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Pantalla de detalle de persona.
 * 
 * Muestra:
 * - Resumen (KPIs)
 * - Calendario/timeline horizontal
 * - Lista de tareas activas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    workspace: Workspace,
    person: Person,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val planningUseCases = remember { PlanningUseCases() }
    
    // Calcular datos
    val activeTasks = remember(workspace.tasks, person.id) {
        workspace.tasks.filter { 
            it.assigneeId == person.id && it.status != "completed" 
        }
    }
    
    val scheduleBlocks = remember(workspace.planning.scheduleBlocks, person.id) {
        planningUseCases.getScheduleForPerson(workspace, person.id)
    }
    
    val plannedHours = remember(scheduleBlocks) {
        scheduleBlocks.sumOf { it.hoursPlanned }
    }
    
    val doneHours = remember(activeTasks) {
        activeTasks.sumOf { it.doneHours }
    }
    
    val totalHours = remember(activeTasks) {
        activeTasks.sumOf { it.costHours }
    }
    
    val estimatedEndDate = remember(workspace, person.id) {
        planningUseCases.getEstimatedEndDate(workspace, person.id)
    }
    
    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.UTC).date
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KodeForgeColors.Background)
    ) {
        // Header
        TopAppBar(
            title = { 
                Column {
                    Text(person.displayName)
                    person.role?.let { role ->
                        Text(
                            text = role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Volver")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Contenido scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Card Resumen
            PersonSummaryCard(
                activeTasksCount = activeTasks.size,
                plannedHours = plannedHours,
                doneHours = doneHours,
                totalHours = totalHours,
                estimatedEndDate = estimatedEndDate
            )
            
            // Card Calendario
            PersonCalendar(
                scheduleBlocks = scheduleBlocks,
                tasks = workspace.tasks,
                today = today
            )
            
            // Card Tareas Activas
            TaskListCard(
                tasks = activeTasks
            )
            
            // Espaciador final
            Spacer(Modifier.height(16.dp))
        }
    }
}


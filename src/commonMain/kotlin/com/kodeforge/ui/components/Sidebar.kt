package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.Person
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.Task
import com.kodeforge.ui.theme.KodeForgeColors
import com.kodeforge.ui.theme.KodeForgeSpacing

/**
 * Sidebar de KodeForge con Projects y Personas.
 * 
 * Layout refinado según specs/p1.png:
 * - Ancho: 240dp (según specs)
 * - Fondo: #F7F8FA (BackgroundSecondary)
 * - Padding: 12dp
 * - Spacing entre secciones: 24dp
 */
@Composable
fun Sidebar(
    projects: List<Project>,
    people: List<Person>,
    tasks: List<Task>,
    selectedProjectId: String?,
    onProjectClick: (Project) -> Unit,
    onPersonClick: (Person) -> Unit,
    onManageProjects: () -> Unit,
    onManagePeople: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Ordenar personas: idle-first
    val sortedPeople = people.sortedBy { person ->
        val hasTasks = tasks.any { it.assigneeId == person.id && it.status != "completed" }
        if (hasTasks) 1 else 0
    }
    
    // Surface sin sombra (más limpio según specs)
    Surface(
        modifier = modifier
            .width(KodeForgeSpacing.SidebarWidth) // 240dp según specs
            .fillMaxHeight(),
        color = KodeForgeColors.BackgroundSecondary, // #F7F8FA según specs
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(KodeForgeSpacing.SM) // 12dp según specs
        ) {
            // Sección Projects
            SidebarSection(
                title = "Projects",
                onManage = onManageProjects,
                items = projects,
                itemContent = { project ->
                    ProjectItem(
                        project = project,
                        isSelected = project.id == selectedProjectId,
                        onClick = { onProjectClick(project) }
                    )
                }
            )
            
            // Espaciador entre secciones (según specs)
            Spacer(Modifier.height(KodeForgeSpacing.LG)) // 24dp
            Divider(
                modifier = Modifier.padding(horizontal = KodeForgeSpacing.MD), // 16dp
                color = KodeForgeColors.Divider,
                thickness = 1.dp
            )
            Spacer(Modifier.height(KodeForgeSpacing.LG)) // 24dp
            
            // Sección Personas
            SidebarSection(
                title = "Personas",
                onManage = onManagePeople,
                items = sortedPeople,
                itemContent = { person ->
                    val isIdle = tasks.none { it.assigneeId == person.id && it.status != "completed" }
                    val isOverloaded = false
                    
                    PersonItem(
                        person = person,
                        isIdle = isIdle,
                        isOverloaded = isOverloaded,
                        onClick = { onPersonClick(person) }
                    )
                }
            )
        }
    }
}

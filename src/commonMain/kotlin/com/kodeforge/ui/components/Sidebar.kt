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

/**
 * Sidebar de KodeForge con Projects y Personas.
 * 
 * Layout refinado según specs/p1.png:
 * - Ancho: 280dp (más espacioso)
 * - Padding vertical superior: 20dp (más generoso)
 * - Separación entre secciones: 32dp (más clara)
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
    
    // Surface con sombra para separación del main content
    Surface(
        modifier = modifier
            .width(280.dp) // Aumentado de 240dp para más espacio
            .fillMaxHeight(),
        color = KodeForgeColors.SidebarBackground,
        shadowElevation = 0.5.dp // Sombra más sutil
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 20.dp) // Padding vertical más generoso
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
            
            // Espaciador entre secciones (más generoso según p1.png)
            Spacer(Modifier.height(32.dp)) // Aumentado de 24dp
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp), // Padding más generoso
                color = KodeForgeColors.Divider,
                thickness = 1.dp
            )
            Spacer(Modifier.height(32.dp)) // Aumentado de 24dp
            
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

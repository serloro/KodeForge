package com.kodeforge.domain.model

import kotlinx.serialization.Serializable

/**
 * Workspace portable - Representa el estado completo de la aplicación KodeForge.
 * Este objeto se serializa/deserializa a JSON para persistencia portable.
 */
@Serializable
data class Workspace(
    val app: AppMetadata,
    val people: List<Person> = emptyList(),
    val projects: List<Project> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val planning: Planning = Planning(),
    val uiState: UiState = UiState(),
    val secrets: Secrets = Secrets()
)

@Serializable
data class AppMetadata(
    val name: String = "KodeForge",
    val schemaVersion: Int, // REQUIRED - para versionado y migraciones futuras
    val createdAt: String,
    val updatedAt: String,
    val defaultLocale: String = "es",
    val supportedLocales: List<String> = listOf("es", "en"),
    val settings: AppSettings = AppSettings()
)

@Serializable
data class AppSettings(
    val workweek: WorkweekSettings = WorkweekSettings(),
    val ui: UiSettings = UiSettings()
)

@Serializable
data class WorkweekSettings(
    val workingDays: List<Int> = listOf(1, 2, 3, 4, 5), // 1=Monday, 7=Sunday
    val timezone: String = "Europe/Madrid"
)

@Serializable
data class UiSettings(
    val idleFirst: Boolean = true,
    val todayLineEnabled: Boolean = true,
    val taskStatuses: List<TaskStatus> = defaultTaskStatuses()
)

@Serializable
data class TaskStatus(
    val id: String,
    val label: String,
    val color: String, // Hex color
    val order: Int
)

/**
 * Estados de tareas por defecto.
 */
fun defaultTaskStatuses(): List<TaskStatus> = listOf(
    TaskStatus(id = "backlog", label = "Backlog", color = "#9E9E9E", order = 0),
    TaskStatus(id = "in_progress", label = "In Progress", color = "#2196F3", order = 1),
    TaskStatus(id = "in_review", label = "In Review", color = "#FF9800", order = 2),
    TaskStatus(id = "testing", label = "Testing", color = "#9C27B0", order = 3),
    TaskStatus(id = "done", label = "Done", color = "#4CAF50", order = 4)
)


package com.kodeforge.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String? = null,
    val status: String = "backlog", // backlog, in_progress, in_review, testing, done (configurable)
    val priority: Int = 0, // Menor = más prioritario (1, 2, 3...)
    val costHours: Double, // REQUIRED si hay assigneeId
    val doneHours: Double = 0.0,
    val assigneeId: String? = null, // ID de persona o null si no asignada
    val createdAt: String,
    val updatedAt: String
)


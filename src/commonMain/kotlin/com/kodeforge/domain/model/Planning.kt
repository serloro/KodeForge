package com.kodeforge.domain.model

import kotlinx.serialization.Serializable

/**
 * Planning - Resultado del scheduler secuencial.
 * Contiene los bloques de planificación por persona/día.
 */
@Serializable
data class Planning(
    val generatedAt: String? = null,
    val strategy: PlanningStrategy = PlanningStrategy(),
    val scheduleBlocks: List<ScheduleBlock> = emptyList()
)

@Serializable
data class PlanningStrategy(
    val type: String = "sequential", // sequential (MVP)
    val splitAcrossDays: Boolean = true
)

@Serializable
data class ScheduleBlock(
    val id: String,
    val personId: String,
    val taskId: String,
    val projectId: String,
    val date: String, // YYYY-MM-DD
    val hoursPlanned: Double
)


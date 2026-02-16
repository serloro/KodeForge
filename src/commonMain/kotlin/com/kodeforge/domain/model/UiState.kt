package com.kodeforge.domain.model

import kotlinx.serialization.Serializable

/**
 * UiState - Estado de la UI (selecciones, filtros, etc.)
 * Se persiste para mantener contexto al reabrir la app.
 */
@Serializable
data class UiState(
    val selectedProjectId: String? = null,
    val selectedPersonId: String? = null,
    val lastViewed: String = "home", // home, project, person
    val filters: UiFilters = UiFilters()
)

@Serializable
data class UiFilters(
    val homeRangeDays: Int = 7,
    val projectRange: String = "month" // week, month, quarter
)


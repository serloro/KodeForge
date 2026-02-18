package com.kodeforge.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val id: String,
    val displayName: String,
    val avatar: String? = null, // Path relativo a carpeta de workspace o null
    val role: String? = null,
    val hoursPerDay: Double, // REQUIRED - horas disponibles por día (por defecto, usado si no hay hoursPerWeekday)
    val hoursPerWeekday: Map<Int, Double>? = null, // Horas por día de la semana (1=Lunes, 7=Domingo). Si null, usa hoursPerDay
    val active: Boolean = true,
    val tags: List<String> = emptyList(), // Tags libres
    val meta: PersonMeta = PersonMeta()
) {
    /**
     * Obtiene las horas configuradas para un día específico de la semana.
     * @param dayOfWeek Día de la semana (1=Lunes, 7=Domingo)
     * @return Horas configuradas para ese día, o hoursPerDay si no hay configuración específica
     */
    fun getHoursForDay(dayOfWeek: Int): Double {
        return hoursPerWeekday?.get(dayOfWeek) ?: hoursPerDay
    }
}

@Serializable
data class PersonMeta(
    val createdAt: String? = null
)


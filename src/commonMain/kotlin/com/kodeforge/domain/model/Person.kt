package com.kodeforge.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val id: String,
    val displayName: String,
    val avatar: String? = null, // Path relativo a carpeta de workspace o null
    val role: String? = null,
    val hoursPerDay: Double, // REQUIRED - horas disponibles por día
    val active: Boolean = true,
    val tags: List<String> = emptyList(), // Tags libres
    val meta: PersonMeta = PersonMeta()
)

@Serializable
data class PersonMeta(
    val createdAt: String? = null
)


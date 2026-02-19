package com.kodeforge.ui.error

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Centraliza el reporte de errores para que la app no "casque".
 *
 * Objetivo:
 * - Cualquier excepción inesperada se convierte en un mensaje sencillo en UI.
 * - La app intenta seguir funcionando (o al menos mostrar una pantalla estable).
 */
object AppErrorReporter {

    data class UiError(
        val title: String,
        val message: String,
        val technical: String? = null
    )

    private val _errors = MutableStateFlow<UiError?>(null)
    val errors: StateFlow<UiError?> = _errors.asStateFlow()

    fun clear() {
        _errors.value = null
    }

    fun report(
        title: String = "Ha ocurrido un error",
        message: String,
        technical: String? = null
    ) {
        _errors.value = UiError(title = title, message = message, technical = technical)
    }

    fun report(throwable: Throwable, context: String? = null) {
        // Mensaje "humano": corto y sin stacktrace
        val msg = buildString {
            if (!context.isNullOrBlank()) append(context).append(": ")
            append(throwable.message ?: throwable::class.simpleName ?: "Error desconocido")
        }

        // Mensaje técnico opcional (por si se quiere copiar/pegar)
        val technical = buildString {
            append(throwable::class.qualifiedName ?: "Throwable")
            if (!throwable.message.isNullOrBlank()) {
                append(": ").append(throwable.message)
            }
        }

        // También lo dejamos en consola para depurar
        println("[KodeForge][ERROR] $msg")
        throwable.printStackTrace()

        report(
            title = "Ha ocurrido un error",
            message = msg,
            technical = technical
        )
    }
}

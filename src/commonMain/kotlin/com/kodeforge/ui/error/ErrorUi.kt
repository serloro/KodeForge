package com.kodeforge.ui.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Host global de errores: si hay un error reportado, muestra un diálogo sencillo
 * y mantiene la app viva.
 */
@Composable
fun GlobalErrorHost(content: @Composable () -> Unit) {
    val uiError by AppErrorReporter.errors.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (uiError != null) {
            AlertDialog(
                onDismissRequest = { AppErrorReporter.clear() },
                confirmButton = {
                    TextButton(onClick = { AppErrorReporter.clear() }) {
                        Text("Cerrar")
                    }
                },
                title = { Text(uiError!!.title) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(uiError!!.message)
                        if (!uiError!!.technical.isNullOrBlank()) {
                            Divider()
                            Text(
                                text = uiError!!.technical!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    }
}

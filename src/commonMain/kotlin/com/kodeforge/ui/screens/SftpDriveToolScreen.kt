package com.kodeforge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kodeforge.domain.model.Project
import com.kodeforge.domain.model.SftpDriveMountConfig
import com.kodeforge.domain.model.Workspace
import com.kodeforge.sftpdrive.SftpDriveMounter
import com.kodeforge.ui.components.ToolLayout
import com.kodeforge.ui.error.AppErrorReporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SftpDriveToolScreen(
    workspace: Workspace,
    projectId: String,
    project: Project,
    onWorkspaceUpdate: (Workspace) -> Unit,
    onBack: () -> Unit,
    onToolClick: (String) -> Unit,
    onBackToHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    var config by remember {
        mutableStateOf(
            SftpDriveMountConfig(
                driveName = "SFTP Drive",
                driveLetter = "S",
                remotePort = 22,
                rootMode = "specified",
                remoteFolder = "/",
                openFolderPath = "/"
            )
        )
    }
    var status by remember { mutableStateOf<String?>(null) }
    var installing by remember { mutableStateOf(false) }
    var mounting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ToolLayout(
        project = project,
        toolTitle = "Unidad SFTP",
        selectedToolId = "sftpdrive",
        onBack = onBack,
        onToolClick = onToolClick,
        onBackToHub = onBackToHub,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!SftpDriveMounter.isSupported()) {
                Text("Solo disponible en Windows", style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            Text(
                text = "Monta una unidad (letra) usando WinFsp + SSHFS-Win",
                style = MaterialTheme.typography.bodyMedium
            )

            Divider()

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = config.driveName,
                    onValueChange = { config = config.copy(driveName = it) },
                    label = { Text("Drive name") }
                )
                OutlinedTextField(
                    modifier = Modifier.width(120.dp),
                    value = config.driveLetter,
                    onValueChange = { config = config.copy(driveLetter = it) },
                    label = { Text("Letra") },
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = config.remoteHost,
                    onValueChange = { config = config.copy(remoteHost = it) },
                    label = { Text("Remote host") }
                )
                OutlinedTextField(
                    modifier = Modifier.width(140.dp),
                    value = config.remotePort.toString(),
                    onValueChange = { v -> config = config.copy(remotePort = v.toIntOrNull() ?: 22) },
                    label = { Text("Puerto") },
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = config.username,
                    onValueChange = { config = config.copy(username = it) },
                    label = { Text("Username") }
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = config.password,
                    onValueChange = { config = config.copy(password = it) },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = config.privateKeyPath,
                onValueChange = { config = config.copy(privateKeyPath = it) },
                label = { Text("Private key path (opcional)") },
                singleLine = true
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = config.remoteFolder,
                onValueChange = { config = config.copy(remoteFolder = it) },
                label = { Text("Remote folder") },
                singleLine = true
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Read-only")
                Switch(
                    checked = config.readOnly,
                    onCheckedChange = { config = config.copy(readOnly = it) }
                )
                Spacer(Modifier.weight(1f))
                Text("Abrir carpeta")
                Switch(
                    checked = config.openFolderOnConnect,
                    onCheckedChange = { config = config.copy(openFolderOnConnect = it) }
                )
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = config.openFolderPath,
                onValueChange = { config = config.copy(openFolderPath = it) },
                label = { Text("Ruta a abrir (ej: /)") },
                singleLine = true
            )

            Divider()

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    enabled = !installing,
                    onClick = {
                        installing = true
                        status = "Instalando dependencias..."
                        AppErrorReporter.clear()
                        scope.launch {
                                runCatching {
                                    withContext(Dispatchers.IO) {
                                        SftpDriveMounter.ensureDependenciesInstalled(silent = false)
                                    }
                                }.onSuccess {
                                    status = it
                                }.onFailure {
                                    AppErrorReporter.report(it, "Unidad SFTP")
                                    status = "Error instalando dependencias"
                                }
                                installing = false
                            }
                    }
                ) {
                    Text("Instalar dependencias")
                }

                Button(
                    enabled = !mounting,
                    onClick = {
                        mounting = true
                        status = "Montando..."
                        AppErrorReporter.clear()
                        scope.launch {
                                runCatching {
                                    withContext(Dispatchers.IO) {
                                        SftpDriveMounter.mount(config)
                                    }
                                }.onSuccess {
                                    status = it
                                }.onFailure {
                                    AppErrorReporter.report(it, "Unidad SFTP")
                                    status = "Error montando unidad"
                                }
                                mounting = false
                            }
                    }
                ) {
                    Text("Montar")
                }

                TextButton(
                    onClick = {
                        AppErrorReporter.clear()
                        scope.launch {
                                runCatching {
                                    withContext(Dispatchers.IO) {
                                        SftpDriveMounter.unmount(config.driveLetter)
                                    }
                                }.onSuccess {
                                    status = it
                                }.onFailure {
                                    AppErrorReporter.report(it, "Unidad SFTP")
                                    status = "Error desmontando"
                                }
                            }
                    }
                ) {
                    Text("Desmontar")
                }
            }

            if (status != null) {
                Spacer(Modifier.height(4.dp))
                Text(status!!, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

package com.kodeforge.sftp

import com.jcraft.jsch.*
import com.kodeforge.domain.model.SftpConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementación JVM del cliente SFTP usando JSch.
 */
actual class SftpClient {
    
    private var jsch: JSch? = null
    private var session: Session? = null
    private var channel: ChannelSftp? = null
    private var connectionState: SftpConnectionState = SftpConnectionState.DISCONNECTED
    
    /**
     * Conecta al servidor SFTP.
     */
    actual suspend fun connect(connection: SftpConnection, password: String): SftpResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                connectionState = SftpConnectionState.CONNECTING
                
                // Inicializar JSch
                jsch = JSch()
                
                // Configurar autenticación según el tipo
                when (connection.auth.type) {
                    "key" -> {
                        // Para keys, el password sería la passphrase
                        // En un caso real, connection.auth.valueRef apuntaría al archivo de clave
                        // Por ahora, solo soportamos password
                        return@withContext SftpResult.Error(
                            "Autenticación por clave SSH no implementada aún. Use 'password' o 'none'."
                        )
                    }
                }
                
                // Crear sesión
                session = jsch!!.getSession(connection.username, connection.host, connection.port)
                
                // Configurar password si es necesario
                if (connection.auth.type == "password" && password.isNotEmpty()) {
                    session!!.setPassword(password)
                }
                
                // Configurar propiedades de la sesión
                val config = Properties()
                config["StrictHostKeyChecking"] = "no" // Para MVP, no validar host key
                session!!.setConfig(config)
                
                // Timeout de conexión: 10 segundos
                session!!.timeout = 10000
                
                // Conectar
                session!!.connect()
                
                // Abrir canal SFTP
                channel = session!!.openChannel("sftp") as ChannelSftp
                channel!!.connect()
                
                connectionState = SftpConnectionState.CONNECTED
                
                SftpResult.Success(Unit)
            } catch (e: JSchException) {
                connectionState = SftpConnectionState.ERROR
                disconnect()
                SftpResult.Error(
                    message = "Error de conexión SFTP: ${e.message ?: "Desconocido"}",
                    exception = e
                )
            } catch (e: Exception) {
                connectionState = SftpConnectionState.ERROR
                disconnect()
                SftpResult.Error(
                    message = "Error inesperado: ${e.message ?: "Desconocido"}",
                    exception = e
                )
            }
        }
    }
    
    /**
     * Desconecta del servidor SFTP.
     */
    actual fun disconnect() {
        try {
            channel?.disconnect()
            session?.disconnect()
        } catch (e: Exception) {
            // Ignorar errores al desconectar
        } finally {
            channel = null
            session = null
            jsch = null
            connectionState = SftpConnectionState.DISCONNECTED
        }
    }
    
    /**
     * Lista archivos y directorios en la ruta especificada.
     */
    @Suppress("UNCHECKED_CAST")
    actual suspend fun listFiles(path: String): SftpResult<List<RemoteFile>> {
        return withContext(Dispatchers.IO) {
            try {
                if (channel == null || !channel!!.isConnected) {
                    return@withContext SftpResult.Error("No hay conexión SFTP activa")
                }
                
                val entries = channel!!.ls(path) as Vector<ChannelSftp.LsEntry>
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                
                val files = entries
                    .filter { entry ->
                        // Filtrar "." y ".."
                        entry.filename != "." && entry.filename != ".."
                    }
                    .map { entry ->
                        val attrs = entry.attrs
                        val fullPath = if (path == "." || path == "") {
                            entry.filename
                        } else {
                            "${path.trimEnd('/')}/${entry.filename}"
                        }
                        
                        RemoteFile(
                            name = entry.filename,
                            path = fullPath,
                            isDirectory = attrs.isDir,
                            size = attrs.size,
                            modifiedAt = dateFormat.format(Date(attrs.mTime.toLong() * 1000)),
                            permissions = attrs.permissionsString
                        )
                    }
                    .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                
                SftpResult.Success(files)
            } catch (e: SftpException) {
                SftpResult.Error(
                    message = "Error al listar archivos: ${e.message ?: "Desconocido"}",
                    exception = e
                )
            } catch (e: Exception) {
                SftpResult.Error(
                    message = "Error inesperado: ${e.message ?: "Desconocido"}",
                    exception = e
                )
            }
        }
    }
    
    /**
     * Lee el contenido de un archivo remoto como texto.
     */
    actual suspend fun readFileAsText(path: String): SftpResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (channel == null || !channel!!.isConnected) {
                    return@withContext SftpResult.Error("No hay conexión SFTP activa")
                }
                
                // Verificar que el archivo existe y no es un directorio
                val attrs = channel!!.stat(path)
                if (attrs.isDir) {
                    return@withContext SftpResult.Error("La ruta especificada es un directorio")
                }
                
                // Limitar el tamaño del archivo a 1MB para preview
                if (attrs.size > 1024 * 1024) {
                    return@withContext SftpResult.Error(
                        "Archivo demasiado grande para preview (máximo 1MB). Tamaño: ${attrs.size / 1024}KB"
                    )
                }
                
                // Leer el archivo
                val inputStream = channel!!.get(path)
                val outputStream = ByteArrayOutputStream()
                
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                
                inputStream.close()
                
                val content = outputStream.toString("UTF-8")
                
                SftpResult.Success(content)
            } catch (e: SftpException) {
                SftpResult.Error(
                    message = "Error al leer archivo: ${e.message ?: "Desconocido"}",
                    exception = e
                )
            } catch (e: Exception) {
                SftpResult.Error(
                    message = "Error inesperado: ${e.message ?: "Desconocido"}",
                    exception = e
                )
            }
        }
    }
    
    /**
     * Obtiene el estado actual de la conexión.
     */
    actual fun getConnectionState(): SftpConnectionState {
        return connectionState
    }
    
    /**
     * Verifica si el cliente está conectado.
     */
    actual fun isConnected(): Boolean {
        return channel != null && channel!!.isConnected && 
               session != null && session!!.isConnected
    }
}


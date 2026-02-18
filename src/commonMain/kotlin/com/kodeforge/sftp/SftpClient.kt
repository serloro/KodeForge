package com.kodeforge.sftp

import com.kodeforge.domain.model.SftpConnection

/**
 * Cliente SFTP multiplataforma (expect/actual).
 */
expect class SftpClient() {
    
    /**
     * Conecta al servidor SFTP usando la configuración proporcionada.
     * 
     * @param connection Configuración de la conexión
     * @param password Password o passphrase (si aplica)
     * @return Result con éxito o error
     */
    suspend fun connect(connection: SftpConnection, password: String): SftpResult<Unit>
    
    /**
     * Desconecta del servidor SFTP.
     */
    fun disconnect()
    
    /**
     * Lista archivos y directorios en la ruta especificada.
     * 
     * @param path Ruta remota (default: directorio home)
     * @return Result con lista de archivos o error
     */
    suspend fun listFiles(path: String = "."): SftpResult<List<RemoteFile>>
    
    /**
     * Lee el contenido de un archivo remoto como texto.
     * 
     * @param path Ruta completa del archivo
     * @return Result con contenido del archivo o error
     */
    suspend fun readFileAsText(path: String): SftpResult<String>

    /**
     * Descarga un archivo remoto a una ruta local.
     *
     * @param remotePath Ruta completa del archivo remoto
     * @param localPath Ruta completa donde guardar el archivo en local
     */
    suspend fun downloadFile(remotePath: String, localPath: String): SftpResult<Unit>
    
    /**
     * Obtiene el estado actual de la conexión.
     */
    fun getConnectionState(): SftpConnectionState
    
    /**
     * Verifica si el cliente está conectado.
     */
    fun isConnected(): Boolean
}


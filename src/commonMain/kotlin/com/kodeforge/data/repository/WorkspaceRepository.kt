package com.kodeforge.data.repository

import com.kodeforge.domain.model.Workspace
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * WorkspaceRepository - Gestiona la persistencia portable del workspace.
 * 
 * Principios:
 * - Load/Save atómico (evita corrupción)
 * - Portable: copiar JSON a otro equipo funciona igual
 * - schemaVersion obligatorio para migraciones futuras
 */
class WorkspaceRepository(
    private val fileSystem: FileSystemAdapter
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Carga el workspace desde un archivo JSON.
     * @throws WorkspaceLoadException si el archivo no existe o está corrupto
     */
    suspend fun load(path: String): Workspace {
        try {
            val content = fileSystem.readFile(path)
            val workspace = json.decodeFromString<Workspace>(content)
            
            // Validar schemaVersion
            if (workspace.app.schemaVersion <= 0) {
                throw WorkspaceLoadException("schemaVersion must be > 0")
            }
            
            return workspace
        } catch (e: Exception) {
            throw WorkspaceLoadException("Failed to load workspace from $path", e)
        }
    }

    /**
     * Guarda el workspace de forma atómica.
     * Estrategia: escribir a archivo temporal, luego renombrar (atomic move).
     * 
     * @param path Ruta del archivo destino
     * @param workspace Workspace a guardar
     * @throws WorkspaceSaveException si falla la escritura
     */
    suspend fun save(path: String, workspace: Workspace) {
        // Validar schemaVersion antes de guardar
        if (workspace.app.schemaVersion <= 0) {
            throw WorkspaceSaveException("schemaVersion must be > 0")
        }
        
        try {
            val content = json.encodeToString(workspace)
            
            // Escritura atómica: temp file + atomic rename
            val tempPath = "$path.tmp"
            fileSystem.writeFile(tempPath, content)
            fileSystem.atomicMove(tempPath, path)
            
        } catch (e: Exception) {
            throw WorkspaceSaveException("Failed to save workspace to $path", e)
        }
    }

    /**
     * Carga el workspace inicial desde el schema de ejemplo.
     * Útil para primera ejecución o reset.
     */
    suspend fun loadInitialSchema(schemaPath: String = "specs/data-schema.json"): Workspace {
        return load(schemaPath)
    }
}

/**
 * Adapter para operaciones de filesystem multiplataforma.
 * Cada plataforma (JVM, Native, JS) implementará su versión.
 */
interface FileSystemAdapter {
    suspend fun readFile(path: String): String
    suspend fun writeFile(path: String, content: String)
    suspend fun atomicMove(sourcePath: String, destPath: String)
    suspend fun exists(path: String): Boolean
    suspend fun delete(path: String)
}

// Excepciones específicas
class WorkspaceLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)
class WorkspaceSaveException(message: String, cause: Throwable? = null) : Exception(message, cause)


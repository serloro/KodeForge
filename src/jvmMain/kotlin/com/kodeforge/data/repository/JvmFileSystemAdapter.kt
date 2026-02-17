package com.kodeforge.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Implementación JVM del FileSystemAdapter.
 * Usa java.io.File y java.nio.file para operaciones atómicas.
 * Guarda los archivos en el directorio home del usuario para aplicaciones empaquetadas.
 */
class JvmFileSystemAdapter : FileSystemAdapter {
    
    private val dataDir: File by lazy {
        val userHome = System.getProperty("user.home")
        val appDataDir = File(userHome, ".kodeforge")
        appDataDir.mkdirs()
        appDataDir
    }
    
    /**
     * Resuelve la ruta del archivo. Si es un path relativo, lo guarda en el directorio de datos de la app.
     */
    private fun resolvePath(path: String): File {
        val file = File(path)
        return if (file.isAbsolute) {
            file
        } else {
            File(dataDir, path)
        }
    }
    
    override suspend fun readFile(path: String): String = withContext(Dispatchers.IO) {
        val file = resolvePath(path)
        if (!file.exists()) {
            throw IllegalArgumentException("File not found: ${file.absolutePath}")
        }
        file.readText(Charsets.UTF_8)
    }

    override suspend fun writeFile(path: String, content: String) = withContext(Dispatchers.IO) {
        val file = resolvePath(path)
        file.parentFile?.mkdirs()
        file.writeText(content, Charsets.UTF_8)
    }

    override suspend fun atomicMove(sourcePath: String, destPath: String): Unit = withContext(Dispatchers.IO) {
        val source = resolvePath(sourcePath).toPath()
        val dest = resolvePath(destPath).toPath()
        
        // ATOMIC_MOVE garantiza atomicidad (o falla si no es posible)
        // REPLACE_EXISTING permite sobrescribir el archivo destino
        Files.move(source, dest, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        Unit
    }

    override suspend fun exists(path: String): Boolean = withContext(Dispatchers.IO) {
        resolvePath(path).exists()
    }

    override suspend fun delete(path: String) = withContext(Dispatchers.IO) {
        val file = resolvePath(path)
        if (file.exists()) {
            file.delete()
        }
    }
}


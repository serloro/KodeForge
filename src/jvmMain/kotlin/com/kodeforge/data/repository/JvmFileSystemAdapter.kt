package com.kodeforge.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Implementación JVM del FileSystemAdapter.
 * Usa java.io.File y java.nio.file para operaciones atómicas.
 */
class JvmFileSystemAdapter : FileSystemAdapter {
    
    override suspend fun readFile(path: String): String = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) {
            throw IllegalArgumentException("File not found: $path")
        }
        file.readText(Charsets.UTF_8)
    }

    override suspend fun writeFile(path: String, content: String) = withContext(Dispatchers.IO) {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeText(content, Charsets.UTF_8)
    }

    override suspend fun atomicMove(sourcePath: String, destPath: String): Unit = withContext(Dispatchers.IO) {
        val source = File(sourcePath).toPath()
        val dest = File(destPath).toPath()
        
        // ATOMIC_MOVE garantiza atomicidad (o falla si no es posible)
        // REPLACE_EXISTING permite sobrescribir el archivo destino
        Files.move(source, dest, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        Unit
    }

    override suspend fun exists(path: String): Boolean = withContext(Dispatchers.IO) {
        File(path).exists()
    }

    override suspend fun delete(path: String) = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }
}


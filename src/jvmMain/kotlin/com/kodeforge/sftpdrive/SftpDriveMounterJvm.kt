package com.kodeforge.sftpdrive

import com.kodeforge.domain.model.SftpDriveMountConfig
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Implementación JVM.
 *
 * En Windows monta unidades con WinFsp + SSHFS-Win.
 * - Si los instaladores vienen incluidos en resources (bundled/winfsp.msi y bundled/sshfs-win.msi),
 *   puede lanzarlos desde la propia app.
 */
actual object SftpDriveMounter {
    private fun isWindows(): Boolean =
        System.getProperty("os.name").lowercase().contains("windows")

    actual fun isSupported(): Boolean = isWindows()

    actual suspend fun ensureDependenciesInstalled(silent: Boolean): String {
        if (!isWindows()) return "Solo disponible en Windows"

        val winfspOk = findWinFspBin() != null
        val sshfsOk = findSshfsExe() != null
        if (winfspOk && sshfsOk) return "Dependencias OK"

        val results = mutableListOf<String>()

        if (!winfspOk) {
            results += runMsiFromResources(
                resourcePath = "bundled/winfsp.msi",
                silent = silent
            )
        }

        if (findSshfsExe() == null) {
            results += runMsiFromResources(
                resourcePath = "bundled/sshfs-win.msi",
                silent = silent
            )
        }

        return results.joinToString("\n").ifBlank { "Dependencias instaladas" }
    }

    actual suspend fun mount(config: SftpDriveMountConfig): String {
        if (!isWindows()) return "Solo disponible en Windows"
        val sshfs = findSshfsExe() ?: error(
            "No se encuentra sshfs.exe. Instala SSHFS-Win (o usa 'Instalar dependencias' en esta utilidad)."
        )

        val drive = config.driveLetter.trim().trimEnd(':').uppercase()
        if (drive.length != 1) error("Letra de unidad inválida: '${config.driveLetter}'")

        val remotePath = when (config.rootMode) {
            "server_root" -> "/"
            "user_home" -> ""
            else -> config.remoteFolder.ifBlank { "/" }
        }

        val target = "${config.username}@${config.remoteHost}:$remotePath"

        val args = mutableListOf(
            sshfs.absolutePath,
            target,
            "$drive:",
            "-p",
            config.remotePort.toString(),
            "-o",
            "reconnect"
        )

        if (config.readOnly) {
            args += listOf("-o", "ro")
        }

        if (config.privateKeyPath.isNotBlank()) {
            args += listOf("-o", "IdentityFile=${config.privateKeyPath}")
        }

        // Mejor UX por defecto: no bloquear por prompt de host key (se puede endurecer luego).
        args += listOf("-o", "StrictHostKeyChecking=no")
        args += listOf("-o", "UserKnownHostsFile=/dev/null")

        val (exit, out) = runProcess(args)
        if (exit != 0) {
            error("No se pudo montar la unidad $drive:. Detalle: $out")
        }

        if (config.openFolderOnConnect) {
            runCatching {
                val toOpen = "$drive:${config.openFolderPath}".replace("//", "/")
                runProcess(listOf("explorer.exe", toOpen))
            }
        }

        return "Unidad montada: $drive:"
    }

    actual suspend fun unmount(driveLetter: String): String {
        if (!isWindows()) return "Solo disponible en Windows"
        val drive = driveLetter.trim().trimEnd(':').uppercase()
        if (drive.length != 1) error("Letra de unidad inválida: '$driveLetter'")

        // SSHFS-Win soporta 'net use X: /delete' para desmontar.
        val (exit, out) = runProcess(listOf("cmd.exe", "/c", "net", "use", "$drive:", "/delete", "/y"))
        if (exit != 0) error("No se pudo desmontar $drive:. Detalle: $out")
        return "Unidad desmontada: $drive:"
    }

    private fun findWinFspBin(): File? {
        val candidates = listOf(
            File("C:/Program Files/WinFsp/bin"),
            File("C:/Program Files (x86)/WinFsp/bin")
        )
        return candidates.firstOrNull { it.exists() && it.isDirectory }
    }

    private fun findSshfsExe(): File? {
        val candidates = listOf(
            File("C:/Program Files/SSHFS-Win/bin/sshfs.exe"),
            File("C:/Program Files (x86)/SSHFS-Win/bin/sshfs.exe"),
            File("C:/Program Files/SSHFS-Win/sshfs.exe"),
            File("C:/Program Files (x86)/SSHFS-Win/sshfs.exe")
        )
        return candidates.firstOrNull { it.exists() && it.isFile }
    }

    private fun runMsiFromResources(resourcePath: String, silent: Boolean): String {
        val resource = Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
            ?: return "Falta recurso: $resourcePath (no incluido en el build)"

        val tmp = writeTempFile(resourcePath.substringAfterLast('/'), resource)
        val args = mutableListOf(
            "msiexec.exe",
            "/i",
            tmp.absolutePathString()
        )
        if (silent) {
            args += listOf("/qn", "/norestart")
        }
        val (exit, out) = runProcess(args)
        if (exit != 0) {
            return "Fallo instalando $resourcePath. Detalle: $out"
        }
        return "Instalador lanzado: $resourcePath"
    }

    private fun writeTempFile(name: String, input: java.io.InputStream): Path {
        val tmpDir = Files.createTempDirectory("kodeforge-install")
        val out = tmpDir.resolve(name)
        Files.copy(input, out)
        return out
    }

    private fun runProcess(args: List<String>): Pair<Int, String> {
        val pb = ProcessBuilder(args)
        pb.redirectErrorStream(true)
        val p = pb.start()
        val out = p.inputStream.bufferedReader().readText()
        val exit = p.waitFor()
        return exit to out.trim()
    }
}

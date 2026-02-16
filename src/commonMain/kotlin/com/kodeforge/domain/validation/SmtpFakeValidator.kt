package com.kodeforge.domain.validation

/**
 * Validador para configuración de SMTP Fake.
 */
object SmtpFakeValidator {
    
    /**
     * Valida el puerto SMTP.
     */
    fun validatePort(port: Int): Result<Unit> {
        return when {
            port < 1 -> Result.failure(Exception("Puerto debe ser mayor que 0"))
            port > 65535 -> Result.failure(Exception("Puerto debe ser menor o igual a 65535"))
            else -> Result.success(Unit)
        }
    }
    
    /**
     * Valida el host SMTP.
     */
    fun validateHost(host: String): Result<Unit> {
        return when {
            host.isBlank() -> Result.failure(Exception("Host no puede estar vacío"))
            else -> Result.success(Unit)
        }
    }
    
    /**
     * Valida un email.
     */
    fun validateEmail(email: String): Result<Unit> {
        return when {
            email.isBlank() -> Result.failure(Exception("Email no puede estar vacío"))
            !email.contains("@") -> Result.failure(Exception("Email debe contener @"))
            email.count { it == '@' } != 1 -> Result.failure(Exception("Email debe contener exactamente un @"))
            email.startsWith("@") -> Result.failure(Exception("Email no puede empezar con @"))
            email.endsWith("@") -> Result.failure(Exception("Email no puede terminar con @"))
            else -> Result.success(Unit)
        }
    }
    
    /**
     * Valida una lista de emails.
     */
    fun validateEmails(emails: List<String>): Result<Unit> {
        emails.forEach { email ->
            validateEmail(email).getOrElse { 
                return Result.failure(Exception("Email inválido: $email - ${it.message}"))
            }
        }
        return Result.success(Unit)
    }
    
    /**
     * Valida un subject.
     */
    fun validateSubject(subject: String): Result<Unit> {
        return when {
            subject.isBlank() -> Result.failure(Exception("Subject no puede estar vacío"))
            else -> Result.success(Unit)
        }
    }
}


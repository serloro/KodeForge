# Tool: SMTP Fake (modelo + persistencia)

## Estado: ✅ COMPLETADO

## Objetivo

Implementar SOLO el modelo y persistencia para la herramienta SMTP Fake por proyecto, siguiendo:
- `specs/spec.md` (SMTP Fake)
- `specs/data-schema.json` (estructura de ejemplo)

## Implementación

### Modelo de Datos

**`SmtpFakeTool`** (ya existía en `Project.kt`):
```kotlin
data class SmtpFakeTool(
    val enabled: Boolean = false,
    val listenHost: String = "127.0.0.1",
    val listenPort: Int = 2525,
    val allowedRecipients: List<String> = emptyList(),
    val storedInbox: List<EmailMessage> = emptyList()
)
```

**`EmailMessage`** (ya existía en `Project.kt`):
```kotlin
data class EmailMessage(
    val id: String,
    val receivedAt: String,
    val from: String,
    val to: List<String>,
    val subject: String,
    val bodyText: String,
    val headers: Map<String, String> = emptyMap()
)
```

### Validador

**`SmtpFakeValidator`** - Validaciones de negocio:
- ✅ `validatePort()` - Puerto entre 1-65535
- ✅ `validateHost()` - Host no vacío
- ✅ `validateEmail()` - Email válido con @
- ✅ `validateEmails()` - Lista de emails válidos
- ✅ `validateSubject()` - Subject no vacío

### Use Cases

**`SmtpFakeUseCases`** - Operaciones CRUD:

**Configuración:**
- ✅ `enableSmtpServer()` - Habilita servidor con host/port
- ✅ `disableSmtpServer()` - Deshabilita servidor
- ✅ `updateSmtpConfig()` - Actualiza host/port

**Destinatarios Permitidos:**
- ✅ `addAllowedRecipient()` - Añade email a la lista
- ✅ `removeAllowedRecipient()` - Elimina email de la lista
- ✅ `getAllowedRecipients()` - Obtiene lista de emails

**Inbox Almacenado:**
- ✅ `addEmailToInbox()` - Añade email capturado
- ✅ `deleteEmailFromInbox()` - Elimina email por ID
- ✅ `clearInbox()` - Limpia todo el inbox
- ✅ `getInbox()` - Obtiene todos los emails
- ✅ `getEmailById()` - Obtiene email específico

## Tests Implementados

### Use Cases Tests (18 tests)

**Configuración (5 tests):**
1. ✅ `enableSmtpServer - enables server with default config`
2. ✅ `enableSmtpServer - enables server with custom config`
3. ✅ `enableSmtpServer - fails with invalid port`
4. ✅ `disableSmtpServer - disables enabled server`
5. ✅ `updateSmtpConfig - updates host and port`

**Destinatarios Permitidos (5 tests):**
6. ✅ `addAllowedRecipient - adds valid email`
7. ✅ `addAllowedRecipient - fails with invalid email`
8. ✅ `addAllowedRecipient - fails with duplicate email`
9. ✅ `removeAllowedRecipient - removes existing email`
10. ✅ `getAllowedRecipients - returns list`

**Inbox Almacenado (8 tests):**
11. ✅ `addEmailToInbox - adds valid email`
12. ✅ `addEmailToInbox - fails with invalid from email`
13. ✅ `addEmailToInbox - fails with invalid to email`
14. ✅ `deleteEmailFromInbox - deletes existing email`
15. ✅ `clearInbox - clears all emails`
16. ✅ `getInbox - returns all emails`
17. ✅ `getEmailById - returns specific email`
18. ✅ `getEmailById - returns null for non-existent email`

### Portability Tests (6 tests)

1. ✅ `portable persistence - smtp config survives save and reload`
   - Valida: enabled, listenHost, listenPort

2. ✅ `portable persistence - allowed recipients preserved`
   - Valida: Lista completa de emails permitidos

3. ✅ `portable persistence - stored inbox preserved`
   - Valida: Todos los campos de EmailMessage (id, receivedAt, from, to, subject, bodyText, headers)

4. ✅ `portable persistence - complete workflow with use cases`
   - Flujo: load → enable → add recipients → add emails → save → reload → assert
   - Valida: Workflow completo con múltiples operaciones

5. ✅ `portable persistence - complex email content preserved`
   - Valida: Emails con comillas, caracteres especiales, Unicode, emojis, múltiples líneas

6. ✅ `portable persistence - empty values preserved`
   - Valida: Listas vacías, servidor deshabilitado

**Total: 24 tests (18 use cases + 6 portability)**

## Ejecución de Tests

```bash
./gradlew jvmTest --tests "SmtpFake*"
```

**Resultado:**
```
BUILD SUCCESSFUL
24 tests completed, 24 passed (100%)
```

## Archivos Creados/Modificados

### Nuevos Archivos (3)

1. **`src/commonMain/kotlin/com/kodeforge/domain/validation/SmtpFakeValidator.kt`**
   - Validador con 5 métodos de validación
   - Validaciones: port, host, email, emails, subject

2. **`src/commonMain/kotlin/com/kodeforge/domain/usecases/SmtpFakeUseCases.kt`**
   - Use cases con 11 operaciones CRUD
   - Configuración: enable, disable, update
   - Recipients: add, remove, get
   - Inbox: add, delete, clear, get, getById

3. **`src/jvmTest/kotlin/com/kodeforge/SmtpFakeUseCasesTest.kt`**
   - 18 tests de use cases
   - Cobertura completa de todas las operaciones

4. **`src/jvmTest/kotlin/com/kodeforge/SmtpFakePortabilityTest.kt`**
   - 6 tests de portabilidad
   - Validación completa de persistencia JSON

### Archivos Existentes (sin cambios)

- **`src/commonMain/kotlin/com/kodeforge/domain/model/Project.kt`**
  - El modelo `SmtpFakeTool` y `EmailMessage` ya existían
  - No se realizaron cambios

## Validaciones

### ✅ Persistencia JSON

Todos los campos se persisten correctamente:
- ✅ `enabled` - Boolean
- ✅ `listenHost` - String
- ✅ `listenPort` - Int
- ✅ `allowedRecipients` - List<String>
- ✅ `storedInbox` - List<EmailMessage>
  - ✅ `id`, `receivedAt`, `from`, `subject`, `bodyText`
  - ✅ `to` - List<String>
  - ✅ `headers` - Map<String, String>

### ✅ Casos Especiales

- ✅ Emails con caracteres especiales (+, ., subdominios)
- ✅ Subjects con comillas y caracteres especiales
- ✅ Body con múltiples líneas
- ✅ Body con Unicode y emojis
- ✅ Headers con espacios y comas
- ✅ Listas vacías
- ✅ Servidor deshabilitado

### ✅ Validaciones de Negocio

- ✅ Puerto válido (1-65535)
- ✅ Host no vacío
- ✅ Email válido (contiene @, no empieza/termina con @, solo un @)
- ✅ Subject no vacío
- ✅ No permite emails duplicados en allowedRecipients

## Características Técnicas

### Arquitectura
- Separación clara entre modelo, validación y use cases
- Uso de `Result<T>` para manejo de errores
- Validaciones antes de modificar el workspace
- Generación automática de IDs y timestamps

### Persistencia
- Serialización/deserialización con `kotlinx.serialization`
- Formato JSON legible (prettyPrint)
- Portable entre plataformas
- Sobrevive reinicio de aplicación

### Generación de Datos
- IDs únicos: `mail_<timestamp>_<random>`
- Timestamps en formato ISO 8601 UTC
- Inmutabilidad del workspace (copy)

## NO Implementado (por diseño)

❌ UI del tool SMTP Fake
❌ Servidor SMTP real
❌ Envío real de emails
❌ Recepción real de emails
❌ Filtrado de emails por remitente/asunto
❌ Búsqueda en inbox

Estos elementos se implementarán en fases posteriores.

## Próximos Pasos

### Fase siguiente: UI SMTP Fake
- Pantalla de configuración (enable/disable, host, port)
- Lista de destinatarios permitidos (CRUD)
- Inbox con lista de emails
- Detalle de email seleccionado
- Filtros y búsqueda

### Fase posterior: Servidor SMTP Real
- Implementación de servidor SMTP con librería multiplataforma
- Recepción real de emails
- Validación de destinatarios permitidos
- Almacenamiento automático en inbox

## Conclusión

✅ **Modelo y persistencia COMPLETADOS**

La herramienta SMTP Fake tiene:
- ✅ Modelo de datos completo y serializable
- ✅ Validador con reglas de negocio
- ✅ Use cases para todas las operaciones CRUD
- ✅ 24 tests (100% pass rate)
- ✅ Persistencia portable en JSON
- ✅ Casos especiales validados

El modelo está **listo para la implementación de la UI** en la siguiente fase.


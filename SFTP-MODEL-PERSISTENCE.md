# SFTP — Modelo y Persistencia

## Objetivo

Implementar SOLO el modelo y persistencia para la herramienta SFTP por proyecto, siguiendo:
- `specs/spec.md` (SFTP / PuTTY)
- `specs/data-schema.json`

## Implementación

### 1. Modelo de Datos

El modelo SFTP ya existía en `Project.kt`, pero se validó y utilizó correctamente:

#### SftpTool

```kotlin
@Serializable
data class SftpTool(
    val enabled: Boolean = false,
    val connections: List<SftpConnection> = emptyList()
)
```

#### SftpConnection

```kotlin
@Serializable
data class SftpConnection(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val auth: AuthConfig
)
```

#### AuthConfig

```kotlin
@Serializable
data class AuthConfig(
    val type: String,      // "password", "key", o "none"
    val valueRef: String   // Referencia al secreto (ej: "secret:sftp_prod")
)
```

### 2. Validador (SftpValidator)

**Archivo:** `src/commonMain/kotlin/com/kodeforge/domain/validation/SftpValidator.kt`

**Funciones:**

- ✅ `validateConnection(connection: SftpConnection)`: Valida una conexión individual
  - Nombre no vacío
  - Host no vacío
  - Puerto en rango válido (1-65535)
  - Username no vacío
  - Auth type válido ("password", "key", "none")
  - Auth valueRef no vacío si type no es "none"

- ✅ `validateUniqueNames(connections: List<SftpConnection>)`: Valida nombres únicos

- ✅ `validateUniqueIds(connections: List<SftpConnection>)`: Valida IDs únicos

- ✅ `validateTool(tool: SftpTool)`: Valida todo el SftpTool

### 3. Casos de Uso (SftpUseCases)

**Archivo:** `src/commonMain/kotlin/com/kodeforge/domain/usecases/SftpUseCases.kt`

**Métodos implementados:**

#### Configuración
- ✅ `enableSftpTool(workspace, projectId)`: Habilita el tool
- ✅ `disableSftpTool(workspace, projectId)`: Deshabilita el tool

#### CRUD Conexiones
- ✅ `addConnection(...)`: Añade una nueva conexión SFTP
  - Valida que no exista nombre duplicado
  - Genera ID único con timestamp + contador
  - Soporta auth types: password, key, none

- ✅ `updateConnection(...)`: Actualiza una conexión existente
  - Valida que no se duplique nombre con otra conexión
  - Preserva el ID original

- ✅ `deleteConnection(workspace, projectId, connectionId)`: Elimina una conexión

- ✅ `getConnection(workspace, projectId, connectionId)`: Obtiene una conexión por ID

- ✅ `getConnections(workspace, projectId)`: Obtiene todas las conexiones

**Nota importante:** Se implementó un contador incremental en la generación de IDs para evitar colisiones cuando se crean múltiples conexiones en el mismo milisegundo:

```kotlin
private var idCounter = 0

private fun generateId(prefix: String): String {
    idCounter++
    return "${prefix}_${Clock.System.now().toEpochMilliseconds()}_$idCounter"
}
```

### 4. Tests de Use Cases

**Archivo:** `src/jvmTest/kotlin/com/kodeforge/SftpUseCasesTest.kt`

**Total:** 16 tests

#### Configuración (2 tests)
1. ✅ `enable SFTP tool`
2. ✅ `disable SFTP tool`

#### CRUD Conexiones (14 tests)
3. ✅ `add connection with password auth`
4. ✅ `add connection with key auth`
5. ✅ `add multiple connections`
6. ✅ `cannot add connection with duplicate name`
7. ✅ `cannot add connection with blank name`
8. ✅ `cannot add connection with blank host`
9. ✅ `cannot add connection with invalid port`
10. ✅ `cannot add connection with blank username`
11. ✅ `update connection`
12. ✅ `cannot update connection to duplicate name`
13. ✅ `delete connection`
14. ✅ `cannot delete non-existent connection`
15. ✅ `get connection by id`
16. ✅ `get non-existent connection returns null`

### 5. Tests de Portabilidad

**Archivo:** `src/jvmTest/kotlin/com/kodeforge/SftpPortabilityTest.kt`

**Total:** 6 tests

1. ✅ `basic connection persists correctly`
   - Añade una conexión con password auth
   - Guarda → Recarga → Valida todos los campos

2. ✅ `multiple connections persist correctly`
   - Añade 3 conexiones (password, key, none)
   - Valida que todas se recuperan exactamente igual

3. ✅ `enabled state persists correctly`
   - Habilita el tool
   - Añade conexión
   - Valida que el estado enabled se preserva

4. ✅ `special characters in connection data persist correctly`
   - Conexión con caracteres especiales: 'quotes', "double", áéíóú, ñ, @, /, \, :
   - Valida que todos los caracteres se preservan

5. ✅ `complete workflow - add, update, delete persists correctly`
   - Flujo completo: habilitar → añadir 3 → actualizar 1 → eliminar 1
   - Valida estado final con 2 conexiones

6. ✅ `empty connections list persists correctly`
   - Valida que un tool sin conexiones se persiste correctamente

## Resultados de Tests

### Ejecución

```bash
./gradlew jvmTest --tests "com.kodeforge.SftpUseCasesTest"
./gradlew jvmTest --tests "com.kodeforge.SftpPortabilityTest"
./gradlew build
```

**Resultado:** ✅ **BUILD SUCCESSFUL**

- **SftpUseCasesTest:** 16/16 tests pasados
- **SftpPortabilityTest:** 6/6 tests pasados
- **Total:** 22 tests pasados

## Validaciones Implementadas

### Validación de Conexión
- ✅ Nombre obligatorio (no vacío)
- ✅ Host obligatorio (no vacío)
- ✅ Puerto en rango válido (1-65535)
- ✅ Username obligatorio (no vacío)
- ✅ Auth type válido ("password", "key", "none")
- ✅ Auth valueRef obligatorio si type no es "none"

### Validación de Tool
- ✅ No permite nombres duplicados
- ✅ No permite IDs duplicados
- ✅ Valida cada conexión individualmente

### Persistencia
- ✅ Todas las conexiones se persisten en workspace JSON
- ✅ Estado `enabled` se persiste correctamente
- ✅ Caracteres especiales se preservan
- ✅ Flujo completo (CRUD) es portable

## Casos de Uso Soportados

### Auth Types
- ✅ **Password:** `auth: { type: "password", valueRef: "secret:sftp_prod" }`
- ✅ **Key:** `auth: { type: "key", valueRef: "secret:sftp_key" }`
- ✅ **None:** `auth: { type: "none", valueRef: "" }`

### Puertos
- ✅ Puerto estándar: 22
- ✅ Puertos personalizados: 2222, 3333, etc.
- ✅ Validación de rango: 1-65535

### Hosts
- ✅ Dominios: `sftp.production.com`
- ✅ IPs: `127.0.0.1`
- ✅ Localhost: `localhost`
- ✅ Caracteres especiales: `sftp-ñ.example.com`

## Archivos Modificados

```
src/commonMain/kotlin/com/kodeforge/domain/validation/SftpValidator.kt
src/commonMain/kotlin/com/kodeforge/domain/usecases/SftpUseCases.kt
src/jvmTest/kotlin/com/kodeforge/SftpUseCasesTest.kt
src/jvmTest/kotlin/com/kodeforge/SftpPortabilityTest.kt
```

**Archivos existentes (no modificados):**
- `src/commonMain/kotlin/com/kodeforge/domain/model/Project.kt` (modelo ya existía)

## NO Implementado (según requisitos)

- ❌ UI (pantallas, formularios, listas)
- ❌ Conexión SFTP real
- ❌ Lectura de archivos remotos
- ❌ Navegación de directorios
- ❌ Transferencia de archivos

## Conclusión

✅ **Modelo y persistencia completados exitosamente**

El modelo SFTP es **100% portable**:
- Las **conexiones** se recuperan exactamente igual
- El **estado enabled** se preserva
- Los **caracteres especiales** se mantienen
- El **flujo CRUD completo** es portable

Al copiar el `workspace.json` a otro sistema, **todas las conexiones SFTP se preservan sin pérdida de información**.

---

**Fecha:** 2026-02-16  
**Tests:** 22/22 pasados (16 use cases + 6 portabilidad)  
**Estado:** ✅ Completado


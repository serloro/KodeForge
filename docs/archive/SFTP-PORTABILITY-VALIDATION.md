# SFTP — Validación de Portabilidad

## Objetivo

Validar que al copiar el workspace JSON, todas las conexiones SFTP y configuración se recuperan exactamente igual.

## Tests Implementados

Los tests de portabilidad ya estaban implementados en el archivo `SftpPortabilityTest.kt` desde la fase de modelo y persistencia. Se ejecutaron para validar la portabilidad completa incluyendo la nueva funcionalidad del explorador.

### Archivo de Tests

**`src/jvmTest/kotlin/com/kodeforge/SftpPortabilityTest.kt`**

Total: **6 tests de portabilidad**

### Tests Ejecutados

#### 1. `basic connection persists correctly`

**Propósito:** Valida que una conexión básica se persiste y recupera correctamente.

**Flujo:**
1. Crear workspace con proyecto
2. Añadir conexión SFTP con password auth
3. Guardar workspace en JSON
4. Recargar workspace desde JSON
5. Validar que todos los campos se recuperaron exactamente igual

**Validaciones:**
- ✅ `id` se preserva
- ✅ `name` se preserva
- ✅ `host` se preserva
- ✅ `port` se preserva
- ✅ `username` se preserva
- ✅ `auth.type` se preserva
- ✅ `auth.valueRef` se preserva

**Datos de prueba:**
```kotlin
name = "Production SFTP"
host = "sftp.production.com"
port = 22
username = "deploy"
authType = "password"
authValueRef = "secret:sftp_prod"
```

#### 2. `multiple connections persist correctly`

**Propósito:** Valida que múltiples conexiones con diferentes configuraciones se persisten correctamente.

**Flujo:**
1. Crear workspace
2. Añadir 3 conexiones con diferentes auth types:
   - Production (password)
   - Staging (key, puerto 2222)
   - Development (none)
3. Guardar workspace
4. Recargar workspace
5. Validar que todas las conexiones se recuperaron

**Validaciones:**
- ✅ Número de conexiones: 3
- ✅ Orden de conexiones preservado
- ✅ Todos los campos de cada conexión preservados
- ✅ Auth types diferentes: password, key, none
- ✅ Puertos diferentes: 22, 2222

#### 3. `enabled state persists correctly`

**Propósito:** Valida que el estado enabled/disabled del tool se persiste correctamente.

**Flujo:**
1. Crear workspace
2. Habilitar SftpTool
3. Añadir conexión
4. Guardar workspace
5. Recargar workspace
6. Validar estado enabled y conexiones

**Validaciones:**
- ✅ `enabled = true` se preserva
- ✅ Conexiones se preservan junto con el estado

#### 4. `special characters in connection data persist correctly`

**Propósito:** Valida que caracteres especiales en los datos de conexión se preservan correctamente.

**Flujo:**
1. Crear workspace
2. Añadir conexión con caracteres especiales:
   - Comillas simples y dobles en nombre
   - Acentos (áéíóú)
   - Ñ en host
   - @ en username
   - Slashes, backslashes y colons en valueRef
3. Guardar workspace
4. Recargar workspace
5. Validar que todos los caracteres especiales se preservaron

**Validaciones:**
- ✅ `'comillas'` se preserva
- ✅ `"dobles"` se preserva
- ✅ `áéíóú` se preserva
- ✅ `ñ` se preserva
- ✅ `@` se preserva
- ✅ `/`, `\`, `:` se preservan

**Datos de prueba:**
```kotlin
name = "Server with 'quotes' and \"double\" and áéíóú"
host = "sftp-ñ.example.com"
username = "user@domain.com"
authValueRef = "secret:with/slashes\\and:colons"
```

#### 5. `complete workflow - add, update, delete persists correctly`

**Propósito:** Valida que un flujo completo de operaciones CRUD se persiste correctamente.

**Flujo:**
1. Crear workspace vacío
2. Habilitar SftpTool
3. Añadir 3 conexiones
4. Actualizar la segunda conexión (cambiar todos los campos)
5. Eliminar la tercera conexión
6. Guardar workspace
7. Recargar workspace (simula copiar JSON a otro sistema)
8. Validar estado final

**Validaciones:**
- ✅ Tool habilitado
- ✅ 2 conexiones finales (3 - 1 eliminada)
- ✅ Primera conexión sin cambios
- ✅ Segunda conexión con todos los campos actualizados:
  - Nombre: "Connection 2 Updated"
  - Host: "server2-new.com"
  - Puerto: 3333
  - Username: "user2_new"
  - ValueRef: "secret:2_new"
- ✅ Tercera conexión eliminada (no existe)

#### 6. `empty connections list persists correctly`

**Propósito:** Valida que un tool sin conexiones se persiste correctamente.

**Flujo:**
1. Crear workspace con SftpTool vacío
2. Guardar workspace
3. Recargar workspace
4. Validar estado

**Validaciones:**
- ✅ `enabled = false` se preserva
- ✅ `connections = []` (lista vacía) se preserva

## Resultados de Ejecución

### Comando

```bash
./gradlew jvmTest --tests "com.kodeforge.SftpPortabilityTest"
```

### Resultado

✅ **BUILD SUCCESSFUL**

```
> Task :jvmTest

BUILD SUCCESSFUL in 956ms
4 actionable tasks: 2 executed, 2 up-to-date
```

**Tests ejecutados:** 6/6  
**Tests pasados:** 6/6  
**Tests fallidos:** 0

## Casos de Uso Validados

### Configuración del Tool
- ✅ Estado enabled/disabled se preserva
- ✅ Lista vacía de conexiones se preserva

### Conexiones Básicas
- ✅ Una conexión se preserva completamente
- ✅ Múltiples conexiones se preservan
- ✅ Orden de conexiones se mantiene

### Auth Types
- ✅ **Password:** `auth: { type: "password", valueRef: "secret:..." }`
- ✅ **Key:** `auth: { type: "key", valueRef: "secret:..." }`
- ✅ **None:** `auth: { type: "none", valueRef: "" }`

### Puertos
- ✅ Puerto estándar: 22
- ✅ Puertos personalizados: 2222, 3333, etc.

### Caracteres Especiales
- ✅ Comillas simples y dobles
- ✅ Acentos y caracteres Unicode
- ✅ Caracteres especiales en rutas (@, /, \, :)

### Operaciones CRUD
- ✅ Crear conexión
- ✅ Actualizar conexión (todos los campos)
- ✅ Eliminar conexión
- ✅ Estado final correcto después de múltiples operaciones

## Estructura de Datos Validada

### SftpTool

```kotlin
@Serializable
data class SftpTool(
    val enabled: Boolean = false,              // ✅ Validado
    val connections: List<SftpConnection> = emptyList()  // ✅ Validado
)
```

### SftpConnection

```kotlin
@Serializable
data class SftpConnection(
    val id: String,                // ✅ Validado
    val name: String,              // ✅ Validado
    val host: String,              // ✅ Validado
    val port: Int = 22,            // ✅ Validado
    val username: String,          // ✅ Validado
    val auth: AuthConfig           // ✅ Validado
)
```

### AuthConfig

```kotlin
@Serializable
data class AuthConfig(
    val type: String,              // ✅ Validado
    val valueRef: String           // ✅ Validado
)
```

## Portabilidad Confirmada

✅ **Al copiar el workspace JSON a otro sistema:**

1. **Todas las conexiones SFTP se recuperan exactamente igual:**
   - IDs preservados
   - Nombres preservados
   - Hosts y puertos preservados
   - Usuarios preservados
   - Configuración de autenticación preservada
   - Caracteres especiales preservados

2. **La configuración del tool se recupera exactamente igual:**
   - Estado enabled/disabled preservado
   - Lista de conexiones preservada (incluso si está vacía)

3. **Las operaciones CRUD se reflejan correctamente:**
   - Conexiones añadidas están presentes
   - Conexiones actualizadas tienen los nuevos valores
   - Conexiones eliminadas no están presentes

## Archivos Involucrados

```
src/jvmTest/kotlin/com/kodeforge/SftpPortabilityTest.kt (existente, no modificado)
```

**Nota:** Los tests de portabilidad ya estaban implementados desde la fase de modelo y persistencia (T: SFTP modelo + persistencia). Esta validación confirma que siguen funcionando correctamente con la nueva funcionalidad del explorador.

## Conclusión

✅ **Validación completada exitosamente**

Todos los datos de SFTP son **100% portables**:
- Las **conexiones** se recuperan exactamente igual
- La **configuración** se recupera exactamente igual
- Los **caracteres especiales** se preservan
- Las **operaciones CRUD** se reflejan correctamente

Al copiar el `workspace.json` a otro sistema, **todas las conexiones SFTP se preservan sin pérdida de información**, permitiendo que el usuario pueda conectarse inmediatamente a los mismos servidores sin necesidad de reconfigurar nada.

---

**Fecha:** 2026-02-16  
**Tests:** 6/6 pasados  
**Estado:** ✅ Completado  
**Archivos modificados:** Ninguno (tests ya existían)


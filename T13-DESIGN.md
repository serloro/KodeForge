# T13 - Tool REST/SOAP (Modelo + Persistencia) - Diseño

**Objetivo:** Implementar modelo y persistencia para la herramienta REST/SOAP.

**Alcance:** SOLO modelo de datos y CRUD. NO UI ni servidor real.

---

## 📋 ANÁLISIS DE ESPECIFICACIONES

### **specs/data-schema.json:**

```json
{
  "restSoap": {
    "enabled": true,
    "clientHistory": [
      {
        "id": "req_001",
        "at": "2026-02-15T12:10:00Z",
        "type": "REST",
        "method": "GET",
        "url": "https://api.local.test/health",
        "headers": { "accept": "application/json" },
        "body": null,
        "response": { "status": 200, "body": "{\"ok\":true}" }
      }
    ],
    "mockServer": {
      "enabled": true,
      "listenHost": "127.0.0.1",
      "listenPort": 8089,
      "mode": "catchAll",
      "routes": [
        {
          "id": "route_001",
          "method": "POST",
          "path": "/v1/login",
          "response": {
            "status": 200,
            "headers": { "content-type": "application/json" },
            "body": "{\"token\":\"fake-token\"}"
          }
        }
      ],
      "capturedRequests": [
        {
          "id": "cap_001",
          "at": "2026-02-15T13:00:00Z",
          "method": "POST",
          "path": "/anything",
          "headers": { "content-type": "application/json" },
          "body": "{\"hello\":\"world\"}"
        }
      ]
    }
  }
}
```

---

## 🏗️ MODELO DE DATOS (YA EXISTE)

El modelo ya está definido en `Project.kt`:

- `RestSoapTool`
- `HttpRequest`
- `HttpResponse`
- `MockServer`
- `MockRoute`
- `CapturedRequest`

**No requiere modificaciones.**

---

## 📊 CASOS DE USO

### **RestSoapUseCases:**

```kotlin
class RestSoapUseCases {
    
    // ===== Client History =====
    
    fun addRequestToHistory(
        workspace: Workspace,
        projectId: String,
        type: String, // REST, SOAP
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        response: HttpResponse? = null
    ): Result<Workspace>
    
    fun clearHistory(
        workspace: Workspace,
        projectId: String
    ): Result<Workspace>
    
    fun getHistory(
        workspace: Workspace,
        projectId: String
    ): List<HttpRequest>
    
    // ===== Mock Server Config =====
    
    fun enableMockServer(
        workspace: Workspace,
        projectId: String,
        listenHost: String = "127.0.0.1",
        listenPort: Int = 8089
    ): Result<Workspace>
    
    fun disableMockServer(
        workspace: Workspace,
        projectId: String
    ): Result<Workspace>
    
    fun setMockServerMode(
        workspace: Workspace,
        projectId: String,
        mode: String // "catchAll" | "defined"
    ): Result<Workspace>
    
    // ===== Mock Routes =====
    
    fun addRoute(
        workspace: Workspace,
        projectId: String,
        method: String,
        path: String,
        responseStatus: Int,
        responseBody: String? = null,
        responseHeaders: Map<String, String> = emptyMap()
    ): Result<Workspace>
    
    fun updateRoute(
        workspace: Workspace,
        projectId: String,
        routeId: String,
        method: String? = null,
        path: String? = null,
        responseStatus: Int? = null,
        responseBody: String? = null,
        responseHeaders: Map<String, String>? = null
    ): Result<Workspace>
    
    fun deleteRoute(
        workspace: Workspace,
        projectId: String,
        routeId: String
    ): Result<Workspace>
    
    fun getRoutes(
        workspace: Workspace,
        projectId: String
    ): List<MockRoute>
    
    // ===== Captured Requests =====
    
    fun addCapturedRequest(
        workspace: Workspace,
        projectId: String,
        method: String,
        path: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null
    ): Result<Workspace>
    
    fun clearCapturedRequests(
        workspace: Workspace,
        projectId: String
    ): Result<Workspace>
    
    fun getCapturedRequests(
        workspace: Workspace,
        projectId: String
    ): List<CapturedRequest>
}
```

---

## 🎯 VALIDACIONES

### **RestSoapValidator:**

```kotlin
object RestSoapValidator {
    
    sealed class ValidationError(val message: String) {
        object MethodEmpty : ValidationError("El método HTTP no puede estar vacío")
        object MethodInvalid : ValidationError("Método HTTP inválido")
        object UrlEmpty : ValidationError("La URL no puede estar vacía")
        object UrlInvalid : ValidationError("URL inválida")
        object PathEmpty : ValidationError("El path no puede estar vacío")
        object PathInvalid : ValidationError("El path debe empezar con /")
        object PortInvalid : ValidationError("El puerto debe estar entre 1 y 65535")
        object ModeInvalid : ValidationError("Modo debe ser 'catchAll' o 'defined'")
        object StatusInvalid : ValidationError("El status debe estar entre 100 y 599")
    }
    
    fun validateMethod(method: String): Result<Unit>
    fun validateUrl(url: String): Result<Unit>
    fun validatePath(path: String): Result<Unit>
    fun validatePort(port: Int): Result<Unit>
    fun validateMode(mode: String): Result<Unit>
    fun validateStatus(status: Int): Result<Unit>
}
```

---

## 📁 ARCHIVOS A CREAR

1. **`src/commonMain/kotlin/com/kodeforge/domain/validation/RestSoapValidator.kt`**
   - Validaciones de método, URL, path, puerto, modo, status

2. **`src/commonMain/kotlin/com/kodeforge/domain/usecases/RestSoapUseCases.kt`**
   - CRUD de client history
   - CRUD de mock server config
   - CRUD de routes
   - CRUD de captured requests

3. **`src/jvmTest/kotlin/com/kodeforge/RestSoapUseCasesTest.kt`**
   - Tests de CRUD y persistencia

4. **`src/jvmTest/kotlin/com/kodeforge/RestSoapPortabilityTest.kt`**
   - Tests de load/save/reload

---

## 📁 ARCHIVOS A MODIFICAR

Ninguno (el modelo ya existe en `Project.kt`).

---

## 🧪 TESTS

### **RestSoapUseCasesTest.kt:**

```kotlin
class RestSoapUseCasesTest {
    
    @Test
    fun `addRequestToHistory - adds request to client history`()
    
    @Test
    fun `clearHistory - removes all requests`()
    
    @Test
    fun `enableMockServer - enables mock server with config`()
    
    @Test
    fun `setMockServerMode - changes mode`()
    
    @Test
    fun `addRoute - adds route to mock server`()
    
    @Test
    fun `updateRoute - updates existing route`()
    
    @Test
    fun `deleteRoute - removes route`()
    
    @Test
    fun `addCapturedRequest - adds captured request`()
    
    @Test
    fun `clearCapturedRequests - removes all captured requests`()
}
```

### **RestSoapPortabilityTest.kt:**

```kotlin
class RestSoapPortabilityTest {
    
    @Test
    fun `portable persistence - rest soap config survives save and reload`()
    
    @Test
    fun `portable persistence - client history preserved`()
    
    @Test
    fun `portable persistence - mock routes preserved`()
    
    @Test
    fun `portable persistence - captured requests preserved`()
}
```

---

## ✅ CRITERIOS DE ACEPTACIÓN

| Requisito | Implementación |
|-----------|----------------|
| Modelo RestSoapTool | Ya existe en Project.kt |
| Client history CRUD | RestSoapUseCases |
| Mock server config | RestSoapUseCases |
| Routes CRUD | RestSoapUseCases |
| Captured requests CRUD | RestSoapUseCases |
| Validaciones | RestSoapValidator |
| Persistencia en JSON | Serializable |
| Tests de CRUD | RestSoapUseCasesTest |
| Tests de portabilidad | RestSoapPortabilityTest |
| NO UI | Correcto |
| NO servidor real | Correcto |

---

## 🎯 PLAN DE IMPLEMENTACIÓN

1. ✅ Crear `RestSoapValidator.kt`
2. ✅ Crear `RestSoapUseCases.kt`
3. ✅ Crear `RestSoapUseCasesTest.kt`
4. ✅ Crear `RestSoapPortabilityTest.kt`
5. ✅ Compilar y ejecutar tests
6. ✅ Validar persistencia

---

**Tiempo estimado:** 2-3 horas  
**Complejidad:** Media  
**Dependencias:** Project, Workspace, WorkspaceRepository

---

*Diseño completado - Listo para implementación*


# T14 - Tool REST/SOAP (UI Cliente + Historial) - Diseño

**Objetivo:** Implementar UI del cliente REST/SOAP.

**Alcance:** UI de cliente + historial. NO mock server.

---

## 📋 ANÁLISIS

### **Requisitos:**

1. **Selector REST/SOAP**
2. **Para REST:**
   - Método (GET, POST, PUT, DELETE, PATCH)
   - URL
   - Headers (key/value)
   - Body (raw)
3. **Para SOAP:**
   - Endpoint URL
   - SOAPAction (opcional)
   - Headers (key/value)
   - Body XML
4. **Botón "Enviar"**
5. **Respuesta:**
   - Status
   - Headers
   - Body
6. **Historial:**
   - Guardar en clientHistory
   - Mostrar historial
   - Click para recargar request

### **Restricciones:**

- Modo simulado temporal (sin red real por ahora)
- Estilo consistente con p2.png
- NO implementar mock server

---

## 🎨 DISEÑO VISUAL

```
┌────────────────────────────────────────────────────────────┐
│ ← REST/SOAP Cliente                                        │
├────────────────────────────────────────────────────────────┤
│ ┌────────────┐  ┌──────────────────────────────────────┐  │
│ │ Historial  │  │ [REST] [SOAP]                        │  │
│ │            │  │                                      │  │
│ │ GET /users │  │ Método: [GET ▼]                      │  │
│ │ 200 OK     │  │ URL: https://api.test.com/users      │  │
│ │            │  │                                      │  │
│ │ POST /login│  │ Headers:                             │  │
│ │ 200 OK     │  │ [+ Añadir header]                    │  │
│ │            │  │ ┌──────────────┬──────────────────┐  │  │
│ │            │  │ │ content-type │ application/json │  │  │
│ │            │  │ └──────────────┴──────────────────┘  │  │
│ │            │  │                                      │  │
│ │            │  │ Body:                                │  │
│ │            │  │ ┌──────────────────────────────────┐ │  │
│ │            │  │ │ {"name": "John"}                 │ │  │
│ │            │  │ └──────────────────────────────────┘ │  │
│ │            │  │                                      │  │
│ │            │  │ [Enviar (Simulado)]                  │  │
│ │            │  │                                      │  │
│ │            │  │ Respuesta:                           │  │
│ │            │  │ Status: 200 OK                       │  │
│ │            │  │ ┌──────────────────────────────────┐ │  │
│ │            │  │ │ {"id": 1, "name": "John"}        │ │  │
│ │            │  │ └──────────────────────────────────┘ │  │
│ └────────────┘  └──────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
```

---

## 🏗️ ARQUITECTURA

### **Componentes:**

1. **`RestSoapClientScreen.kt`** - Pantalla principal
   - Layout 2 columnas: Historial + Cliente
   - Estado: tipo (REST/SOAP), request, response

2. **`RestSoapClientForm.kt`** - Formulario del cliente
   - Selector REST/SOAP
   - Campos según tipo
   - Botón enviar

3. **`HeadersEditor.kt`** - Editor de headers
   - Lista de key/value
   - Añadir/eliminar headers

4. **`ResponseViewer.kt`** - Visor de respuesta
   - Status
   - Headers
   - Body (con scroll)

5. **`RequestHistoryList.kt`** - Lista de historial
   - Items de request
   - Click para recargar

6. **`RequestHistoryItem.kt`** - Item de historial
   - Método + URL
   - Status
   - Timestamp

---

## 📊 ESTADO

### **RestSoapClientScreen State:**

```kotlin
var requestType by remember { mutableStateOf("REST") } // REST, SOAP
var method by remember { mutableStateOf("GET") }
var url by remember { mutableStateOf("") }
var headers by remember { mutableStateOf(listOf<Pair<String, String>>()) }
var body by remember { mutableStateOf("") }
var soapAction by remember { mutableStateOf("") }

var response by remember { mutableStateOf<HttpResponse?>(null) }
var isLoading by remember { mutableStateOf(false) }
var errorMessage by remember { mutableStateOf<String?>(null) }
```

---

## 🔧 FUNCIONALIDADES

### **Enviar Request (Simulado):**

```kotlin
fun sendRequest() {
    isLoading = true
    
    // Simular delay
    delay(500)
    
    // Generar respuesta simulada
    val simulatedResponse = when (method) {
        "GET" -> HttpResponse(
            status = 200,
            body = "[{\"id\":1,\"name\":\"John\"},{\"id\":2,\"name\":\"Jane\"}]",
            headers = mapOf("content-type" to "application/json")
        )
        "POST" -> HttpResponse(
            status = 201,
            body = "{\"id\":3,\"name\":\"Created\"}",
            headers = mapOf("content-type" to "application/json")
        )
        "PUT" -> HttpResponse(
            status = 200,
            body = "{\"id\":1,\"name\":\"Updated\"}",
            headers = mapOf("content-type" to "application/json")
        )
        "DELETE" -> HttpResponse(
            status = 204,
            body = null,
            headers = emptyMap()
        )
        else -> HttpResponse(
            status = 200,
            body = "{\"ok\":true}",
            headers = mapOf("content-type" to "application/json")
        )
    }
    
    response = simulatedResponse
    
    // Guardar en historial
    val result = restSoapUseCases.addRequestToHistory(
        workspace = workspace,
        projectId = projectId,
        type = requestType,
        method = method,
        url = url,
        headers = headers.toMap(),
        body = body.ifBlank { null },
        response = simulatedResponse
    )
    
    if (result.isSuccess) {
        onWorkspaceUpdate(result.getOrNull()!!)
    }
    
    isLoading = false
}
```

---

## 📁 ARCHIVOS A CREAR

1. **`src/commonMain/kotlin/com/kodeforge/ui/components/HeadersEditor.kt`**
2. **`src/commonMain/kotlin/com/kodeforge/ui/components/ResponseViewer.kt`**
3. **`src/commonMain/kotlin/com/kodeforge/ui/components/RequestHistoryItem.kt`**
4. **`src/commonMain/kotlin/com/kodeforge/ui/components/RequestHistoryList.kt`**
5. **`src/commonMain/kotlin/com/kodeforge/ui/components/RestSoapClientForm.kt`**
6. **`src/commonMain/kotlin/com/kodeforge/ui/screens/RestSoapClientScreen.kt`**

---

## 📁 ARCHIVOS A MODIFICAR

1. **`src/commonMain/kotlin/com/kodeforge/ui/screens/ToolScreen.kt`**
   - Añadir caso para "rest" → `RestSoapClientScreen`

---

## ✅ CRITERIOS DE ACEPTACIÓN

| Requisito | Implementación |
|-----------|----------------|
| Selector REST/SOAP | FilterChips |
| Método (REST) | Dropdown |
| URL | TextField |
| Headers | HeadersEditor |
| Body | TextField multiline |
| SOAPAction (SOAP) | TextField |
| Botón Enviar | Button con modo simulado |
| Respuesta | ResponseViewer |
| Historial | RequestHistoryList |
| Guardar en JSON | RestSoapUseCases |
| Estilo p2.png | Cards, spacing |
| NO mock server | Correcto |

---

## 🎯 PLAN DE IMPLEMENTACIÓN

1. ✅ Crear `HeadersEditor.kt`
2. ✅ Crear `ResponseViewer.kt`
3. ✅ Crear `RequestHistoryItem.kt`
4. ✅ Crear `RequestHistoryList.kt`
5. ✅ Crear `RestSoapClientForm.kt`
6. ✅ Crear `RestSoapClientScreen.kt`
7. ✅ Modificar `ToolScreen.kt`
8. ✅ Compilar y validar

---

**Tiempo estimado:** 3-4 horas  
**Complejidad:** Alta  
**Dependencias:** RestSoapUseCases, ToolScreen

---

*Diseño completado - Listo para implementación*


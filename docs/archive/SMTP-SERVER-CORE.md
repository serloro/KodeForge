# Tool: SMTP Fake (Server Core)

## Estado: ✅ COMPLETADO

## Objetivo

Implementar servidor SMTP real que captura emails y los guarda en `storedInbox[]` del workspace JSON.

## Implementación

### Arquitectura Multiplataforma (expect/actual)

**Common (expect):**
- `SmtpServer` - Interfaz común
- `EmailReceived` - Data class para emails capturados
- `SmtpServerManager` - Gestor de servidores por proyecto

**JVM (actual):**
- Implementación con SubEthaSMTP 3.1.7
- Servidor SMTP real funcional
- Parsing de mensajes MIME con JavaMail

**Otros targets:**
- No implementado (placeholder)
- Documentado en limitaciones

### Componentes Creados

#### 1. `SmtpServer.kt` (commonMain)
**Interfaz expect para servidor SMTP**
- `start()` - Inicia servidor con host, port, allowedRecipients, callback
- `stop()` - Detiene servidor
- `isRunning()` - Estado del servidor
- `EmailReceived` - Data class con from, to, subject, bodyText, headers

#### 2. `SmtpServer.jvm.kt` (jvmMain)
**Implementación JVM con SubEthaSMTP**
- Servidor SMTP real en puerto configurable
- Handler personalizado (`KodeForgeMessageHandler`)
- Parsing de mensajes MIME
- Extracción de headers y body
- Validación de destinatarios permitidos
- Logging detallado

**Comportamiento con allowedRecipients:**
- Si lista vacía: acepta todos los destinatarios
- Si lista definida: solo acepta emails que contengan alguno de los permitidos
- Emails rechazados: no se guardan (se ignoran silenciosamente)

#### 3. `SmtpServerManager.kt` (commonMain)
**Gestor de servidores por proyecto**
- Mantiene un servidor por proyecto
- `startServer()` - Inicia servidor y configura callback
- `stopServer()` - Detiene servidor de un proyecto
- `stopAllServers()` - Detiene todos (al cerrar app)
- `isServerRunning()` - Verifica estado
- `getServer()` - Obtiene servidor de un proyecto

**Callback automático:**
- Al recibir email → llama a `SmtpFakeUseCases.addEmailToInbox()`
- Actualiza workspace automáticamente
- Persiste en JSON

#### 4. Integración UI
**Modificaciones en pantallas existentes:**
- `Main.kt` - Crea instancia global de `SmtpServerManager`
- `HomeScreen.kt` - Pasa `smtpServerManager` a componentes
- `ToolScreen.kt` - Pasa `smtpServerManager` a tools
- `SmtpFakeToolScreen.kt` - Integra start/stop real del servidor

**Funcionalidad UI:**
- Switch enable/disable → inicia/detiene servidor real
- Estado visual: "Servidor corriendo en host:port"
- Nota informativa dinámica (verde cuando corriendo)
- Manejo de errores inline

### Dependencias Añadidas

**build.gradle.kts:**
```kotlin
implementation("org.subethamail:subethasmtp:3.1.7")
```

## Características Implementadas

### ✅ Servidor SMTP Real

**Funcionalidad:**
- ✅ Escucha en `listenHost:listenPort` configurables
- ✅ Acepta conexiones SMTP
- ✅ Recibe emails completos (headers + body)
- ✅ Parsea mensajes MIME
- ✅ Extrae from, to, subject, body, headers
- ✅ Valida destinatarios permitidos
- ✅ Guarda en `storedInbox[]`
- ✅ Persiste en workspace JSON

**Validación de Destinatarios:**
- ✅ Si `allowedRecipients` vacío → acepta todos
- ✅ Si `allowedRecipients` definido → valida con `contains()`
- ✅ Emails rechazados → no se guardan (ignorados)
- ✅ Logging claro de aceptados/rechazados

**Gestión del Ciclo de Vida:**
- ✅ Start/Stop desde UI
- ✅ Un servidor por proyecto
- ✅ Detención automática al cerrar app
- ✅ Manejo de errores (puerto ocupado, etc.)

### ✅ Integración con Workspace

**Flujo completo:**
1. Usuario habilita servidor en UI
2. `SmtpServerManager.startServer()` inicia servidor real
3. Email llega al servidor
4. `KodeForgeMessageHandler` procesa email
5. Callback llama a `SmtpFakeUseCases.addEmailToInbox()`
6. Workspace se actualiza
7. JSON se guarda automáticamente
8. UI se actualiza (inbox muestra nuevo email)

**Persistencia:**
- ✅ Emails capturados se guardan en JSON
- ✅ Sobreviven reinicio de aplicación
- ✅ Portables entre máquinas

## Tests Implementados

### SmtpServerTest.kt (2 tests)

**1. `smtp server - receives and stores email`**
- Inicia servidor en puerto 2526
- Envía email de prueba con JavaMail
- Verifica que se guarda en workspace
- Valida: from, to, subject, body

**2. `smtp server - respects allowed recipients`**
- Inicia servidor con `allowedRecipients = ["allowed@example.com"]`
- Envía email a destinatario permitido → se guarda
- Envía email a destinatario NO permitido → NO se guarda
- Valida comportamiento de filtrado

**Resultado:**
```bash
./gradlew jvmTest --tests "SmtpServerTest"
# BUILD SUCCESSFUL
# 2 tests passed
```

## Archivos Creados/Modificados

### Nuevos Archivos (4)

1. **`src/commonMain/kotlin/com/kodeforge/smtp/SmtpServer.kt`**
   - Interfaz expect/actual
   - 50 líneas

2. **`src/jvmMain/kotlin/com/kodeforge/smtp/SmtpServer.jvm.kt`**
   - Implementación JVM con SubEthaSMTP
   - 200 líneas

3. **`src/commonMain/kotlin/com/kodeforge/smtp/SmtpServerManager.kt`**
   - Gestor de servidores
   - 110 líneas

4. **`src/jvmTest/kotlin/com/kodeforge/SmtpServerTest.kt`**
   - Tests del servidor real
   - 200 líneas

### Archivos Modificados (5)

5. **`build.gradle.kts`**
   - Añadida dependencia SubEthaSMTP
   - +1 línea

6. **`src/jvmMain/kotlin/com/kodeforge/ui/Main.kt`**
   - Creación de `SmtpServerManager`
   - Detención al cerrar app
   - +10 líneas

7. **`src/commonMain/kotlin/com/kodeforge/ui/screens/HomeScreen.kt`**
   - Pasar `smtpServerManager` a componentes
   - +3 líneas

8. **`src/commonMain/kotlin/com/kodeforge/ui/screens/ToolScreen.kt`**
   - Pasar `smtpServerManager` a tools
   - +3 líneas

9. **`src/commonMain/kotlin/com/kodeforge/ui/screens/SmtpFakeToolScreen.kt`**
   - Integración con servidor real
   - Start/Stop en switch
   - Estado visual dinámico
   - +40 líneas

**Total: ~620 líneas de código nuevo**

## Limitaciones Multiplataforma

### ✅ JVM/Desktop
- Servidor SMTP completamente funcional
- SubEthaSMTP es una librería Java madura y estable
- Soporta todos los features implementados

### ❌ Otros Targets (JS, Native, etc.)
- **No implementado**
- Razón: SubEthaSMTP es específico de JVM
- Alternativas futuras:
  - JS: Implementar con Node.js SMTP server (si target es Node)
  - Native: Implementar con librerías C/C++ de SMTP
  - Común: Usar servidor externo y solo cliente

**Documentación:**
- La interfaz `expect/actual` permite añadir implementaciones futuras
- El código común no depende de la implementación JVM
- Los use cases y el modelo son completamente multiplataforma

## Validación Manual

### Pasos para probar:

1. **Iniciar aplicación:**
   ```bash
   ./gradlew run
   ```

2. **Configurar servidor:**
   - Abrir proyecto
   - Ir a tool SMTP
   - Tab "Configuración"
   - Verificar host: 127.0.0.1, port: 2525
   - Habilitar servidor (switch ON)

3. **Enviar email de prueba:**
   ```bash
   # Usando telnet
   telnet 127.0.0.1 2525
   HELO localhost
   MAIL FROM:<sender@example.com>
   RCPT TO:<recipient@example.com>
   DATA
   Subject: Test Email
   
   This is a test email body.
   .
   QUIT
   ```

4. **Verificar captura:**
   - Tab "Inbox"
   - Debe aparecer el email
   - Ver detalle: from, to, subject, body

5. **Probar destinatarios permitidos:**
   - Tab "Configuración"
   - Añadir "allowed@example.com" a destinatarios permitidos
   - Enviar email a "allowed@example.com" → debe guardarse
   - Enviar email a "other@example.com" → NO debe guardarse

6. **Verificar persistencia:**
   - Cerrar aplicación
   - Reabrir aplicación
   - Tab "Inbox" → emails deben seguir ahí

## Logging

El servidor genera logs detallados:

```
[SMTP Server] Started on 127.0.0.1:2525
[SMTP Server] Allowed recipients: dev@local.test, qa@local.test
[SMTP] FROM: sender@example.com
[SMTP] TO: dev@local.test
[SMTP] Subject: Test Email
[SMTP] Body length: 25 chars
[SMTP] Email captured successfully
[SmtpServerManager] Email saved to workspace
```

## Próximos Pasos (Opcional)

### Mejoras del Servidor
- Autenticación SMTP (usuario/contraseña)
- TLS/SSL support
- Límites de tamaño de email
- Rate limiting
- Blacklist de remitentes

### Mejoras UI
- Botón "Test Connection" (envía email de prueba)
- Logs del servidor en UI
- Estadísticas (emails recibidos/rechazados)
- Configuración avanzada (timeouts, etc.)

### Multiplataforma
- Implementación para otros targets
- Servidor externo como alternativa
- Cliente SMTP para envío

## Conclusión

✅ **Servidor SMTP Core COMPLETADO**

El servidor está:
- ✅ Completamente funcional en JVM/Desktop
- ✅ Integrado con la UI
- ✅ Capturando y guardando emails reales
- ✅ Validando destinatarios permitidos
- ✅ Persistiendo en JSON
- ✅ Testeado con 2 tests automáticos

**Estado del Tool SMTP Fake:**
- Modelo: ✅ COMPLETO
- Persistencia: ✅ COMPLETA
- UI: ✅ COMPLETA
- Servidor real: ✅ COMPLETO (JVM)

El tool SMTP Fake está **100% funcional** en Desktop y listo para uso en desarrollo y testing.

